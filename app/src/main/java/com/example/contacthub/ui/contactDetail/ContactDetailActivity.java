package com.example.contacthub.ui.contactDetail;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.contacthub.databinding.FragmentContactBinding;
import com.example.contacthub.model.Contact;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ContactDetailActivity extends AppCompatActivity {

    private FragmentContactBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使用 ViewBinding 绑定布局
        binding = FragmentContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置工具栏
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // 工具栏返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 获取传递的 Contact 对象
        Contact contact = (Contact) getIntent().getSerializableExtra("contact");
        if (contact != null) {
            displayContactInfo(contact);
            setupActionButtons(contact);
        } else {
            // 处理未找到联系人信息的情况
            Toast.makeText(this, "未找到联系人信息", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayContactInfo(Contact contact) {
        // 设置联系人姓名
        binding.tvContactName.setText(contact.getName());
        
        // 设置联系人详细信息
        binding.tvMobileNumber.setText(contact.getMobileNumber());
        binding.tvTelephoneNumber.setText(contact.getTelephoneNumber());
        binding.tvContactEmail.setText(contact.getEmail());
        binding.tvContactAddress.setText(contact.getAddress());
        
        // 设置浮动编辑按钮点击事件
        binding.fabEdit.setOnClickListener(v -> {
            Toast.makeText(this, "编辑联系人: " + contact.getName(), Toast.LENGTH_SHORT).show();
            // 这里可以添加跳转到编辑页面的代码
        });
    }

    private void setupActionButtons(Contact contact) {
        // 设置拨打电话按钮点击事件
        binding.btnCall.setOnClickListener(v -> handleCallButtonClick(contact));

        // 设置发送短信按钮点击事件
        binding.btnMessage.setOnClickListener(v -> handleMessageButtonClick(contact));

        // 设置分享按钮点击事件
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareContent = "联系人: " + contact.getName() + "\n" +
                    "手机: " + contact.getMobileNumber() + "\n" +
                    "邮箱: " + contact.getEmail();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
            startActivity(Intent.createChooser(shareIntent, "分享联系人"));
        });
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