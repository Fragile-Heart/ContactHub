package com.example.contacthub.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;

public class ContactCardView extends CardView {
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView telephoneTextView;
    private TextView mobileTextView;
    private TextView emailTextView;
    private TextView addressTextView;

    public ContactCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_contact_card, this, true);

        avatarImageView = findViewById(R.id.iv_contact_avatar);
        nameTextView = findViewById(R.id.tv_contact_name);
        telephoneTextView = findViewById(R.id.tv_telephone_number);
        mobileTextView = findViewById(R.id.tv_mobile_number);
        emailTextView = findViewById(R.id.tv_contact_email);
        addressTextView = findViewById(R.id.tv_contact_address);
        avatarImageView = findViewById(R.id.iv_contact_avatar);
    }

    public void setContact(Contact contact) {
        // 设置联系人头像
        // 如果有自定义头像加载逻辑，可以在这里处理

        // 设置联系人姓名
        nameTextView.setText(contact.getName());

        // 设置电话号码
        telephoneTextView.setText(contact.getTelephoneNumber());
        mobileTextView.setText(contact.getMobileNumber());

        // 设置邮箱（需要确保Contact类有这些字段）
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            emailTextView.setText(contact.getEmail());
        } else {
            findViewById(R.id.email_container).setVisibility(GONE);
        }

        // 设置地址
        if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
            addressTextView.setText(contact.getAddress());
        } else {
            findViewById(R.id.address_container).setVisibility(GONE);
        }
    }
}