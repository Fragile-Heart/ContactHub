package com.example.contacthub.ui.contactDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.widget.ContactCardView;

public class ContactDetailActivity extends AppCompatActivity implements ContactCardView.OnContactUpdatedListener {

    private ContactCardView contactCardView;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);

        // 修改为使用正确的Toolbar类型
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
