package com.example.contacthub.ui.contactDetail;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.contacthub.databinding.FragmentContactBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.widget.ContactCardView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contacthub.R;
;

public class ContactDetailActivity extends AppCompatActivity {

    private FragmentContactBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使用 ViewBinding 绑定布局
        binding = FragmentContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取传递的 Contact 对象
        Contact contact = (Contact) getIntent().getSerializableExtra("contact");
        if (contact != null) {
            setupContactCard(contact);
            // 使用 contact 对象
            TextView nameTextView = findViewById(R.id.tv_contact_name);
            nameTextView.setText(contact.getName());
            TextView emailTextView = findViewById(R.id.tv_contact_email);
            emailTextView.setText(contact.getEmail());
            TextView addressTextView = findViewById(R.id.tv_contact_address);
            addressTextView.setText(contact.getAddress());
            TextView mobileTextView = findViewById(R.id.tv_mobile_number);
            mobileTextView.setText(contact.getMobileNumber());
            TextView telephoneTextView = findViewById(R.id.tv_telephone_number);
            telephoneTextView.setText(contact.getTelephoneNumber());

        } else {
            // 处理未找到联系人信息的情况
            Toast.makeText(this, "未找到联系人信息", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupContactCard(Contact contact) {
        // 设置联系人卡片视图
        ContactCardView contactCard = binding.contactCard;
        contactCard.setContact(contact);

        // 设置拨打电话按钮点击事件
        binding.btnCall.setOnClickListener(v -> handleCallButtonClick(contact));

        // 设置发送短信按钮点击事件
        binding.btnMessage.setOnClickListener(v -> handleMessageButtonClick(contact));

        // 隐藏分享按钮（如果不需要）
        binding.btnShare.setVisibility(View.GONE);

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void handleCallButtonClick(Contact contact) {
        boolean hasMobile = contact.getMobileNumber() != null && !contact.getMobileNumber().isEmpty();
        boolean hasTelephone = contact.getTelephoneNumber() != null && !contact.getTelephoneNumber().isEmpty();

        if (hasMobile && hasTelephone) {
            // 同时有手机和座机号码，显示选择对话框
            String[] options = new String[]{"手机: " + contact.getMobileNumber(),
                    "座机: " + contact.getTelephoneNumber()};

            new AlertDialog.Builder(this)
                    .setTitle("选择拨打号码")
                    .setItems(options, (dialog, which) -> {
                        String number = which == 0 ? contact.getMobileNumber() : contact.getTelephoneNumber();
                        dialNumber(number);
                    })
                    .show();
        } else if (hasMobile) {
            // 只有手机号
            dialNumber(contact.getMobileNumber());
        } else if (hasTelephone) {
            // 只有座机号
            dialNumber(contact.getTelephoneNumber());
        } else {
            Toast.makeText(this, "无可用电话号码", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMessageButtonClick(Contact contact) {
        boolean hasMobile = contact.getMobileNumber() != null && !contact.getMobileNumber().isEmpty();

        if (hasMobile) {
            // 只提供手机号发送短信
            sendSms(contact.getMobileNumber());
        } else {
            Toast.makeText(this, "没有可用的手机号码发送短信", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber.replaceAll("[^0-9]", "")));
        startActivity(intent);
    }

    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("[^0-9]", "")));
        startActivity(intent);
    }
}