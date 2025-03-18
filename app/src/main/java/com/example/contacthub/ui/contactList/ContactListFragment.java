package com.example.contacthub.ui.contactList;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contacthub.adapter.ContactAdapter;
import com.example.contacthub.adapter.ContactSortByPinyinAdapter;
import com.example.contacthub.databinding.FragmentContactListBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.utils.ContactIndexer;
import com.example.contacthub.utils.FileUtil;
import com.example.contacthub.widget.AlphabetIndexView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContactListFragment extends Fragment implements AlphabetIndexView.OnLetterSelectedListener {

    private FragmentContactListBinding binding;
    private FileUtil fileUtil;
    private List<Contact> allContacts;
    Map<String, List<Contact>> contactMapByPinyin;
    
    private AlphabetIndexView alphabetIndexView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图绑定初始化布局
        binding = FragmentContactListBinding.inflate(inflater, container, false);

        alphabetIndexView = binding.alphabetIndex;
        alphabetIndexView.setOnLetterSelectedListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化文件工具类
        fileUtil = new FileUtil(requireContext());

        allContacts = loadContactList();

        contactMapByPinyin = ContactIndexer.groupByFirstLetter(allContacts);

        updateContactList("");

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString().trim().toLowerCase();
                // 更新联系人列表
                updateContactList(key);
            }
        });

    }

    //模糊搜索更新联系人列表
    private void updateContactList(String key){

        List<Contact> nowContactList;

        if(key.isEmpty())
        {
            binding.recyclerContactList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recyclerContactList.setAdapter(new ContactSortByPinyinAdapter(contactMapByPinyin));

            binding.alphabetIndex.setVisibility(View.VISIBLE);

        } else{
            nowContactList = ContactIndexer.search(allContacts, key);
            binding.recyclerContactList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recyclerContactList.setAdapter(new ContactAdapter(nowContactList));

            binding.alphabetIndex.setVisibility(View.GONE);
        }

    }

    private List<Contact> loadContactList()
    {
        try {
            Contact[] contacts = fileUtil.readJSON("contacts.json", Contact[].class);
            return Arrays.asList(contacts);
        }catch (Exception e)
        {
            Log.e("ContactListFragment", "加载联系人失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void onLetterSelected(String letter) {
        ContactSortByPinyinAdapter adapter = (ContactSortByPinyinAdapter) binding.recyclerContactList.getAdapter();
//
        Map<String,Integer> sectionIndexer = adapter.getSectionIndexer();
//
        Integer position = sectionIndexer.get(letter);
        if (position != null) {
            binding.recyclerContactList.scrollToPosition(position);
        }else{
            Log.d("ContactListFragment", "onLetterSelected: 未找到对应的位置");
        }
    }
}