package com.example.contacthub.ui.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.widget.ContactCardView;

public class MyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my, container, false);

        // 获取ContactCardView引用
        ContactCardView contactCardView = root.findViewById(R.id.contact_card_view);

        // 创建联系人数据
        Contact contact = new Contact();
        contact.setName("张三");
        contact.setMobileNumber("13800138000");
        contact.setTelephoneNumber("010-12345678");
        contact.setEmail("zhangsan@example.com");
        contact.setAddress("北京市海淀区");
        contactCardView.findViewById(R.id.btn_call).setVisibility(View.GONE);
        contactCardView.findViewById(R.id.btn_message).setVisibility(View.GONE);

        ((android.widget.Button) contactCardView.findViewById(R.id.btn_share)).setText("分享我的名片");
        // 显示联系人信息
        contactCardView.setContact(contact);

        return root;
    }
}