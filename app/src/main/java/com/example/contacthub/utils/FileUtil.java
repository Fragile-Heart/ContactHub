package com.example.contacthub.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文件工具类
 * 提供JSON格式文件的读写功能
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private final Context context;
    private final Gson gson = new Gson();

    /**
     * 构造函数
     * 
     * @param context 应用程序上下文
     */
    public FileUtil(Context context) {
        this.context = context;
    }

    /**
     * 将对象数组保存为JSON文件
     * 
     * @param objects 要保存的对象数组
     * @param filename 目标文件名
     * @param <T> 对象类型
     */
    public <T> void saveJSON(T[] objects, String filename) {
        try {
            String json = gson.toJson(objects);
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "保存JSON到文件失败: " + filename, e);
        }
    }

    /**
     * 从JSON文件读取并转换为指定类型的对象
     * 
     * @param filename 要读取的文件名
     * @param classOfT 目标对象类型
     * @param <T> 对象类型
     * @return 转换后的对象，如果读取失败则返回null
     */
    public <T> T readFile(String filename, Class<T> classOfT) {
        String json = readFile(filename);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, classOfT);
    }

    /**
     * 读取文件内容
     * 
     * @param filename 要读取的文件名
     * @return 文件内容字符串，如果读取失败则返回null
     */
    public String readFile(String filename) {
        try (FileInputStream fis = context.openFileInput(filename);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "读取文件失败: " + filename, e);
            return null;
        }
    }
}
