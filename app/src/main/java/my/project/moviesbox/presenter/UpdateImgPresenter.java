package my.project.moviesbox.presenter;

import my.project.moviesbox.contract.UpdateImgContract;
import my.project.moviesbox.model.UpdateImgModel;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/17 20:37
 */
public class UpdateImgPresenter extends Presenter<UpdateImgContract.View, UpdateImgModel> implements BasePresenter, UpdateImgContract.LoadDataCallback  {
    private UpdateImgContract.View view;

    public UpdateImgPresenter(UpdateImgContract.View view) {
        super(view);
        this.view = view;
        model = new UpdateImgModel();
    }

    public void loadData(String oldImgUrl, String descUrl) {
        model.getImg(oldImgUrl, descUrl, this);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void successImg(String descUrl, String imgUrl) {
        view.successImg(descUrl, imgUrl);
    }

    @Override
    public void errorImg(String descUrl) {
        view.errorImg(descUrl);
    }

    @Override
    public void loadData(boolean isMain) {

    }
}
