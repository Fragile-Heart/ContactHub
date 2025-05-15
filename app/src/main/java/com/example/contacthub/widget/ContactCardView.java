package com.example.contacthub.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.contacthub.ui.contactDetail.ContactEditActivity;
import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ContactCardView extends FrameLayout {

    private static final String TAG = "ContactCardView";
    private static final int EDIT_CONTACT_REQUEST_CODE = 100;

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

    private Contact currentContact;
    private OnContactUpdatedListener contactUpdatedListener;

    // 回调接口用于通知联系人更新事件
    public interface OnContactUpdatedListener {
        void onContactUpdated(Contact updatedContact);
    }

    public void setOnContactUpdatedListener(OnContactUpdatedListener listener) {
        this.contactUpdatedListener = listener;
    }

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

    public Contact getCurrentContact() {
        return currentContact;
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
            if (currentContact != null) {
                // 启动编辑联系人页面
                Intent intent = new Intent(getContext(), ContactEditActivity.class);
                // 将当前联系人对象传递给编辑页面
                intent.putExtra("contact", currentContact);
                
                // 如果上下文是Activity，使用startActivityForResult
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE);
                } else {
                    // 非Activity上下文直接启动，但无法接收返回结果
                    context.startActivity(intent);
                    Log.w(TAG, "编辑联系人：当前上下文不是Activity，无法接收编辑结果");
                }
            } else {
                android.widget.Toast.makeText(getContext(), "没有联系人可编辑", 
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化时清空显示，或者显示默认占位符
        clearContactInfo();
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

    // 供宿主Activity调用，处理编辑结果
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Contact updatedContact = (Contact) data.getSerializableExtra("updatedContact");
            if (updatedContact != null) {
                // 更新当前显示的联系人
                setContact(updatedContact);
                
                // 通知监听器联系人已更新
                if (contactUpdatedListener != null) {
                    contactUpdatedListener.onContactUpdated(updatedContact);
                }
                
                Log.d(TAG, "联系人已更新: " + updatedContact.getName());
            }
        }
    }
}
