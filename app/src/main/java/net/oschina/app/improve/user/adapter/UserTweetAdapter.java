package net.oschina.app.improve.user.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.emoji.InputHelper;
import net.oschina.app.improve.app.AppOperator;
import net.oschina.app.improve.base.adapter.BaseGeneralRecyclerAdapter;
import net.oschina.app.improve.bean.Tweet;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.bean.simple.About;
import net.oschina.app.improve.bean.simple.TweetLikeReverse;
import net.oschina.app.improve.user.activities.OtherUserHomeActivity;
import net.oschina.app.improve.utils.AssimilateUtils;
import net.oschina.app.improve.widget.SimplexToast;
import net.oschina.app.improve.widget.TweetPicturesLayout;
import net.oschina.app.util.ImageUtils;
import net.oschina.app.util.PlatfromUtil;
import net.oschina.app.util.StringUtils;
import net.oschina.app.widget.TweetTextView;

import org.kymjs.kjframe.utils.DensityUtils;

import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by
 * thanatos on 16/8/17.
 */
public class UserTweetAdapter extends BaseGeneralRecyclerAdapter<Tweet> {
    private Bitmap mRecordBitmap;
    private View.OnClickListener mOnLikeClickListener;

    public UserTweetAdapter(Callback callback, int mode) {
        super(callback, mode);
        initListener();
    }

    private void initListener() {
        mOnLikeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = Integer.valueOf(v.getTag().toString());
                Tweet tweet = getItem(position);
                if (tweet == null) return;
                OSChinaApi.reverseTweetLike(tweet.getId(), new TweetLikedHandler(position));
            }
        };
    }

    private void initRecordImg(Context cxt) {
        mRecordBitmap = BitmapFactory.decodeResource(cxt.getResources(), R.mipmap.audio3);
        mRecordBitmap = ImageUtils.zoomBitmap(mRecordBitmap,
                DensityUtils.dip2px(cxt, 20f), DensityUtils.dip2px(cxt, 20f));
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_tweet_improve, parent, false));
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder h, final Tweet item, int position) {
        ViewHolder holder = (ViewHolder) h;
        mCallBack.getImgLoader()
                .load(item.getAuthor().getPortrait())
                .asBitmap()
                .placeholder(R.mipmap.widget_dface)
                .error(R.mipmap.widget_dface)
                .into(holder.mViewPortrait);
        holder.mViewPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherUserHomeActivity.show(mContext, item.getAuthor());
            }
        });

        holder.mViewName.setText(item.getAuthor().getName());
        holder.mViewTime.setText(StringUtils.formatSomeAgo(item.getPubDate()));
        PlatfromUtil.setPlatFromString(holder.mViewPlatform, item.getAppClient());
        holder.mViewLikeCount.setText(String.valueOf(item.getLikeCount()));
        holder.mViewCmmCount.setText(String.valueOf(item.getCommentCount()));

        String content = "";
        if (!TextUtils.isEmpty(item.getContent())) {
            content = item.getContent().replaceAll("[\n\\s]+", " ");
        }
        Spannable spannable = AssimilateUtils.assimilateOnlyAtUser(mContext, content);
        spannable = AssimilateUtils.assimilateOnlyTag(mContext, spannable);
        spannable = AssimilateUtils.assimilateOnlyLink(mContext, spannable);
        spannable = InputHelper.displayEmoji(mContext.getResources(), spannable);
        holder.mViewContent.setText(spannable);
        holder.mViewContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.mViewContent.setFocusable(false);
        holder.mViewContent.setDispatchToParent(true);
        holder.mViewContent.setLongClickable(false);

        if (item.getAudio() != null) {
            if (mRecordBitmap == null) {
                initRecordImg(mContext);
            }
            ImageSpan recordImg = new ImageSpan(mContext, mRecordBitmap);
            SpannableString str = new SpannableString("c");
            str.setSpan(recordImg, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.mViewContent.setText(str);
            holder.mViewContent.append(spannable);
        } else {
            holder.mViewContent.setText(spannable);
        }

        holder.mViewLikeState.setImageResource(item.isLiked() ? R.mipmap.ic_thumbup_actived : R.mipmap.ic_thumb_normal);
        holder.mViewLikeState.setTag(position);
        holder.mViewLikeState.setOnClickListener(mOnLikeClickListener);

        Tweet.Image[] images = item.getImages();
        holder.mLayoutFlow.setImage(images);

        if (item.getAbout() != null){
            holder.mLayoutRef.setVisibility(View.VISIBLE);
            holder.mViewDispatch.setVisibility(View.VISIBLE);

            About about = item.getAbout();
            if (about.getType() == 6){
                holder.mViewRefTitle.setVisibility(View.GONE);
                holder.mLayoutRefImages.setImage(about.getImages());
                String aname = "@" + about.getTitle();
                String cnt = about.getContent();
                Spannable sp = new SpannableString(aname + ": " + cnt);
                ForegroundColorSpan span = new ForegroundColorSpan(
                        mContext.getResources().getColor(R.color.day_colorPrimary));
                sp.setSpan(span, 0, aname.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                holder.mViewRefContent.setText(sp);
            }else {
                holder.mViewRefTitle.setVisibility(View.VISIBLE);
                holder.mViewRefTitle.setText(about.getTitle());
                holder.mViewRefContent.setText(about.getContent());
            }
        }else {
            holder.mLayoutRef.setVisibility(View.GONE);
            holder.mViewDispatch.setVisibility(View.GONE);
        }
    }

    /**
     * Tweet Item View Holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_tweet_face)
        CircleImageView mViewPortrait;
        @Bind(R.id.tv_tweet_name)
        TextView mViewName;
        @Bind(R.id.tv_tweet_time)
        TextView mViewTime;
        @Bind(R.id.tv_tweet_platform)
        TextView mViewPlatform;
        @Bind(R.id.tv_tweet_like_count)
        TextView mViewLikeCount;
        @Bind(R.id.tv_tweet_comment_count)
        TextView mViewCmmCount;
        @Bind(R.id.tweet_item)
        TweetTextView mViewContent;
        @Bind(R.id.iv_like_state)
        ImageView mViewLikeState;
        @Bind(R.id.fl_image)
        TweetPicturesLayout mLayoutFlow;
        @Bind(R.id.tv_ref_title)
        TextView mViewRefTitle;
        @Bind(R.id.tv_ref_content)
        TextView mViewRefContent;
        @Bind(R.id.layout_ref_images)
        TweetPicturesLayout mLayoutRefImages;
        @Bind(R.id.iv_dispatch)
        ImageView mViewDispatch;
        @Bind(R.id.layout_ref)
        LinearLayout mLayoutRef;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * 点赞请求的回调
     */
    private class TweetLikedHandler extends TextHttpResponseHandler {
        private int position;

        TweetLikedHandler(int position) {
            this.position = position;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            SimplexToast.show(mContext, "点赞操作失败");
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            try {
                Type type = new TypeToken<ResultBean<TweetLikeReverse>>() {
                }.getType();
                ResultBean<TweetLikeReverse> resultBean = AppOperator.createGson().fromJson(responseString, type);
                Tweet tweet = getItem(position);
                if (tweet == null) return;
                tweet.setLiked(resultBean.getResult().isLiked());
                tweet.setLikeCount(resultBean.getResult().getLikeCount());
                updateItem(position);
            } catch (Exception e) {
                e.printStackTrace();
                onFailure(statusCode, headers, responseString, e);
            }
        }
    }
}
