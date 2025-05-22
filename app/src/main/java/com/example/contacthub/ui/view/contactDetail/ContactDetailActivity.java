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

    /**
     * 初始化Activity，设置UI组件并获取联系人数据
     * 
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactCardView = findViewById(R.id.contact_card_view);
        contactCardView.setOnContactUpdatedListener(this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (getIntent().hasExtra("contact")) {
            contact = (Contact) getIntent().getSerializableExtra("contact");
            displayContactDetails();
        } else {
            Toast.makeText(this, "没有联系人信息", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_delete_contact).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除此联系人吗？")
                .setPositiveButton("是", (dialog, which) -> {
                    fileUtil = new FileUtil(this);
                    Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
                    List<Contact> contactList = new ArrayList<>(Arrays.asList(contacts));
                    contactList.removeIf(c -> Objects.equals(c.getId(), contact.getId()));
                    Contact[] updatedContacts = contactList.toArray(new Contact[0]);
                    fileUtil.saveJSON(updatedContacts, "contacts.json");
                    Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("否", null)
                .show();
        });
    }

    /**
     * 在视图中显示联系人详细信息
     */
    private void displayContactDetails() {
        if (contact != null) {
            contactCardView.setContact(contact);
        }
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

    /**
     * 处理Activity结果
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        contactCardView.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * 联系人更新回调方法
     * 
     * @param updatedContact 更新后的联系人对象
     */
    @Override
    public void onContactUpdated(Contact updatedContact) {
        this.contact = updatedContact;
        setTitle(updatedContact.getName());
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", updatedContact);
        setResult(RESULT_OK, resultIntent);
    }
}
