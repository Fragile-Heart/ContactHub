package com.example.contacthub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.model.Group;

import java.util.ArrayList;
import java.util.List;

// 分组适配器 - 负责显示所有分组以及其中的联系人
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Group> groups; // 所有分组
    private List<Contact> allContacts; // 所有联系人

    // 构造函数接收分组和联系人数据
    public GroupAdapter(List<Group> groups, List<Contact> allContacts) {
        this.groups = groups;
        this.allContacts = allContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建分组列表项视图
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);

        // 设置分组名称
        holder.nameTextView.setText(group.getName());

        // 根据分组展开状态设置图标
        holder.expandIcon.setImageResource(
                group.isExpanded() ? R.drawable.ic_expand_less : R.drawable.ic_expand_more
        );

        // 根据分组展开状态设置成员列表可见性
        holder.membersRecyclerView.setVisibility(
                group.isExpanded() ? View.VISIBLE : View.GONE
        );

        // 设置点击分组头部时的展开/折叠行为
        holder.groupHeader.setOnClickListener(v -> {
            // 切换分组的展开状态
            group.setExpanded(!group.isExpanded());
            // 通知适配器此项已变化
            notifyItemChanged(position);
        });

        // 如果分组已展开，则显示其中的联系人
        if (group.isExpanded()) {
            // 筛选属于当前分组的联系人
            List<Contact> groupContacts = new ArrayList<>();
            for (Contact contact : allContacts) {
                if (contact.getGroupIds().contains(group.getId())) {
                    groupContacts.add(contact);
                }
            }

            // 设置联系人列表的适配器和布局管理器
            ContactAdapter contactAdapter = new ContactAdapter(groupContacts);
            holder.membersRecyclerView.setAdapter(contactAdapter);
            holder.membersRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        }
    }

    @Override
    public int getItemCount() {
        return groups.size(); // 返回分组数量
    }

    // ViewHolder保存分组列表项视图组件的引用
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView expandIcon;
        RecyclerView membersRecyclerView;
        RelativeLayout groupHeader;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.group_name);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            membersRecyclerView = itemView.findViewById(R.id.recycler_members);
            groupHeader = itemView.findViewById(R.id.group_header);
        }
    }
}