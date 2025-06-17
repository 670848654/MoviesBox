package my.project.moviesbox.view.lazyLoadImage;

import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_SINGLE_LINE_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.VOD_LIST;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import my.project.moviesbox.adapter.HomeItemAdapter;
import my.project.moviesbox.contract.LazyLoadImgContract;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.presenter.LazyLoadImgPresenter;
import my.project.moviesbox.view.HomeFragment;

/**
  * @包名: my.project.moviesbox.view
  * @类名: HomeFragment
  * @描述: 首页数据视图(图片懒加载)
  * @作者: Li Z
  * @日期: 2024/2/4 17:11
  * @版本: 1.0
 */
public class HomeLazyImgFragment extends HomeFragment implements LazyLoadImgListener, LazyLoadImgContract.View {
    private final static String KEY = "f5d965df75336270";   // 解密KEY
    private final static String IV = "97b60394abc2fbe1";    // 解密IV

    private final LazyLoadImgPresenter imagePresenter = new LazyLoadImgPresenter(this);

    public HomeLazyImgFragment() {
        setLazyLoadImgListener(this);
    }

    @Override
    public void loadImg() {
        getActivity().runOnUiThread(() -> {
            List<String> imageUrls = new ArrayList<>();
            List<MultiItemEntity> list = adapter.getData();
            for (MultiItemEntity multiItemEntity : list) {
                if (multiItemEntity.getItemType() == ITEM_LIST.getType() ||
                        multiItemEntity.getItemType() == ITEM_SINGLE_LINE_LIST.getType()||
                        multiItemEntity.getItemType() == VOD_LIST.getType()) {
                    MainDataBean mainDataBean = (MainDataBean) multiItemEntity;
                    List<MainDataBean.Item> items = mainDataBean.getItems();
                    for (MainDataBean.Item item : items) {
                        imageUrls.add(item.getImg());
                    }
                }
            }
            String[] imageUrlArray = imageUrls.toArray(new String[0]);
            imagePresenter.getImage(KEY, IV, imageUrlArray);
        });
    }

    @Override
    public void imageSuccess(String imageUrl, String base64) {
        getActivity().runOnUiThread(() -> {
            List<MultiItemEntity> list = adapter.getData();
            for (int i=0,size=list.size(); i<size; i++) {
                MultiItemEntity multiItemEntity = list.get(i);
                if (multiItemEntity.getItemType() == ITEM_LIST.getType() ||
                    multiItemEntity.getItemType() == ITEM_SINGLE_LINE_LIST.getType()||
                    multiItemEntity.getItemType() == VOD_LIST.getType()) {
                    MainDataBean mainDataBean = (MainDataBean) multiItemEntity;
                    List<MainDataBean.Item> items = mainDataBean.getItems();
                    for (int j=0,size2=items.size(); j<size2; j++) {
                        MainDataBean.Item item = items.get(j);
                        if (imageUrl.equals(item.getImg())) {
                            item.setBase64Img(base64);
                            // 获取 homeItemAdapter 并刷新它
                            HomeItemAdapter homeItemAdapter = mainDataBean.getHomeItemAdapter();
                            if (homeItemAdapter != null) {
                                homeItemAdapter.notifyItemChanged(j);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void imageError(String imageUlr) {
        LogUtil.logInfo(imageUlr, "获取base64图片失败");
    }
}
