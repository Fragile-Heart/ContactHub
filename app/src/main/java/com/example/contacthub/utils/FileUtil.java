package com.example.contacthub.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
    private final Context context;
    private final Gson gson = new Gson();

    public FileUtil(Context context) {
        this.context = context;
    }

    // 新增方法：保存对象数组到文件
    public <T> void saveJSON(T[] objects, String filename) {
        try {
            String json = gson.toJson(objects);
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {

            Log.e("FileUtil", "Error saving JSON to file: " + filename, e);
        }
    }

    // 读取 - 获取JSON文件作为特定对象
    public <T> T readJSON(String filename, Class<T> classOfT) {
        String json = readFile(filename);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, classOfT);
    }

    // 辅助方法 - 读取文件内容
    private String readFile(String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e("FileUtil", "Error read JSON to file: " + filename, e);
            return null;
        }
    }
}
