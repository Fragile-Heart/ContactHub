package com.example.contacthub.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
    private final Context context;
    private final Gson gson = new Gson();

    public FileUtil(Context context) {
        this.context = context;
    }

    // 创建/更新 - 保存整个Map对象到文件
    public boolean saveJSON(Map<String, Object> dataMap, String filename) {
        try {
            String json = gson.toJson(dataMap);
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 读取 - 获取整个JSON文件内容为Map
    public Map<String, Object> readJSONAsMap(String filename) {
        String json = readFile(filename);
        if (json == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // 读取 - 获取JSON文件作为特定对象
    public <T> T readJSON(String filename, Class<T> classOfT) {
        String json = readFile(filename);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, classOfT);
    }

    // 读取 - 获取JSON文件作为对象列表
    public <T> List<T> readJSONList(String filename, Class<T[]> arrayClass) {
        String json = readFile(filename);
        if (json == null) {
            return new ArrayList<>();
        }
        T[] array = gson.fromJson(json, arrayClass);
        return new ArrayList<>(List.of(array));
    }

    // 更新 - 更新Map中的特定字段
    public boolean updateJSONField(String filename, String key, Object value) {
        Map<String, Object> data = readJSONAsMap(filename);
        data.put(key, value);
        return saveJSON(data, filename);
    }

    // 删除 - 删除Map中的特定字段
    public boolean deleteJSONField(String filename, String key) {
        Map<String, Object> data = readJSONAsMap(filename);
        data.remove(key);
        return saveJSON(data, filename);
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
            e.printStackTrace();
            return null;
        }
    }
}