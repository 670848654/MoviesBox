package my.project.moviesbox.parser.sourceCustomView;

import android.content.Intent;

import androidx.annotation.Nullable;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.SearchActivity;
import my.project.moviesbox.view.VerifyWebActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 存在手动验证使用的搜索界面
 * @date 2024/8/17 9:54
 */
public class VerifySearchActivity extends SearchActivity {
    private boolean firstTimeData;

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Bundle bundle = getIntent().getExtras();
        if (!Utils.isNullOrEmpty(bundle)) {
            title = bundle.getString("title");
//            mSearchView.setQuery(title, true);
            searchBar.setText(title);
        }
        return true;
    }*/

    @Override
    protected void init() {
        /*Bundle bundle = getIntent().getExtras();
        if (!Utils.isNullOrEmpty(bundle)) {
            title = bundle.getString("title");
            searchBar.setText(title);
        }*/
        super.init();
    }

    @Override
    public void error(boolean firstTimeData, String msg) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            this.firstTimeData = firstTimeData;
            isSearch = false;
            mSwipe.setEnabled(true);
            mSwipe.setRefreshing(false);
            rvError(msg);
            App.getInstance().showToastMsg(msg, DialogXTipEnum.ERROR);
            String[] params = new String[2];
            params[0] = searchContent;
            params[1] = String.valueOf(page);
            String url = parserInterface.getSearchUrl(params);
            Utils.showAlert(this,
                    R.drawable.round_warning_24,
                    getString(R.string.errorDialogTitle),
                    "可能需要进行手动验证码验证，校验动过后请手动点击【已完成校验】",
                    false,
                    "去验证",
                    "取消",
                    "",
                    (dialog, which) -> {
                        startActivityForResult(new Intent(this, VerifyWebActivity.class).putExtra("url", url), 0x1001);
                        dialog.dismiss();
                    },
                    null,
                    null);
            if (!firstTimeData) {
                setLoadState(adapter, false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x1001 && resultCode == RESULT_OK) {
            if (firstTimeData)
                retryListener();
            else
                mPresenter.loadPageData(searchContent, String.valueOf(page));
        }
    }
}
