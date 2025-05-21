package com.example.contacthub.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.contactDetail.ContactDetailActivity;
import com.example.contacthub.utils.PhotoUtil;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contacts;
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getName());
        String photoBase64 = contact.getPhoto();
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            // 使用PhotoUtil将Base64字符串转换为Bitmap
            Bitmap avatarBitmap = PhotoUtil.base64ToBitmap(photoBase64);
            if (avatarBitmap != null) {
                holder.profileImageView.setImageBitmap(avatarBitmap);
            } else {
                // 解码失败，显示默认头像
                holder.profileImageView.setImageResource(R.drawable.ic_person);
            }
        } else {
            // 没有头像数据，显示默认头像
            holder.profileImageView.setImageResource(R.drawable.ic_person);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }

            // 跳转到 ContactDetailActivity
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("contact", contact);
            context.startActivity(intent);
        });
    }

    public ContactAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView profileImageView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_contact_name);
            profileImageView = itemView.findViewById(R.id.iv_contact_photo);
        }
    }
}
