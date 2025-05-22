package com.example.contacthub.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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

/**
 * 联系人列表适配器，用于显示联系人信息并支持搜索高亮
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contacts;
    private OnContactClickListener listener;
    private String searchKeyword = "";

    // 显示设置的常量
    private static final String PREFS_NAME = "ContactDisplayPrefs";
    private static final String KEY_SHOW_MOBILE = "show_mobile";
    private static final String KEY_SHOW_TELEPHONE = "show_telephone";
    private static final String KEY_SHOW_ADDRESS = "show_address";

    /**
     * 联系人点击监听器接口
     */
    public interface OnContactClickListener {
        /**
         * 联系人被点击时的回调方法
         * 
         * @param contact 被点击的联系人对象
         */
        void onContactClick(Contact contact);
    }

    /**
     * 设置联系人点击监听器
     *
     * @param listener 实现了OnContactClickListener接口的监听器
     */
    public void setOnContactClickListener(OnContactClickListener listener) {
        this.listener = listener;
    }

    /**
     * 绑定视图数据
     *
     * @param holder 视图持有者
     * @param position 项目在列表中的位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        Context context = holder.itemView.getContext();

        // 获取显示设置
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean showMobile = prefs.getBoolean(KEY_SHOW_MOBILE, true);
        boolean showTelephone = prefs.getBoolean(KEY_SHOW_TELEPHONE, true);
        boolean showAddress = prefs.getBoolean(KEY_SHOW_ADDRESS, true);

        // 设置联系人姓名（带高亮）
        setHighlightedText(holder.nameTextView, contact.getName(), searchKeyword);

        // 检查是否有任何字段匹配
        boolean mobileMatches = !TextUtils.isEmpty(searchKeyword) &&
                              contact.getMobileNumber() != null &&
                              contact.getMobileNumber().contains(searchKeyword);

        boolean telephoneMatches = !TextUtils.isEmpty(searchKeyword) &&
                                 contact.getTelephoneNumber() != null &&
                                 contact.getTelephoneNumber().contains(searchKeyword);

        boolean addressMatches = !TextUtils.isEmpty(searchKeyword) &&
                               contact.getAddress() != null &&
                               contact.getAddress().contains(searchKeyword);

        // 显示手机号(高亮匹配部分)
        if ((showMobile || mobileMatches) && !TextUtils.isEmpty(contact.getMobileNumber())) {
            holder.mobileTextView.setVisibility(View.VISIBLE);
            setHighlightedText(holder.mobileTextView, "手机: " + contact.getMobileNumber(), searchKeyword);
        } else {
            holder.mobileTextView.setVisibility(View.GONE);
        }

        // 显示电话号(高亮匹配部分)
        if ((showTelephone || telephoneMatches) && !TextUtils.isEmpty(contact.getTelephoneNumber())) {
            holder.telephoneTextView.setVisibility(View.VISIBLE);
            setHighlightedText(holder.telephoneTextView, "电话: " + contact.getTelephoneNumber(), searchKeyword);
        } else {
            holder.telephoneTextView.setVisibility(View.GONE);
        }

        // 显示地址(高亮匹配部分)
        if ((showAddress || addressMatches) && !TextUtils.isEmpty(contact.getAddress())) {
            holder.addressTextView.setVisibility(View.VISIBLE);
            setHighlightedText(holder.addressTextView, "地址: " + contact.getAddress(), searchKeyword);
        } else {
            holder.addressTextView.setVisibility(View.GONE);
        }

        // 设置联系人头像
        if (!TextUtils.isEmpty(contact.getPhoto())) {
            Bitmap avatarBitmap = PhotoUtil.base64ToBitmap(contact.getPhoto());
            if (avatarBitmap != null) {
                holder.profileImageView.setImageBitmap(avatarBitmap);
            } else {
                holder.profileImageView.setImageResource(R.drawable.ic_person);
            }
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_person);
        }

        // 设置点击事件处理
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }

            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("contact", contact);
            context.startActivity(intent);
        });
    }

    /**
     * 设置高亮文本
     *
     * @param textView 要设置的TextView
     * @param text 原始文本
     * @param keyword 要高亮的关键词
     */
    private void setHighlightedText(TextView textView, String text, String keyword) {
        if (TextUtils.isEmpty(keyword) || TextUtils.isEmpty(text)) {
            textView.setText(text);
            return;
        }

        SpannableString spannableString = new SpannableString(text);
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        int startIndex = lowerText.indexOf(lowerKeyword);
        while (startIndex >= 0) {
            int endIndex = startIndex + keyword.length();
            if (endIndex <= text.length()) {
                spannableString.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            startIndex = lowerText.indexOf(lowerKeyword, startIndex + 1);
        }
        
        textView.setText(spannableString);
    }

    /**
     * 构造函数
     *
     * @param contacts 联系人列表
     */
    public ContactAdapter(List<Contact> contacts) {
        this.contacts = contacts;
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
     * 创建视图持有者
     *
     * @param parent 父视图组
     * @param viewType 视图类型
     * @return 新创建的ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 获取项目数量
     *
     * @return 联系人列表的大小
     */
    @Override
    public int getItemCount() {
        return contacts.size();
    }

    /**
     * 联系人视图持有者
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView mobileTextView;
        TextView telephoneTextView;
        TextView addressTextView;
        ImageView profileImageView;

        /**
         * 构造函数
         *
         * @param itemView 项目视图
         */
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
