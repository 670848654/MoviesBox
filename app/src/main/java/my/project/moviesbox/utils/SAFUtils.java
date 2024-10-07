package my.project.moviesbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import my.project.moviesbox.parser.LogUtil;

/**
 * @author Li
 * @version 1.0
 * @description: Storage Access Framework 工具类
 * @date 2024/5/13 16:20
 */
public class SAFUtils {

    /**
     * 公共目录Download是否可读
     * 不可读则存入私有目录中
     * @return
     */
    public static boolean canReadDownloadDirectory() {
        return new File(Utils.APP_DATA_PATH).canRead();
    }

    /**
     * 检查是否授权保存目录
     * @return
     */
    public static boolean checkHasSetDataSaveUri() {
        return !SharedPreferencesUtils.getDataSaveUri().isEmpty();
    }

    /**
     * 获取授权目录路劲
     * @return
     */
    public static String getUriDirectoryName() {
        String uri = SharedPreferencesUtils.getDataSaveUri();
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
            return uri.split(":")[2];
        } catch (Exception e) {
            return uri.split("3A%")[2];
        }
    }

    /**
     * 显示未授权弹窗
     * @param context
     * @param onClickListener
     */
    public static void showUnauthorizedAlert(Activity activity, DialogInterface.OnClickListener onClickListener) {
        Utils.showAlert(
                activity,
                "!*操作失败*!",
                "无法写入外部存储，请选择授权保存目录后再进行此操作！",
                false,
                "目录授权",
                "取消",
                "",
                 onClickListener,
                null,
                null);
    }

    /**
     * 创建文件夹
     * @param context
     * @param treeUri
     * @param directoryPath
     * @return
     */
    public static DocumentFile createDirectory(Context context, Uri treeUri, String directoryPath) {
        String[] parts = directoryPath.split("/");
        DocumentFile parent = DocumentFile.fromTreeUri(context, treeUri);
        for (String part : parts) {
            DocumentFile nextDir = parent.findFile(part);
            if (nextDir == null) {
                // 如果目录不存在，则创建
                nextDir = parent.createDirectory(part);
            }
            parent = nextDir;
        }
        return parent;
    }

    /**
     * 复制私有目录文件到授权目录下
     * @param context
     * @param sourceFilePath
     * @param mimeType
     * @param deleteFile
     * @return
     */
    public static Uri copyConfigFileToSAF(Context context, String sourceFilePath, String mimeType, boolean deleteFile) {
        File sourceFile = new File(sourceFilePath);
        LogUtil.logInfo("absolutePath", sourceFile.getAbsolutePath());
        String relativePath = sourceFilePath.replaceAll(context.getFilesDir().getAbsolutePath(), "");
        LogUtil.logInfo("relativePath", relativePath);
        String[] relativePathArr = relativePath.split("/");
        StringBuilder stringBuilder = new StringBuilder();
        String fileName = "";
        for (int i=0,size=relativePathArr.length; i<size; i++) {
            if (!relativePathArr[i].isEmpty()) {
                if (i == relativePathArr.length-1) {
                    fileName = relativePathArr[i];
                } else {
                    stringBuilder.append(relativePathArr[i]);
                    if (i != relativePathArr.length-2)
                        stringBuilder.append("/");
                }
            }
        }
        LogUtil.logInfo("directory", stringBuilder.toString());
        LogUtil.logInfo("fileName", fileName);
        /*if (!sourceFile.exists()) {
            // 源文件不存在
            return false;
        }*/
        /*DocumentFile targetDirectory = DocumentFile.fromTreeUri(context, targetDirectoryUri);
        if (targetDirectory == null || !targetDirectory.exists()) {
            // 目标目录不存在
            return false;
        }*/
        Uri uri = DocumentFile.fromTreeUri(context, Uri.parse(SharedPreferencesUtils.getDataSaveUri())).getUri();
        DocumentFile saveDF;
        // 创建文件夹
        if (!stringBuilder.toString().isEmpty()) {
            saveDF = SAFUtils.createDirectory(context, uri, stringBuilder.toString());
            LogUtil.logInfo("createDirectory", saveDF.getUri().toString());
            uri = saveDF.getUri();
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (!isFileNameExists(context, uri, relativePath)) {
                Uri targetFileUri = DocumentsContract.createDocument(context.getContentResolver(), uri, mimeType, fileName);
                inputStream = new FileInputStream(sourceFile);
                outputStream = context.getContentResolver().openOutputStream(targetFileUri);
                if (inputStream == null || outputStream == null) {
                    // 输入流或输出流为 null
                    return null;
                }

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;
                long totalBytes = sourceFile.length();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                outputStream.flush();
                if (deleteFile)
                    sourceFile.delete();
                return targetFileUri; // 返回移动后的目标文件 Uri
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询授权目录是否有相同名称文件
     * @param context
     * @param parentUri
     * @param relativePath
     * @return
     */
    public static boolean isFileNameExists(Context context, Uri parentUri, String relativePath) {
        // 获取目录中所有文件的 ID
        String[] projection = {DocumentsContract.Document.COLUMN_DOCUMENT_ID};
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, DocumentsContract.getDocumentId(parentUri));
        try (Cursor cursor = context.getContentResolver().query(childrenUri, projection, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 获取索引
                    int columnIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                    if (columnIndex != -1) {
                        // 检查是否存在相同的文件名
                        String documentId = cursor.getString(columnIndex);
                        /*String existingFileName = DocumentsContract.getDocumentId(parentUri) + ":" + documentId;
                        LogUtil.logInfo("parentUri", DocumentsContract.getDocumentId(parentUri));
                        LogUtil.logInfo("documentId", documentId);
                        LogUtil.logInfo("existingFileName", existingFileName);*/
                        if (documentId.contains(relativePath)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
