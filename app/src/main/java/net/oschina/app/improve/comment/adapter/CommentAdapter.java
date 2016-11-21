package net.oschina.app.improve.comment.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;

import net.oschina.app.R;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.comment.Comment;
import net.oschina.app.improve.comment.CommentView;
import net.oschina.app.improve.comment.OnCommentClickListener;


/**
 * Created by fei
 * on 2016/11/21.
 * desc:
 */

public class CommentAdapter extends BaseRecyclerAdapter<Comment> {


    private RequestManager mRequestManager;

    public CommentAdapter(Context context, RequestManager requestManager) {
        super(context, ONLY_FOOTER);
        mState = STATE_HIDE;
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, long itemId) {
                //  CommentsActivity.this.onItemClick(getItem(position));
            }
        });
        this.mRequestManager = requestManager;
    }

    @Override
    protected CommentHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_comment_item, parent, false);
        return new CommentHolder(view);
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, Comment item, int position) {
        if (holder instanceof CommentHolder) {
            ((CommentHolder) holder).addComment(item, mRequestManager);
        }
    }


    private static class CommentHolder extends RecyclerView.ViewHolder implements OnCommentClickListener {

        private CommentView mCommentView;

        CommentHolder(View itemView) {
            super(itemView);
            CommentView commentView = (CommentView) itemView.findViewById(R.id.comment);
            commentView.setTitleGone();
            commentView.setSeeMoreGone();
            this.mCommentView = commentView;
        }

        /**
         * add comment
         *
         * @param comment comment
         */
        public void addComment(Comment comment, RequestManager requestManager) {
            CommentView commentView = this.mCommentView;
            commentView.addComment(comment, requestManager, this);
        }

        @Override
        public void onClick(View view, Comment comment) {

        }
    }
}
