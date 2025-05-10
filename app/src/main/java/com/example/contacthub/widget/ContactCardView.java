package com.example.contacthub.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ContactCardView extends FrameLayout {

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

    // 成员变量，用于存储当前显示的联系人对象
    private Contact currentContact;

    public ContactCardView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setContact(Contact contact) {
        this.currentContact = contact;
        updateContactInfo();
    }

    private void updateContactInfo() {
        if (currentContact == null) {
            clearContactInfo();
            return;
        }

        // 更新各个UI元素显示联系人信息
        tvName.setText(currentContact.getName());

        // 更新手机号码，如果为空则隐藏
        String mobileNumber = currentContact.getMobileNumber();
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            tvMobileNumber.setText(mobileNumber);
            tvMobileNumber.setVisibility(View.VISIBLE);
        } else {
            tvMobileNumber.setVisibility(View.GONE);
        }

        // 更新座机号码，如果为空则隐藏
        String telephoneNumber = currentContact.getTelephoneNumber();
        if (telephoneNumber != null && !telephoneNumber.isEmpty()) {
            tvTelephoneNumber.setText(telephoneNumber);
            tvTelephoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvTelephoneNumber.setVisibility(View.GONE);
        }

        // 更新电子邮件，如果为空则隐藏
        String email = currentContact.getEmail();
        if (email != null && !email.isEmpty()) {
            tvEmail.setText(email);
            tvEmail.setVisibility(View.VISIBLE);
        } else {
            tvEmail.setVisibility(View.GONE);
        }

        // 更新地址，如果为空则隐藏
        String address = currentContact.getAddress();
        if (address != null && !address.isEmpty()) {
            tvAddress.setText(address);
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }

//        // 更新联系人头像（如果Contact类提供了头像）
//        if (currentContact.getAvatarUri() != null) {
//            // 假设Contact类有getAvatarUri方法
//            // 可以使用Glide或Picasso等图片加载库来加载头像
//            contactAvatar.setImageURI(currentContact.getAvatarUri());
//        } else {
//            contactAvatar.setImageResource(R.drawable.ic_person);
//        }
        contactAvatar.setImageResource(R.drawable.ic_person);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_contact_card, this, true);

        // 找到布局中的各个 View 元素
        //cardContactInfo = findViewById(R.id.card_contact_info);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnShare = findViewById(R.id.btn_share);
        fabEdit = findViewById(R.id.fab_edit);
        tvName = findViewById(R.id.tv_contact_name);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvTelephoneNumber = findViewById(R.id.tv_telephone_number);
        tvEmail = findViewById(R.id.tv_contact_email);
        tvAddress = findViewById(R.id.tv_location);
        contactAvatar = findViewById(R.id.contact_avatar);

        // 设置按钮点击监听器，在触发时传递 currentContact 对象
        btnCall.setOnClickListener(v -> {
            if (currentContact != null) {
                boolean hasMobile = currentContact.getMobileNumber() != null && !currentContact.getMobileNumber().isEmpty();
                boolean hasTelephone = currentContact.getTelephoneNumber() != null && !currentContact.getTelephoneNumber().isEmpty();

                if (hasMobile && hasTelephone) {
                    // 同时有手机和座机号码，显示选择对话框
                    String[] options = new String[]{"手机: " + currentContact.getMobileNumber(),
                            "座机: " + currentContact.getTelephoneNumber()};

                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
                            .setTitle("选择拨打号码")
                            .setItems(options, (dialog, which) -> {
                                String number = which == 0 ? currentContact.getMobileNumber() : currentContact.getTelephoneNumber();
                                dialNumber(number);
                            })
                            .show();
                } else if (hasMobile) {
                    // 只有手机号
                    dialNumber(currentContact.getMobileNumber());
                } else if (hasTelephone) {
                    // 只有座机号
                    dialNumber(currentContact.getTelephoneNumber());
                } else {
                    android.widget.Toast.makeText(getContext(), "无可用电话号码", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnMessage.setOnClickListener(v -> {
            if (currentContact != null && currentContact.getMobileNumber() != null
                    && !currentContact.getMobileNumber().isEmpty()) {
                // 直接实现发送短信逻辑
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + currentContact.getMobileNumber()));
                getContext().startActivity(intent);
            } else {
                // 提醒用户手机号码为空
                android.widget.Toast.makeText(getContext(), "无可用手机号",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            // TODO: 实现分享联系人信息的逻辑
        });

        fabEdit.setOnClickListener(v -> {
            // TODO: 实现编辑联系人信息的逻辑
        });

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

    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("[^0-9]", "")));
        getContext().startActivity(intent);
    }

}
