package net.oschina.app.improve.account.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.account.AccountHelper;
import net.oschina.app.improve.account.base.AccountBaseActivity;
import net.oschina.app.improve.account.constants.UserConstants;
import net.oschina.app.improve.app.AppOperator;
import net.oschina.app.improve.bean.User;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.utils.AssimilateUtils;
import net.oschina.app.improve.widget.AccountEditText;
import net.oschina.app.util.TDevice;
import net.oschina.open.constants.OpenConstant;
import net.oschina.open.factory.OpenBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;


/**
 * Created by fei on 2016/10/14.
 * desc:
 */

public class LoginActivity extends AccountBaseActivity implements View.OnClickListener, IUiListener, View.OnFocusChangeListener {

    private static final String HOLD_PWD_KEY = "holdPwdKey";
    public static final String HOLD_USERNAME_KEY = "holdUsernameKey";
    private static final String HOLD_PWD_STATUS_KEY = "holdStatusKey";

    @Bind(R.id.ly_retrieve_bar)
    LinearLayout mLayBackBar;


    @Bind(R.id.et_account)
    AccountEditText mEtAccount;
    @Bind(R.id.ll_login_username)
    LinearLayout mLlLoginUsername;
    @Bind(R.id.et_login_username)
    EditText mEtLoginUsername;
    @Bind(R.id.iv_login_username_del)
    ImageView mIvLoginUsernameDel;

    @Bind(R.id.ll_login_pwd)
    LinearLayout mLlLoginPwd;
    @Bind(R.id.et_login_pwd)
    EditText mEtLoginPwd;
    @Bind(R.id.iv_login_pwd_del)
    ImageView mIvLoginPwdDel;

    @Bind(R.id.iv_login_hold_pwd)
    ImageView mIvHoldPwd;
    @Bind(R.id.tv_login_forget_pwd)
    TextView mTvLoginForgetPwd;

    @Bind(R.id.bt_login_submit)
    Button mTvLoginSubmit;
    @Bind(R.id.bt_login_register)
    Button mTvLoginRegister;

    @Bind(R.id.ll_login_layer)
    View mLlLoginLayer;
    @Bind(R.id.ll_login_pull)
    LinearLayout mLlLoginPull;

    @Bind(R.id.ll_login_options)
    LinearLayout mLlLoginOptions;

    @Bind(R.id.ib_login_weibo)
    ImageView mIbLoginWeiBo;
    @Bind(R.id.ib_login_wx)
    ImageView mIbLoginWx;
    @Bind(R.id.ib_login_qq)
    ImageView mImLoginQq;

    private int openType;
    //private int mHoldPwd;
    private SsoHandler mSsoHandler;
    private Tencent mTencent;

