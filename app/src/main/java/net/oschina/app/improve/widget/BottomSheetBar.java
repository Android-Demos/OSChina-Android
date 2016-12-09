package net.oschina.app.improve.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.emoji.Emojicon;
import net.oschina.app.emoji.InputHelper;
import net.oschina.app.emoji.OnEmojiClickListener;
import net.oschina.app.improve.emoji.EmojiView;
import net.oschina.app.util.TDevice;

/**
 * 底部弹出评论框
 * Created by haibin
 * on 2016/11/10.
 */
@SuppressWarnings("unused")
public class BottomSheetBar implements View.OnClickListener {

    public static final String TAG = "BottomSheetBar";
    private View mRootView;
    private EditText mEditText;
    private ImageButton mAtView;
    private ImageButton mFaceView;
    private ImageButton mSyncToTweetView;
    private Context mContext;
    private Button mBtnCommit;
    private AlertDialog mDialog;
    private FrameLayout mFrameLayout;
    private EmojiView mEmojiView;
    private TextView mTvSyncLabel;


    private BottomSheetBar(Context context) {
        this.mContext = context;
    }

    @SuppressLint("InflateParams")
    public static BottomSheetBar delegation(Context context) {
        BottomSheetBar bar = new BottomSheetBar(context);
        bar.mRootView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_comment_bar, null, false);
        bar.initView();
        return bar;
    }

    private void initView() {
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_face);
        mEditText = (EditText) mRootView.findViewById(R.id.et_comment);
        mAtView = (ImageButton) mRootView.findViewById(R.id.ib_mention);
        mFaceView = (ImageButton) mRootView.findViewById(R.id.ib_face);
        mFaceView.setVisibility(View.GONE);
        mSyncToTweetView = (ImageButton) mRootView.findViewById(R.id.cb_sync);
        mTvSyncLabel = (TextView) mRootView.findViewById(R.id.tv_sync);
        if (mFaceView.getVisibility() == View.GONE) {
            mTvSyncLabel.setText(R.string.send_tweet);
        }
        mSyncToTweetView.setOnClickListener(this);
        mBtnCommit = (Button) mRootView.findViewById(R.id.btn_comment);
        mBtnCommit.setEnabled(false);

        //mDialog = new Dialog(mContext, R.style.Comment_Dialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.share_dialog);
        builder.setView(mRootView);
        mDialog = builder.create();
        Window window = mDialog.getWindow();

        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager m = window.getWindowManager();
            Display d = m.getDefaultDisplay();
            WindowManager.LayoutParams p = window.getAttributes();
            p.width = d.getWidth();
            window.setAttributes(p);
        }

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mFrameLayout.setVisibility(View.GONE);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBtnCommit.setEnabled(s.length() > 0);
            }
        });
    }

    public void showEmoji() {
        mTvSyncLabel.setText(R.string.tweet_publish_title);
        mFaceView.setVisibility(View.VISIBLE);
        mFaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmojiView == null) {
                    mEmojiView = new EmojiView(mContext, mEditText);
                    mEmojiView.setListener(new OnEmojiClickListener() {
                        @Override
                        public void onDeleteButtonClick(View v) {
                            InputHelper.backspace(mEditText);
                        }

                        @Override
                        public void onEmojiClick(Emojicon v) {

                        }
                    });
                    mFrameLayout.addView(mEmojiView);
                }
                TDevice.closeKeyboard(mEditText);
                mFrameLayout.setVisibility(View.VISIBLE);

            }
        });

        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrameLayout.setVisibility(View.GONE);
            }
        });
    }

    public void show(String hint) {
        mDialog.show();
        if (!"添加评论".equals(hint)) {
            mEditText.setHint(hint + " ");
        }
        //Selection.setSelection(mEditText.getText(), mEditText.length());
        mRootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                TDevice.showSoftKeyboard(mEditText);
            }
        }, 50);
    }

    public void dismiss() {
        TDevice.closeKeyboard(mEditText);
        mDialog.dismiss();
    }

    public void setMentionListener(View.OnClickListener listener) {
        mAtView.setOnClickListener(listener);
    }

    public void setFaceListener(View.OnClickListener listener) {
        mFaceView.setOnClickListener(listener);
    }

    public void setCommitListener(View.OnClickListener listener) {
        mBtnCommit.setOnClickListener(listener);
    }

    public void handleSelectFriendsResult(Intent data) {
        String names[] = data.getStringArrayExtra("names");
        if (names != null && names.length > 0) {
            String text = "";
            for (String n : names) {
                text += "@" + n + " ";
            }
            mEditText.getText().insert(mEditText.getSelectionEnd(), text);
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    public String getCommentText() {
        return mEditText.getText().toString().trim();
    }

    public Button getBtnCommit() {
        return mBtnCommit;
    }

    @Override
    public void onClick(View v) {
        ImageButton shareView = this.mSyncToTweetView;
        Object tag = v.getTag();
        if (tag == null) {
            shareView.setBackgroundResource(R.mipmap.form_checkbox_checked);
            shareView.setTag(true);
        } else {
            shareView.setBackgroundResource(R.mipmap.form_checkbox_normal);
            shareView.setTag(null);
        }
    }

    public boolean isSyncToTweet() {
        return mSyncToTweetView != null && mSyncToTweetView.getTag() != null;
    }

}
