package net.oschina.app.improve.comment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.account.AccountHelper;
import net.oschina.app.improve.account.activity.LoginActivity;
import net.oschina.app.improve.app.AppOperator;
import net.oschina.app.improve.bean.base.PageBean;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.bean.comment.Comment;
import net.oschina.app.improve.bean.comment.Refer;
import net.oschina.app.improve.bean.comment.Reply;
import net.oschina.app.improve.bean.comment.Vote;
import net.oschina.app.improve.bean.simple.Author;
import net.oschina.app.improve.user.activities.OtherUserHomeActivity;
import net.oschina.app.improve.utils.DialogHelper;
import net.oschina.app.util.StringUtils;
import net.oschina.app.util.TDevice;
import net.oschina.app.widget.TweetTextView;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;

/**
 * Created by fei
 * on 2016/11/16.
 * desc:  资讯、问答、博客、翻译、活动详情评论列表当中进行展示的子view.
 * 包括直接渲染出评论下的refer和reply
 */
public class CommentView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "CommentsView";
    private long mId;
    private int mType;
    private TextView mTitle;
    private TextView mSeeMore;
    private LinearLayout mLayComments;
    private ProgressDialog mDialog;

    public CommentView(Context context) {
        super(context);
        init();
    }

    public CommentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommentView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.lay_detail_comment_layout, this, true);

        mTitle = (TextView) findViewById(R.id.tv_blog_detail_comment);
        mLayComments = (LinearLayout) findViewById(R.id.lay_blog_detail_comment);
        mSeeMore = (TextView) findViewById(R.id.tv_see_more_comment);
    }

    public void setTitle(String title) {
        if (!android.text.TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }
    }

    public void setTitleGone() {
        if (mTitle != null)
            mTitle.setVisibility(GONE);
    }

    public void setSeeMoreGone() {
        if (mSeeMore != null)
            mSeeMore.setVisibility(GONE);
    }

    /**
     * @return TypeToken
     */
    Type getCommentType() {
        return new TypeToken<ResultBean<PageBean<Comment>>>() {
        }.getType();
    }

    Type getVoteType() {
        return new TypeToken<ResultBean<Vote>>() {
        }.getType();
    }

    public void init(long id, int type, final RequestManager imageLoader, final OnCommentClickListener onCommentClickListener) {
        this.mId = id;
        this.mType = type;

        mSeeMore.setVisibility(View.GONE);
        setVisibility(GONE);

        OSChinaApi.getComments(id, type, "refer,reply", null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {

                    Comment[] comments = new Comment[5];
                    for (int i = 0, len = comments.length; i < len; i++) {

                        Comment comment = new Comment();
                        comment.setId((i + 100));
                        Author author = new Author();
                        author.setId((i + 20));
                        author.setName("大神" + i);
                        author.setPortrait("https://static.oschina.net/uploads/user/1133/2267007_50.jpg?t=1415270116000");
                        comment.setAuthor(author);
                        comment.setContent("这是第一条评论" + i);
                        comment.setPubDate("2013-09-17 16:49:34");
                        comment.setAppClient(2);
                        comment.setVote(200);
                        comment.setVoteState((int) (Math.random() * 1));
                        comment.setBest(true);

                        Refer[] refers = new Refer[(int) (Math.random() * 10 + 1)];

                        for (int j = 0; j < refers.length; j++) {
                            Refer refer = new Refer();
                            refer.setAuthor("引用的人的名字" + j);
                            refer.setContent("引用的内容" + j);
                            refer.setPubDate("2013-09-18 16:49:34");
                            refers[j] = refer;
                        }
                        comment.setRefer(refers);

                        Reply[] reply = new Reply[2];
                        for (int j = 0; j < reply.length; j++) {
                            Reply reply1 = new Reply();
                            reply1.setId((j + 50));
                            Author author1 = new Author();
                            author1.setId((j + 90));
                            author1.setName(("这是评论的人的名字" + j));
                            author1.setPortrait("https://static.oschina.net/uploads/user/1133/2267007_50.jpg?t=1415270116000");
                            reply1.setAuthor(author1);
                            reply[j] = reply1;
                        }
                        comment.setReply(reply);
                        comments[i] = comment;
                    }
                    addComment(comments, imageLoader, onCommentClickListener);

                    //onFailure(statusCode, headers, responseString, null);
                } catch (Exception e) {
                    onFailure(statusCode, headers, responseString, e);
                }
            }
        });
    }

    private void addComment(final Comment[] comments, final RequestManager imageLoader, final OnCommentClickListener onCommentClickListener) {

        if (mLayComments != null)

            if (comments != null && comments.length > 0) {

                if (comments.length > 4) {
                    mSeeMore.setVisibility(VISIBLE);
                    mSeeMore.setOnClickListener(CommentView.this);
                }

                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                }

                for (final Comment comment : comments) {
                    if (comment != null) {
                        ViewGroup lay = addComment(false, comment, imageLoader, onCommentClickListener);
                        lay.findViewById(R.id.line).setVisibility(View.VISIBLE);
                    }
                }
            } else {
                setVisibility(View.GONE);
            }
    }

    /**
     * 添加comment
     *
     * @param comment                comment
     * @param imageLoader            imageLoader
     * @param onCommentClickListener onCommentClickListener
     * @return viewGroup
     */
    public ViewGroup addComment(final Comment comment, RequestManager imageLoader, final OnCommentClickListener onCommentClickListener) {
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        return addComment(false, comment, imageLoader, onCommentClickListener);
    }

    private ViewGroup addComment(final boolean first, final Comment comment, final RequestManager imageLoader, final OnCommentClickListener onCommentClickListener) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") final ViewGroup lay = (ViewGroup) inflater.inflate(R.layout.lay_comment_item, null, false);

