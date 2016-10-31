package net.oschina.app.improve.account.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.account.base.AccountBaseActivity;
import net.oschina.app.improve.account.bean.PhoneToken;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.utils.AssimilateUtils;
import net.oschina.app.util.TDevice;

import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

/**
 * Created by fei
 * on 2016/10/14.
 * desc:
 */

public class RegisterStepOneActivity extends AccountBaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final String TAG = "RegisterStepOneActivity";


    @Bind(R.id.ll_register_phone)
    LinearLayout mLlRegisterPhone;
    @Bind(R.id.et_register_username)
    EditText mEtRegisterUsername;
    @Bind(R.id.iv_register_username_del)
    ImageView mIvRegisterDel;

    @Bind(R.id.ll_register_sms_code)
    LinearLayout mLlRegisterSmsCode;
    @Bind(R.id.et_register_auth_code)
    EditText mEtRegisterAuthCode;
    @Bind(R.id.tv_register_sms_call)
    TextView mTvRegisterSmsCall;
    @Bind(R.id.bt_register_submit)
    Button mBtRegisterSubmit;

    private boolean mMachPhoneNum;

    private CountDownTimer mTimer;

    private int mRequestType = 1;//1. 请求发送验证码  2.请求phoneToken


    private TextHttpResponseHandler mHandler = new TextHttpResponseHandler() {

        @Override
        public void onStart() {
            super.onStart();
            showWaitDialog();
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

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            throwable.printStackTrace();
            Log.e(TAG, "onFailure: -------------->");
            if (mRequestType == 1) {
                if (mTimer != null) {
                    mTimer.onFinish();
                    mTimer.cancel();
                }
            }
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {

            try {
                switch (mRequestType) {
                    //第一步请求发送验证码
                    case 1:

                        Log.e(TAG, "onSuccess: ------>收到手机验证码");

                        Type type = new TypeToken<ResultBean>() {
                        }.getType();
                        ResultBean resultBean = AppContext.createGson().fromJson(responseString, type);
                        int code = resultBean.getCode();
                        switch (code) {
                            case 1:
                                //发送验证码成功,请求进入下一步
                                //意味着我们可以进行第二次请求了,获取phoneToken
                                //mRequestType = 2;
                                break;
                            case 218:
                                //手机号已被注册,提示重新输入
                                mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_error);
                                break;
                            case 0:
                                //异常错误，发送验证码失败,回收timer,需重新请求发送验证码
                                if (mTimer != null) {
                                    mTimer.onFinish();
                                    mTimer.cancel();
                                }
                                break;
                            default:
                                break;
                        }
                        AppContext.showToast(resultBean.getMessage(), Toast.LENGTH_SHORT);
                        break;
                    //第二步请求进行注册
                    case 2:

                        Type phoneType = new TypeToken<ResultBean<PhoneToken>>() {
                        }.getType();

                        ResultBean<PhoneToken> phoneTokenResultBean = AppContext.createGson().fromJson(responseString, phoneType);
                        int smsCode = phoneTokenResultBean.getCode();
                        switch (smsCode) {
                            case 1://注册成功,进行用户信息填写
                                if (phoneTokenResultBean.isSuccess()) {
                                    PhoneToken phoneToken = phoneTokenResultBean.getResult();
                                    if (phoneToken != null) {
                                        if (mTimer != null) {
                                            mTimer.onFinish();
                                            mTimer.cancel();
                                        }
                                        RegisterStepTwoActivity.show(RegisterStepOneActivity.this, phoneToken);
                                    }
                                } else {
                                    AppContext.showToast(phoneTokenResultBean.getMessage());
                                }
                                break;
                            case 215://注册失败,手机验证码错误
                                mLlRegisterSmsCode.setBackgroundResource(R.drawable.bg_login_input_error);
                                break;
                            default:
                                break;
                        }
                        AppContext.showToast(phoneTokenResultBean.getMessage());
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                onFailure(statusCode, headers, responseString, e);
            }

        }
    };

    /**
     * show the register activity
     *
     * @param context context
     */
    public static void show(Context context) {
        Intent intent = new Intent(context, RegisterStepOneActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main_register_step_one;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mEtRegisterUsername.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        int length = s.length();
                        if (length > 0) {
                            mIvRegisterDel.setVisibility(View.VISIBLE);
                        } else {
                            mIvRegisterDel.setVisibility(View.INVISIBLE);
                        }
                    }

                    @SuppressWarnings("deprecation")
                    @Override
                    public void afterTextChanged(Editable s) {
                        int length = s.length();
                        String input = s.toString();
                        mMachPhoneNum = AssimilateUtils.MachPhoneNum(input);

                        if (mMachPhoneNum) {
                            String smsCode = mEtRegisterAuthCode.getText().toString().trim();

                            if (!TextUtils.isEmpty(smsCode)) {
                                mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_login_submit);
                                mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.white));
                            } else {
                                mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_login_submit_lock);
                                mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.account_lock_font_color));
                            }
                        } else {
                            mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_login_submit_lock);
                            mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.account_lock_font_color));
                        }

                        if (length > 0 && length < 11) {
                            mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_error);
                            mTvRegisterSmsCall.setAlpha(0.4f);
                        } else if (length == 11) {
                            if (mMachPhoneNum) {
                                mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_ok);
                                if (mTvRegisterSmsCall.getTag() == null) {
                                    mTvRegisterSmsCall.setAlpha(1.0f);
                                } else {
                                    mTvRegisterSmsCall.setAlpha(0.4f);
                                }
                            } else {
                                mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_error);
                                AppContext.showToast(getResources().getString(R.string.hint_username_ok), Toast.LENGTH_SHORT);
                                mTvRegisterSmsCall.setAlpha(0.4f);
                            }
                        } else if (length > 11) {
                            mTvRegisterSmsCall.setAlpha(0.4f);
                            mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_error);
                        } else if (length <= 0) {
                            mTvRegisterSmsCall.setAlpha(0.4f);
                            mLlRegisterPhone.setBackgroundResource(R.drawable.bg_login_input_ok);
                        }


                    }
                }

        );
        mEtRegisterUsername.setOnFocusChangeListener(this);
        mEtRegisterAuthCode.setOnFocusChangeListener(this);
        mEtRegisterAuthCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressWarnings("deprecation")
            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                if (length > 0 && mMachPhoneNum) {
                    mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_login_submit);
                    mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_login_submit_lock);
                    mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.account_lock_font_color));
                }

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @OnClick({R.id.iv_register_username_del, R.id.tv_register_sms_call,
            R.id.bt_register_submit})
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_register_username_del:
                mEtRegisterUsername.setText(null);
                break;
            case R.id.tv_register_sms_call:

                if (!mMachPhoneNum) {
                    AppContext.showToast(getString(R.string.hint_username_ok), Toast.LENGTH_SHORT);
                    return;
                }
                if (!TDevice.hasInternet()) {
                    AppContext.showToast(getResources().getString(R.string.tip_network_error), Toast.LENGTH_SHORT);
                    return;
                }


                if (mTvRegisterSmsCall.getTag() == null) {
                    mRequestType = 1;
                    mTvRegisterSmsCall.setAlpha(0.6f);
                    mTvRegisterSmsCall.setTag(true);
                    mTimer = new CountDownTimer(60 * 1000, 1000) {

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mTvRegisterSmsCall.setText(String.format("%s%s%d%s",
                                    getResources().getString(R.string.register_sms_hint), "(", millisUntilFinished / 1000, ")"));
                        }

                        @Override
                        public void onFinish() {
                            mTvRegisterSmsCall.setTag(null);
                            mTvRegisterSmsCall.setText(getResources().getString(R.string.register_sms_hint));
                            mTvRegisterSmsCall.setAlpha(1.0f);
                        }
                    }.start();
                    String phoneNumber = mEtRegisterUsername.getText().toString().trim();
                    String appToken = "123";//Verifier.getPrivateToken(getApplication());
                    OSChinaApi.sendSmsCode(phoneNumber, appToken, OSChinaApi.REGISTER_INTENT, mHandler);
                } else {
                    AppContext.showToast(getResources().getString(R.string.register_sms_wait_hint), Toast.LENGTH_SHORT);
                }

                break;
            case R.id.bt_register_submit:

                if (!mMachPhoneNum) {
                    AppContext.showToast(getString(R.string.hint_username_ok), Toast.LENGTH_SHORT);
                    return;
                }

                String SmsCode = mEtRegisterAuthCode.getText().toString().trim();

                if (TextUtils.isEmpty(SmsCode)) {
                    AppContext.showToast(getString(R.string.retrieve_pwd_sms_coe_error), Toast.LENGTH_SHORT);
                    return;
                }

                if (!TDevice.hasInternet()) {
                    AppContext.showToast(getResources().getString(R.string.tip_network_error), Toast.LENGTH_SHORT);
                    return;
                }

                mRequestType = 2;
                String phoneNumber = mEtRegisterUsername.getText().toString().trim();
                String appToken = "123";//Verifier.getPrivateToken(getApplication());
                OSChinaApi.validateRegisterInfo(phoneNumber, SmsCode, appToken, mHandler);

                break;
            default:
                break;
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        switch (id) {
            case R.id.et_register_username:
                if (hasFocus) {
                    mLlRegisterPhone.setActivated(true);
                    mLlRegisterSmsCode.setActivated(false);
                }
                break;
            case R.id.et_register_auth_code:
                if (hasFocus) {
                    mLlRegisterSmsCode.setActivated(true);
                    mLlRegisterPhone.setActivated(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
