package net.oschina.app.improve.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.oschina.app.R;
import net.oschina.app.api.ApiHttpClient;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.base.activities.BaseActivity;
import net.oschina.app.improve.bean.UserV2;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.main.MainActivity;
import net.oschina.app.improve.share.constant.OpenConstant;
import net.oschina.app.wxapi.WXEntryActivity;
import net.oschina.open.constants.OpenConstants;
import net.oschina.open.factory.OpenLogin;

import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;


/**
 * Created by fei on 2016/10/14.
 * desc:
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener, WeiboAuthListener, IUiListener {

    private static final String TAG = "LoginActivity";

    @Bind(R.id.et_login_username)
    EditText mEtLoginUsername;
    @Bind(R.id.et_login_pwd)
    EditText mEtLoginPwd;
    @Bind(R.id.iv_login_hold_pwd)
    ImageView mIvHoldPwd;
    @Bind(R.id.tv_login_forget_pwd)
    TextView mTvLoginForgetPwd;
    @Bind(R.id.bt_login_submit)
    Button mTvLoginSubmit;
    @Bind(R.id.bt_login_register)
    Button mTvLoginRegister;

    @Bind(R.id.ll_login_layer)
    LinearLayout mLlLoginLayer;
    @Bind(R.id.bt_login_pull)
    Button mBtLoginPull;

    @Bind(R.id.ll_login_options)
    LinearLayout mLlLoginOptions;

    @Bind(R.id.ib_login_weibo)
    ImageButton mIbLoginWeiBo;
    @Bind(R.id.ib_login_wx)
    ImageButton mIbLoginWx;
    @Bind(R.id.ib_login_qq)
    ImageButton mImLoginQq;

    private int openType;
    private TextHttpResponseHandler mHandler = new TextHttpResponseHandler() {
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {

            Type type = new TypeToken<ResultBean<UserV2>>() {
            }.getType();

            GsonBuilder gsonBuilder = new GsonBuilder();
            ResultBean<UserV2> resultBean = gsonBuilder.create().fromJson(responseString, type);
            if (resultBean != null && resultBean.isSuccess()) {

                // 更新相关Cookie信息
                ApiHttpClient.updateCookie(ApiHttpClient.getHttpClient(), headers);

                UserV2 userV2 = resultBean.getResult();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userInfo", userV2);
                startActivity(intent);
                finish();

            }

        }
    };


    /**
     * show the login activity
     *
     * @param context context
     */
    public static void show(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main_login;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
    }

    @Override
    protected void initData() {
        super.initData();

        Intent intent = getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            if (WXEntryActivity.ACTION_LOGIN_WX.equals(action)) {
                String wxOpenInfo = intent.getStringExtra(WXEntryActivity.EXTRA_LOGIN_WX);
                if (TextUtils.isEmpty(wxOpenInfo)) {
                    OSChinaApi.openLogin(2, wxOpenInfo, mHandler);
                }
            }
        }
    }


    @OnClick({R.id.tv_login_forget_pwd, R.id.iv_login_hold_pwd, R.id.bt_login_submit, R.id.bt_login_register,
            R.id.ib_login_weibo, R.id.ib_login_wx, R.id.ib_login_qq})
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_login_forget_pwd:
                RegisterStepOneActivity.show(LoginActivity.this);
                break;
            case R.id.bt_login_submit:

                break;
            case R.id.iv_login_hold_pwd:
                //记住密码

                break;
            case R.id.bt_login_register:
                RegisterStepOneActivity.show(LoginActivity.this);
                break;
            case R.id.ib_login_weibo:
                //新浪微博登录

                openType = OpenConstants.SINA;

                OpenLogin<SsoHandler> ssoHandlerOpenLogin = new OpenLogin<>();
                try {
                    ssoHandlerOpenLogin.addAppKey(OpenConstant.WB_APP_KEY)
                            .addRedirectUrl(OpenConstant.REDIRECT_URL)
                            .addWeiboAuthListener(this)
                            .toLogin(getApplicationContext(), LoginActivity.this, OpenConstants.SINA);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.ib_login_wx:
                //微信登录

                openType = OpenConstants.WECHAT;

                OpenLogin<IWXAPI> iwxapiOpenLogin = new OpenLogin<>();
                try {
                    iwxapiOpenLogin.addAppId(OpenConstant.WECHAT_APP_ID)
                            .toLogin(getApplicationContext(), LoginActivity.this, OpenConstants.WECHAT);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.ib_login_qq:

                openType = OpenConstants.TENCENT;

                OpenLogin<Tencent> tencentOpenLogin = new OpenLogin<>();
                try {
                    tencentOpenLogin.addAppId(OpenConstant.QQ_APP_ID)
                            .addIUiListener(this)
                            .toLogin(getApplicationContext(), LoginActivity.this, OpenConstants.TENCENT);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (openType == OpenConstants.TENCENT) {
            // 对于tencent
            // 注：在某些低端机上调用登录后，由于内存紧张导致APP被系统回收，登录成功后无法成功回传数据。
            OpenLogin<Tencent> tencentOpenLogin = new OpenLogin<>();
            tencentOpenLogin.addAppId(OpenConstant.QQ_APP_ID)
                    .addAppKey(OpenConstant.QQ_APP_KEY);
            try {
                Tencent tencent = tencentOpenLogin.createOpen(this, OpenConstants.TENCENT);
                tencent.handleLoginData(data, this);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (openType == OpenConstants.SINA) {
            // SSO 授权回调
            // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
            AuthInfo authInfo = new AuthInfo(getApplicationContext(), OpenConstant.WB_APP_KEY, OpenConstant.REDIRECT_URL, "all");
            SsoHandler ssoHandler = new SsoHandler(LoginActivity.this, authInfo);
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    /**
     * sina  callback
     *
     * @param bundle bundle
     */
    @Override
    public void onComplete(Bundle bundle) {
        Log.e(TAG, "onComplete: -----------sina------->");

        Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(bundle);

        if (oauth2AccessToken.isSessionValid()) {

            Gson gson = new Gson();
            String openInfo = gson.toJson(oauth2AccessToken);
            OSChinaApi.openLogin(3, openInfo, mHandler);

        }

    }

    /**
     * sina callback
     */
    @Override
    public void onWeiboException(WeiboException e) {

    }

    /**
     * tencent callback
     *
     * @param o json
     */
    @Override
    public void onComplete(Object o) {
        OSChinaApi.openLogin(1, (String) o, mHandler);
    }

    /**
     * tencent callback
     *
     * @param uiError uiError
     */
    @Override
    public void onError(UiError uiError) {

    }


    /**
     * tencent / sina callback
     */
    @Override
    public void onCancel() {

    }
}
