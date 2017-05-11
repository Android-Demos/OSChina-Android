package net.oschina.app.improve.git.gist.detail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.improve.base.fragments.BaseFragment;
import net.oschina.app.improve.git.bean.CodeDetail;
import net.oschina.app.improve.git.bean.Gist;
import net.oschina.app.improve.git.utils.MarkdownUtils;
import net.oschina.app.improve.git.utils.SourceEditor;
import net.oschina.app.util.StringUtils;

import java.text.SimpleDateFormat;

import butterknife.Bind;

/**
 * 代码片段详情
 * Created by haibin on 2017/5/10.
 */

public class GistDetailFragment extends BaseFragment implements GistDetailContract.View {
    private SourceEditor mEditor;
    @Bind(R.id.webView)
    WebView mWebView;
    @Bind(R.id.tv_file_name)
    TextView mTextFileName;
    @Bind(R.id.tv_start_count)
    TextView mTexStartCount;
    @Bind(R.id.tv_fork_count)
    TextView mTextForkCount;
    @Bind(R.id.tv_description)
    TextView mTextDescription;
    @Bind(R.id.tv_language)
    TextView mTextLanguage;
    @Bind(R.id.tv_last_update)
    TextView mTextLastUpdate;
    private Gist mGist;

    static GistDetailFragment newInstance(Gist gist) {
        GistDetailFragment fragment = new GistDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("gist", gist);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gist_detail;
    }


    @Override
    protected void initBundle(Bundle bundle) {
        super.initBundle(bundle);
        mGist = (Gist) bundle.getSerializable("gist");
    }

    @SuppressWarnings("MalformedFormatString")
    @SuppressLint("DefaultLocale")
    private void init(Gist gist) {
        assert gist != null;
        mTextFileName.setText(String.format("%s / %s", gist.getOwner().getName(), gist.getName()));
        mTexStartCount.setText(String.valueOf(gist.getStartCounts()));
        mTextForkCount.setText(String.valueOf(gist.getForkCounts()));
        mTextDescription.setText(gist.getDescription());
        mTextLanguage.setText(gist.getLanguage());
        if (gist.getLastUpdateDate() != null) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mTextLastUpdate.setText(String.format("最后更新于%s", StringUtils.formatSomeAgo(dateFormat.format(gist.getLastUpdateDate()))));
        }
        mTextDescription.setVisibility(TextUtils.isEmpty(gist.getDescription()) ? View.GONE : View.VISIBLE);
        mTextLanguage.setVisibility(TextUtils.isEmpty(gist.getLanguage()) ? View.GONE : View.VISIBLE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultFontSize(10);
        settings.setAllowContentAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        mEditor = new SourceEditor(mWebView);
        init(mGist);
    }

    @Override
    public void setPresenter(GistDetailContract.Presenter presenter) {

    }

    @Override
    public void showNetworkError(int strId) {

    }

    @Override
    public void showGetDetailSuccess(Gist gist, int strId) {
        init(gist);
        mEditor.setMarkdown(MarkdownUtils.isMarkdown(gist.getName()));
        CodeDetail detail = new CodeDetail();
        detail.setContent(gist.getContent());
        mEditor.setSource(gist.getName(), detail);
    }
}
