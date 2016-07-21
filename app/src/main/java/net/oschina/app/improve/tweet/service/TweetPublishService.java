package net.oschina.app.improve.tweet.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import net.oschina.app.R;

import java.util.List;
import java.util.Map;

/**
 * 动弹发布服务
 * 专用于动弹发布
 */
public class TweetPublishService extends Service implements Contract.IService {
    private final static String TAG = TweetPublishService.class.getName();

    private static final String ACTION_PUBLISH = "net.oschina.app.improve.tweet.service.action.PUBLISH";
    private static final String ACTION_CONTINUE = "net.oschina.app.improve.tweet.service.action.CONTINUE";
    private static final String ACTION_DELETE = "net.oschina.app.improve.tweet.service.action.DELETE";

    private static final String EXTRA_CONTENT = "net.oschina.app.improve.tweet.service.extra.CONTENT";
    private static final String EXTRA_IMAGES = "net.oschina.app.improve.tweet.service.extra.IMAGES";
    private static final String EXTRA_ID = "net.oschina.app.improve.tweet.service.extra.ID";

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private Map<String, Contract.IOperator> mTasks = new ArrayMap<>();

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj, msg.arg1);
        }
    }

    public TweetPublishService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread(TweetPublishService.class.getSimpleName());
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        log("onCreate");
    }

    /**
     * You should not override this method for your IntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the IntentService
     * receives a start request.
     *
     * @see android.app.Service#onStartCommand
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceLooper.quit();
        log("onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 发起动弹发布服务
     */
    public static void startActionPublish(Context context, String content, List<String> images) {
        Intent intent = new Intent(context, TweetPublishService.class);
        intent.setAction(ACTION_PUBLISH);
        intent.putExtra(EXTRA_CONTENT, content);
        if (images != null && images.size() > 0) {
            String[] pubImages = new String[images.size()];
            images.toArray(pubImages);
            intent.putExtra(EXTRA_IMAGES, pubImages);
        }
        context.startService(intent);
    }

    /**
     * 在线程中处理请求数据
     *
     * @param intent  请求的数据
     * @param startId 启动服务的Id
     */
    private void onHandleIntent(Intent intent, int startId) {
        if (intent != null) {
            final String action = intent.getAction();

            log(action);

            if (ACTION_PUBLISH.equals(action)) {
                final String content = intent.getStringExtra(EXTRA_CONTENT);
                final String[] images = intent.getStringArrayExtra(EXTRA_IMAGES);
                handleActionPublish(content, images, startId);
            } else {
                if (ACTION_CONTINUE.equals(action)) {
                    final String id = intent.getStringExtra(EXTRA_ID);
                    if (id == null || handleActionContinue(id, startId)) {
                        stopSelf(startId);
                    }
                } else if (ACTION_DELETE.equals(action)) {
                    final String id = intent.getStringExtra(EXTRA_ID);
                    if (id == null || handleActionDelete(id, startId)) {
                        stopSelf(startId);
                    }
                }
            }
        }
    }

    /**
     * 发布动弹,在后台服务中进行
     */
    private void handleActionPublish(String content, String[] images, int startId) {
        TweetPublishModel model = new TweetPublishModel(content, images);
        Contract.IOperator operator = new TweetPublishOperator(model, this, startId);
        operator.run();
    }

    /**
     * 继续发送动弹
     *
     * @param id      动弹Id
     * @param startId 服务Id
     * @return 返回是否销毁当前服务
     */
    private boolean handleActionContinue(String id, int startId) {
        Contract.IOperator operator = mTasks.get(id);
        if (operator != null) {
            // 正在运行, 不做操作
            return true;
        }
        TweetPublishModel model = TweetPublishCache.get(id);
        if (model != null) {
            operator = new TweetPublishOperator(model, this, startId);
            operator.run();
            return false;
        }
        return true;
    }

    /**
     * 移除动弹
     * 该动弹的缓存将进行清空
     *
     * @param id      动弹Id
     * @param startId 服务Id
     * @return 返回是否销毁当前服务
     */
    private boolean handleActionDelete(String id, int startId) {
        Contract.IOperator operator = mTasks.get(id);
        if (operator != null)
            operator.stop();
        TweetPublishCache.remove(id);
        return true;
    }

    @Override
    public String getCachePath(String id) {
        return String.format("%s/Pictures/%s", getCacheDir().getAbsolutePath(), id);
    }

    @Override
    public void start(String modelId, Contract.IOperator operator) {
        mTasks.put(modelId, operator);
    }

    @Override
    public void stop(String id, int startId) {
        if (mTasks.containsKey(id)) {
            mTasks.remove(id);
        }
        // stop self
        stopSelf(startId);
    }

    @Override
    public void notifyMsg(int notifyId, String modelId, boolean haveReDo, boolean haveDelete, int resId, Object... values) {
        PendingIntent contentIntent = null;
        if (haveReDo) {
            Intent intent = new Intent(this, TweetPublishService.class);
            intent.setAction(ACTION_CONTINUE);
            intent.putExtra(EXTRA_ID, modelId);
            contentIntent = PendingIntent.getService(getApplicationContext(), notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        PendingIntent deleteIntent = null;
        if (haveDelete) {
            Intent intent = new Intent(this, TweetPublishService.class);
            intent.setAction(ACTION_DELETE);
            intent.putExtra(EXTRA_ID, modelId);
            deleteIntent = PendingIntent.getService(getApplicationContext(), notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String content = getString(resId, values);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this)
                .setTicker(content)
                .setContentTitle(getString(R.string.tweet_publish_title))
                .setContentText(content)
                .setAutoCancel(haveDelete)
                .setOngoing(!haveReDo)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setSmallIcon(R.drawable.ic_notification);
        Notification notification = builder.build();
        NotificationManagerCompat.from(this).notify(notifyId, notification);

        log(content);
    }

    @Override
    public void notifyCancel(int notifyId) {
        NotificationManagerCompat.from(this).cancel(notifyId);
    }

    private static void log(String str) {
        Log.e(TAG, str);
    }
}
