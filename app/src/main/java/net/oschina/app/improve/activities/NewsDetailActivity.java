package net.oschina.app.improve.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.bean.Report;
import net.oschina.app.bean.Result;
import net.oschina.app.improve.bean.NewsDetail;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.bean.simple.Comment;
import net.oschina.app.improve.contract.NewsDetailContract;
import net.oschina.app.improve.detail.activities.DetailActivity;
import net.oschina.app.improve.fragments.news.NewsDetailFragment;
import net.oschina.app.ui.ShareDialog;
import net.oschina.app.ui.empty.EmptyLayout;
import net.oschina.app.util.HTMLUtil;
import net.oschina.app.util.StringUtils;
import net.oschina.app.util.URLsUtils;
import net.oschina.app.util.XmlUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by fei on 2016/6/13.
 * desc:   news detail  module
 */
public class NewsDetailActivity extends DetailActivity<NewsDetail, NewsDetailContract.View> implements NewsDetailContract.Operator {

    private long mId;
    private EmptyLayout mEmptyLayout;
    private NewsDetail newsDetail;
    private NewsDetailContract.View mView;
    private ShareDialog dialog;
    private List<Comment> comments;

    public static void show(Context context, long id) {
        Intent intent = new Intent(context, NewsDetailActivity.class);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_blog_detail;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_blog_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_report) {
            toReport();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showError(int type) {
        EmptyLayout layout = mEmptyLayout;
        if (layout != null) {
            layout.setErrorType(type);
            layout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void requestData() {

        OSChinaApi.getNewsDetail(mId, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
                showError(EmptyLayout.NETWORK_ERROR);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Type type = new TypeToken<ResultBean<NewsDetail>>() {
                    }.getType();

                    ResultBean<NewsDetail> resultBean = AppContext.createGson().fromJson(responseString, type);
                    if (resultBean != null && resultBean.isSuccess()) {
                        handleData(resultBean.getResult());
                        return;
                    }
                    showError(EmptyLayout.NODATA);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
            }
        });

    }

    @Override
    protected void showView() {

        NewsDetailFragment fragment = NewsDetailFragment.instantiate(mData);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.lay_container, fragment);
        trans.commitAllowingStateLoss();

    }

    /**
     * 获取评论列表
     *
     * @param id 当前资讯的id
     */
    private void getComments(long id) {

        OSChinaApi.getComments(mId, 6, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(EmptyLayout.NETWORK_ERROR);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Type type = new TypeToken<ResultBean<List<Comment>>>() {
                    }.getType();

                    ResultBean<List<Comment>> resultBean = AppContext.createGson().fromJson(responseString, type);
                    if (resultBean != null && resultBean.isSuccess()) {
                        List<Comment> commentList = resultBean.getResult();
                        if (commentList != null && !commentList.isEmpty()) {
                            comments = commentList;
                        }
                    }
                    showError(EmptyLayout.NODATA);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
            }
        });
    }

    @Override
    public NewsDetail getNewsDetail() {
        return getData();
    }

    @Override
    public void toFavorite() {
        int uid = requestCheck();
        if (uid == 0)
            return;
        showWaitDialog(R.string.progress_submit);
        final NewsDetail newsDetail = getData();
        OSChinaApi.getFavReverse(getDataId(), 3, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                hideWaitDialog();
                if (newsDetail == null)
                    return;
                if (newsDetail.isFavorite())
                    AppContext.showToastShort(R.string.del_favorite_faile);
                else
                    AppContext.showToastShort(R.string.add_favorite_faile);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Type type = new TypeToken<ResultBean<NewsDetail>>() {
                    }.getType();

                    ResultBean<NewsDetail> resultBean = AppContext.createGson().fromJson(responseString, type);
                    if (resultBean != null && resultBean.isSuccess()) {
                        newsDetail.setFavorite(!newsDetail.isFavorite());
                        mView.toFavoriteOk(newsDetail);
                        if (newsDetail.isFavorite())
                            AppContext.showToastShort(R.string.add_favorite_success);
                        else
                            AppContext.showToastShort(R.string.del_favorite_success);
                    }
                    hideWaitDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
            }
        });
    }

    @Override
    public void toShare() {
        if (getDataId() != 0 && getData() != null) {
            String content;

            String url = String.format(URLsUtils.URL_MOBILE + "blog/%s", getDataId());
            final NewsDetail newsDetail = getData();
            if (newsDetail.getBody().length() > 55) {
                content = HTMLUtil.delHTMLTag(newsDetail.getBody().trim());
                if (content.length() > 55)
                    content = StringUtils.getSubString(0, 55, content);
            } else {
                content = HTMLUtil.delHTMLTag(newsDetail.getBody().trim());
            }
            String title = newsDetail.getTitle();

            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(content) || TextUtils.isEmpty(title)) {
                AppContext.showToast("内容加载失败...");
                return;
            }
            toShare(title, content, url);
        } else {
            AppContext.showToast("内容加载失败...");
        }
    }

    @Override
    public void toFollow() {

    }

    @Override
    public void toSendComment(long id, long authorId, String comment) {
        int uid = requestCheck();
        if (uid == 0)
            return;

        if (TextUtils.isEmpty(comment)) {
            AppContext.showToastShort(R.string.tip_comment_content_empty);
            return;
        }

        AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                try {
                    net.oschina.app.bean.ResultBean rsb = XmlUtils.toBean(net.oschina.app.bean.ResultBean.class,
                            new ByteArrayInputStream(arg2));
                    Result res = rsb.getResult();
                    if (res.OK()) {
                        NewsDetailContract.View view = mView;
                        if (view != null)
                            view.toSendCommentOk();
                    } else {
                        AppContext.showToastShort(res.getErrorMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(arg0, arg1, arg2, e);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                AppContext.showToastShort(R.string.comment_publish_faile);
            }

            @Override
            public void onStart() {
                showWaitDialog(R.string.progress_submit);
            }

            @Override
            public void onFinish() {
                hideWaitDialog();
            }
        };

        long dataId = getDataId();

        if (dataId != id)
            OSChinaApi.replyBlogComment(dataId, uid, comment, id, authorId, handler);
        else
            OSChinaApi.publicBlogComment(dataId, uid, comment, handler);
    }

    @Override
    public void toReport() {
        int uid = requestCheck();
        if (uid == 0)
            return;
        toReport(getDataId(), getData().getHref(), Report.TYPE_QUESTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mView = null;
    }
}
