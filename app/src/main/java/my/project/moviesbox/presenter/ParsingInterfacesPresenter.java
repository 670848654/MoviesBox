package my.project.moviesbox.presenter;

import com.alibaba.fastjson.JSONObject;

import my.project.moviesbox.contract.ParsingInterfacesContract;
import my.project.moviesbox.model.ParsingInterfacesModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/21 8:37
 */
public class ParsingInterfacesPresenter extends Presenter<ParsingInterfacesContract.View, ParsingInterfacesModel> implements BasePresenter, ParsingInterfacesContract.LoadDataCallback {
    private final ParsingInterfacesContract.View view;
    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public ParsingInterfacesPresenter(ParsingInterfacesContract.View view) {
        super(view);
        this.view = view;
        model = new ParsingInterfacesModel();
    }

    public void parser(String url, boolean isEpisodes) {
        view.loadingView();
        model.parser(url, isEpisodes, this);
    }

    /**
     * @param msg 错误信息
     * @return
     * @方法名称: error
     * @方法描述: 失败回调方法
     * @日期: 2024/1/22 19:51
     * @作者: Li Z
     */
    @Override
    public void error(String msg) {

    }

    @Override
    public void success(JSONObject object, boolean isEpisodes) {
        view.success(object, isEpisodes);
    }

    @Override
    public void error(String msg, boolean isEpisodes) {
        view.error(msg, isEpisodes);
    }

    @Override
    public void loadData(boolean isMain) {

    }
}
