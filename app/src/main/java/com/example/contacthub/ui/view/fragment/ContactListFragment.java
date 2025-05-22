package com.example.contacthub.ui.view.fragment;

import android.content.Intent;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contacthub.ui.adapter.ContactAdapter;
import com.example.contacthub.ui.adapter.ContactSortByPinyinAdapter;
import com.example.contacthub.databinding.FragmentContactListBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.view.contactDetail.ContactEditActivity;
import com.example.contacthub.utils.ContactIndexer;
import com.example.contacthub.utils.FileUtil;
import com.example.contacthub.ui.widget.AlphabetIndexView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContactListFragment extends Fragment implements AlphabetIndexView.OnLetterSelectedListener {

    private FragmentContactListBinding binding;
    private FileUtil fileUtil;
    private List<Contact> allContacts;
    Map<String, List<Contact>> contactMapByPinyin;

    private ActivityResultLauncher<Intent> addContactLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册 ActivityResultLauncher
        addContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    // 获取更新后的联系人
                    Contact updatedContact = (Contact) result.getData().getSerializableExtra("updatedContact");
                    if (updatedContact != null) {
                        // 保存新联系人并刷新列表
                        saveNewContact(updatedContact);
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图绑定初始化布局
        binding = FragmentContactListBinding.inflate(inflater, container, false);

        AlphabetIndexView alphabetIndexView = binding.alphabetIndex;
        alphabetIndexView.setOnLetterSelectedListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化文件工具类
        fileUtil = new FileUtil(requireContext());

        // 加载联系人数据并更新UI
        loadContactsAndUpdateUI();

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

        // 添加FAB点击事件，用于新建联系人
        binding.fabAddGroup.setOnClickListener(v -> {
            addNewContact();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在Fragment恢复可见状态时重新加载联系人数据
        // 这确保了当用户从编辑页面返回时，能看到最新的联系人数据
        loadContactsAndUpdateUI();
        Log.d("ContactListFragment", "onResume: 重新加载联系人数据");
    }

    // 加载联系人数据并更新UI的方法
    private void loadContactsAndUpdateUI() {
        allContacts = loadContactList();
        contactMapByPinyin = ContactIndexer.groupByFirstLetter(allContacts);
        updateContactList("");
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
            Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
            return Arrays.asList(contacts);
        }catch (Exception e)
        {
            Log.e("ContactListFragment", "加载联系人失败", e);
            return new ArrayList<>();
        }
    }

    // 添加新联系人的方法
    private void addNewContact() {
        // 创建一个空白的联系人对象
        Contact newContact = new Contact();

        // 设置默认值
        newContact.setName("");
        newContact.setMobileNumber("");
        newContact.setTelephoneNumber("");
        newContact.setEmail("");
        newContact.setAddress("");

        // 生成拼音信息（虽然为空）
        newContact.generatePinyin();

        // 启动编辑页面，使用新的 ActivityResultLauncher
        Intent intent = new Intent(requireContext(), ContactEditActivity.class);
        intent.putExtra("contact", newContact);
        intent.putExtra("isNewContact", true); // 标记为新建联系人
        addContactLauncher.launch(intent);
    }

    // 保存新联系人
    private void saveNewContact(Contact newContact) {
        // 为新联系人生成ID
        if (newContact.getId() == null) {
            // 找出当前最大ID并加1
            int maxId = 0;
            for (Contact contact : allContacts) {
                if (contact.getId() != null && contact.getId() > maxId) {
                    maxId = contact.getId();
                }
            }
            newContact.setId(maxId + 1);
        }

        // 确保重新生成拼音信息
        newContact.generatePinyin();

        // 将新联系人添加到列表
        List<Contact> updatedContacts = new ArrayList<>(allContacts);
        updatedContacts.add(newContact);

        // 保存到文件
        try {
            Contact[] contactsArray = updatedContacts.toArray(new Contact[0]);
            fileUtil.saveJSON(contactsArray, "contacts.json");
            Log.d("ContactListFragment", "新联系人已保存: " + newContact.getName());

            // 重新加载列表
            loadContactsAndUpdateUI();
        } catch (Exception e) {
            Log.e("ContactListFragment", "保存新联系人失败", e);
        }
    }

    @Override
    public void onLetterSelected(String letter) {
        ContactSortByPinyinAdapter adapter = (ContactSortByPinyinAdapter) binding.recyclerContactList.getAdapter();

        Map<String,Integer> sectionIndexer = adapter.getSectionIndexer();

        Integer position = sectionIndexer.get(letter);
        if (position != null) {
            binding.recyclerContactList.scrollToPosition(position);
        }else{
            Log.d("ContactListFragment", "onLetterSelected: 未找到对应的位置");
        }
    }
}
