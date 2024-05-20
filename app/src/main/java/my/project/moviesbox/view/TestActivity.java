package my.project.moviesbox.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.permissionx.guolindev.PermissionX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import my.project.moviesbox.R;
import my.project.moviesbox.utils.SAFUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 9:31
 */
public class TestActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 123;
    private static final int REQUEST_DOCUMENT_TREE = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        checkPermission();
    }

    private void checkPermission() {
        if (gtSdk33()) {
            // Android13以上通知权限
            PermissionX.init(this)
                    .permissions(PermissionX.permission.POST_NOTIFICATIONS)
                    .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, getString(R.string.notificationsPermissionsTitle), getString(R.string.permissionsAgree), getString(R.string.permissionsDisagree)))
                    .request((allGranted, grantedList, deniedList) -> performStorageOperations());
        } else
            performStorageOperations();
    }

    private boolean gtSdk33() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private void performStorageOperations() {
        String savePath = SharedPreferencesUtils.getDataSaveUri();
        if (!savePath.isEmpty()) {
            Uri targetFolderUri = Uri.parse(savePath);
            // 如果已经有权限，直接执行操作
            Toast.makeText(this, "已授权的目录：" + targetFolderUri.toString(), Toast.LENGTH_SHORT).show();
            onStorageAccessGranted(targetFolderUri);
        } else {
            // 否则，打开文档树以获取权限
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_DOCUMENT_TREE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DOCUMENT_TREE && resultCode == RESULT_OK) {
            // 用户选择了目录
            Uri treeUri = data.getData();
            if (treeUri != null) {
                // 授予权限给这个 URI
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // 保存授权的目录
                SharedPreferencesUtils.setParam("saveUri", treeUri.toString());

                // 这里可以进行其他操作，比如显示一个成功消息
                Toast.makeText(this, "已授权的目录：" + treeUri.toString() + ", 开始测试", Toast.LENGTH_SHORT).show();

                // Step 1: 在应用的私有目录中创建一个名为 config.MP4 的文件
                createConfigFile();

                // Step 2: 获取用户授权，访问外部存储的特定目录（例如 "download" 目录）
//                onStorageAccessGranted(treeUri);
//                Uri uri = SAFUtils.copyConfigFileToSAF(this, 0, getFilesDir().getAbsolutePath()+"/樱花动漫/进击的巨人/第一集.MP4", "", true, null);
//                LogUtil.logInfo("moveUri", uri.toString());
            }
        }
    }

    private void createConfigFile() {
        // 应用的私有目录
        File privateDir = new File(getFilesDir().getAbsolutePath()+"/樱花动漫/进击的巨人");
        if (!privateDir.exists())
            privateDir.mkdirs();
        File configFile = new File(privateDir, "第一集.MP4");
        try {
            // 创建一个示例文件
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            // 这里可以写入一些配置信息到 config.MP4 中
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onStorageAccessGranted(Uri uri) {
        // Step 3: 在授权的目录中创建多级目录，如：/video/touhou/
        // 创建第一级目录
        /*Uri firstLevelDirUri = createDirectoryInSAF(uri, "video");
        if (firstLevelDirUri != null) {
            // 创建第二级目录
            Uri secondLevelDirUri = createDirectoryInSAF(firstLevelDirUri, "touhou");
            if (secondLevelDirUri != null) {
                // 在第二级目录中创建文件
                // Step 4: 将应用私有目录中的 config.MP4 文件复制到授权目录中
                copyConfigFileToSAF(secondLevelDirUri);

                // Step 5: 删除应用私有目录中的 config.MP4 文件
                deleteConfigFile();
            }
        }*/
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        /*DocumentFile documentFile = pickedDir.findFile("FUCK");
        if (documentFile.exists()) {
            LogUtil.logInfo("FUCK一级文件夹", "存在");
            documentFile.delete();
        }
        DocumentFile newDir = pickedDir.createDirectory("FUCK");

        DocumentFile newDir2 = newDir.createDirectory("FUCK2");*/
        DocumentFile path = SAFUtils.createDirectory(this, uri, "樱花动漫/进击的巨人");
        // 在第二级目录中创建文件
        // Step 4: 将应用私有目录中的 config.MP4 文件复制到授权目录中
        copyConfigFileToSAF(path.getUri());

        // Step 5: 删除应用私有目录中的 config.MP4 文件
        deleteConfigFile();
    }

    private boolean copyConfigFileToSAF(Uri uri) {
        /*LogUtil.logInfo("fileSavePath", uri.toString());
        LogUtil.logInfo("pickedDir ", pickedDir.getUri().toString());
        DocumentFile newFile =
                pickedDir.createFile("video/mp4", "config.mp4");*/
        File sourceFile = new File(getFilesDir(), "config.MP4");
        if (!sourceFile.exists()) {
            // 源文件不存在
            return false;
        }
        DocumentFile targetDirectory = DocumentFile.fromTreeUri(this, uri);
        try {
            Uri targetFileUri = DocumentsContract.createDocument(getContentResolver(), uri, "video/mp4", "config.mp4");

            try (InputStream inputStream = new FileInputStream(sourceFile);
                 OutputStream outputStream = getContentResolver().openOutputStream(targetFileUri)) {
                if (inputStream == null || outputStream == null) {
                    // 输入流或输出流为 null
                    return false;
                }

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                // 移动成功后删除源文件
                if (sourceFile.delete()) {
                    return true; // 移动成功并且源文件删除成功
                } else {
                    return false; // 移动成功但是源文件删除失败
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void deleteConfigFile() {
        File configFile = new File(getFilesDir(), "config.MP4");
        if (configFile.exists()) {
            configFile.delete();
        }
    }
}
