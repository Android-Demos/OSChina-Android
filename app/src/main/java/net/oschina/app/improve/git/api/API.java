package net.oschina.app.improve.git.api;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.api.ApiHttpClient;

/**
 * Created by haibin
 * on 2017/3/9.
 */

public final class API {
    /**
     * 获取码云推荐列表
     *
     * @param page    page=1、2、3
     * @param handler 回调
     */
    public static void getFeatureProjects(int page, TextHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("page", page);
        ApiHttpClient.getHttpClient().get("http://git.oschina.net/api/v3/projects/featured/osc", params, handler);
    }

    /**
     * 获取项目详情
     *
     * @param id      项目id
     * @param handler 回调
     */
    public static void getProjectDetail(int id, TextHttpResponseHandler handler) {
        ApiHttpClient.getHttpClient().get(String.format("http://git.oschina.net/api/v3/projects/%d/osc", id), handler);
    }
}
