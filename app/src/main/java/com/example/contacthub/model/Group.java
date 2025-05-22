package com.example.contacthub.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;

/**
 * 联系人分组实体类
 * 用于管理联系人的分类
 */
public class Group {
    private int id;          // 分组ID
    private String name;     // 分组名称
    
    private transient boolean expanded;  // 界面展示状态，不序列化

    /**
     * 完整构造方法
     * 
     * @param id 分组ID
     * @param expanded 展开状态
     * @param name 分组名称
     */
    public Group(int id, boolean expanded, String name) {
        this.id = id;
        this.expanded = expanded;
        this.name = name;
    }

    /**
     * 默认构造方法
     * 创建未展开的空分组
     */
    public Group() {
        this.expanded = false;
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

    /**
     * 检查分组是否处于展开状态
     * 
     * @return 如果分组展开则返回true，否则返回false
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * 设置分组展开状态
     * 
     * @param expanded 分组展开状态
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * 为分组生成新的唯一ID
     * 通过读取现有分组文件确定最大ID值并加1
     * 若读取失败则使用基于时间戳的ID生成方式
     * 
     * @param context 应用上下文，用于访问文件
     */
    public void generateNewId(Context context) {
        int newId = 0;
        try {
            // 读取现有分组列表找到最大ID
            FileInputStream fis = context.openFileInput("groups.json");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String json = new String(buffer);
            JSONArray groupsArray = new JSONArray(json);

            // 找出最大ID
            int maxId = 0;
            for (int i = 0; i < groupsArray.length(); i++) {
                JSONObject group = groupsArray.getJSONObject(i);
                int id = group.getInt("id");
                if (id > maxId) {
                    maxId = id;
                }
            }

            // 新ID为最大ID加1
            newId = maxId + 1;
        } catch (Exception e) {
            Log.e("Group", "生成新ID失败，使用时间戳ID", e);
            // 使用时间戳生成唯一ID
            long timestamp = System.currentTimeMillis();
            newId = (int) (timestamp % Integer.MAX_VALUE);
            if (newId < 10000) newId += 10000; // 确保ID至少有5位数
        } finally {
            this.id = newId;
        }
    }
}
