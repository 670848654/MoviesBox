package my.project.moviesbox.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.adapter.WeekPageAdapter;
import my.project.moviesbox.contract.WeekContract;
import my.project.moviesbox.databinding.ActivityWeekBinding;
import my.project.moviesbox.model.WeekModel;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.presenter.WeekPresenter;
import my.project.moviesbox.utils.DateUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseMvpActivity;

/**
  * @包名: my.project.moviesbox.view
  * @类名: WeekActivity
  * @描述: 星期时间表数据视图 一般用于动漫时间表
  * @作者: Li Z
  * @日期: 2024/1/26 10:22
  * @版本: 1.0
 */
public class WeekActivity extends BaseMvpActivity<WeekModel, WeekContract.View, WeekPresenter, ActivityWeekBinding> implements WeekContract.View {
    private String title, url;
    private List<WeekDataBean> weekDataBeans = new ArrayList<>();
    private WeekPageAdapter weekPageAdapter;

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityWeekBinding inflateBinding(LayoutInflater inflater) {
        return ActivityWeekBinding.inflate(inflater);
    }

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbar;
        progressIndicator = binding.progress;
        tabLayout = binding.tab;
        viewPager2 = binding.viewpage2;
    }

    @Override
    public void initClickListeners() {

    }

    @Override
    protected WeekPresenter createPresenter() {
        return new WeekPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true, url);
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        url = bundle.getString("url");
        setToolbar(toolbar, title, "");
        initViewPage();
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
                    R.drawable.round_warning_24,
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
                viewPager2.setCurrentItem(nowWeek, false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPager2 != null)
            viewPager2.setAdapter(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            View view = findViewById(R.id.action_search);
            Utils.setVibration(view);
            startActivity(new Intent(this, parserInterface.searchOpenClass()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
