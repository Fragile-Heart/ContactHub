package com.example.contacthub.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 图片处理工具类，提供图片与Base64编码之间的转换功能
 */
public class PhotoUtil {
    private static final String TAG = "PhotoUtil";

    /**
     * 将Bitmap转换为Base64字符串
     * 
     * @param bitmap 要转换的Bitmap图像
     * @return Base64编码的字符串，如果转换失败则返回null
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap转Base64失败", e);
            return null;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭输出流失败", e);
            }
        }
    }

    /**
     * 将Base64字符串解码为Bitmap图像
     * 
     * @param base64String Base64编码的字符串
     * @return 解码后的Bitmap图像，如果解码失败则返回null
     */
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Base64转Bitmap失败", e);
            return null;
        }
    }

    /**
     * 将图片文件转换为Base64字符串
     * 
     * @param filePath 图片文件路径
     * @return Base64编码的字符串，如果转换失败则返回null
     */
    public static String imageFileToBase64(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "文件不存在: " + filePath);
            return null;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            inputStream.read(buffer);
            return Base64.encodeToString(buffer, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "文件转Base64失败", e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 将Bitmap保存到输出流
     * 
     * @param bitmap 要保存的Bitmap图像
     * @param out 输出流
     * @throws IOException 当写入失败时抛出
     */
    public static void saveBitmapToStream(Bitmap bitmap, java.io.OutputStream out) throws IOException {
        if (bitmap == null || out == null) {
            throw new IllegalArgumentException("Bitmap和输出流不能为null");
        }
        
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
}
