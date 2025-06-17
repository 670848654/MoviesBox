package my.project.moviesbox.view.lazyLoadImage;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.contract.LazyLoadImgContract;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.presenter.LazyLoadImgPresenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.SearchActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/5/17 23:06
 */
public class SearchLazyImgActivity extends SearchActivity implements LazyLoadImgListener, LazyLoadImgContract.View {
    private final static String KEY = "f5d965df75336270";   // 解密KEY
    private final static String IV = "97b60394abc2fbe1";    // 解密IV

    private final LazyLoadImgPresenter imagePresenter = new LazyLoadImgPresenter(this);

    public SearchLazyImgActivity() {
        setLazyLoadImgListener(this);
    }

    @Override
    public void imageSuccess(String imageUrl, String base64) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            List<MultiItemEntity> list = adapter.getData();
            for (int i=0,size=list.size(); i<size; i++) {
                VodDataBean vodDataBean = (VodDataBean) list.get(i);
                if (imageUrl.equals(vodDataBean.getImg())) {
                    vodDataBean.setImg(base64);
                    adapter.notifyItemChanged(i);
                    return;
                }
            }
        });
    }

    @Override
    public void imageError(String imageUlr) {
        LogUtil.logInfo(imageUlr, "获取base64图片失败");
    }

    @Override
    public void loadImg() {
        List<String> imageUrls = new ArrayList<>();
        for (MultiItemEntity multiItemEntity : adapter.getData()) {
            VodDataBean vodDataBean = (VodDataBean) multiItemEntity;
            imageUrls.add(vodDataBean.getImg());
        }
        String[] imageUrlArray = imageUrls.toArray(new String[0]);
        imagePresenter.getImage(KEY, IV, imageUrlArray);
    }

    @Override
    protected void onDestroy() {
        if (!Utils.isNullOrEmpty(imagePresenter))
            imagePresenter.detachView();
        super.onDestroy();
    }
}
