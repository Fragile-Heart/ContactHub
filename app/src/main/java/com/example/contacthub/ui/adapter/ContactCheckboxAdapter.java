package com.example.contacthub.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 联系人复选框适配器，用于选择多个联系人
 */
public class ContactCheckboxAdapter extends RecyclerView.Adapter<ContactCheckboxAdapter.ViewHolder> {

    private final List<Contact> contacts; // 所有联系人
    private final List<Contact> filteredContacts; // 过滤后的联系人
    private final Set<Integer> selectedContactIds; // 已选择的联系人ID集合

    /**
     * 构造函数
     *
     * @param contacts 联系人列表
     * @param groupMemberIds 群组成员ID列表，用于预选中已在群组中的联系人
     */
    public ContactCheckboxAdapter(List<Contact> contacts, List<Integer> groupMemberIds) {
        this.contacts = contacts;
        this.filteredContacts = new ArrayList<>(contacts);
        this.selectedContactIds = new HashSet<>(groupMemberIds != null ? groupMemberIds : new ArrayList<>());
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
                .inflate(R.layout.item_contact_checkbox, parent, false);
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
        Contact contact = filteredContacts.get(position);
        holder.tvContactName.setText(contact.getName());
        
        // 设置选中状态但不触发监听器
        holder.checkboxContact.setOnCheckedChangeListener(null);
        holder.checkboxContact.setChecked(selectedContactIds.contains(contact.getId()));
        
        // 设置点击整行或复选框都能切换状态
        View.OnClickListener clickListener = v -> {
            boolean isChecked = !holder.checkboxContact.isChecked();
            holder.checkboxContact.setChecked(isChecked);
            if (isChecked) {
                selectedContactIds.add(contact.getId());
            } else {
                selectedContactIds.remove(contact.getId());
            }
        };
        
        holder.itemView.setOnClickListener(clickListener);
        holder.checkboxContact.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedContactIds.add(contact.getId());
            } else {
                selectedContactIds.remove(contact.getId());
            }
        });
    }

    /**
     * 获取项目数量
     *
     * @return 过滤后的联系人列表大小
     */
    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    /**
     * 获取已选择的联系人ID列表
     *
     * @return 已选择联系人ID的列表
     */
    public List<Integer> getSelectedContactIds() {
        return new ArrayList<>(selectedContactIds);
    }

    /**
     * 根据关键词过滤联系人列表
     *
     * @param query 搜索关键词
     */
    public void filter(String query) {
        filteredContacts.clear();
        
        if (query == null || query.isEmpty()) {
            // 如果搜索词为空，显示所有联系人
            filteredContacts.addAll(contacts);
        } else {
            // 转为小写以便不区分大小写搜索
            String lowerCaseQuery = query.toLowerCase();
            
            // 筛选匹配的联系人
            for (Contact contact : contacts) {
                if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredContacts.add(contact);
                } else if (contact.getMobileNumber() != null && contact.getMobileNumber().contains(query)) {
                    filteredContacts.add(contact);
                } else if (contact.getEmail() != null && contact.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    /**
     * 联系人复选框视图持有者
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkboxContact;
        final TextView tvContactName;

        /**
         * 构造函数
         *
         * @param itemView 项目视图
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxContact = itemView.findViewById(R.id.checkbox_contact);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);
        }
    }
}
