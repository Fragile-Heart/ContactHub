package com.example.contacthub.ui.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.contacthub.R;
import com.example.contacthub.databinding.FragmentContactBinding;
import com.example.contacthub.model.Contact;
import com.google.android.material.appbar.AppBarLayout;

public class MyFragment extends Fragment {

    private FragmentContactBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                        ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentContactBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 隐藏顶部 AppBar，因为主界面已有导航栏
        binding.appbar.setVisibility(View.GONE);
        
        // 隐藏浮动编辑按钮
        //binding.fabEdit.setVisibility(View.GONE);
        
        // 调整 NestedScrollView 的布局参数，不再依赖于 AppBar
        ViewGroup.LayoutParams params = binding.nestedScrollView.getLayoutParams();
        if (params instanceof androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
            ((androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) params).setBehavior(null);
        }

        setupUserProfile();

        return root;
    }

    private void setupUserProfile() {
        // 创建一个用户资料
        Contact userProfile = new Contact();
        userProfile.setName("张三");
        userProfile.setMobileNumber("178-2526-0421");
        userProfile.setTelephoneNumber("010-1234-5678");
        userProfile.setEmail("zhangsan@example.com");
        userProfile.setAddress("北京市海淀区中关村大街1号");

        // 设置用户信息
        binding.tvContactName.setText(userProfile.getName());
        binding.tvMobileNumber.setText(userProfile.getMobileNumber());
        binding.tvTelephoneNumber.setText(userProfile.getTelephoneNumber());
        binding.tvContactEmail.setText(userProfile.getEmail());
        binding.tvContactAddress.setText(userProfile.getAddress());
        binding.contactAvatar.setImageResource(R.drawable.ic_person);

        // 设置拨打电话按钮点击事件
        binding.btnCall.setOnClickListener(v -> handleCallButtonClick(userProfile));

        // 设置发送短信按钮点击事件
        binding.btnMessage.setOnClickListener(v -> handleMessageButtonClick(userProfile));

        // 修改分享按钮为"退出登录"
        binding.btnShare.setVisibility(View.GONE);
    }

    private void handleCallButtonClick(Contact contact) {
        boolean hasMobile = contact.getMobileNumber() != null && !contact.getMobileNumber().isEmpty();
        boolean hasTelephone = contact.getTelephoneNumber() != null && !contact.getTelephoneNumber().isEmpty();

        if (hasMobile && hasTelephone) {
            // 同时有手机和座机号码，显示选择对话框
            String[] options = new String[]{"手机: " + contact.getMobileNumber(),
                    "座机: " + contact.getTelephoneNumber()};

            new AlertDialog.Builder(requireContext())
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
            Toast.makeText(requireContext(), "无可用电话号码", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMessageButtonClick(Contact contact) {
        boolean hasMobile = contact.getMobileNumber() != null && !contact.getMobileNumber().isEmpty();

        if (hasMobile) {
            // 只提供手机号发送短信
            sendSms(contact.getMobileNumber());
        } else {
            Toast.makeText(requireContext(), "没有可用的手机号码发送短信", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}