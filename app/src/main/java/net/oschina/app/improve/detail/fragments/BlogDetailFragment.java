package net.oschina.app.improve.detail.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
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
import net.oschina.app.improve.bean.BlogDetail;
import net.oschina.app.improve.behavior.FloatingAutoHideDownBehavior;
import net.oschina.app.improve.detail.activities.BlogDetailActivity;
import net.oschina.app.improve.detail.contract.BlogDetailContract;
import net.oschina.app.improve.widget.DetailAboutView;
import net.oschina.app.improve.widget.DetailCommentView;
import net.oschina.app.util.StringUtils;
import net.oschina.app.util.UIHelper;

import butterknife.Bind;

/**
 * Created by qiujuer
 * on 16/5/26.
 */

public class BlogDetailFragment
        extends DetailFragment<BlogDetail, BlogDetailContract.View, BlogDetailContract.Operator>
        implements BlogDetailContract.View, View.OnClickListener {
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
    NestedScrollView mLayContent;
    @Bind(R.id.lay_option)
    View mLayBottom;

    private long mCommentId;
    private long mCommentAuthorId;


    public static BlogDetailFragment instantiate(BlogDetail detail) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("key", detail);
        BlogDetailFragment fragment = new BlogDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_general_blog_detail;
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        mBtnRelation = (Button) root.findViewById(R.id.btn_relation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtnRelation.setElevation(0);
        }

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
            // 关注按钮
            case R.id.btn_relation: {
                handleRelation();
            }
            break;
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
            case R.id.tv_see_comment: {
                UIHelper.showBlogComment(getActivity(), (int) mId,
                        (int) mOperator.getBlogDetail().getAuthorId());
            }
            break;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initData() {
        BlogDetail blog = (BlogDetail) mBundle.getSerializable("key");
        if (blog == null)
            return;

        mId = mCommentId = blog.getId();

        String body = getWebViewBody(blog);
        mWebView.loadDataWithBaseURL("", body, "text/html", "UTF-8", "");

        mTVAuthorName.setText(blog.getAuthor());
        getImgLoader().load(blog.getAuthorPortrait()).error(R.drawable.widget_dface).into(mIVAuthorPortrait);

        String time = String.format("%s (%s)", StringUtils.friendly_time(blog.getPubDate()), blog.getPubDate());
        mTVPubDate.setText(time);

        mTVTitle.setText(blog.getTitle());

        if (TextUtils.isEmpty(blog.getAbstract())) {
            mLayAbstract.setVisibility(View.GONE);
        } else {
            mTVAbstract.setText(blog.getAbstract());
            mLayAbstract.setVisibility(View.VISIBLE);
        }

        mIVLabelRecommend.setVisibility(blog.isRecommend() ? View.VISIBLE : View.GONE);
        mIVLabelOriginate.setImageDrawable(blog.isOriginal() ?
                getResources().getDrawable(R.drawable.ic_label_originate) :
                getResources().getDrawable(R.drawable.ic_label_reprint));

        toFollowOk(blog);
        toFavoriteOk(blog);

        setText(R.id.tv_info_view, String.valueOf(blog.getViewCount()));
        setText(R.id.tv_info_comment, String.valueOf(blog.getCommentCount()));

        mAbouts.setAbout(blog.getAbouts(), new DetailAboutView.OnAboutClickListener() {
            @Override
            public void onClick(View view, BlogDetail.About about) {
                BlogDetailActivity.show(getActivity(), about.id);
            }
        });


        //mComments.show(blog.getId(), 3, getImgLoader());


        mComments.setComment(blog.getComments(), blog.getCommentCount(), getImgLoader(), new DetailCommentView.OnCommentClickListener() {
            @Override
            public void onClick(View view, BlogDetail.Comment comment) {
                FloatingAutoHideDownBehavior.showBottomLayout(mLayCoordinator, mLayContent, mLayBottom);
                mCommentId = comment.id;
                mCommentAuthorId = comment.authorId;
                mETInput.setHint(String.format("回复: %s", comment.author));
            }
        }, this);

    }

    private final static String linkCss = "<script type=\"text/javascript\" " +
            "src=\"file:///android_asset/shCore.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/brush.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/client.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/detail_page" +
            ".js\"></script>"
            + "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>"
            + "<script type=\"text/javascript\">function showImagePreview(var url){window" +
            ".location.url= url;}</script>"
            + "<link rel=\"stylesheet\" type=\"text/css\" " +
            "href=\"file:///android_asset/shThemeDefault.css\">"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore" +
            ".css\">"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/common_new" +
            ".css\">";

    private String getWebViewBody(BlogDetail blog) {
        return String.format("<!DOCTYPE HTML><html><head>%s</head><body><div class=\"body-content\">%s</div></body></html>",
                linkCss + UIHelper.WEB_LOAD_IMAGES,
                UIHelper.setHtmlCotentSupportImagePreview(blog.getBody()));
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

    private void handleRelation() {
        mOperator.toFollow();
    }

    private void handleFavorite() {
        mOperator.toFavorite();
    }

    private void handleShare() {
        mOperator.toShare();
    }

    private void handleSendComment() {
        mOperator.toSendComment(mCommentId, mCommentAuthorId, mETInput.getText().toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void toFavoriteOk(BlogDetail blogDetail) {
        if (blogDetail.isFavorite())
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_faved_normal));
        else
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_normal));
    }

    @Override
    public void toFollowOk(BlogDetail blogDetail) {
        if (blogDetail.getAuthorRelation() <= 2) {
            mBtnRelation.setText("已关注");
        } else {
            mBtnRelation.setText("关注");
        }
    }

    @Override
    public void toSendCommentOk() {
        (Toast.makeText(getContext(), "评论成功", Toast.LENGTH_LONG)).show();
        mETInput.setText("");
    }

    @Override
    public void scrollToComment() {
        mLayContent.scrollTo(0, mComments.getTop());
    }
}