//        lay.post(new Runnable() {
//            @Override
//            public void run() {

                ImageView ivAvatar = (ImageView) lay.findViewById(R.id.iv_avatar);
                final TextView tvVoteCount = (TextView) lay.findViewById(R.id.tv_vote_count);
                tvVoteCount.setText(String.valueOf(comment.getVote()));
                final ImageView ivVoteStatus = (ImageView) lay.findViewById(R.id.btn_vote);
                ivVoteStatus.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        handVote();
                    }

                    private void handVote() {

                        if (!AccountHelper.isLogin()) {
                            LoginActivity.show(getContext());
                            return;
                        }
                        if (!TDevice.hasInternet()) {
                            AppContext.showToast(getResources().getString(R.string.state_network_error), Toast.LENGTH_SHORT);
                            return;
                        }
                        int voteState = 0;
                        if (comment.getVoteState() == 0) {
                            voteState = 1;
                        } else if (comment.getVoteState() == 1) {
                            voteState = 0;
                        }
                        OSChinaApi.voteComment(comment.getId(), mId, voteState, new TextHttpResponseHandler() {

                            @Override
                            public void onStart() {
                                super.onStart();
                                showWaitDialog(R.string.progress_submit);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                requestFailureHint(throwable);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                                ResultBean<Vote> resultBean = AppOperator.createGson().fromJson(responseString, getVoteType());
                                if (resultBean.isSuccess()) {
                                    Vote vote = resultBean.getResult();
                                    if (vote != null) {
                                        if (vote.getVoteState() == 1) {
                                            comment.setVoteState(1);
                                            ivVoteStatus.setImageResource(R.mipmap.ic_thumbup_actived);
                                        } else if (vote.getVoteState() == 0) {
                                            comment.setVoteState(0);
                                            ivVoteStatus.setImageResource(R.mipmap.ic_thumb_normal);
                                        }
                                        long voteVoteCount = vote.getVote();
                                        comment.setVote(voteVoteCount);
                                        tvVoteCount.setText(String.valueOf(voteVoteCount));
                                    }
                                } else {
                                    AppContext.showToast(resultBean.getMessage(), Toast.LENGTH_SHORT);
                                }
                            }

                            @Override
                            public void onFinish() {
                                super.onFinish();
                                hideWaitDialog();
                            }
                        });
                    }
                });

                if (comment.getVoteState() == 1) {
                    ivVoteStatus.setImageResource(R.mipmap.ic_thumbup_actived);
                } else if (comment.getVoteState() == 0) {
                    ivVoteStatus.setImageResource(R.mipmap.ic_thumb_normal);
                }
                imageLoader.load(comment.getAuthor().getPortrait()).error(R.mipmap.widget_dface)
                        .into(ivAvatar);
                ivAvatar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OtherUserHomeActivity.show(getContext(), comment.getAuthor().getId());
                    }
                });

                ((TextView) lay.findViewById(R.id.tv_name)).setText(comment.getAuthor().getName());

                ((TextView) lay.findViewById(R.id.tv_pub_date)).setText(
                        StringUtils.formatSomeAgo(comment.getPubDate()));

                TweetTextView content = ((TweetTextView) lay.findViewById(R.id.tv_content));
                CommentsUtil.formatHtml(getResources(), content, comment.getContent());
                Refer[] refers = comment.getRefer();

                if (refers != null && refers.length > 0) {
                    int len = refers.length;
                    Log.e(TAG, "addComment: ------------------>len=" + len);

                    View view = CommentsUtil.getReferLayout(inflater, refers, 0);
                    lay.addView(view, lay.indexOfChild(content));

                }

                lay.findViewById(R.id.btn_comment).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentClickListener.onClick(v, comment);
                    }
                });

                if (first)
                    mLayComments.addView(lay, 0);
                else
                    mLayComments.addView(lay);

            //}
       // });

        return lay;
    }

    @Override
    public void onClick(View v) {
        if (mId != 0 && mType != 0)
            CommentsActivity.show(getContext(), mId, mType);
    }

    /**
     * show WaitDialog
     *
     * @return progressDialog
     */
    private ProgressDialog showWaitDialog(@StringRes int messageId) {

        if (mDialog == null) {
            if (messageId <= 0) {
                mDialog = DialogHelper.getProgressDialog(getContext(), true);
            } else {
                String message = getResources().getString(messageId);
                mDialog = DialogHelper.getProgressDialog(getContext(), message, true);
            }
        }
        mDialog.show();

        return mDialog;
    }


    /**
     * hide waitDialog
     */
    private void hideWaitDialog() {
        ProgressDialog dialog = mDialog;
        if (dialog != null) {
            mDialog = null;
            try {
                dialog.cancel();
                // dialog.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * request network error
     *
     * @param throwable throwable
     */
    protected void requestFailureHint(Throwable throwable) {
        AppContext.showToast(R.string.request_error_hint);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}



