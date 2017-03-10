package net.oschina.app.improve.git.detail;

import net.oschina.app.improve.base.BasePresenter;
import net.oschina.app.improve.base.BaseView;
import net.oschina.app.improve.git.bean.Project;

/**
 * Created by haibin
 * on 2017/3/9.
 */
interface ProjectDetailContract {

    interface View extends BaseView<Presenter> {
        void showGetDetailSuccess(Project project, int strId);

        void showGetDetailFailure(int strId);
    }

    interface Presenter extends BasePresenter {
        void getProjectDetail(long id);
    }
}
