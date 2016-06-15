package net.oschina.app.improve.fragments.tweet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.bean.TweetLikeUserList;
import net.oschina.app.bean.User;
import net.oschina.app.improve.adapter.base.BaseRecyclerAdapter;
import net.oschina.app.improve.adapter.tweet.TweetLikeUsersAdapter;
import net.oschina.app.improve.contract.TweetDetailContract;
import net.oschina.app.improve.fragments.base.BaseRecyclerViewFragment;
import net.oschina.app.util.XmlUtils;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by thanatos on 16/6/13.
 */
public class ListTweetLikeUsersFragment extends BaseRecyclerViewFragment<User> implements TweetDetailContract.ThumbupView{

    public static final String BUNDLE_KEY_TWEET_ID = "BUNDLE_KEY_TWEET_ID";

    private int pageNum = 0;
    private TweetDetailContract.Operator mOperator;
    private AsyncHttpResponseHandler reqHandler;

    public static ListTweetLikeUsersFragment instantiate(TweetDetailContract.Operator operator){
        ListTweetLikeUsersFragment fragment = new ListTweetLikeUsersFragment();
        fragment.mOperator = operator;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reqHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                TweetLikeUserList data = XmlUtils.toBean(TweetLikeUserList.class, responseBody);
                setListData(data.getList());
                mOperator.getTweetDetail().setLikeCount(data.getList().size());
                onRequestSuccess(1);
                onRequestFinish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("oschina", error.getMessage());
            }
        };
    }

    @Override
    protected BaseRecyclerAdapter<User> getRecyclerAdapter() {
        return new TweetLikeUsersAdapter(getContext());
    }

    @Override
    protected Type getType() {
        return new TypeToken<TweetLikeUserList>() {}.getType();
    }

    @Override
    public void onLoadMore() {
        requestData(pageNum + 1);
    }

    @Override
    protected void requestData() {
        requestData(0);
    }

    @Override
    protected void onRequestSuccess(int code) {
        super.onRequestSuccess(code);
        ++pageNum;
    }

    private void requestData(int pageNum){
        OSChinaApi.getTweetLikeList(mOperator.getTweetDetail().getId(), pageNum, reqHandler);
    }

    private void setListData(List<User> users){
        if (mIsRefresh) {
            //cache the time
            mAdapter.clear();
            mAdapter.addAll(users);
            mRefreshLayout.setCanLoadMore(true);
        } else {
            mAdapter.addAll(users);
        }
        if (users.size() < 20) {
            mAdapter.setState(BaseRecyclerAdapter.STATE_NO_MORE, true);
        }
    }

    @Override
    public void onLikeSuccess(boolean isUp, User user) {
        onRefreshing();
    }
}