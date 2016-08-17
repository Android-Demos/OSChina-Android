package net.oschina.app.improve.user.fragments;

import com.google.gson.reflect.TypeToken;

import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.base.adapter.BaseListAdapter;
import net.oschina.app.improve.base.fragments.BaseListFragment;
import net.oschina.app.improve.bean.Mention;
import net.oschina.app.improve.bean.base.PageBean;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.user.adapter.UserMentionAdapter;

import java.lang.reflect.Type;

/**
 * Created by huanghaibin_dev
 * on 2016/8/16.
 */

public class UserMentionFragment extends BaseListFragment<Mention> {

    @Override
    protected void requestData() {
        super.requestData();
        OSChinaApi.getMsgMentionList(mIsRefresh ? null : mBean.getNextPageToken(), mHandler);
    }

    @Override
    protected BaseListAdapter<Mention> getListAdapter() {
        return new UserMentionAdapter(this);
    }

    @Override
    protected Type getType() {
        return new TypeToken<ResultBean<PageBean<Mention>>>() {
        }.getType();
    }
}
