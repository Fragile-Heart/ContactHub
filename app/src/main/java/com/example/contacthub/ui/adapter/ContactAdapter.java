package com.example.contacthub.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.contacthub.ui.view.contactDetail.ContactDetailActivity;
import com.example.contacthub.utils.PhotoUtil;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contacts;
    private OnContactClickListener listener;

    // 添加显示设置的常量
    private static final String PREFS_NAME = "ContactDisplayPrefs";
    private static final String KEY_SHOW_MOBILE = "show_mobile";
    private static final String KEY_SHOW_TELEPHONE = "show_telephone";
    private static final String KEY_SHOW_ADDRESS = "show_address";

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        Context context = holder.itemView.getContext();

        // 获取显示设置
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean showMobile = prefs.getBoolean(KEY_SHOW_MOBILE, true);
        boolean showTelephone = prefs.getBoolean(KEY_SHOW_TELEPHONE, true);
        boolean showAddress = prefs.getBoolean(KEY_SHOW_ADDRESS, true);

        // 设置联系人姓名
        holder.nameTextView.setText(contact.getName());

        // 根据设置显示手机号码
        String mobileNumber = contact.getMobileNumber();
        if (showMobile && mobileNumber != null && !mobileNumber.isEmpty()) {
            holder.mobileTextView.setText("手机: " + mobileNumber);
            holder.mobileTextView.setVisibility(View.VISIBLE);
        } else {
            holder.mobileTextView.setVisibility(View.GONE);
        }

        // 根据设置显示固定电话
        String telephoneNumber = contact.getTelephoneNumber();
        if (showTelephone && telephoneNumber != null && !telephoneNumber.isEmpty()) {
            holder.telephoneTextView.setText("电话: " + telephoneNumber);
            holder.telephoneTextView.setVisibility(View.VISIBLE);
        } else {
            holder.telephoneTextView.setVisibility(View.GONE);
        }

        // 根据设置显示地址
        String address = contact.getAddress();
        if (showAddress && address != null && !address.isEmpty()) {
            holder.addressTextView.setText("地址: " + address);
            holder.addressTextView.setVisibility(View.VISIBLE);
        } else {
            holder.addressTextView.setVisibility(View.GONE);
        }

        // 设置联系人头像
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
        TextView mobileTextView;
        TextView telephoneTextView;
        TextView addressTextView;
        ImageView profileImageView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_contact_name);
            mobileTextView = itemView.findViewById(R.id.tv_contact_mobile);
            telephoneTextView = itemView.findViewById(R.id.tv_contact_telephone);
            addressTextView = itemView.findViewById(R.id.tv_contact_address);
            profileImageView = itemView.findViewById(R.id.iv_contact_photo);
        }
    }
}
