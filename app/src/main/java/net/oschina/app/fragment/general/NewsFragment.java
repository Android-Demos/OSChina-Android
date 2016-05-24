package net.oschina.app.fragment.general;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.reflect.TypeToken;

import net.oschina.app.R;
import net.oschina.app.adapter.base.BaseListAdapter;
import net.oschina.app.adapter.general.NewsAdapter;
import net.oschina.app.bean.News;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 资讯界面
 */
public class NewsFragment extends net.oschina.app.fragment.base.BaseListFragment<News> {
    private View mHeaderView;

    private ViewPager vp_news;

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        mHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout.item_list_news_header, null);
        vp_news = (ViewPager) mHeaderView.findViewById(R.id.vp_news);
        mListView.addHeaderView(mHeaderView);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected BaseListAdapter<News> getListAdapter() {
        return new NewsAdapter(this);
    }

    @Override
    protected Type getType() {
        return new TypeToken<List<News>>() {
        }.getType();
    }
}