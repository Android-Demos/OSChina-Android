package net.oschina.app.improve.main.subscription;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.SubBean;
import net.oschina.app.improve.general.fragments.NewsFragment;
import net.oschina.app.util.StringUtils;

/**
 * 新版新闻订阅栏目
 * Created by haibin
 * on 2016/10/26.
 */

public class NewsSubAdapter extends BaseRecyclerAdapter<SubBean> {

    private String mSystemTime;

    public NewsSubAdapter(Context context, int mode) {
        super(context, mode);
    }

    public void setSystemTime(String systemTime) {
        this.mSystemTime = systemTime;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new NewsViewHolder(mInflater.inflate(R.layout.item_list_sub_news, parent, false));
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, SubBean item, int position) {
        NewsViewHolder vh = (NewsViewHolder) holder;
        if (AppContext.isOnReadedPostList(NewsFragment.HISTORY_NEWS, String.valueOf(item.getId()))) {
            vh.tv_title.setTextColor(mContext.getResources().getColor(R.color.count_text_color_light));
            vh.tv_description.setTextColor(mContext.getResources().getColor(R.color.count_text_color_light));
        } else {
            vh.tv_title.setTextColor(mContext.getResources().getColor(R.color.blog_title_text_color_light));
            vh.tv_description.setTextColor(mContext.getResources().getColor(R.color.blog_title_text_color_light));
        }

        vh.tv_description.setText(item.getBody());
        vh.tv_time.setText(StringUtils.formatSomeAgo(item.getPubDate()));
        vh.tv_comment_count.setText(String.valueOf(item.getStatistics().getComment()));

        if (StringUtils.isSameDay(mSystemTime, item.getPubDate())) {

            String text = "[icon] " + item.getTitle();
            Drawable drawable = mContext.getResources().getDrawable(R.mipmap.ic_label_today);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);

            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(imageSpan, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            vh.tv_title.setText(spannable);
            vh.tv_title.setTextSize(16.0f);
        } else {
            vh.tv_title.setText(item.getTitle());
        }
    }

    private static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title, tv_description, tv_time, tv_comment_count;
        LinearLayout ll_title;

        public NewsViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_description = (TextView) itemView.findViewById(R.id.tv_description);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_comment_count = (TextView) itemView.findViewById(R.id.tv_comment_count);
            ll_title = (LinearLayout) itemView.findViewById(R.id.ll_title);
        }
    }
}
