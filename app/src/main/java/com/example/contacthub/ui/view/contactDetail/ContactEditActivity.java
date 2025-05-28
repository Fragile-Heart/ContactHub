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
                        Uri selectedImageUri = result.getData().getData();
                        Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        Bitmap resizedBitmap = resizeBitmap(selectedBitmap, 500);
                        editContactAvatar.setImageBitmap(resizedBitmap);
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

    /**
     * 调整位图大小到指定尺寸
     * 
     * @param originalBitmap 原始位图
     * @param maxDimension 最大尺寸限制
     * @return 调整大小后的位图
     */
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

    /**
     * 初始化Activity，设置UI组件和事件监听器
     * 
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        fileUtil = new FileUtil(this);

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

        // 设置头像选择
        fabEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveContact());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * 从文件中加载分组数据
     */
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

    /**
     * 填充联系人信息到UI控件
     */
    private void populateContactInfo() {
        if (contact != null) {
            etName.setText(contact.getName());
            etMobile.setText(contact.getMobileNumber());
            etTelephone.setText(contact.getTelephoneNumber());
            etEmail.setText(contact.getEmail());
            etAddress.setText(contact.getAddress());

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
                groupsCard.setVisibility(View.GONE);
            } else {
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

    /**
     * 创建分组选择的复选框UI
     */
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            checkBox.setLayoutParams(params);

            groupCheckboxes.put(group.getId(), checkBox);
            groupsContainer.addView(checkBox);
        }
    }

    /**
     * 保存联系人信息
     */
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
                fileUtil.saveObject(contact, "my.json");
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

                // 保存到文件
                Contact[] updatedContacts = contactList.toArray(new Contact[0]);
                fileUtil.saveJSON(updatedContacts, "contacts.json");
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

    /**
     * 更新联系人分组信息
     */
    private void updateContactGroups() {
        List<Integer> selectedGroups = new ArrayList<>();

        for (Map.Entry<Integer, CheckBox> entry : groupCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedGroups.add(entry.getKey());
            }
        }

        contact.setGroupIds(selectedGroups);
        Log.d(TAG, "更新联系人分组: " + selectedGroups);
    }

    /**
     * 处理菜单项选择事件
     * 
     * @param item 被点击的菜单项
     * @return 如果事件已处理则返回true，否则返回父类处理结果
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
