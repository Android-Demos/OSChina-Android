package net.oschina.app.improve.app;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import net.oschina.app.AppContext;
import net.oschina.app.api.ApiHttpClient;
import net.oschina.app.improve.account.AccountHelper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JuQiu
 * on 16/6/24.
 */

public final class AppOperator {
    private static ExecutorService EXECUTORS_INSTANCE;

    public static Executor getExecutor() {
        if (EXECUTORS_INSTANCE == null) {
            synchronized (AppOperator.class) {
                if (EXECUTORS_INSTANCE == null) {
                    EXECUTORS_INSTANCE = Executors.newFixedThreadPool(
                            Runtime.getRuntime().availableProcessors() > 0 ?
                                    Runtime.getRuntime().availableProcessors() : 2);
                }
            }
        }
        return EXECUTORS_INSTANCE;
    }

    public static void runOnThread(Runnable runnable) {
        getExecutor().execute(runnable);
    }

    public static GlideUrl getGlideUrlByUser(String url) {
        if (AccountHelper.isLogin()) {
            return new GlideUrl(url,
                    new LazyHeaders
                            .Builder()
                            .addHeader("Cookie",
                                    ApiHttpClient.getCookieString(AppContext.getInstance()))
                            .build());
        } else {
            return new GlideUrl(url);
        }
    }
}
