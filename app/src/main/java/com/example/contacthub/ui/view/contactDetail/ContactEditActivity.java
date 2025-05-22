package com.example.contacthub.ui.view.contactDetail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.model.Group;
import com.example.contacthub.utils.FileUtil;
import com.example.contacthub.utils.PhotoUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactEditActivity extends AppCompatActivity {

    private static final String TAG = "ContactEditActivity";
    private ShapeableImageView editContactAvatar;
    private TextInputEditText etName, etMobile, etTelephone, etEmail, etAddress;
    private TextInputEditText etQQ, etWechat, etWebsite, etBirthday, etCompany, etPostalCode, etNotes;
    private LinearLayout groupsContainer;
    private MaterialCardView groupsCard;

    private Contact contact;
    private FileUtil fileUtil;
    private List<Group> allGroups;
    private final Map<Integer, CheckBox> groupCheckboxes = new HashMap<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        // 获取选择的图片URI
                        Uri selectedImageUri = result.getData().getData();

                        // 从URI加载位图
                        Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                        // 压缩图片（可选，避免图片过大）
                        Bitmap resizedBitmap = resizeBitmap(selectedBitmap, 500); // 调整为合适大小

                        // 显示选择的图片
                        editContactAvatar.setImageBitmap(resizedBitmap);

                        // 将位图转换为Base64并保存到联系人对象
                        String base64Image = PhotoUtil.bitmapToBase64(resizedBitmap);
                        contact.setPhoto(base64Image);

                        Log.d(TAG, "头像已更新为Base64数据");
                    } catch (Exception e) {
                        Log.e(TAG, "处理选择的图片失败", e);
                        Toast.makeText(ContactEditActivity.this, "加载图片失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxDimension) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float ratio = (float) width / height;

        int newWidth, newHeight;
        if (width > height) {
            newWidth = maxDimension;
            newHeight = (int) (maxDimension / ratio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (maxDimension * ratio);
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        // 初始化文件工具类
        fileUtil = new FileUtil(this);

        // 初始化工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化控件
        editContactAvatar = findViewById(R.id.edit_contact_avatar);
        etName = findViewById(R.id.et_name);
        etMobile = findViewById(R.id.et_mobile);
        etTelephone = findViewById(R.id.et_telephone);
        etEmail = findViewById(R.id.et_email);
        etAddress = findViewById(R.id.et_address);
        etQQ = findViewById(R.id.et_qq);
        etWechat = findViewById(R.id.et_wechat);
        etWebsite = findViewById(R.id.et_website);
        etBirthday = findViewById(R.id.et_birthday);
        etCompany = findViewById(R.id.et_company);
        etPostalCode = findViewById(R.id.et_postal_code);
        etNotes = findViewById(R.id.et_notes);

        groupsContainer = findViewById(R.id.groups_container);
        groupsCard = findViewById(R.id.groups_card);

        FloatingActionButton fabEditAvatar = findViewById(R.id.fab_edit_avatar);
        MaterialButton btnSave = findViewById(R.id.btn_save);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        // 加载所有分组
        loadGroups();

        // 获取传递过来的联系人
        if (getIntent().hasExtra("contact")) {
            contact = (Contact) getIntent().getSerializableExtra("contact");
            populateContactInfo();
        } else {
            // 如果是新建联系人
            contact = new Contact();
            contact.setGroupIds(new ArrayList<>());
            setTitle("新建联系人");
        }

        // 设置头像选择 - 使用新的Activity Result API
        fabEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            imagePickerLauncher.launch(intent);
        });

        // 保存按钮点击
        btnSave.setOnClickListener(v -> saveContact());

        // 取消按钮点击
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadGroups() {
        try {
            Group[] groups = fileUtil.readFile("groups.json", Group[].class);
            allGroups = new ArrayList<>(Arrays.asList(groups));
            Log.d(TAG, "加载分组成功: " + allGroups.size() + "个分组");
        } catch (Exception e) {
            Log.e(TAG, "加载分组失败", e);
            allGroups = new ArrayList<>();
        }
    }

    private void populateContactInfo() {
        if (contact != null) {
            etName.setText(contact.getName());
            etMobile.setText(contact.getMobileNumber());
            etTelephone.setText(contact.getTelephoneNumber());
            etEmail.setText(contact.getEmail());
            etAddress.setText(contact.getAddress());

            // 设置扩展字段的值
            etQQ.setText(contact.getQq());
            etWechat.setText(contact.getWechat());
            etWebsite.setText(contact.getWebsite());
            etBirthday.setText(contact.getBirthday());
            etCompany.setText(contact.getCompany());
            etPostalCode.setText(contact.getPostalCode());
            etNotes.setText(contact.getNotes());

            // 判断是否为我的名片，如果是则隐藏分组选择
            boolean isMyCard = getIntent().getBooleanExtra("isMyCard", false);
            if (isMyCard) {
                // 如果是我的名片，隐藏分组卡片
                groupsCard.setVisibility(View.GONE);
            } else {
                // 创建分组选择UI
                createGroupCheckboxes();
            }

            // 如果联系人有头像数据，则显示
            if (contact.getPhoto() != null && !contact.getPhoto().isEmpty()) {
                Bitmap avatarBitmap = PhotoUtil.base64ToBitmap(contact.getPhoto());
                if (avatarBitmap != null) {
                    editContactAvatar.setImageBitmap(avatarBitmap);
                }
            }
        }
    }

    private void createGroupCheckboxes() {
        // 清除现有的复选框
        groupsContainer.removeAllViews();
        groupCheckboxes.clear();

        // 如果没有分组数据，隐藏分组卡片
        if (allGroups == null || allGroups.isEmpty()) {
            groupsCard.setVisibility(View.GONE);
            return;
        }

        groupsCard.setVisibility(View.VISIBLE);

        // 获取联系人的分组ID列表
        List<Integer> contactGroupIds = contact.getGroupIds();
        if (contactGroupIds == null) {
            contactGroupIds = new ArrayList<>();
            contact.setGroupIds(contactGroupIds);
        }

        // 为每个分组创建复选框
        for (Group group : allGroups) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(group.getName());
            checkBox.setChecked(contactGroupIds.contains(group.getId()));

            // 设置复选框边距
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            checkBox.setLayoutParams(params);

            // 保存复选框引用
            groupCheckboxes.put(group.getId(), checkBox);

            // 添加到容器
            groupsContainer.addView(checkBox);
        }
    }

    private void saveContact() {
        // 验证姓名不能为空
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        // 更新联系人信息
        contact.setName(name);
        contact.setMobileNumber(etMobile.getText().toString().trim());
        contact.setTelephoneNumber(etTelephone.getText().toString().trim());
        contact.setEmail(etEmail.getText().toString().trim());
        contact.setAddress(etAddress.getText().toString().trim());

        contact.setQq(etQQ.getText().toString().trim());
        contact.setWechat(etWechat.getText().toString().trim());
        contact.setWebsite(etWebsite.getText().toString().trim());
        contact.setBirthday(etBirthday.getText().toString().trim());
        contact.setCompany(etCompany.getText().toString().trim());
        contact.setPostalCode(etPostalCode.getText().toString().trim());
        contact.setNotes(etNotes.getText().toString().trim());


        boolean isMyCard = getIntent().getBooleanExtra("isMyCard", false);



        // 保存联系人到文件
        try {
            if (isMyCard) {
                // 保存到my.json
                contact.setId(null); // 移除ID字段
                String json = new Gson().toJson(contact);
                FileOutputStream fos = openFileOutput("my.json", MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.close();
                Log.d(TAG, "我的名片已更新");
            } else {
                updateContactGroups();

                // 读取现有联系人列表
                Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
                List<Contact> contactList = new ArrayList<>(Arrays.asList(contacts));

                // 查找并更新联系人
                boolean found = false;
                for (int i = 0; i < contactList.size(); i++) {
                    if (contactList.get(i).getId().equals(contact.getId())) {
                        contactList.set(i, contact);
                        found = true;
                        break;
                    }
                }

                // 如果没找到，说明是新联系人，添加到列表
                if (!found) {
                    contactList.add(contact);
                }

                // 将更新后的列表转换为数组并保存
                Contact[] updatedContacts = contactList.toArray(new Contact[0]);
                String json = new Gson().toJson(updatedContacts);

                // 保存到文件
                java.io.FileOutputStream fos = openFileOutput("contacts.json", MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.close();

                Log.d(TAG, "联系人保存成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "保存联系人失败", e);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // 将更新后的联系人传回
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", contact);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "联系人已更新", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateContactGroups() {
        // 清空当前分组列表
        List<Integer> selectedGroups = new ArrayList<>();

        // 遍历所有分组复选框，添加选中的分组ID
        for (Map.Entry<Integer, CheckBox> entry : groupCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedGroups.add(entry.getKey());
            }
        }

        // 更新联系人的分组列表
        contact.setGroupIds(selectedGroups);
        Log.d(TAG, "更新联系人分组: " + selectedGroups);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
