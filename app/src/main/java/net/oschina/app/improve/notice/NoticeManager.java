package net.oschina.app.improve.notice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuQiu on 16/8/18.
 */
public final class NoticeManager {
    public static final int FLAG_CLEAR_MENTION = 0x1;
    public static final int FLAG_CLEAR_LETTER = 0x2;
    public static final int FLAG_CLEAR_REVIEW = 0x3;
    public static final int FLAG_CLEAR_FANS = 0x4;
    public static final int FLAG_CLEAR_LIKE = 0x5;

    private static NoticeManager INSTANCE;

    static {
        INSTANCE = new NoticeManager();
    }

    private final List<NoticeNotify> mNotifies = new ArrayList<>();
    private NoticeBean mNotice;

    public static NoticeBean getNotice() {
        return new NoticeBean();
    }

    public static void bindNotify(NoticeNotify noticeNotify) {
        INSTANCE.mNotifies.add(noticeNotify);
        INSTANCE.check(noticeNotify);
    }

    public static void unBindNotify(NoticeNotify noticeNotify) {
        INSTANCE.mNotifies.remove(noticeNotify);
    }

    private void check(NoticeNotify noticeNotify) {
        if (mNotice != null)
            noticeNotify.onNoticeArrived(mNotice);
    }

    public static void init(Context context) {
        NoticeServer.startAction(context);
        IntentFilter filter = new IntentFilter(NoticeServer.FLAG_BROADCAST_REFRESH);
        context.registerReceiver(INSTANCE.mReceiver, filter);
    }

    public static void stopListen(Context context) {
        context.unregisterReceiver(INSTANCE.mReceiver);
    }

    public static void exitServer(Context context) {
        NoticeServer.startActionExit(context);
    }

    /**
     * 已读清理Context
     *
     * @param context Context
     * @param type    {@link #FLAG_CLEAR_MENTION}, {@link #FLAG_CLEAR_LETTER},
     *                {@link #FLAG_CLEAR_REVIEW},{@link #FLAG_CLEAR_FANS},
     *                {@link #FLAG_CLEAR_LIKE}
     */
    public static void clear(Context context, int type) {
        NoticeServer.startActionClear(context, type);
    }

    public interface NoticeNotify {
        void onNoticeArrived(NoticeBean bean);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null &&
                    NoticeServer.FLAG_BROADCAST_REFRESH.equals(intent.getAction())) {
                Serializable serializable = intent.getSerializableExtra(NoticeServer.EXTRA_BEAN);
                if (serializable != null) {
                    try {
                        onNoticeChanged((NoticeBean) serializable);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
            }
        }
    };

    private void onNoticeChanged(NoticeBean bean) {
        mNotice = bean;
        for (NoticeNotify notify : mNotifies) {
            notify.onNoticeArrived(mNotice);
        }
    }
}
