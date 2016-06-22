package net.oschina.app.improve.fragments.news;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.bean.Banner;
import net.oschina.app.cache.CacheManager;
import net.oschina.app.improve.activities.EventDetailActivity;
import net.oschina.app.improve.adapter.base.BaseListAdapter;
import net.oschina.app.improve.adapter.general.NewsAdapter;
import net.oschina.app.improve.bean.News;
import net.oschina.app.improve.bean.base.PageBean;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.detail.activities.BlogDetailActivity;
import net.oschina.app.improve.detail.activities.NewsDetailActivity;
import net.oschina.app.improve.detail.activities.QuestionDetailActivity;
import net.oschina.app.improve.fragments.base.BaseGeneralListFragment;
import net.oschina.app.util.UIHelper;
import net.oschina.app.widget.ViewNewsHeader;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;


/**
 * 资讯界面
 */
public class NewsFragment extends BaseGeneralListFragment<News> {

    public static final String HISTORY_NEWS = "history_news";

    private static final String TAG = "NewsFragment";
    // private static final String TAG = "NewsFragment";

    public static final String NEWS_SYSTEM_TIME = "news_system_time";

    private boolean isFirst = true;

    private static final String NEWS_BANNER = "news_banner";

    private ViewNewsHeader mHeaderView;
    private Handler handler = new Handler();

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        mHeaderView = new ViewNewsHeader(getActivity());
        mExeService.execute(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                final PageBean<Banner> pageBean = (PageBean<Banner>) CacheManager.readObject(getActivity(), NEWS_BANNER);
                if (pageBean != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((NewsAdapter) mAdapter).setSystemTime(AppContext.get(NEWS_SYSTEM_TIME, null));
                            mHeaderView.initData(getImgLoader(), pageBean.getItems());
                        }
                    });
                }
            }
        });

        mHeaderView.setRefreshLayout(mRefreshLayout);
        mListView.addHeaderView(mHeaderView);
        getBannerList();
    }

    @Override
    public void onRefreshing() {
        super.onRefreshing();
        if (!isFirst)
            getBannerList();
    }

    @Override
    protected void requestData() {
        super.requestData();
        OSChinaApi.getNewsList(mIsRefresh ? mBean.getPrevPageToken() : mBean.getNextPageToken(), mHandler);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        News news = mAdapter.getItem(position - 1);
        if (news != null) {

            // UIHelper.showNewsDetail(getActivity(), news);
            Log.d(TAG, "onItemClick: ------>type==" + news.getType() + " id=" + news.getId());
            switch (news.getType()) {
                case 0:
                    //新闻链接
                    UIHelper.showUrlRedirect(getActivity(), news.getHref());
                    break;
                case 1:
                    //软件推荐
                    UIHelper.showSoftwareDetailById(getActivity(), (int) news.getId());
                    break;
                case 2:
                    //问答
                     QuestionDetailActivity.show(getActivity(),news.getId());
                    break;
                case 3:
                    //博客
                    BlogDetailActivity.show(getActivity(), news.getId());
                    break;
                case 4:
                    //4.翻译
                    NewsDetailActivity.show(getActivity(), news.getId());
                    break;
                case 5:
                    //活动
                    EventDetailActivity.show(getActivity(), news.getId());
                    break;
                default:
                    //6.资讯
                    NewsDetailActivity.show(getActivity(), news.getId());
                    break;
            }

            TextView title = (TextView) view.findViewById(R.id.tv_title);
            TextView content = (TextView) view.findViewById(R.id.tv_description);
            updateTextColor(title, content);
            saveToReadedList(HISTORY_NEWS, news.getId() + "");

        }
    }

    @Override
    protected BaseListAdapter<News> getListAdapter() {
        return new NewsAdapter(this);
    }

    @Override
    protected Type getType() {
        return new TypeToken<ResultBean<PageBean<News>>>() {
        }.getType();
    }

    @Override
    protected void onRequestFinish() {
        super.onRequestFinish();
        isFirst = false;
    }

    @Override
    protected void setListData(ResultBean<PageBean<News>> resultBean) {
        ((NewsAdapter) mAdapter).setSystemTime(resultBean.getTime());
        AppContext.set(NEWS_SYSTEM_TIME, resultBean.getTime());
        super.setListData(resultBean);
    }

    private void getBannerList() {
        OSChinaApi.getBannerList(OSChinaApi.CATALOG_BANNER_NEWS, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    final ResultBean<PageBean<Banner>> resultBean = AppContext.createGson().fromJson(responseString, new TypeToken<ResultBean<PageBean<Banner>>>() {
                    }.getType());
                    if (resultBean != null && resultBean.isSuccess()) {
                        mExeService.execute(new Runnable() {
                            @Override
                            public void run() {
                                CacheManager.saveObject(getActivity(), resultBean.getResult(), NEWS_BANNER);
                            }
                        });
                        mHeaderView.initData(getImgLoader(), resultBean.getResult().getItems());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
