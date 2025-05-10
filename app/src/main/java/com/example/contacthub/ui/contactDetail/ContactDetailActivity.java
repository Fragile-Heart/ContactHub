package com.example.contacthub.ui.contactDetail;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contacthub.databinding.FragmentContactBinding;
import com.example.contacthub.model.Contact;

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
        } else {
            // 处理未找到联系人信息的情况
            finish();
        }
    }

    private void displayContactInfo(Contact contact) {
        // 直接使用ContactCardView设置联系人信息
        binding.contactCardView.setContact(contact);
    }
}