    private TextHttpResponseHandler mHandler = new TextHttpResponseHandler() {

        @Override
        public void onStart() {
            super.onStart();
            showFocusWaitDialog();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            requestFailureHint(throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {

            Type type = new TypeToken<ResultBean<User>>() {
            }.getType();

            GsonBuilder gsonBuilder = new GsonBuilder();
            ResultBean<User> resultBean = gsonBuilder.create().fromJson(responseString, type);
            if (resultBean.isSuccess()) {
                User user = resultBean.getResult();
                AccountHelper.login(user, headers);
                AppContext.showToast(R.string.login_success_hint);
                setResult(RESULT_OK);
                sendLocalReceiver();
            } else {
                AppContext.showToast(resultBean.getMessage(), Toast.LENGTH_SHORT);
            }

        }

        @Override
        public void onFinish() {
            super.onFinish();
            hideWaitDialog();
        }

        @Override
        public void onCancel() {
            super.onCancel();
            hideWaitDialog();
        }
    };

    /**
     * hold account information
     */
    private void holdAccount() {
        String username = mEtLoginUsername.getText().toString().trim();
        //String inputPwd = mEtLoginPwd.getText().toString().trim();

        SharedPreferences sp = getSharedPreferences(UserConstants.HOLD_ACCOUNT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (!TextUtils.isEmpty(username)) {
            editor.putString(HOLD_USERNAME_KEY, username);
        }

//        if (mHoldPwd != 2) {
//            if (!TextUtils.isEmpty(inputPwd)) {
//                byte[] bytes = inputPwd.getBytes();
//                String tempPwd = Base64.encodeToString(bytes, 0, bytes.length, Base64.DEFAULT);
//                editor.putString(HOLD_PWD_KEY, tempPwd);
//                editor.putInt(HOLD_PWD_STATUS_KEY, 1);
//            } else {
//                editor.putString(HOLD_PWD_KEY, inputPwd);
//                editor.putInt(HOLD_PWD_STATUS_KEY, 2);
//            }
//        } else {
//            editor.putString(HOLD_PWD_KEY, null);
//            editor.putInt(HOLD_PWD_STATUS_KEY, 2);
//        }
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    /**
     * show the login activity
     *
     * @param context context
     */
    public static void show(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    /**
     * show the login activity
     *
     * @param context context
     */
    public static void show(Activity context, int requestCode) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * show the login activity
     *
     * @param fragment fragment
     */
    public static void show(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), LoginActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main_login;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mLlLoginLayer.setVisibility(View.GONE);
        mEtAccount.setOnDelTextCallback(new AccountEditText.OnDelTextCallback() {
            @Override
            public void delText() {
                mEtAccount.setText(null);
            }
        });
        mEtLoginUsername.setOnFocusChangeListener(this);
        mEtLoginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString();
                if (username.length() > 0) {
                    if (AssimilateUtils.MachPhoneNum(username) || AssimilateUtils.machEmail(username)) {
                        mLlLoginUsername.setBackgroundResource(R.drawable.bg_login_input_ok);
                    } else {
                        mLlLoginUsername.setBackgroundResource(R.drawable.bg_login_input_error);
                    }
                    mIvLoginUsernameDel.setVisibility(View.VISIBLE);
                } else {
                    mLlLoginUsername.setBackgroundResource(R.drawable.bg_login_input_ok);
                    mIvLoginUsernameDel.setVisibility(View.INVISIBLE);
                }

            }
        });

        mEtLoginPwd.setOnFocusChangeListener(this);
        mEtLoginPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                if (length > 0) {
                    mLlLoginPwd.setBackgroundResource(R.drawable.bg_login_input_ok);
                    mIvLoginPwdDel.setVisibility(View.VISIBLE);
                } else {
                    mIvLoginPwdDel.setVisibility(View.INVISIBLE);
                }
            }
        });

        TextView label = (TextView) mLayBackBar.findViewById(R.id.tv_navigation_label);
        label.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initData() {
        super.initData();//必须要,用来注册本地广播

        //初始化控件状态数据
        SharedPreferences sp = getSharedPreferences(UserConstants.HOLD_ACCOUNT, Context.MODE_PRIVATE);
        String holdUsername = sp.getString(HOLD_USERNAME_KEY, null);
        //String holdPwd = sp.getString(HOLD_PWD_KEY, null);
        //int holdStatus = sp.getInt(HOLD_PWD_STATUS_KEY, 0);//0第一次默认/1用户设置保存/2用户设置未保存

        mEtLoginUsername.setText(holdUsername);

//        if (!TextUtils.isEmpty(holdPwd)) {
//            byte[] bytes = holdPwd.getBytes();
//            byte[] decode = Base64.decode(bytes, 0, bytes.length, Base64.DEFAULT);
//            try {
//                String tempPwd = new String(decode, 0, decode.length, "utf-8");
//
//                mEtLoginPwd.setText(tempPwd);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        } else {
//            mEtLoginPwd.setText(null);
//        }
        // updateHoldPwd(holdStatus);
        // mHoldPwd = holdStatus;
    }

    private void updateHoldPwd(int holdStatus) {
        ImageView ivHoldPwd = this.mIvHoldPwd;
        if (holdStatus == 1 || holdStatus == 0) {
            ivHoldPwd.setImageResource(R.mipmap.checkbox_checked);
        } else {
            ivHoldPwd.setImageResource(R.mipmap.checkbox_normal);
        }
    }

    @OnClick({R.id.ib_navigation_back, R.id.et_login_username, R.id.et_login_pwd, R.id.tv_login_forget_pwd,
            R.id.iv_login_hold_pwd, R.id.bt_login_submit, R.id.bt_login_register, R.id.ll_login_pull,
            R.id.ib_login_weibo, R.id.ib_login_wx, R.id.ib_login_qq, R.id.ll_login_layer,
            R.id.iv_login_username_del, R.id.iv_login_pwd_del})
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ib_navigation_back:
                finish();
                break;
            case R.id.et_login_username:
                mEtLoginPwd.clearFocus();
                mEtLoginUsername.setFocusableInTouchMode(true);
                mEtLoginUsername.requestFocus();
                break;
            case R.id.et_login_pwd:
                mEtLoginUsername.clearFocus();
                mEtLoginPwd.setFocusableInTouchMode(true);
                mEtLoginPwd.requestFocus();
                break;
            case R.id.tv_login_forget_pwd:
                //忘记密码
                RetrieveActivity.show(LoginActivity.this);
                break;
            case R.id.bt_login_submit:
                //用户登录
                loginRequest();
                break;
            case R.id.iv_login_hold_pwd:
                //记住密码
