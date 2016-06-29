package net.oschina.app.improve.detail.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.oschina.app.R;
import net.oschina.app.improve.bean.SoftwareDetail;
import net.oschina.app.improve.bean.simple.About;
import net.oschina.app.improve.detail.activities.SoftwareDetailActivity;
import net.oschina.app.improve.detail.contract.SoftDetailContract;
import net.oschina.app.improve.widget.DetailAboutView;
import net.oschina.app.util.UIHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by fei on 2016/6/20.
 * desc:  software detail module
 */
public class SoftWareDetailFragment extends DetailFragment<SoftwareDetail, SoftDetailContract.View, SoftDetailContract.Operator>
        implements View.OnClickListener, SoftDetailContract.View {

    private long mId;

    @Bind(R.id.iv_label_recommend)
    ImageView ivRecomment;

    @Bind(R.id.iv_software_icon)
    ImageView ivIcon;
    @Bind(R.id.tv_software_name)
    TextView tvName;

    @Bind(R.id.tv_software_authorName)
    TextView tvAuthor;
    @Bind(R.id.tv_software_law)
    TextView tvLicense;
    @Bind(R.id.tv_software_language)
    TextView tvLanguage;
    @Bind(R.id.tv_software_system)
    TextView tvSystem;
    @Bind(R.id.tv_software_record_time)
    TextView tvRecordTime;

    @Bind(R.id.lay_detail_about)
    DetailAboutView mAbouts;

    @Bind(R.id.lay_option_fav_text)
    TextView mCommentText;
    @Bind(R.id.lay_option_fav_icon)
    ImageView mIVFav;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_general_soft_detail;
    }

    @OnClick({R.id.lay_option_share, R.id.lay_option_fav, R.id.bt_software_home, R.id.bt_software_document,
            R.id.lay_option_comment})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lay_option_share:
                // 分享
                handleShare();
                break;
            case R.id.lay_option_fav:
                // 收藏
                handleFavorite();
                break;
            case R.id.bt_software_home:
                //进入官网
                UIHelper.showUrlRedirect(getActivity(), mOperator.getData().getHomePage());
                break;
            case R.id.bt_software_document:
                //软件文档
                UIHelper.showUrlRedirect(getActivity(), mOperator.getData().getDocument());
                break;
            case R.id.lay_option_comment:
                // 评论列表
                UIHelper.showSoftwareTweets(getActivity(), (int) mId);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initData() {
        final SoftwareDetail softwareDetail = mOperator.getData();
        if (softwareDetail == null)
            return;

        mId = softwareDetail.getId();

        if (softwareDetail.isRecommend()) {
            ivRecomment.setVisibility(View.VISIBLE);
        } else {
            ivRecomment.setVisibility(View.INVISIBLE);
        }
        tvName.setText(softwareDetail.getName().trim());

        tvAuthor.setText(softwareDetail.getAuthor().trim());
        tvLicense.setText(softwareDetail.getLicense().trim());
        tvLanguage.setText(softwareDetail.getLanguage().trim());
        tvSystem.setText(softwareDetail.getSupportOS());
        tvRecordTime.setText(softwareDetail.getCollectionDate().trim());

        setCommentCount(softwareDetail.getCommentCount());
        setBodyContent(softwareDetail.getBody());
        getImgLoader().load(softwareDetail.getLogo())
                .error(R.drawable.logo_software_default)
                .placeholder(R.drawable.logo_software_default)
                .into(ivIcon);

        toFavoriteOk(softwareDetail);

        mAbouts.setAbout(softwareDetail.getAbouts(), new DetailAboutView.OnAboutClickListener() {
            @Override
            public void onClick(View view, About about) {
                SoftwareDetailActivity.show(getActivity(), about.getId());
            }
        });
    }

    @Override
    void setCommentCount(int count) {
        mCommentText.setText(String.format("评论(%s)", count));
    }

    private void handleFavorite() {
        mOperator.toFavorite();
    }

    private void handleShare() {
        mOperator.toShare();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void toFavoriteOk(SoftwareDetail softwareDetail) {
        if (softwareDetail.isFavorite())
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_faved));
        else
            mIVFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
    }
}
