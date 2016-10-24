package net.oschina.app.improve.account.activity.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.improve.base.activities.BaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by fei
 * on 2016/10/14.
 * desc:
 */

public class RegisterStepTwoActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.ly_register_bar)
    LinearLayout mLlRegisterBar;
    @Bind(R.id.et_register_username)
    EditText mEtRegisterUsername;
    @Bind(R.id.iv_register_username_del)
    ImageView mIvRegisterUsernameDel;
    @Bind(R.id.et_register_pwd_input)
    EditText mEtRegisterPwd;
    @Bind(R.id.tv_register_man)
    TextView mTvRegisterMan;
    @Bind(R.id.tv_register_female)
    TextView mTvRegisterFemale;
    @Bind(R.id.bt_register_submit)
    Button mBtRegisterSubmit;

    /**
     * show register step two activity
     *
     * @param context context
     */
    public static void show(Context context) {
        Intent intent = new Intent(context, RegisterStepTwoActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_main_register_step_two;
    }

    @Override
    protected void initWidget() {
        super.initWidget();

    }

    @Override
    protected void initData() {
        super.initData();
    }

    @OnClick({R.id.ib_navigation_back, R.id.iv_register_username_del, R.id.tv_register_man,
            R.id.tv_register_female, R.id.bt_register_submit})
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ib_navigation_back:
                finish();
                break;
            case R.id.iv_register_username_del:
                break;
            case R.id.tv_register_man:
                break;
            case R.id.tv_register_female:
                break;
            case R.id.bt_register_submit:
                LoginActivity.show(RegisterStepTwoActivity.this);
                break;
            default:
                break;
        }

    }


}
