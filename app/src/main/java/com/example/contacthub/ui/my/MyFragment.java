package com.example.contacthub.ui.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.widget.ContactCardView;

public class MyFragment extends Fragment {

    private ContactCardView contactCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my, container, false);

        // 获取ContactCardView引用
        contactCardView = root.findViewById(R.id.contact_card_view);

        // 创建联系人数据
        Contact contact = new Contact();
        contact.setName("张三");
        contact.setMobileNumber("13800138000");
        contact.setTelephoneNumber("010-12345678");
        contact.setEmail("zhangsan@example.com");
        contact.setAddress("北京市海淀区");

        // 显示联系人信息
        contactCardView.setContact(contact);

        return root;
    }
}