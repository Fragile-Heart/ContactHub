package com.example.contacthub.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按拼音首字母分组显示联系人的适配器
 */
public class ContactSortByPinyinAdapter extends RecyclerView.Adapter<ContactSortByPinyinAdapter.ViewHolder> {

    private Map<String, List<Contact>> contactMapByPinyin;
    private List<String> sortedKeys; // 存储排序后的首字母键
    private String searchKeyword = "";

    /**
     * 构造函数
     *
     * @param contactMapByPinyin 按拼音首字母分组的联系人映射
     */
    public ContactSortByPinyinAdapter(Map<String, List<Contact>> contactMapByPinyin) {
        this.contactMapByPinyin = contactMapByPinyin;
        this.sortedKeys = getSortedKeyList();
    }

    /**
     * 获取排序后的首字母键列表，确保"#"在最后
     *
     * @return 排序后的首字母键列表
     */
    private List<String> getSortedKeyList() {
        List<String> keys = new ArrayList<>(contactMapByPinyin.keySet());

        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // 如果是"#"，始终排在后面
                if ("#".equals(s1)) return 1;
                if ("#".equals(s2)) return -1;
                // 其他按字母顺序排列
                return s1.compareTo(s2);
            }
        });

        return keys;
    }

    /**
     * 创建ViewHolder
     *
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return 新创建的ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_sort_by_pinyin, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定视图数据
     *
     * @param holder 视图持有者
     * @param position 项目在列表中的位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String pinyin = sortedKeys.get(position);

        // 设置拼音首字母
        holder.pinyinTextView.setText(pinyin);

        // 为嵌套RecyclerView设置适配器
        holder.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        ContactAdapter contactAdapter = new ContactAdapter(contactMapByPinyin.get(pinyin));
        
        // 将搜索关键词传递给子适配器
        if (!searchKeyword.isEmpty()) {
            contactAdapter.setSearchKeyword(searchKeyword);
        }
        
        holder.contactsRecyclerView.setAdapter(contactAdapter);
        holder.contactsRecyclerView.setHasFixedSize(true);
    }

    /**
     * 获取项目数量
     *
     * @return 拼音分组的数量
     */
    @Override
    public int getItemCount() {
        return sortedKeys.size();
    }

    /**
     * 获取字母索引位置映射，用于快速滚动到对应字母分组
     *
     * @return 字母到位置的映射
     */
    public Map<String, Integer> getSectionIndexer() {
        Map<String, Integer> sectionIndexer = new HashMap<>();

        // 使用排序后的键列表
        for (int i = 0; i < sortedKeys.size(); i++) {
            sectionIndexer.put(sortedKeys.get(i), i);
        }

        // 处理不存在的字母索引
        char index = 'Z';
        for (char c = 'Z'; c >= 'A'; c--) {
            String letter = String.valueOf(c);
            if (sectionIndexer.get(letter) == null) {
                sectionIndexer.put(letter, sectionIndexer.get(String.valueOf(index)));
            } else {
                index = c;
            }
        }

        return sectionIndexer;
    }

    /**
     * 设置搜索关键词，用于高亮显示匹配内容
     *
     * @param keyword 搜索关键词
     */
    public void setSearchKeyword(String keyword) {
        this.searchKeyword = keyword;
        notifyDataSetChanged();
    }

    /**
     * 视图持有者类
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pinyinTextView;
        RecyclerView contactsRecyclerView;

        /**
         * 构造函数
         *
         * @param itemView 项目视图
         */
        ViewHolder(View itemView) {
            super(itemView);
            pinyinTextView = itemView.findViewById(R.id.text_pinyin_letter);
            contactsRecyclerView = itemView.findViewById(R.id.recycler_contact_Pinyin);
        }
    }
}
