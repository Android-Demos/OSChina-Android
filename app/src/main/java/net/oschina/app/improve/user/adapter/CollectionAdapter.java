package net.oschina.app.improve.user.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.Collection;
import net.oschina.app.improve.bean.News;

/**
 * Created by haibin
 * on 2016/10/18.
 */

public class CollectionAdapter extends BaseRecyclerAdapter<Collection> {
    public CollectionAdapter(Context context) {
        super(context, ONLY_FOOTER);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new CollectionViewHolder(mInflater.inflate(R.layout.item_list_collection, parent, false));
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, Collection item, int position) {
        CollectionViewHolder h = (CollectionViewHolder) holder;
        String type = "";
        switch (item.getType()) {
            case News.TYPE_SOFTWARE:
                type = "软件";
                break;
            case News.TYPE_QUESTION:
                type = "问答";
                break;
            case News.TYPE_BLOG:
                type = "博客";
                break;
            case News.TYPE_TRNSLATE:
                type = "翻译";
                break;
            case News.TYPE_EVENT:
                type = "活动";
                break;
            case News.TYPE_NEWS:
                type = "资讯";
                break;
        }
        h.mTypeView.setText(type);
        h.mTitleView.setText(item.getTitle());
    }

    private class CollectionViewHolder extends RecyclerView.ViewHolder {
        private TextView mTypeView, mTitleView;

        public CollectionViewHolder(View itemView) {
            super(itemView);
            mTypeView = (TextView) itemView.findViewById(R.id.tv_type);
            mTitleView = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }
}
