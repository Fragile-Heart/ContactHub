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

/**
 * 联系人列表Fragment，负责展示所有联系人并提供搜索、添加、按字母索引等功能
 */
public class ContactListFragment extends Fragment implements AlphabetIndexView.OnLetterSelectedListener {

    private FragmentContactListBinding binding;
    private FileUtil fileUtil;
    private List<Contact> allContacts;
    private Map<String, List<Contact>> contactMapByPinyin;

    private ActivityResultLauncher<Intent> addContactLauncher;

    /**
     * 初始化Fragment并注册ActivityResultLauncher
     * 
     * @param savedInstanceState 保存的实例状态
     */
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

    /**
     * 创建并返回Fragment的视图
     * 
     * @param inflater 用于膨胀布局的LayoutInflater
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 创建的视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactListBinding.inflate(inflater, container, false);
        
        AlphabetIndexView alphabetIndexView = binding.alphabetIndex;
        alphabetIndexView.setOnLetterSelectedListener(this);
        return binding.getRoot();
    }

    /**
     * 当视图创建完成后进行初始化操作
     * 
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化文件工具类
        fileUtil = new FileUtil(requireContext());

        // 加载联系人数据并更新UI
        loadContactsAndUpdateUI();

        // 设置搜索框监听器
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString().trim().toLowerCase();
                updateContactList(key);
            }
        });

        // 添加FAB点击事件，用于新建联系人
        binding.fabAddGroup.setOnClickListener(v -> addNewContact());
    }

    /**
     * Fragment恢复可见状态时重新加载联系人数据
     */
    @Override
    public void onResume() {
        super.onResume();
        loadContactsAndUpdateUI();
        Log.d("ContactListFragment", "onResume: 重新加载联系人数据");
    }

    /**
     * 加载联系人数据并更新UI
     * 从本地存储加载所有联系人，并按拼音首字母分组
     */
    private void loadContactsAndUpdateUI() {
        allContacts = loadContactList();
        contactMapByPinyin = ContactIndexer.groupByFirstLetter(allContacts);
        updateContactList("");
    }

    /**
     * 根据搜索关键词更新联系人列表
     * 
     * @param key 搜索关键词，为空时显示所有联系人并按拼音分组
     */
    private void updateContactList(String key) {
        if (key.isEmpty()) {
            // 显示所有联系人，按拼音分组
            binding.recyclerContactList.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recyclerContactList.setAdapter(new ContactSortByPinyinAdapter(contactMapByPinyin));
            binding.alphabetIndex.setVisibility(View.VISIBLE);
        } else {
            // 搜索联系人
            List<Contact> filteredContacts = ContactIndexer.search(allContacts, key);

            if (filteredContacts.isEmpty()) {
                // 无搜索结果时显示空列表
                binding.recyclerContactList.setLayoutManager(new LinearLayoutManager(requireContext()));
                ContactAdapter adapter = new ContactAdapter(filteredContacts);
                adapter.setSearchKeyword(key);
                binding.recyclerContactList.setAdapter(adapter);
                binding.alphabetIndex.setVisibility(View.GONE);
            } else {
                // 有搜索结果时仍然按拼音分组显示
                Map<String, List<Contact>> filteredMap = ContactIndexer.groupByFirstLetter(filteredContacts);
                binding.recyclerContactList.setLayoutManager(new LinearLayoutManager(requireContext()));
                ContactSortByPinyinAdapter adapter = new ContactSortByPinyinAdapter(filteredMap);
                adapter.setSearchKeyword(key);
                binding.recyclerContactList.setAdapter(adapter);
                binding.alphabetIndex.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 从存储加载联系人列表
     * 
     * @return 加载的联系人列表，如果出错则返回空列表
     */
    private List<Contact> loadContactList() {
        try {
            Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
            return Arrays.asList(contacts);
        } catch (Exception e) {
            Log.e("ContactListFragment", "加载联系人失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 添加新联系人
     * 创建空白联系人对象并跳转到编辑页面
     */
    private void addNewContact() {
        // 创建一个空白的联系人对象
        Contact newContact = new Contact();
        newContact.setName("");
        newContact.setMobileNumber("");
        newContact.setTelephoneNumber("");
        newContact.setEmail("");
        newContact.setAddress("");
        newContact.generatePinyin();

        // 启动编辑页面
        Intent intent = new Intent(requireContext(), ContactEditActivity.class);
        intent.putExtra("contact", newContact);
        intent.putExtra("isNewContact", true);
        addContactLauncher.launch(intent);
    }

    /**
     * 保存新联系人到存储并更新UI
     * 
     * @param newContact 需要保存的新联系人对象
     */
    private void saveNewContact(Contact newContact) {
        // 为新联系人生成ID
        if (newContact.getId() == null) {
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

    /**
     * 处理字母索引被选中的事件
     * 
     * @param letter 被选中的字母
     */
    @Override
    public void onLetterSelected(String letter) {
        if (binding.recyclerContactList.getAdapter() instanceof ContactSortByPinyinAdapter) {
            ContactSortByPinyinAdapter adapter = (ContactSortByPinyinAdapter) binding.recyclerContactList.getAdapter();
            Map<String, Integer> sectionIndexer = adapter.getSectionIndexer();
            
            Integer position = sectionIndexer.get(letter);
            if (position != null) {
                binding.recyclerContactList.scrollToPosition(position);
            } else {
                Log.d("ContactListFragment", "onLetterSelected: 未找到对应的位置");
            }
        }
    }
}
