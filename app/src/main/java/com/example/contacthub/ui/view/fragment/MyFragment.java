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

    /**
     * 创建Fragment视图
     * 
     * @param inflater 用于加载布局的LayoutInflater
     * @param container 视图的父容器
     * @param savedInstanceState 保存的状态数据
     * @return 创建的视图
     */
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

    /**
     * 处理Activity返回结果
     * 用于处理名片编辑后的数据更新
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 先尝试让ContactCardView处理结果
        contactCardView.handleActivityResult(requestCode, resultCode, data);

        // 无论如何都重新加载我的名片数据以确保显示最新内容
        loadMyCard();

        Log.d(TAG, "已刷新我的名片数据");
    }

    /**
     * Fragment恢复可见时的处理
     * 确保显示最新的名片数据
     */
    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时也刷新数据
        loadMyCard();
    }

    /**
     * 加载个人名片数据
     * 从本地存储读取my.json并显示到视图中
     */
    private void loadMyCard() {
        Contact contact = fileUtil.readFile("my.json", Contact.class);
        if (contact != null) {
            contactCardView.setContact(contact);
        }
    }
}
