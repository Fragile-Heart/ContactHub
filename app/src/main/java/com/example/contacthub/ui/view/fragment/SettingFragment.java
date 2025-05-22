package com.example.contacthub.ui.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.contacthub.databinding.FragmentSettingBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.utils.ContactIOUtil;
import com.example.contacthub.utils.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private static final String TAG = "SettingFragment";
    private String fileContent;
    private ActivityResultLauncher<Intent> saveFileLauncher;
    private ActivityResultLauncher<String[]> openFileLauncher;

    // 显示设置的常量
    private static final String PREFS_NAME = "ContactDisplayPrefs";
    private static final String KEY_SHOW_MOBILE = "show_mobile";
    private static final String KEY_SHOW_TELEPHONE = "show_telephone";
    private static final String KEY_SHOW_ADDRESS = "show_address";

    /**
     * Fragment创建时的初始化
     * 注册文件操作的结果处理器
     * 
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册文件保存结果处理器
        saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        writeContentToUri(uri);
                    }
                }
            }
        );

        // 注册文件打开结果处理器
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        importContactsFromUri(uri);
                    }
                }
        );
    }

    /**
     * 创建Fragment视图
     * 
     * @param inflater 用于加载布局的LayoutInflater
     * @param container 视图的父容器
     * @param savedInstanceState 保存的状态数据
     * @return 创建的视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后的初始化
     * 设置各按钮的点击事件监听器
     * 
     * @param view 创建的视图
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonImportContacts.setOnClickListener(v ->
                openFileLauncher.launch(new String[]{
                        "text/csv",
                        "text/plain",
                        "text/comma-separated-values",
                        "application/csv",
                        "application/vnd.ms-excel",
                        "text/x-vcard",
                        "text/vcard",
                        "application/vnd.ms-outlook"})
        );

        binding.buttonExportContacts.setOnClickListener(v -> showExportOptions());
        binding.buttonContactDisplaySettings.setOnClickListener(v -> showContactDisplaySettings());
        binding.buttonBatchDeleteContacts.setOnClickListener(v -> showBatchDeleteContacts());
    }

    /**
     * 显示导出格式选择对话框
     * 用户可选择CSV或vCard格式导出联系人
     */
    private void showExportOptions() {
        String[] formats = {"CSV格式", "vCard格式"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择导出格式")
               .setItems(formats, (dialog, which) -> {
                   if (which == 0) {
                       exportContacts("csv");
                   } else {
                       exportContacts("vcard");
                   }
               })
               .setNegativeButton("取消", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * 导出联系人数据
     * 将联系人转换为指定格式并准备导出
     * 
     * @param format 导出格式，"csv"或"vcard"
     */
    private void exportContacts(String format) {
        FileUtil fileUtil = new FileUtil(requireContext());
        String contactsJson = fileUtil.readFile("contacts.json");
        if (contactsJson == null) {
            showToast("无法读取联系人数据");
            return;
        }

        String exportFileExtension;
        String exportMimeType;
        try {
            if ("csv".equals(format)) {
                fileContent = ContactIOUtil.convertContactsToCSV(contactsJson);
                exportMimeType = "text/csv";
                exportFileExtension = ".csv";
            } else {
                fileContent = ContactIOUtil.convertContactsToVCard(contactsJson);
                exportMimeType = "text/x-vcard";
                exportFileExtension = ".vcf";
            }
        } catch (Exception e) {
            Log.e(TAG, "转换联系人失败", e);
            showToast("转换联系人失败: " + e.getMessage());
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(exportMimeType);
        intent.putExtra(Intent.EXTRA_TITLE, "contacts" + exportFileExtension);

        saveFileLauncher.launch(intent);
    }

    /**
     * 将内容写入指定URI
     * 用于完成导出联系人的文件写入
     * 
     * @param uri 目标文件URI
     */
    private void writeContentToUri(Uri uri) {
        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(fileContent.getBytes());
                outputStream.close();
                showToast("联系人导出成功: " + uri.getPath());
            }
        } catch (IOException e) {
            Log.e(TAG, "导出联系人失败", e);
            showToast("导出联系人失败: " + e.getMessage());
        }
    }

    /**
     * 显示Toast消息
     * 
     * @param message 要显示的消息
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 从URI导入联系人
     * 读取并解析不同格式的联系人文件
     * 
     * @param uri 联系人文件的URI
     */
    private void importContactsFromUri(Uri uri) {
        try {
            String fileName = getFileNameFromUri(uri);
            String mimeType = requireContext().getContentResolver().getType(uri);
            String fileContent = readTextFromUri(uri);

            if (fileContent.isEmpty()) {
                showToast("文件为空或无法读取");
                return;
            }

            List<Contact> contacts;

            if (fileName.toLowerCase().endsWith(".csv") ||
                (mimeType != null && (
                    mimeType.contains("csv") ||
                    mimeType.contains("comma") ||
                    mimeType.equals("text/plain")))) {

                if (mimeType != null && mimeType.equals("text/plain") &&
                    !fileContent.contains(",") && !fileName.toLowerCase().endsWith(".csv")) {
                    contacts = ContactIOUtil.parseContactsFromVCard(fileContent);
                } else {
                    contacts = ContactIOUtil.parseContactsFromCSV(fileContent);
                }

            } else if (fileName.toLowerCase().endsWith(".vcf") ||
                      (mimeType != null && (
                          mimeType.contains("vcard") ||
                          mimeType.contains("outlook")))) {
                contacts = ContactIOUtil.parseContactsFromVCard(fileContent);
            } else {
                // 无法确定文件类型，尝试检测内容
                if (fileContent.startsWith("BEGIN:VCARD")) {
                    contacts = ContactIOUtil.parseContactsFromVCard(fileContent);
                } else if (fileContent.contains(",") &&
                          (fileContent.toLowerCase().contains("姓名") ||
                           fileContent.contains("name"))) {
                    contacts = ContactIOUtil.parseContactsFromCSV(fileContent);
                } else {
                    showToast("无法识别的文件格式，请选择CSV或vCard文件");
                    return;
                }
            }

            if (contacts.isEmpty()) {
                showToast("未找到有效联系人数据");
                return;
            }

            saveImportedContacts(contacts);

        } catch (Exception e) {
            Log.e(TAG, "导入联系人失败", e);
            showToast("导入失败: " + e.getMessage());
        }
    }

    /**
     * 从URI获取文件名
     * 
     * @param uri 文件URI
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取文件名失败", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * 从URI读取文本内容
     * 
     * @param uri 文件URI
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出异常
     */
    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 保存导入的联系人
     * 合并现有联系人列表与新导入的联系人
     * 
     * @param newContacts 新导入的联系人列表
     */
    private void saveImportedContacts(List<Contact> newContacts) {
        FileUtil fileUtil = new FileUtil(requireContext());
        String contactsJson = fileUtil.readFile("contacts.json");

        List<Contact> existingContacts = new ArrayList<>();
        if (contactsJson != null && !contactsJson.isEmpty()) {
            Type contactListType = new TypeToken<List<Contact>>(){}.getType();
            existingContacts = new Gson().fromJson(contactsJson, contactListType);
        }

        // 为新联系人生成ID并添加拼音
        int maxId = 0;
        for (Contact contact : existingContacts) {
            if (contact.getId() > maxId) {
                maxId = contact.getId();
            }
        }

        for (Contact contact : newContacts) {
            contact.setId(++maxId);
            contact.generatePinyin();
            existingContacts.add(contact);
        }

        // 保存合并后的联系人列表
        String updatedJson = new Gson().toJson(existingContacts);
        try {
            FileOutputStream fos = requireContext().openFileOutput("contacts.json", Context.MODE_PRIVATE);
            fos.write(updatedJson.getBytes());
            fos.close();
            showToast("成功导入 " + newContacts.size() + " 个联系人");
        } catch (IOException e) {
            Log.e(TAG, "保存导入的联系人失败", e);
            showToast("保存联系人失败: " + e.getMessage());
        }
    }

    /**
     * 显示联系人显示设置对话框
     * 控制联系人信息显示的选项
     */
    private void showContactDisplaySettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean showMobile = prefs.getBoolean(KEY_SHOW_MOBILE, true);
        boolean showTelephone = prefs.getBoolean(KEY_SHOW_TELEPHONE, true);
        boolean showAddress = prefs.getBoolean(KEY_SHOW_ADDRESS, true);

        String[] options = {"显示手机号码", "显示固定电话", "显示地址"};
        boolean[] checkedItems = {showMobile, showTelephone, showAddress};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("联系人显示设置")
               .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                   checkedItems[which] = isChecked;
               })
               .setPositiveButton("确定", (dialog, id) -> {
                   // 保存设置
                   SharedPreferences.Editor editor = prefs.edit();
                   editor.putBoolean(KEY_SHOW_MOBILE, checkedItems[0]);
                   editor.putBoolean(KEY_SHOW_TELEPHONE, checkedItems[1]);
                   editor.putBoolean(KEY_SHOW_ADDRESS, checkedItems[2]);
                   editor.apply();

                   showToast("显示设置已保存");
               })
               .setNegativeButton("取消", (dialog, id) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * 显示批量删除联系人对话框
     * 允许用户选择多个联系人进行删除
     */
    private void showBatchDeleteContacts() {
        FileUtil fileUtil = new FileUtil(requireContext());
        String contactsJson = fileUtil.readFile("contacts.json");

        if (contactsJson == null || contactsJson.isEmpty()) {
            showToast("无法读取联系人数据");
            return;
        }

        Type contactListType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = new Gson().fromJson(contactsJson, contactListType);

        if (contacts.isEmpty()) {
            showToast("没有联系人可删除");
            return;
        }

        String[] contactNames = new String[contacts.size()];
        boolean[] checkedItems = new boolean[contacts.size()];

        for (int i = 0; i < contacts.size(); i++) {
            contactNames[i] = contacts.get(i).getName();
            checkedItems[i] = false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择要删除的联系人")
               .setMultiChoiceItems(contactNames, checkedItems, (dialog, which, isChecked) -> {
                   checkedItems[which] = isChecked;
               })
               .setPositiveButton("删除", (dialog, id) -> {
                   deleteSelectedContacts(contacts, checkedItems);
               })
               .setNegativeButton("取消", (dialog, id) -> dialog.dismiss());
        
        builder.create().show();
    }
    
    /**
     * 删除选中的联系人
     * 二次确认后执行删除操作
     * 
     * @param contacts 所有联系人列表
     * @param checkedItems 联系人选中状态数组
     */
    private void deleteSelectedContacts(List<Contact> contacts, boolean[] checkedItems) {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(requireContext());
        confirmBuilder.setTitle("确认删除")
                      .setMessage("确定要删除选中的联系人吗？此操作不可撤销。")
                      .setPositiveButton("确定", (dialog, which) -> {
                          List<Contact> remainingContacts = new ArrayList<>();
                          int deleteCount = 0;
                          
                          for (int i = 0; i < contacts.size(); i++) {
                              if (!checkedItems[i]) {
                                  remainingContacts.add(contacts.get(i));
                              } else {
                                  deleteCount++;
                              }
                          }
                          
                          if (deleteCount == 0) {
                              showToast("未选择任何联系人");
                              return;
                          }
                          
                          String updatedJson = new Gson().toJson(remainingContacts);
                          try {
                              FileOutputStream fos = requireContext().openFileOutput("contacts.json", Context.MODE_PRIVATE);
                              fos.write(updatedJson.getBytes());
                              fos.close();
                              showToast("成功删除 " + deleteCount + " 个联系人");
                          } catch (IOException e) {
                              Log.e(TAG, "保存联系人失败", e);
                              showToast("操作失败: " + e.getMessage());
                          }
                      })
                      .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        confirmBuilder.create().show();
    }
}
