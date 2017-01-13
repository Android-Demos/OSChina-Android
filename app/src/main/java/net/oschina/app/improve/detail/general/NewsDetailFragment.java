package net.oschina.app.improve.detail.general;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.bean.Software;
import net.oschina.app.improve.bean.SubBean;
import net.oschina.app.improve.bean.simple.Author;
import net.oschina.app.improve.detail.v2.DetailFragment;
import net.oschina.app.improve.utils.ReadedIndexCacheManager;

import butterknife.Bind;

/**
 * Created by haibin
 * on 2016/11/30.
 */

public class NewsDetailFragment extends DetailFragment {
    @Bind(R.id.tv_title)
    TextView mTextTitle;

    @Bind(R.id.tv_pub_date)
    TextView mTextPubDate;

    @Bind(R.id.tv_author)
    TextView mTextAuthor;

    @Bind(R.id.lay_about_software)
    LinearLayout mLinearSoftware;

    @Bind(R.id.tv_about_software_title)
    TextView mTextSoftwareTitle;

    public static NewsDetailFragment newInstance() {
        NewsDetailFragment fragment = new NewsDetailFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news_detail_v2;
    }

    @Override
    protected void initData() {
        super.initData();
        CACHE_CATALOG = OSChinaApi.CATALOG_NEWS;
    }

    @Override
    public void showGetDetailSuccess(SubBean bean) {
        super.showGetDetailSuccess(bean);
        mTextTitle.setText(bean.getTitle());
        mTextPubDate.setText("发布于 " + bean.getPubDate());
        Author author = mBean.getAuthor();
        if (author != null) {
            mTextAuthor.setText("@" + author.getName());
        }
        final Software software = bean.getSoftware();
        if (software != null) {
            mLinearSoftware.setVisibility(View.VISIBLE);
            mTextSoftwareTitle.setText(software.getName());
            mLinearSoftware.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoftwareDetailActivity.show(getActivity(), software.getId());
                }
            });
        } else {
            mLinearSoftware.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getCommentOrder() {
        return OSChinaApi.COMMENT_HOT_ORDER;
    }

    @Override
    public void onDestroy() {
        if (mBean != null && mBean.getId() > 0) {
            ReadedIndexCacheManager.saveIndex(getContext(), mBean.getId(), OSChinaApi.CATALOG_NEWS,
                    mViewScroller.getScrollY());
        }
        super.onDestroy();
    }
}
