package com.example.contacthub.ui.view.contactDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.utils.FileUtil;
import com.example.contacthub.ui.widget.ContactCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ContactDetailActivity extends AppCompatActivity implements ContactCardView.OnContactUpdatedListener {

    private ContactCardView contactCardView;
    private Contact contact;

    private FileUtil fileUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);

        // 修改为使用正确的Toolbar类型
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 初始化联系人卡片视图
        contactCardView = findViewById(R.id.contact_card_view);
        contactCardView.setOnContactUpdatedListener(this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 获取传递过来的联系人
        if (getIntent().hasExtra("contact")) {
            contact = (Contact) getIntent().getSerializableExtra("contact");
            displayContactDetails();
        } else {
            Toast.makeText(this, "没有联系人信息", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_delete_contact).setOnClickListener(v -> {
            // 点击事件处理

            new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除此联系人吗？")
                .setPositiveButton("是", (dialog, which) -> {
                    // 删除联系人逻辑
                    fileUtil = new FileUtil(this);
                    //fileUtil.deleteContact(contact.getId());
                    Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
                    List<Contact> contactList = new ArrayList<>(Arrays.asList(contacts));
                    // 从列表中移除当前联系人
                    contactList.removeIf(c -> Objects.equals(c.getId(), contact.getId()));
                    // 将更新后的列表保存回文件
                    Contact[] updatedContacts = contactList.toArray(new Contact[0]);
                    fileUtil.saveJSON(updatedContacts, "contacts.json");
                    Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("否", null)
                .show();
        });
    }

    private void displayContactDetails() {
        if (contact != null) {
            // 在卡片视图中显示联系人详情
            contactCardView.setContact(contact);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 使用 finish() 替代 onBackPressed()
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 将结果传递给ContactCardView处理
        contactCardView.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onContactUpdated(Contact updatedContact) {
        // 联系人已更新，更新当前页面的联系人对象
        this.contact = updatedContact;
        
        // 更新标题
        setTitle(updatedContact.getName());
        
        // 将更新后的联系人传回给启动此Activity的页面
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", updatedContact);
        setResult(RESULT_OK, resultIntent);
    }
}
