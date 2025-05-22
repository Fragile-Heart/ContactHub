package com.example.contacthub.ui.adapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactSortByPinyinAdapter extends RecyclerView.Adapter<ContactSortByPinyinAdapter.ViewHolder> {

    private Map<String, List<Contact>> contactMapByPinyin;
    private List<String> sortedKeys; // 存储排序后的键

    public ContactSortByPinyinAdapter(Map<String, List<Contact>> contactMapByPinyin) {
        this.contactMapByPinyin = contactMapByPinyin;
        // 初始化并排序键列表
        this.sortedKeys = getSortedKeyList();
    }

    // 获取排序后的键列表，确保"#"在最后
    private List<String> getSortedKeyList() {
        List<String> keys = new ArrayList<>(contactMapByPinyin.keySet());

        // 自定义排序：把"#"放在最后，其他字母按正常顺序
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_sort_by_pinyin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 使用排序后的键
        String pinyin = sortedKeys.get(position);

        // 设置拼音首字母
        holder.pinyinTextView.setText(pinyin);

        // 为嵌套RecyclerView设置LayoutManager和Adapter
        holder.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        ContactAdapter contactAdapter = new ContactAdapter(contactMapByPinyin.get(pinyin));

        holder.contactsRecyclerView.setAdapter(contactAdapter);

        // 确保子RecyclerView显示完整内容
        holder.contactsRecyclerView.setHasFixedSize(true);
    }

    @Override
    public int getItemCount() {
        return sortedKeys.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pinyinTextView;
        RecyclerView contactsRecyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            pinyinTextView = itemView.findViewById(R.id.text_pinyin_letter);
            contactsRecyclerView = itemView.findViewById(R.id.recycler_contact_Pinyin);
        }
    }

    public Map<String, Integer> getSectionIndexer() {
        Map<String, Integer> sectionIndexer = new HashMap<>();

        // 使用排序后的键列表
        for (int i = 0; i < sortedKeys.size(); i++) {
            sectionIndexer.put(sortedKeys.get(i), i);
        }

        // 处理不存在的字母索引
        char index = 'Z';
        for(char c = 'Z'; c >= 'A'; c--) {
            String letter = String.valueOf(c);
            if(sectionIndexer.get(letter) == null) {
                sectionIndexer.put(letter, sectionIndexer.get(String.valueOf(index)));
            } else {
                index = c;
            }
        }

        return sectionIndexer;
    }
}
