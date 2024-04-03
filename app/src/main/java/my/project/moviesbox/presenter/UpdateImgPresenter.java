package my.project.moviesbox.presenter;

import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.model.UpdateImgModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/17 20:37
 */
public class UpdateImgPresenter extends Presenter<UpdateImgContract.View> implements BasePresenter, UpdateImgContract.LoadDataCallback  {
    private UpdateImgContract.View view;
    private UpdateImgModel model;
    private String oldImgUrl;
    private String descUrl;

    public UpdateImgPresenter(String oldImgUrl, String descUrl, UpdateImgContract.View view) {
        super(view);
        this.view = view;
        this.oldImgUrl = oldImgUrl;
        this.descUrl = descUrl;
        model = new UpdateImgModel();
    }

    public void loadData() {
        model.getImg(oldImgUrl, descUrl, this);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successImg(String oldImgUrl, String imgUrl) {
        view.successImg(oldImgUrl, imgUrl);
    }

    @Override
    public void errorImg() {
        view.errorImg();
    }

    @Override
    public void loadData(boolean isMain) {

    }
}
