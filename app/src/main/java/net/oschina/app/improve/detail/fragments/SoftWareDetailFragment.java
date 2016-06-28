package net.oschina.app.improve.detail.fragments;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.oschina.app.R;
import net.oschina.app.improve.bean.SoftwareDetail;
import net.oschina.app.improve.bean.simple.Comment;
import net.oschina.app.improve.detail.contract.SoftDetailContract;
import net.oschina.app.improve.widget.DetailAboutView;
import net.oschina.app.improve.widget.DetailCommentView;
import net.oschina.app.util.StringUtils;
import net.oschina.app.util.UIHelper;

import butterknife.Bind;

/**
 * Created by fei on 2016/6/20.
 * desc:  software detail module
 */
public class SoftWareDetailFragment extends DetailFragment<SoftwareDetail, SoftDetailContract.View, SoftDetailContract.Operator> implements View.OnClickListener, SoftDetailContract.View {
    private long mId;
    private WebView mWebView;
    private TextView mTVAuthorName;
    private TextView mTVPubDate;
    private TextView mTVTitle;
    private TextView mTVAbstract;
    private ImageView mIVLabelRecommend;
    private ImageView mIVLabelOriginate;
    private ImageView mIVAuthorPortrait;
    private ImageView mIVFav;
    private Button mBtnRelation;
    private EditText mETInput;

    private DetailAboutView mAbouts;
    private DetailCommentView mComments;

    private LinearLayout mLayAbstract;

    @Bind(R.id.fragment_blog_detail)
    CoordinatorLayout mLayCoordinator;
    @Bind(R.id.lay_nsv)
    View mLayContent;
    @Bind(R.id.lay_option)
    View mLayBottom;

    private long mCommentId;
    private long mCommentAuthorId;

    private SoftDetailContract.Operator mOperator;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_general_soft_detail;
    }

    @Override
    public void onDestroy() {
        WebView view = mWebView;
        if (view != null) {
            mWebView = null;
            view.getSettings().setJavaScriptEnabled(true);
            view.removeJavascriptInterface("mWebViewImageListener");
            view.removeAllViewsInLayout();
            view.setWebChromeClient(null);
            view.removeAllViews();
            view.destroy();
        }
        mOperator = null;

        super.onDestroy();
    }

    @Override
    protected void initWidget(View root) {
        WebView webView = new WebView(getActivity());
        webView.setHorizontalScrollBarEnabled(false);
        UIHelper.initWebView(webView);
        UIHelper.addWebImageShow(getActivity(), webView);
        ((FrameLayout) root.findViewById(R.id.lay_webview)).addView(webView);
        mWebView = webView;

        mTVAuthorName = (TextView) root.findViewById(R.id.tv_name);
        mTVPubDate = (TextView) root.findViewById(R.id.tv_pub_date);
        mTVTitle = (TextView) root.findViewById(R.id.tv_title);
        mTVAbstract = (TextView) root.findViewById(R.id.tv_blog_detail_abstract);

        mIVLabelRecommend = (ImageView) root.findViewById(R.id.iv_label_recommend);
        mIVLabelOriginate = (ImageView) root.findViewById(R.id.iv_label_originate);
        mIVAuthorPortrait = (ImageView) root.findViewById(R.id.iv_avatar);
        mIVFav = (ImageView) root.findViewById(R.id.iv_fav);


        mAbouts = (DetailAboutView) root.findViewById(R.id.lay_detail_about);
        mComments = (DetailCommentView) root.findViewById(R.id.lay_detail_comment);
        mETInput = (EditText) root.findViewById(R.id.et_input);
        mLayAbstract = (LinearLayout) root.findViewById(R.id.lay_blog_detail_abstract);


        root.findViewById(R.id.iv_share).setOnClickListener(this);
        mIVFav.setOnClickListener(this);
        mBtnRelation.setOnClickListener(this);
        mETInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handleSendComment();
                    return true;
                }
                return false;
            }
        });
        mETInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    handleKeyDel();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 收藏
            case R.id.iv_fav: {
                handleFavorite();
            }
            break;
            // 分享
            case R.id.iv_share: {
                handleShare();
            }
            break;
            // 评论列表
            case R.id.tv_see_more_comment: {
                UIHelper.showBlogComment(getActivity(), (int) mId,
                        (int) mOperator.getData().getAuthorId());
            }
            break;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initData() {
        SoftwareDetail softwareDetail = mOperator.getData();
        if (softwareDetail == null)
            return;
        mId = mCommentId = softwareDetail.getId();
        mTVAuthorName.setText(softwareDetail.getAuthor());
        getImgLoader().load(softwareDetail.getAuthorPortrait()).error(R.drawable.widget_dface).into(mIVAuthorPortrait);

        String time = String.format("%s (%s)", StringUtils.friendly_time(softwareDetail.getPubDate()), softwareDetail.getPubDate());
        mTVPubDate.setText(time);

        mTVTitle.setText(softwareDetail.getName());

        mLayAbstract.setVisibility(View.VISIBLE);

        mIVLabelRecommend.setVisibility(View.VISIBLE);
        mIVLabelOriginate.setImageDrawable(getResources().getDrawable(R.drawable.ic_label_reprint));

        toFavoriteOk(softwareDetail);

        setText(R.id.tv_info_view, String.valueOf(softwareDetail.getViewCount()));
        setText(R.id.tv_info_comment, String.valueOf(softwareDetail.getCommentCount()));


    }

    private boolean mInputDoubleEmpty = false;

    private void handleKeyDel() {
        if (mCommentId != mId) {
            if (TextUtils.isEmpty(mETInput.getText())) {
                if (mInputDoubleEmpty) {
                    mCommentId = mId;
                    mCommentAuthorId = 0;
                    mETInput.setHint("发表评论");
                } else {
                    mInputDoubleEmpty = true;
                }
            } else {
                mInputDoubleEmpty = false;
            }
        }
    }

    private void handleFavorite() {
        mOperator.toFavorite();
    }

    private void handleShare() {
        mOperator.toShare();
    }

    private void handleSendComment() {
        mOperator.toSendComment(mId, mCommentId, mCommentAuthorId, mETInput.getText().toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void toFavoriteOk(SoftwareDetail softwareDetail) {
        if (softwareDetail.isFavorite())
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_faved_normal));
        else
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_normal));
    }

    @Override
    public void toSendCommentOk(Comment comment) {
        (Toast.makeText(getContext(), "评论成功", Toast.LENGTH_LONG)).show();
        mETInput.setText("");
    }
}
