package com.example.contacthub.ui.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contacthub.adapter.GroupAdapter;
import com.example.contacthub.databinding.FragmentGroupBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.model.Group;
import com.example.contacthub.utils.FileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupFragment extends Fragment {

    private FragmentGroupBinding binding;
    private FileUtil fileUtil;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图绑定初始化布局
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化文件工具类
        fileUtil = new FileUtil(requireContext());

        // 加载数据
        List<Contact> contacts = loadContacts();
        List<Group> groups = loadGroups();

        // 设置RecyclerView
        binding.recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGroups.setAdapter(new GroupAdapter(groups, contacts));
    }

    // 从JSON文件加载联系人数据
    private List<Contact> loadContacts() {
        try {
            Contact[] contacts = fileUtil.readJSON("contacts.json", Contact[].class);
            return Arrays.asList(contacts);
        } catch (Exception e) {
            Log.e("GroupFragment", "加载联系人失败", e);
            return new ArrayList<>();
        }
    }

    // 从JSON文件加载分组数据
    private List<Group> loadGroups() {
        try {
            Group[] groups = fileUtil.readJSON("groups.json", Group[].class);
            return Arrays.asList(groups);
        } catch (Exception e) {
            Log.e("HomeFragment", "加载分组失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}