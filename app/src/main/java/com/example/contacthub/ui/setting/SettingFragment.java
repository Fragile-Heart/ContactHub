package com.example.contacthub.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonImportContacts.setOnClickListener(v ->
                openFileLauncher.launch(new String[]{
                        "text/csv",
                        "text/plain",            // 增加plain文本支持
                        "text/comma-separated-values",
                        "application/csv",       // 增加应用类型
                        "application/vnd.ms-excel",  // 一些系统CSV关联Excel
                        "text/x-vcard",
                        "text/vcard",
                        "application/vnd.ms-outlook"})
        );

        binding.buttonExportContacts.setOnClickListener(v -> showExportOptions());

        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("切换到黑夜模式");
            } else {
                showToast("切换到白天模式");
            }
        });

        binding.buttonOtherSettings.setOnClickListener(v ->
            showToast("其他设置功能待实现")
        );
    }

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

    private void exportContacts(String format) {
        FileUtil fileUtil = new FileUtil(requireContext());

        // 获取联系人数据
        String contactsJson = fileUtil.readFile("contacts.json");
        if (contactsJson == null) {
            showToast("无法读取联系人数据");
            return;
        }

        // 根据选择的格式生成文件内容
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
        }catch (Exception e) {
            Log.e(TAG, "转换联系人失败", e);
            showToast("转换联系人失败: " + e.getMessage());
            return;
        }

        // 使用SAF让用户选择保存位置
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(exportMimeType);
        intent.putExtra(Intent.EXTRA_TITLE, "contacts" + exportFileExtension);

        saveFileLauncher.launch(intent);
    }


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

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    // 添加导入联系人的方法
    private void importContactsFromUri(Uri uri) {
        try {
            // 获取文件名以确定文件类型
            String fileName = getFileNameFromUri(uri);
            String mimeType = requireContext().getContentResolver().getType(uri);
            String fileContent = readTextFromUri(uri);

            if (fileContent.isEmpty()) {
                showToast("文件为空或无法读取");
                return;
            }

            // 解析联系人
            List<Contact> contacts;

            // 先根据文件扩展名判断
            if (fileName.toLowerCase().endsWith(".csv") ||
                (mimeType != null && (
                    mimeType.contains("csv") ||
                    mimeType.contains("comma") ||
                    mimeType.equals("text/plain")))) {

                // 对于text/plain，检查内容是否符合CSV格式
                if (mimeType != null && mimeType.equals("text/plain") &&
                    !fileContent.contains(",") && !fileName.toLowerCase().endsWith(".csv")) {
                    // 不像CSV文件，尝试作为vCard处理
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

            // 保存联系人
            saveImportedContacts(contacts);

        } catch (Exception e) {
            Log.e(TAG, "导入联系人失败", e);
            showToast("导入失败: " + e.getMessage());
        }
    }

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
}