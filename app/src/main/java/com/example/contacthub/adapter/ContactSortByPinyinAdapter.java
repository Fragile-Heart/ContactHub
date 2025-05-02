package com.example.contacthub.adapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactSortByPinyinAdapter extends RecyclerView.Adapter<ContactSortByPinyinAdapter.ViewHolder> {

    Map<String, List<Contact>> contactMapByPinyin;
    // 添加监听器字段和设置方法
    public ContactSortByPinyinAdapter(Map<String, List<Contact>> contactMapByPinyin) {
        this.contactMapByPinyin = contactMapByPinyin;
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

        // 获取当前位置的拼音首字母
        String pinyin = (String) contactMapByPinyin.keySet().toArray()[position];

        // 设置拼音首字母
        holder.pinyinTextView.setText(pinyin);

        // 为嵌套RecyclerView设置LayoutManager和Adapter
        holder.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        ContactAdapter contactAdapter = new ContactAdapter(contactMapByPinyin.get(pinyin));

        holder.contactsRecyclerView.setAdapter(contactAdapter);

        holder.contactsRecyclerView.setAdapter(new ContactAdapter(contactMapByPinyin.get(pinyin)));

        // 确保子RecyclerView显示完整内容
        holder.contactsRecyclerView.setHasFixedSize(true);
    }

    @Override
    public int getItemCount() {
        return contactMapByPinyin.size();
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
         int position = 0;
         List<String> keys = new ArrayList<>();

         for (String key : contactMapByPinyin.keySet()) {
             sectionIndexer.put(key, position);
             position++;
             keys.add(key);
         }
         sectionIndexer.put("#", position);

         sectionIndexer.putIfAbsent("Z", position);

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
