package com.example.contacthub.model;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;

public class Group {
    private int id;
    private String name;
    
    private transient boolean expanded;

    public Group(int id, boolean expanded, String name) {
        this.id = id;
        this.expanded = expanded;
        this.name = name;
    }

    public Group()
    {
        this.expanded=false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * 为分组生成新的唯一ID
     */
    public void generateNewId(Context context) {
        int newId = 0;
        try {
            // 读取现有联系人列表找到最大ID
            FileInputStream fis = context.openFileInput("groups.json");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String json = new String(buffer);
            JSONArray contactsArray = new JSONArray(json);

            // 存储所有现有ID
            int maxId = 0;
            for (int i = 0; i < contactsArray.length(); i++) {
                JSONObject contact = contactsArray.getJSONObject(i);
                int id = contact.getInt("id");
                if (id > maxId) {
                    maxId = id;
                }
            }

            // 生成新ID并确保唯一性
            newId = maxId + 1;

        } catch (Exception e) {
            Log.e("groups" , "生成新ID失败，使用随机大数", e);
            // 生成一个较大的随机ID，降低冲突概率
            long timestamp = System.currentTimeMillis();
            newId = (int) (timestamp % Integer.MAX_VALUE); // 取时间戳的低位作为ID基础
            if (newId < 10000) newId += 10000; // 确保ID至少有5位数
        }finally {
            this.id = newId;
        }
    }
}
