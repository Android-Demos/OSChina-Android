package net.oschina.app.team.fragment;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.base.BaseFragment;
import net.oschina.app.bean.SimpleBackPage;
import net.oschina.app.fragment.MyInformationFragment;
import net.oschina.app.team.bean.MyIssueState;
import net.oschina.app.team.bean.Team;
import net.oschina.app.team.bean.TeamList;
import net.oschina.app.team.ui.TeamMainActivity;
import net.oschina.app.util.TLog;
import net.oschina.app.util.UIHelper;
import net.oschina.app.util.XmlUtils;

import org.apache.http.Header;
import org.kymjs.kjframe.utils.PreferenceHelper;
import org.kymjs.kjframe.utils.SystemTool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * Team面板界面
 * 
 * @author kymjs (https://github.com/kymjs)
 * 
 */
public class TeamBoardFragment extends BaseFragment {

    @InjectView(R.id.team_myissue_ing)
    View mRlIng;
    @InjectView(R.id.team_myissue_outdate)
    View mRlWill;
    @InjectView(R.id.team_myissue_ed)
    View mRlEd;
    @InjectView(R.id.team_myissue_all)
    View mRlAll;

    @InjectView(R.id.team_myissue_ing_num)
    TextView mTvIng;
    @InjectView(R.id.team_myissue_outdate_num)
    TextView mTvOutdate;
    @InjectView(R.id.team_myissue_ed_num)
    TextView mTvEd;
    @InjectView(R.id.team_myissue_all_num)
    TextView mTvAll;

    @InjectView(R.id.team_myissue_name)
    TextView mTvName;
    @InjectView(R.id.team_myissue_date)
    TextView mTvDate;

    private Team team;
    private Bundle bundle;

    public static final String WHICH_PAGER_KEY = "MyIssueFragment_wihch_pager";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	bundle = getActivity().getIntent().getExtras();
	if (bundle != null) {
	    int index = bundle.getInt(MyInformationFragment.TEAM_LIST_KEY, 0);
	    String cache = PreferenceHelper.readString(getActivity(),
		    MyInformationFragment.TEAM_LIST_FILE,
		    MyInformationFragment.TEAM_LIST_KEY);
	    List<Team> teams = TeamList.toTeamList(cache);
	    if (teams.size() > index) {
		team = teams.get(index);
	    }
	}
	if (team == null) {
	    team = new Team();
	    TLog.log(getClass().getSimpleName(), "team对象初始化异常");
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	super.onCreateView(inflater, container, savedInstanceState);
	View rootView = inflater.inflate(R.layout.fragment_team_board,
		container, false);
	ButterKnife.inject(this, rootView);
	initData();
	initView(rootView);
	return rootView;
    }

    @Override
    public void initView(View view) {
	mRlIng.setOnClickListener(this);
	mRlWill.setOnClickListener(this);
	mRlEd.setOnClickListener(this);
	mRlAll.setOnClickListener(this);

	mTvName.setText(AppContext.getInstance().getLoginUser().getName() + "，"
		+ getGreetings());
	mTvDate.setText("今天是 " + getWeekDay() + "，"
		+ SystemTool.getDataTime("yyyy年MM月dd日"));
    }

    private String getGreetings() {
	Calendar calendar = Calendar.getInstance();
	int hour = calendar.get(Calendar.HOUR_OF_DAY);
	if (hour < 11 && hour > 7) {
	    return "早上好!";
	} else if (hour < 14) {
	    return "上午好!";
	} else if (hour < 18) {
	    return "下午好!";
	} else if (hour < 24) {
	    return "晚上好!";
	} else {
	    return "夜里好!";
	}
    }

    private String getWeekDay() {
	Calendar c = Calendar.getInstance();
	int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
	String weekStr = "";
	switch (dayOfWeek) {
	case 1:
	    weekStr = "星期日";
	    break;
	case 2:
	    weekStr = "星期一";
	    break;
	case 3:
	    weekStr = "星期二";
	    break;
	case 4:
	    weekStr = "星期三";
	    break;
	case 5:
	    weekStr = "星期四";
	    break;
	case 6:
	    weekStr = "星期五";
	    break;
	case 7:
	    weekStr = "星期六";
	    break;
	}

	return weekStr;
    }

    @Override
    @OnClick({ R.id.ll_team_active, R.id.ll_team_issue, R.id.ll_team_discuss,
	    R.id.ll_team_diary })
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.team_myissue_ing:
	    bundle.putInt(WHICH_PAGER_KEY, 0);
	    UIHelper.showSimpleBack(getActivity(),
		    SimpleBackPage.MY_ISSUE_PAGER, bundle);
	    break;
	case R.id.team_myissue_ed:
	    bundle.putInt(WHICH_PAGER_KEY, 1);
	    UIHelper.showSimpleBack(getActivity(),
		    SimpleBackPage.MY_ISSUE_PAGER, bundle);
	    break;
	case R.id.team_myissue_outdate:
	    bundle.putInt(WHICH_PAGER_KEY, 2);
	    UIHelper.showSimpleBack(getActivity(),
		    SimpleBackPage.MY_ISSUE_PAGER, bundle);
	    break;
	case R.id.team_myissue_all:
	    bundle.putInt(WHICH_PAGER_KEY, 0);
	    UIHelper.showSimpleBack(getActivity(),
		    SimpleBackPage.MY_ISSUE_PAGER, bundle);
	    break;
	case R.id.ll_team_active:
	    UIHelper.showSimpleBack(getActivity(), SimpleBackPage.TEAM_ACTIVE, getArguments());
	    break;
	case R.id.ll_team_issue:
	    UIHelper.showSimpleBack(getActivity(), SimpleBackPage.TEAM_ISSUE, getBundle());
	    break;
	case R.id.ll_team_discuss:
	    UIHelper.showSimpleBack(getActivity(), SimpleBackPage.TEAM_DISCUSS, getBundle());
	    break;
	case R.id.ll_team_diary:
	    UIHelper.showSimpleBack(getActivity(), SimpleBackPage.TEAM_DIRAY, getBundle());
	    break;
	default:
	    break;
	    
	}
    }
    
    private Bundle getBundle() {
	Bundle bundle = new Bundle();
	bundle.putSerializable(TeamMainActivity.BUNDLE_KEY_TEAM, team);
	return bundle;
    }

    @Override
    public void initData() {
	OSChinaApi.getMyIssueState(team.getId() + "", AppContext.getInstance()
		.getLoginUid() + "", new AsyncHttpResponseHandler() {

	    @Override
	    public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
		MyIssueState data = XmlUtils.toBean(MyIssueState.class,
			new ByteArrayInputStream(arg2));
		mTvIng.setText(data.getOpened());
		mTvOutdate.setText(data.getOutdate());
		mTvEd.setText(data.getClosed());
		mTvAll.setText(data.getAll());
	    }

	    @Override
	    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
		    Throwable arg3) {
	    }
	});
    }
}
