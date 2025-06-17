package my.project.moviesbox.view.lazyLoadImage;

import static my.project.moviesbox.utils.Utils.isPad;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.moviesbox.R;
import my.project.moviesbox.config.GlideApp;
import my.project.moviesbox.contract.LazyLoadImgContract;
import my.project.moviesbox.database.manager.THistoryManager;
import my.project.moviesbox.database.manager.TVideoManager;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.presenter.LazyLoadImgPresenter;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.DetailsActivity;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2025/5/17 19:33
 */
public class DetailsLazyImgActivity extends DetailsActivity implements LazyLoadImgListener, LazyLoadImgContract.View  {
    private final static String KEY = "f5d965df75336270";   // 解密KEY
    private final static String IV = "97b60394abc2fbe1";    // 解密IV

    private final LazyLoadImgPresenter imagePresenter = new LazyLoadImgPresenter(this);

    public DetailsLazyImgActivity() {
        setLazyLoadImgListener(this);
    }

    @Override
    public void imageSuccess(String imageUrl, String base64) {
        if (isFinishing()) return;
        runOnUiThread(() -> {
            if (imageUrl.equals(detailsDataBean.getImg())) {
                detailsDataBean.setImg(base64);
                setCollapsingToolbar();
            } else {
                if (!Utils.isNullOrEmpty(multiAdapter)) {
                    List<DetailsDataBean.Recommend> multiList = detailsDataBean.getMultiList();
                    for (int i = 0; i < multiList.size(); i++) {
                        DetailsDataBean.Recommend multi = multiList.get(i);
                        if (imageUrl.equals(multi.getImg())) {
                            multi.setImg(base64);
                            break;
                        }
                    }
                    multiAdapter.notifyDataSetChanged();
                }

                if (!Utils.isNullOrEmpty(recommendAdapter)) {
                    List<DetailsDataBean.Recommend> recommendList = detailsDataBean.getRecommendList();
                    for (int i = 0; i < recommendList.size(); i++) {
                        DetailsDataBean.Recommend recommend = recommendList.get(i);
                        if (imageUrl.equals(recommend.getImg())) {
                            recommend.setImg(base64);
                            break;
                        }
                    }
                    recommendAdapter.notifyDataSetChanged();
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
        imageUrls.add(detailsDataBean.getImg());
        for (DetailsDataBean.Recommend multi : detailsDataBean.getMultiList()) {
            imageUrls.add(multi.getImg());
        }
        for (DetailsDataBean.Recommend recommend : detailsDataBean.getRecommendList()) {
            imageUrls.add(recommend.getImg());
        }
        String[] imageUrlArray = imageUrls.toArray(new String[0]);
        imagePresenter.getImage(KEY, IV, imageUrlArray);
    }

    @Override
    protected void setCollapsingToolbar() {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565);
        if (!detailsDataBean.getImg().contains("base64")) {
            bgView.setVisibility(View.GONE);
        } else {
            // 获取屏幕高度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;

            // 计算高度的二分之一
            int height = screenHeight / 2;

            // 设置ImageView的高度
            ViewGroup.LayoutParams params = bgView.getLayoutParams();
            params.height = height;
            bgView.setLayoutParams(params);
            // 设置图片信息
            if (isPad()) {
                // 如果是平板 设置背景模糊
                GlideApp.with(this)
                        .load(detailsDataBean.getImg())
                        .override(500)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .apply(options)
                        .error(getDrawable(R.drawable.default_bg))
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 5)))
                        .into(bgView);
                bgView.setVisibility(View.VISIBLE);
                GlideApp.with(this)
                        .asBitmap()
                        .load(detailsDataBean.getImg())
                        .override(500)
                        .apply(new RequestOptions()
                                .encodeQuality(70)
                        )
                        .transition(BitmapTransitionOptions.withCrossFade(500))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // 获取图片宽高
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                // 回调结果
                                if (width > height) {
                                    vodImgType1View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType1View.setImageBitmap(resource);
                                    imgType1View.setVisibility(View.VISIBLE);
                                } else {
                                    vodImgType0View.setTag(R.id.imageid, detailsDataBean.getImg());
                                    vodImgType0View.setImageBitmap(resource);
                                    imgType0View.setVisibility(View.VISIBLE);
                                }
                                // 显示图片
                                padImgBoxView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                padImgBoxView.setVisibility(View.GONE);
                            }
                        });
            } else {
                // 手机设备显示原图
                GlideApp.with(this)
                        .load(detailsDataBean.getImg())
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .apply(options)
                        .error(getDrawable(R.drawable.default_bg))
                        .into(bgView);
                bgView.setVisibility(View.VISIBLE);
                // 不显示图片
                padImgBoxView.setVisibility(View.GONE);
                // 将标题完全显示 调整字体大小
                titleView.setTextSize(24);
                titleView.setMaxLines(99);
                infoView.setTextSize(18);
                scoreView.setTextSize(18);
                updateTimeView.setTextSize(18);
            }
            String title = detailsDataBean.getTitle();
            //创建剧集索引
            String vodId = TVideoManager.insertVod(title);
            // 更新历史记录
            THistoryManager.addOrUpdateHistory(vodId, detailsDataBean.getUrl(), detailsDataBean.getImg());
        }
    }
    @Override
    protected void onDestroy() {
        if (!Utils.isNullOrEmpty(imagePresenter))
            imagePresenter.detachView();
        super.onDestroy();
    }
}
