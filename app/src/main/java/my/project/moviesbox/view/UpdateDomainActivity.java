package my.project.moviesbox.view;

import static my.project.moviesbox.event.RefreshEnum.REFRESH_DOMAIN;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_FAVORITE;
import static my.project.moviesbox.event.RefreshEnum.REFRESH_HISTORY;

import android.text.Html;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.database.manager.TFavoriteManager;
import my.project.moviesbox.databinding.ActivityUpdateDomainBinding;
import my.project.moviesbox.enums.DialogXTipEnum;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.base.BaseActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/7/31 16:23
 */
public class UpdateDomainActivity extends BaseActivity<ActivityUpdateDomainBinding> {
    private String updateDomainApiUrl = SharedPreferencesUtils.getUpdateDomainApi();
    private AlertDialog alertDialog;
    private List<UpdateDomain> updateDomains;
    private boolean hasNowSource;

    @Override
    protected void initBeforeView() {}

    /**
     * 子类实现，返回具体的 ViewBinding
     *
     * @param inflater
     * @return
     */
    @Override
    protected ActivityUpdateDomainBinding inflateBinding(LayoutInflater inflater) {
        return ActivityUpdateDomainBinding.inflate(inflater);
    }

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private TextInputLayout apiLayout;
    private LinearLayout updateLayout;
    private TextView dateView;
    private TextView descView;
    /**
     * 初始化控件
     */
    @Override
    protected void findById() {
        appBar = binding.toolbarLayout.appBar;
        appBar.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        toolbar = binding.toolbarLayout.toolbar;
        apiLayout = binding.apiLayout;
        updateLayout = binding.updateLayout;
        dateView = binding.date;
        descView = binding.desc;
    }

    @Override
    public void initClickListeners() {
        binding.save.setOnClickListener(v -> btnClick(v));
        binding.getData.setOnClickListener(v -> btnClick(v));
        binding.update.setOnClickListener(v -> btnClick(v));
    }

    @Override
    protected void init() {
        setToolbar(toolbar, getIntent().getStringExtra("title"), "");
        apiLayout.getEditText().setText(updateDomainApiUrl);
    }

    public void btnClick(View view) {
        Utils.setVibration(view);
        updateDomainApiUrl= apiLayout.getEditText().getText().toString();
        if (!Patterns.WEB_URL.matcher(updateDomainApiUrl).matches()) {
            App.getInstance().showToastMsg("API地址格式错误", DialogXTipEnum.ERROR);
            apiLayout.getEditText().setError("API地址格式错误");
            return;
        }
        switch (view.getId()) {
            case R.id.save:
                SharedPreferencesUtils.setUpdateDomainApi(updateDomainApiUrl);
                App.getInstance().showToastMsg("保存API成功", DialogXTipEnum.SUCCESS);
                break;
            case R.id.getData:
                getUpdateDomainData();
                break;
            case R.id.update:
                alertDialog = Utils.getProDialog(this, R.string.updateDomainUpdateMsg);
                for (UpdateDomain updateDomain : updateDomains) {
                    SharedPreferencesUtils.setUserSetDomain(updateDomain.getIndex(), updateDomain.getNewDomain());
                    TFavoriteManager.updateUrlByChangeDomain(updateDomain.getOldDomain(), updateDomain.getNewDomain());
                }
                if (hasNowSource)
                    EventBus.getDefault().post(REFRESH_DOMAIN);
                EventBus.getDefault().post(REFRESH_FAVORITE);
                EventBus.getDefault().post(REFRESH_HISTORY);
                Utils.cancelDialog(alertDialog);
                App.getInstance().showToastMsg("域名变更完成", DialogXTipEnum.SUCCESS);
                clearData();
                break;
        }
    }

    /**
     * 获取数据
     */
    private void getUpdateDomainData() {
        clearData();
        alertDialog = Utils.getProDialog(this, R.string.updateDomainUpdateConnectMsg);
        OkHttpUtils.getInstance().doGet(updateDomainApiUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Utils.cancelDialog(alertDialog);
                    App.getInstance().showToastMsg("调用接口出错：" + e.getMessage(), DialogXTipEnum.ERROR);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> Utils.cancelDialog(alertDialog));
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String json = response.body().string();
                        LogUtil.logInfo("接口返回信息", json);
                        if (validateJsonFormat(json))
                            runOnUiThread(() -> checkHasUpdate(JSON.parseObject(json)));
                        else
                            runOnUiThread(() -> App.getInstance().showToastMsg("JSON数据格式错误", DialogXTipEnum.ERROR));
                    } else
                        runOnUiThread(() -> App.getInstance().showToastMsg("接口返回数据为空，请重试", DialogXTipEnum.ERROR));
                } else
                    runOnUiThread(() -> App.getInstance().showToastMsg("调用接口出错：" + response, DialogXTipEnum.ERROR));
            }
        });
    }

    /**
     * 验证JSON格式
     * @param json
     * @return
     */
    public static boolean validateJsonFormat(String json) {
        try {
            JSONObject root = JSON.parseObject(json);

            // 检查 date
            if (!root.containsKey("date") || !(root.get("date") instanceof String)) {
                return false;
            }

            // 检查 urls 数组
            if (!root.containsKey("urls") || !(root.get("urls") instanceof JSONArray)) {
                return false;
            }

            JSONArray urls = root.getJSONArray("urls");
            for (Object obj : urls) {
                if (!(obj instanceof JSONObject)) {
                    return false;
                }
                JSONObject item = (JSONObject) obj;
                if (!item.containsKey("index") || !(item.get("index") instanceof Number)) {
                    return false;
                }
                if (!item.containsKey("name") || !(item.get("name") instanceof String)) {
                    return false;
                }
                if (!item.containsKey("url") || !(item.get("url") instanceof String)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // 不是合法 JSON 或解析错误
            return false;
        }
    }

    /**
     * 检查是否存在更新
     * @param jsonObject
     */
    private void checkHasUpdate(JSONObject jsonObject) {
        updateDomains = new ArrayList<>();
        String date = jsonObject.getString("date");
        StringBuilder stringBuilder = new StringBuilder();
        JSONArray urls = jsonObject.getJSONArray("urls");
        for (Object item : urls) {
            JSONObject object = (JSONObject) item;
            int index = object.getInteger("index");
            String name = object.getString("name");
            String url = object.getString("url");
            String oldDomain = SharedPreferencesUtils.getUserSetDomain(index);
            if (!oldDomain.equals(url)) {
                if (parserInterface.getSource() == index)
                    hasNowSource = true;
                updateDomains.add(new UpdateDomain(index, name, oldDomain, url));
                stringBuilder.append("<font color='#f48fb1'><strong>").append(name).append("</strong></font><br>");
                stringBuilder.append("新域名为：<font color='#f48fb1'><strong>").append(url).append("</strong></font><br>");
            }
        }
        if (updateDomains.size() > 0) {
            dateView.setText("更新日期：" + date);
            descView.setText(Html.fromHtml(stringBuilder.toString()));
            updateLayout.setVisibility(View.VISIBLE);
        } else
            App.getInstance().showToastMsg("当前域名数据已是最新", DialogXTipEnum.SUCCESS);
    }

    private void clearData() {
        if (!Utils.isNullOrEmpty(updateDomains))
            updateDomains.clear();
        updateLayout.setVisibility(View.GONE);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    /**
     * 点击重试抽象方法
     *
     * @return
     */
    @Override
    protected void retryListener() {

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateDomain {
        private int index;
        private String name;
        private String oldDomain;
        private String newDomain;
    }
}
