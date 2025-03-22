package com.example.contacthub.widget;

import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.contacthub.R;
import com.example.contacthub.model.Contact;

public class ContactCardView extends CardView {
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView telephoneTextView;
    private TextView mobileTextView;
    private TextView emailTextView;
    private TextView addressTextView;
    private View headerView;

    public ContactCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.item_contact_card, this, true);

        // 优雅的阴影效果
        setRadius(24f);
        setCardElevation(16f);
        setUseCompatPadding(true);

        // 初始化视图引用
        avatarImageView = findViewById(R.id.iv_contact_avatar);
        nameTextView = findViewById(R.id.tv_contact_name);
        telephoneTextView = findViewById(R.id.tv_telephone_number);
        mobileTextView = findViewById(R.id.tv_mobile_number);
        emailTextView = findViewById(R.id.tv_contact_email);
        addressTextView = findViewById(R.id.tv_contact_address);
        headerView = findViewById(R.id.card_header);

        // 头像效果
        if (avatarImageView != null) {
            avatarImageView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            avatarImageView.setClipToOutline(true);
            avatarImageView.setElevation(24f);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            headerView.setRenderEffect(RenderEffect.createBlurEffect(15f, 6f, Shader.TileMode.CLAMP));
        }
    }

    public void setContact(Contact contact) {
        nameTextView.setText(contact.getName());
        telephoneTextView.setText(contact.getTelephoneNumber());
        mobileTextView.setText(contact.getMobileNumber());

        // 设置邮箱
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            emailTextView.setText(contact.getEmail());
        } else {
            findViewById(R.id.email_container).setVisibility(GONE);
        }

        // 设置地址
        if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
            addressTextView.setText(contact.getAddress());
        } else {
            findViewById(R.id.address_container).setVisibility(GONE);
        }
    }
}