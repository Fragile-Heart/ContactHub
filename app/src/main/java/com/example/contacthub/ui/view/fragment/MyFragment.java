package com.example.contacthub.ui.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.widget.ContactCardView;
import com.example.contacthub.utils.FileUtil;

public class MyFragment extends Fragment {

    private static final String TAG = "MyFragment";
    private ContactCardView contactCardView;
    private FileUtil fileUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my, container, false);

        fileUtil = new FileUtil(getContext());

        // 获取ContactCardView引用并保存
        contactCardView = root.findViewById(R.id.contact_card_view);

        // 设置为"我的名片"标记
        contactCardView.setMyCard(true);

        // 加载并显示我的名片
        loadMyCard();

        contactCardView.findViewById(R.id.btn_call).setVisibility(View.GONE);
        contactCardView.findViewById(R.id.btn_message).setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 先尝试让ContactCardView处理结果
        contactCardView.handleActivityResult(requestCode, resultCode, data);

        // 无论如何都重新加载我的名片数据以确保显示最新内容
        loadMyCard();

        Log.d(TAG, "已刷新我的名片数据");
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时也刷新数据
        loadMyCard();
    }

    // 从my.json加载名片数据
    private void loadMyCard() {
        Contact contact = fileUtil.readFile("my.json", Contact.class);
        if (contact != null) {
            contactCardView.setContact(contact);
        }
    }
}