package net.oschina.app.improve.main.subscription;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.SubBean;
import net.oschina.app.util.StringUtils;

/**
 * 新板博客栏目
 * Created by haibin
 * on 2016/10/26.
 */

public class BlogSubAdapter extends BaseRecyclerAdapter<SubBean> {

    private String mSystemTime;

    public BlogSubAdapter(Context context) {
        super(context, ONLY_FOOTER);
    }

    public void setSystemTime(String systemTime) {
        this.mSystemTime = systemTime;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new BlogViewHolder(mInflater.inflate(R.layout.item_list_sub_blog, parent, false));
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, SubBean item, int position) {
        BlogViewHolder vh = (BlogViewHolder) holder;

        TextView title = vh.tv_title;
        TextView content = vh.tv_description;
        TextView history = vh.tv_time;
        TextView see = vh.tv_view;
        TextView answer = vh.tv_comment_count;

        String text = "";
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        if (item.isOriginal()) {
            spannable.append("[icon] ");
            Drawable originate = mContext.getResources().getDrawable(R.mipmap.ic_label_originate);
            if (originate != null) {
                originate.setBounds(0, 0, originate.getIntrinsicWidth(), originate.getIntrinsicHeight());
            }
            ImageSpan imageSpan = new ImageSpan(originate, ImageSpan.ALIGN_BOTTOM);
            spannable.setSpan(imageSpan, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if (item.isRecommend()) {
            spannable.append("[icon] ");
            Drawable recommend = mContext.getResources().getDrawable(R.mipmap.ic_label_recommend);
            if (recommend != null) {
                recommend.setBounds(0, 0, recommend.getIntrinsicWidth(), recommend.getIntrinsicHeight());
            }
            ImageSpan imageSpan = new ImageSpan(recommend, ImageSpan.ALIGN_BOTTOM);
            if (item.isOriginal()) {
                spannable.setSpan(imageSpan, 7, 13, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(imageSpan, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        title.setText(spannable.append(item.getTitle()));

        String body = item.getBody();
        if (!TextUtils.isEmpty(body)) {
            body = body.trim();
            if (!TextUtils.isEmpty(body)) {
                content.setText(body);
                content.setVisibility(View.VISIBLE);
            } else {
                content.setVisibility(View.GONE);
            }
        }


//        if (AppContext.isOnReadedPostList(cacheName, item.getId() + "")) {
//            title.setTextColor(mContext.getResources().getColor(R.color.count_text_color_light));
//            content.setTextColor(mContext.getResources().getColor(R.color.count_text_color_light));
//        } else {
//            title.setTextColor(mContext.getResources().getColor(R.color.blog_title_text_color_light));
//            content.setTextColor(mContext.getResources().getColor(R.color.ques_bt_text_color_dark));
//        }

        String author = item.getAuthor().getName();
        if (!TextUtils.isEmpty(author)) {
            author = author.trim();
            history.setText((author.length() > 9 ? author.substring(0, 9) : author) +
                    "  " + StringUtils.formatSomeAgo(item.getPubDate().trim()));
        }

        see.setText(String.valueOf(item.getStatistics().getView()));
        answer.setText(String.valueOf(item.getStatistics().getComment()));
    }

    private static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title, tv_description, tv_time, tv_comment_count, tv_view;
        LinearLayout ll_title;

        public BlogViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_description = (TextView) itemView.findViewById(R.id.tv_description);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_comment_count = (TextView) itemView.findViewById(R.id.tv_info_comment);
            tv_view = (TextView) itemView.findViewById(R.id.tv_info_view);
            ll_title = (LinearLayout) itemView.findViewById(R.id.ll_title);
        }
    }
}