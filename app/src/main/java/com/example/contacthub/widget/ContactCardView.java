package com.example.contacthub.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ContactCardView extends FrameLayout {

    private MaterialCardView cardContactInfo;
    private MaterialButton btnCall;
    private MaterialButton btnMessage;
    private MaterialButton btnShare;
    private FloatingActionButton fabEdit;

    private TextView tvName;
    private TextView tvMobileNumber;
    private TextView tvTelephoneNumber;
    private TextView tvEmail;
    private TextView tvAddress;
    private ShapeableImageView contactAvatar;

    private Contact contact;

    public interface OnContactCardActionListener {
        // 将 Contact 对象作为参数传递
        void onCallButtonClick(Contact contact);
        void onMessageButtonClick(Contact contact);
        void onShareButtonClick(Contact contact);
        void onEditButtonClick(Contact contact);
    }
    private OnContactCardActionListener actionListener;

    public void setOnContactCardActionListener(OnContactCardActionListener listener) {
        this.actionListener = listener;
    }

    // 成员变量，用于存储当前显示的联系人对象
    private Contact currentContact;

    public ContactCardView(@NonNull Context context, Contact contact) {
        super(context);
        this.contact = contact;
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, Contact contact) {
        super(context, attrs);
        this.contact = contact;
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, Contact contact) {
        super(context, attrs, defStyleAttr);
        this.contact = contact;
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, Contact contact) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.contact = contact;
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_contact_card, this, true);

        // 找到布局中的各个 View 元素
        cardContactInfo = findViewById(R.id.card_contact_info);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnShare = findViewById(R.id.btn_share);
        fabEdit = findViewById(R.id.fab_edit);
        tvName = findViewById(R.id.tv_contact_name);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvTelephoneNumber = findViewById(R.id.tv_telephone_number);
        tvEmail = findViewById(R.id.tv_contact_email);
        tvAddress = findViewById(R.id.tv_contact_address);
        contactAvatar = findViewById(R.id.contact_avatar);

        // 设置按钮点击监听器，在触发时传递 currentContact 对象
        btnCall.setOnClickListener(v -> {
            if (actionListener != null && currentContact != null) {
                // 可以选择在这里检查 phoneNumber 是否为空，或者由 Activity 处理
                actionListener.onCallButtonClick(currentContact);
            }
        });

        btnMessage.setOnClickListener(v -> {
            if (actionListener != null && currentContact != null) {
                // 可以选择在这里检查 mobileNumber 是否为空
                actionListener.onMessageButtonClick(currentContact);
            }
        });

        btnShare.setOnClickListener(v -> {
            if (actionListener != null && currentContact != null) {
                actionListener.onShareButtonClick(currentContact);
            }
        });

        if (fabEdit != null) {
            fabEdit.setOnClickListener(v -> {
                if (actionListener != null && currentContact != null) {
                    actionListener.onEditButtonClick(currentContact);
                }
            });
        }

        // 初始化时清空显示，或者显示默认占位符
        clearContactInfo(); // 添加一个清空信息的方法
    }

    public void clearContactInfo() {
        this.currentContact = null; // 清除存储的联系人对象

        // 清空所有信息

        tvName.setText(""); // 清空姓名
        tvMobileNumber.setText(""); // 清空手机号码
        tvTelephoneNumber.setText(""); // 清空座机号码
        tvEmail.setText(""); // 清空电子邮件
        tvAddress.setText(""); // 清空地址

        // 设置默认头像或清空头像
        contactAvatar.setImageResource(R.drawable.ic_person); // 设置默认头像
    }

}
