package com.example.contacthub.ui.contactDetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class ContactEditActivity extends AppCompatActivity {

    private ShapeableImageView editContactAvatar;
    private FloatingActionButton fabEditAvatar;
    private TextInputEditText etName, etMobile, etTelephone, etEmail, etAddress;
    private MaterialButton btnSave, btnCancel;

    private Contact contact;
    private Uri selectedImageUri;
    
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        editContactAvatar.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        // 初始化工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化控件
        editContactAvatar = findViewById(R.id.edit_contact_avatar);
        fabEditAvatar = findViewById(R.id.fab_edit_avatar);
        etName = findViewById(R.id.et_name);
        etMobile = findViewById(R.id.et_mobile);
        etTelephone = findViewById(R.id.et_telephone);
        etEmail = findViewById(R.id.et_email);
        etAddress = findViewById(R.id.et_address);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // 获取传递过来的联系人
        if (getIntent().hasExtra("contact")) {
            contact = (Contact) getIntent().getSerializableExtra("contact");
            populateContactInfo();
        } else {
            // 如果是新建联系人
            contact = new Contact();
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

    private void populateContactInfo() {
        if (contact != null) {
            etName.setText(contact.getName());
            etMobile.setText(contact.getMobileNumber());
            etTelephone.setText(contact.getTelephoneNumber());
            etEmail.setText(contact.getEmail());
            etAddress.setText(contact.getAddress());

            // 如果联系人有头像，则显示
            // 注意：这里假设Contact类有获取和设置头像的方法
            // if (contact.getAvatarUri() != null) {
            //     editContactAvatar.setImageURI(contact.getAvatarUri());
            //     selectedImageUri = contact.getAvatarUri();
            // }
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

        // 设置头像
        // if (selectedImageUri != null) {
        //     contact.setAvatarUri(selectedImageUri);
        // }

        // 保存联系人到数据库
        // 这部分需要根据您的应用程序架构实现
        // contactRepository.save(contact);

        // 将更新后的联系人传回
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", contact);
        setResult(RESULT_OK, resultIntent);
        
        Toast.makeText(this, "联系人已更新", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