//                String inputPwd = mEtLoginPwd.getText().toString().trim();
//
//                if (TextUtils.isEmpty(inputPwd)) {
//                    AppContext.showToast(getResources().getString(R.string.hint_pwd_null), Toast.LENGTH_SHORT);
//                    return;
//                }
//                if (mHoldPwd == 2) {
//                    mHoldPwd = 1;
//                } else {
//                    mHoldPwd = 2;
//                }
//                updateHoldPwd(mHoldPwd);
                break;
            case R.id.bt_login_register:
                RegisterStepOneActivity.show(LoginActivity.this);
                break;
            case R.id.ll_login_layer:
            case R.id.ll_login_pull:

                mLlLoginPull.animate().cancel();
                mLlLoginLayer.animate().cancel();

                int height = mLlLoginOptions.getHeight();
                float progress = (mLlLoginLayer.getTag() != null && mLlLoginLayer.getTag() instanceof Float) ?
                        (float) mLlLoginLayer.getTag() : 1;
                int time = (int) (360 * progress);

                if (mLlLoginPull.getTag() != null) {
                    mLlLoginPull.setTag(null);
                    glide(height, progress, time);
                } else {
                    mLlLoginPull.setTag(true);
                    upGlide(height, progress, time);
                }
                break;
            case R.id.ib_login_weibo:
                weiBoLogin();
                break;
            case R.id.ib_login_wx:
                //微信登录
                wechatLogin();
                break;
            case R.id.ib_login_qq:
                //QQ登录
                tencentLogin();
                break;
            case R.id.iv_login_username_del:
                mEtLoginUsername.setText(null);
                break;
            case R.id.iv_login_pwd_del:
                mEtLoginPwd.setText(null);
                break;
            default:
                break;
        }

    }


    /**
     * login tencent
     */
    private void tencentLogin() {
        openType = OpenConstant.TENCENT;
        mTencent = OpenBuilder.with(this)
                .useTencent(OpenConstant.QQ_APP_ID)
                .login(this);
    }

    /**
     * login wechat
     */
    private void wechatLogin() {
        openType = OpenConstant.WECHAT;
        OpenBuilder.with(this)
                .useWechat(OpenConstant.WECHAT_APP_ID)
                .login(new OpenBuilder.Callback() {
                    @Override
                    public void onFailed() {
                        AppContext.showToast(R.string.login_hint, Toast.LENGTH_SHORT);
                    }
                });
        //finish();
    }

    /**
     * login weiBo
     */
    private void weiBoLogin() {
        openType = OpenConstant.SINA;
        mSsoHandler = OpenBuilder.with(this)
                .useWeibo(OpenConstant.WB_APP_KEY)
                .login(new WeiboAuthListener() {
                    @Override
                    public void onComplete(Bundle bundle) {

                        Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(bundle);

                        if (oauth2AccessToken.isSessionValid()) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("openid", oauth2AccessToken.getUid());
                                jsonObject.put("expires_in", oauth2AccessToken.getExpiresTime());
                                jsonObject.put("refresh_token", oauth2AccessToken.getRefreshToken());
                                jsonObject.put("access_token", oauth2AccessToken.getToken());

                                OSChinaApi.openLogin(OSChinaApi.LOGIN_WEIBO, jsonObject.toString(), getAppToken(), mHandler);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }


    /**
     * menu up glide
     *
     * @param height   height
     * @param progress progress
     * @param time     time
     */
    private void upGlide(int height, float progress, int time) {
        mLlLoginPull.animate()
                .translationYBy(height * progress)
                .translationY(0)
                .setDuration(time)
                .start();
        mLlLoginLayer.animate()
                .alphaBy(1 - progress)
                .alpha(1)
                .setDuration(time)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mLlLoginLayer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (animation instanceof ValueAnimator) {
                            mLlLoginLayer.setTag(((ValueAnimator) animation).getAnimatedValue());
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animation instanceof ValueAnimator) {
                            mLlLoginLayer.setTag(((ValueAnimator) animation).getAnimatedValue());
                        }
                    }
                })
                .start();
    }

    /**
     * menu glide
     *
     * @param height   height
     * @param progress progress
     * @param time     time
     */
    private void glide(int height, float progress, int time) {
        mLlLoginPull.animate()
                .translationYBy(height - height * progress)
                .translationY(height)
                .setDuration(time)
                .start();

        mLlLoginLayer.animate()
                .alphaBy(1 * progress)
                .alpha(0)
                .setDuration(time)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (animation instanceof ValueAnimator) {
                            mLlLoginLayer.setTag(((ValueAnimator) animation).getAnimatedValue());
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animation instanceof ValueAnimator) {
                            mLlLoginLayer.setTag(((ValueAnimator) animation).getAnimatedValue());
                        }
                        mLlLoginLayer.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void loginRequest() {
        String tempUsername = mEtLoginUsername.getText().toString().trim();
        String tempPwd = mEtLoginPwd.getText().toString().trim();

        if (!TextUtils.isEmpty(tempUsername) && !TextUtils.isEmpty(tempPwd)) {

            boolean machPhoneNum = AssimilateUtils.MachPhoneNum(tempUsername);
            boolean machEmail = AssimilateUtils.machEmail(tempUsername);

            if (machPhoneNum || machEmail) {
                //登录成功,请求数据进入用户个人中心页面

                String appToken = getAppToken();

                if (TDevice.hasInternet()) {
                    requestLogin(tempUsername, tempPwd, appToken);

                } else {
                    AppContext.showToast(getResources().getString(R.string.footer_type_net_error), Toast.LENGTH_SHORT);
                }

            } else {
                AppContext.showToast(getString(R.string.login_input_username_hint_error), Toast.LENGTH_SHORT);
            }
        } else {
            AppContext.showToast(getString(R.string.hint_pwd_null), Toast.LENGTH_SHORT);
        }
    }

    private void requestLogin(String tempUsername, String tempPwd, String appToken) {
        OSChinaApi.login(tempUsername, getSha1(tempPwd), appToken, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showFocusWaitDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                requestFailureHint(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                try {
                    Type type = new TypeToken<ResultBean<User>>() {
                    }.getType();

                    ResultBean<User> resultBean = AppOperator.createGson().fromJson(responseString, type);
                    if (resultBean.isSuccess()) {
                        User user = resultBean.getResult();
                        AccountHelper.login(user, headers);
                        holdAccount();
                        AppContext.showToast(R.string.login_success_hint);
                        setResult(RESULT_OK);
                        sendLocalReceiver();
                    } else {
                        int code = resultBean.getCode();
                        String message = resultBean.getMessage();
                        if (code == 211) {
                            mEtLoginPwd.setFocusableInTouchMode(false);
                            mEtLoginPwd.clearFocus();
                            mEtLoginUsername.requestFocus();
                            mEtLoginUsername.setFocusableInTouchMode(true);
                            mLlLoginUsername.setBackgroundResource(R.drawable.bg_login_input_error);
                        } else if (code == 212) {
                            mEtLoginUsername.setFocusableInTouchMode(false);
                            mEtLoginUsername.clearFocus();
                            mEtLoginPwd.requestFocus();
                            mEtLoginPwd.setFocusableInTouchMode(true);
                            message += "," + getResources().getString(R.string.message_pwd_error);
                            mLlLoginPwd.setBackgroundResource(R.drawable.bg_login_input_error);
                        }
                        AppContext.showToast(message, Toast.LENGTH_SHORT);
                        //更新失败应该是不进行任何的本地操作
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                hideWaitDialog();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                hideWaitDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        tencentOnActivityResult(data);

        super.onActivityResult(requestCode, resultCode, data);

        weiBoOnActivityResult(requestCode, resultCode, data);

    }

    /**
     * weiBo Activity Result
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    private void weiBoOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (openType == OpenConstant.SINA) {
            // SSO 授权回调
            // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
            if (mSsoHandler != null)
                mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * tencent Activity Result
     *
     * @param data data
     */
    @SuppressWarnings("deprecation")
    private void tencentOnActivityResult(Intent data) {
        if (openType == OpenConstant.TENCENT) {
            // 对于tencent
            // 注：在某些低端机上调用登录后，由于内存紧张导致APP被系统回收，登录成功后无法成功回传数据。
            if (mTencent != null) {
                mTencent.handleLoginData(data, this);
            }
        }
    }

    /**
     * tencent callback
     *
     * @param o json
     */
    @Override
    public void onComplete(Object o) {
        JSONObject jsonObject = (JSONObject) o;
        OSChinaApi.openLogin(OSChinaApi.LOGIN_QQ, jsonObject.toString(), getAppToken(), mHandler);
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
     * tencent callback
     */
    @Override
    public void onCancel() {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        int id = v.getId();

        if (id == R.id.et_login_username) {
            if (hasFocus) {
                mLlLoginUsername.setActivated(true);
                mLlLoginPwd.setActivated(false);
            }
        } else {
            if (hasFocus) {
                mLlLoginPwd.setActivated(true);
                mLlLoginUsername.setActivated(false);
            }
        }
    }
}
