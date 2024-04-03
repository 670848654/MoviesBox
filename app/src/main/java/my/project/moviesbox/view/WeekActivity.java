package my.project.moviesbox.view;

import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import my.project.moviesbox.R;
import my.project.moviesbox.adapter.WeekPageAdapter;
import my.project.moviesbox.contract.WeekContract;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.presenter.WeekPresenter;
import my.project.moviesbox.utils.DateUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.view
  * @类名: WeekActivity
  * @描述: 星期时间表数据视图 一般用于动漫时间表
  * @作者: Li Z
  * @日期: 2024/1/26 10:22
  * @版本: 1.0
 */
public class WeekActivity extends BaseActivity<WeekContract.View, WeekPresenter> implements WeekContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    LinearProgressIndicator progressIndicator;
    @BindView(R.id.tab)
    TabLayout tabLayout;
    @BindView(R.id.viewpage2)
    ViewPager2 viewPager2;
    private String title, url;
    private List<WeekDataBean> weekDataBeans = new ArrayList<>();
    private WeekPageAdapter weekPageAdapter;

    @Override
    protected WeekPresenter createPresenter() {
        return new WeekPresenter(url, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_week;
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        url = bundle.getString("url");
        initToolbar();
        initViewPage();
    }

    public void initToolbar() {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            finish();
        });
    }

    private void initViewPage() {
//        viewPager2.setUserInputEnabled(false);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    @Override
    protected void initBeforeView() {

    }

    @Override
    public void loadingView() {
        if (isFinishing()) return;
        progressIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void errorView(String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            progressIndicator.setVisibility(View.GONE);
            Utils.showAlert(this,
                    getString(R.string.errorDialogTitle),
                    msg,
                    false,
                    getString(R.string.retryBtnText),
                    getString(R.string.defaultPositiveBtnText),
                    "",
                    (dialog, which) -> {
                        dialog.dismiss();
                        loadData();
                    },
                    (dialog, which) -> dialog.dismiss(),
                    null);
        });
    }

    @Override
    public void emptyView() {

    }

    @Override
    public void weekSuccess(List<WeekDataBean> datas) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            progressIndicator.setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                weekDataBeans = datas;
//            initViewPage();
                weekPageAdapter = new WeekPageAdapter(this, weekDataBeans);
                viewPager2.setAdapter(weekPageAdapter);
                int nowWeek = DateUtils.getWeekOfDate(new Date());
                tabLayout.getTabAt(nowWeek).select();
                viewPager2.setCurrentItem(nowWeek);
            }, 500);
        });
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void retryListener() {
        loadData();
    }
}
