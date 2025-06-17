package my.project.moviesbox.model;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import my.project.moviesbox.contract.LazyLoadImgContract;
import my.project.moviesbox.event.HtmlSourceEvent;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 图片懒加载
 * @date 2024/4/10 13:57
 */
public class LazyLoadImgModel extends BaseModel implements LazyLoadImgContract.Model {

    @Override
    public void getImage(LazyLoadImgContract.LoadDataCallback callback, String key, String iv, String[] imageUrls) {
        Headers.Builder builder = new Headers.Builder();
        builder.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        for (String img : imageUrls) {
            if (Utils.isNullOrEmpty(img) || !img.startsWith("http")) {
                callback.imageError(img);
            } else {
                OkHttpUtils.getInstance().doGet(img, builder.build(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.imageError(img);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            // 从响应中获取字节数组
                            byte[] bytes = response.body().bytes();
                            // 将字节数组编码为Base64字符串
                            String base64String = Base64.encodeBase64String(bytes);
                            byte[] encryptedBytes = Base64.decodeBase64(base64String);
                            byte[] keyBytes = key.getBytes(); // 16字节密钥
                            byte[] ivBytes = iv.getBytes(); // 16字节 IV
                            // 使用密钥和 IV 创建 SecretKeySpec 和 IvParameterSpec 对象
                            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
                            // 创建 AES 加密器
                            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
                            // 执行解密操作
                            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                            // 将解密后的字节数组转换为 Base64 编码的字符串
                            String base64Decrypted = Base64.encodeBase64String(decryptedBytes);
                            String[] ary = img.split("\\.");
                            // 将数组转换为 ArrayList
                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(ary));
                            // 使用 ArrayList 的 remove() 方法移除最后一个元素
                            String removedElement = arrayList.remove(arrayList.size() - 1);
                            // 解码 Base64 字符串为字节数组
                            callback.imageSuccess(img, "data:image/"+removedElement+";base64,"+base64Decrypted);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.imageError(img);
                        }
                    }
                });
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebViewEvent(HtmlSourceEvent event) {}
}
