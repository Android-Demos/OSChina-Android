package net.oschina.app.improve.tweet.contract;

import android.content.Context;
import android.os.Bundle;

import net.oschina.app.improve.bean.simple.About;

/**
 * Created by JuQiu
 * on 16/7/14.
 */

public interface TweetPublishContract {
    interface Operator {
        void setDataView(View view, String defaultContent, String[] defaultImages, About about);

        void publish();

        void onBack();

        void loadData();

        void onSaveInstanceState(Bundle outState);

        void onRestoreInstanceState(Bundle savedInstanceState);
    }

    interface View {
        Context getContext();

        String getContent();

        void setContent(String content);

        void setAbout(About about);

        String[] getImages();

        void setImages(String[] paths);

        void finish();

        Operator getOperator();
    }
}
