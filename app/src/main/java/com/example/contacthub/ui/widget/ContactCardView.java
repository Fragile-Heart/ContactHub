package com.example.contacthub.ui.widget;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.contacthub.ui.view.contactDetail.ContactEditActivity;
import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
import com.example.contacthub.utils.PhotoUtil;
import com.example.contacthub.utils.QRCodeUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 联系人卡片视图，显示联系人详细信息并提供交互功能
 * 包括显示联系人信息、拨打电话、发送短信、分享联系人和编辑联系人等功能
 */
public class ContactCardView extends FrameLayout {

    private static final String TAG = "ContactCardView";
    private static final int EDIT_CONTACT_REQUEST_CODE = 100;

    private TextView tvName;
    private TextView tvMobileNumber;
    private TextView tvTelephoneNumber;
    private TextView tvEmail;
    private TextView tvAddress;
    private TextView tvQQ;
    private TextView tvWechat;
    private TextView tvWebsite;
    private TextView tvBirthday;
    private TextView tvCompany;
    private TextView tvPostalCode;
    private TextView tvNotes;

    private View layoutMobileNumber;
    private View layoutTelephoneNumber;
    private View layoutEmail;
    private View layoutAddress;
    private View layoutQQ;
    private View layoutWechat;
    private View layoutWebsite;
    private View layoutBirthday;
    private View layoutCompany;
    private View layoutPostalCode;
    private View layoutNotes;

    private ShapeableImageView contactAvatar;
    private boolean isMyCard = false;
    private QRCodeUtil qrCodeUtil;
    private Contact currentContact;
    private OnContactUpdatedListener contactUpdatedListener;

    /**
     * 联系人更新监听器接口
     */
    public interface OnContactUpdatedListener {
        /**
         * 当联系人信息更新时调用
         * 
         * @param updatedContact 更新后的联系人对象
         */
        void onContactUpdated(Contact updatedContact);
    }

    /**
     * 设置是否为"我的名片"模式
     * 
     * @param isMyCard 是否为我的名片
     */
    public void setMyCard(boolean isMyCard) {
        this.isMyCard = isMyCard;
    }

    /**
     * 设置联系人更新监听器
     * 
     * @param listener 联系人更新监听器
     */
    public void setOnContactUpdatedListener(OnContactUpdatedListener listener) {
        this.contactUpdatedListener = listener;
    }

    /**
     * 构造函数
     * 
     * @param context 上下文
     */
    public ContactCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    /**
     * 构造函数
     * 
     * @param context 上下文
     * @param attrs 属性集
     */
    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 构造函数
     * 
     * @param context 上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 设置要显示的联系人
     * 
     * @param contact 联系人对象
     */
    public void setContact(Contact contact) {
        this.currentContact = contact;
        updateContactInfo();
    }

    /**
     * 更新联系人信息显示
     */
    private void updateContactInfo() {
        if (currentContact == null) {
            clearContactInfo();
            return;
        }

        tvName.setText(currentContact.getName());

        // 更新各个字段
        updateFieldVisibility(layoutMobileNumber, tvMobileNumber, currentContact.getMobileNumber());
        updateFieldVisibility(layoutTelephoneNumber, tvTelephoneNumber, currentContact.getTelephoneNumber());
        updateFieldVisibility(layoutEmail, tvEmail, currentContact.getEmail());
        updateFieldVisibility(layoutAddress, tvAddress, currentContact.getAddress());
        updateFieldVisibility(layoutQQ, tvQQ, currentContact.getQq());
        updateFieldVisibility(layoutWechat, tvWechat, currentContact.getWechat());
        updateFieldVisibility(layoutWebsite, tvWebsite, currentContact.getWebsite());
        updateFieldVisibility(layoutBirthday, tvBirthday, currentContact.getBirthday());
        updateFieldVisibility(layoutCompany, tvCompany, currentContact.getCompany());
        updateFieldVisibility(layoutPostalCode, tvPostalCode, currentContact.getPostalCode());
        updateFieldVisibility(layoutNotes, tvNotes, currentContact.getNotes());

        // 更新头像
        String photoBase64 = currentContact.getPhoto();
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            Bitmap avatarBitmap = PhotoUtil.base64ToBitmap(photoBase64);
            if (avatarBitmap != null) {
                contactAvatar.setImageBitmap(avatarBitmap);
            } else {
                contactAvatar.setImageResource(R.drawable.ic_person);
            }
        } else {
            contactAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    /**
     * 更新字段可见性和内容
     * 
     * @param layout 字段布局容器
     * @param textView 字段文本视图
     * @param value 字段值
     */
    private void updateFieldVisibility(View layout, TextView textView, String value) {
        if (value != null && !value.isEmpty()) {
            textView.setText(value);
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化视图组件和事件监听器
     * 
     * @param context 上下文
     */
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_contact_card, this, true);

        // 初始化QRCodeUtils实例
        qrCodeUtil = new QRCodeUtil(context);

        // 找到布局中的各个 View 元素
        MaterialButton btnCall = findViewById(R.id.btn_call);
        MaterialButton btnMessage = findViewById(R.id.btn_message);
        MaterialButton btnShare = findViewById(R.id.btn_share);
        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);
        
        initTextViews();
        initLayouts();

        // 设置按钮点击监听器
        setupButtonListeners(btnCall, btnMessage, btnShare, fabEdit, context);

        // 初始化时清空显示
        clearContactInfo();
    }

    /**
     * 初始化所有TextView组件
     */
    private void initTextViews() {
        tvName = findViewById(R.id.tv_contact_name);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvTelephoneNumber = findViewById(R.id.tv_telephone_number);
        tvEmail = findViewById(R.id.tv_contact_email);
        tvAddress = findViewById(R.id.tv_location);
        contactAvatar = findViewById(R.id.contact_avatar);
        tvQQ = findViewById(R.id.tv_qq);
        tvWechat = findViewById(R.id.tv_wechat);
        tvWebsite = findViewById(R.id.tv_website);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvCompany = findViewById(R.id.tv_company);
        tvPostalCode = findViewById(R.id.tv_postal_code);
        tvNotes = findViewById(R.id.tv_notes);
    }

    /**
     * 初始化所有布局组件
     */
    private void initLayouts() {
        layoutQQ = findViewById(R.id.layout_qq);
        layoutWechat = findViewById(R.id.layout_wechat);
        layoutWebsite = findViewById(R.id.layout_website);
        layoutBirthday = findViewById(R.id.layout_birthday);
        layoutCompany = findViewById(R.id.layout_company);
        layoutPostalCode = findViewById(R.id.layout_postal_code);
        layoutNotes = findViewById(R.id.layout_notes);
        layoutMobileNumber = findViewById(R.id.layout_mobile);
        layoutTelephoneNumber = findViewById(R.id.layout_telephone);
        layoutEmail = findViewById(R.id.layout_email);
        layoutAddress = findViewById(R.id.layout_address);
    }

    /**
     * 设置按钮监听器
     * 
     * @param btnCall 拨打电话按钮
     * @param btnMessage 发送短信按钮
     * @param btnShare 分享联系人按钮
     * @param fabEdit 编辑联系人按钮
     * @param context 上下文
     */
    private void setupButtonListeners(MaterialButton btnCall, MaterialButton btnMessage, 
                                      MaterialButton btnShare, FloatingActionButton fabEdit, 
                                      Context context) {
        // 拨打电话按钮
        btnCall.setOnClickListener(v -> {
            if (currentContact != null) {
                handleCallButton();
            }
        });

        // 发送短信按钮
        btnMessage.setOnClickListener(v -> {
            if (currentContact != null && currentContact.getMobileNumber() != null
                    && !currentContact.getMobileNumber().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + currentContact.getMobileNumber()));
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(), "无可用手机号", Toast.LENGTH_SHORT).show();
            }
        });

        // 分享联系人按钮
        btnShare.setOnClickListener(v -> {
            if (currentContact != null) {
                generateAndShowQRCode();
            } else {
                Toast.makeText(getContext(), "没有联系人可分享", Toast.LENGTH_SHORT).show();
            }
        });

        // 编辑联系人按钮
        fabEdit.setOnClickListener(v -> {
            if (currentContact != null) {
                Intent intent = new Intent(getContext(), ContactEditActivity.class);
                intent.putExtra("contact", currentContact);
                intent.putExtra("isMyCard", isMyCard);

                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE);
                } else {
                    context.startActivity(intent);
                    Log.w(TAG, "编辑联系人：当前上下文不是Activity，无法接收编辑结果");
                }
            } else {
                Toast.makeText(getContext(), "没有联系人可编辑", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 处理拨打电话按钮的点击事件
     */
    private void handleCallButton() {
        boolean hasMobile = currentContact.getMobileNumber() != null && !currentContact.getMobileNumber().isEmpty();
        boolean hasTelephone = currentContact.getTelephoneNumber() != null && !currentContact.getTelephoneNumber().isEmpty();

        if (hasMobile && hasTelephone) {
            // 同时有手机和座机号码，显示选择对话框
            String[] options = new String[]{"手机: " + currentContact.getMobileNumber(),
                    "座机: " + currentContact.getTelephoneNumber()};

            new AlertDialog.Builder(getContext())
                    .setTitle("选择拨打号码")
                    .setItems(options, (dialog, which) -> {
                        String number = which == 0 ? currentContact.getMobileNumber() : currentContact.getTelephoneNumber();
                        dialNumber(number);
                    })
                    .show();
        } else if (hasMobile) {
            // 只有手机号
            dialNumber(currentContact.getMobileNumber());
        } else if (hasTelephone) {
            // 只有座机号
            dialNumber(currentContact.getTelephoneNumber());
        } else {
            Toast.makeText(getContext(), "无可用电话号码", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 清除联系人信息显示
     */
    public void clearContactInfo() {
        // 清除姓名和头像
        if (tvName != null) tvName.setText("");
        if (contactAvatar != null) contactAvatar.setImageResource(R.drawable.ic_person);
        
        // 清除并隐藏所有字段
        clearAndHideField(tvMobileNumber, layoutMobileNumber);
        clearAndHideField(tvTelephoneNumber, layoutTelephoneNumber);
        clearAndHideField(tvEmail, layoutEmail);
        clearAndHideField(tvAddress, layoutAddress);
        clearAndHideField(tvQQ, layoutQQ);
        clearAndHideField(tvWechat, layoutWechat);
        clearAndHideField(tvWebsite, layoutWebsite);
        clearAndHideField(tvBirthday, layoutBirthday);
        clearAndHideField(tvCompany, layoutCompany);
        clearAndHideField(tvPostalCode, layoutPostalCode);
        clearAndHideField(tvNotes, layoutNotes);
    }

    /**
     * 清除文本并隐藏视图
     * 
     * @param textView 文本视图
     * @param layout 布局容器
     */
    private void clearAndHideField(TextView textView, View layout) {
        if (textView != null) textView.setText("");
        if (layout != null) layout.setVisibility(View.GONE);
    }

    /**
     * 拨打电话号码
     * 
     * @param phoneNumber 要拨打的电话号码
     */
    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("[^0-9]", "")));
        getContext().startActivity(intent);
    }

    /**
     * 处理联系人编辑返回结果
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     */
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Contact updatedContact = (Contact) data.getSerializableExtra("updatedContact");
            if (updatedContact != null) {
                setContact(updatedContact);
                if (contactUpdatedListener != null) {
                    contactUpdatedListener.onContactUpdated(updatedContact);
                }
                Log.d(TAG, "联系人已更新: " + updatedContact.getName());
            }
        }
    }

    /**
     * 生成联系人二维码并显示在对话框中
     */
    private void generateAndShowQRCode() {
        try {
            Bitmap qrCodeBitmap = qrCodeUtil.generateContactQRCode(currentContact, 600);
            if (qrCodeBitmap != null) {
                showQRCodeDialog(qrCodeBitmap);
            } else {
                Toast.makeText(getContext(), "生成二维码失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "生成联系人信息失败: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "生成联系人二维码失败", e);
        }
    }

    /**
     * 显示包含二维码的对话框
     * 
     * @param qrCodeBitmap 二维码位图
     */
    private void showQRCodeDialog(Bitmap qrCodeBitmap) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_code, null);
        ImageView imgQrCode = dialogView.findViewById(R.id.img_qr_code);
        imgQrCode.setImageBitmap(qrCodeBitmap);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        tvDialogTitle.setText("扫描二维码添加 " + currentContact.getName() + " 的联系信息");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_save_qr_code).setOnClickListener(v -> {
            saveQRCodeToGallery(qrCodeBitmap);
        });

        dialogView.findViewById(R.id.btn_close).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 保存二维码到相册
     * 
     * @param bitmap 要保存的二维码位图
     */
    private void saveQRCodeToGallery(Bitmap bitmap) {
        String fileName = "联系人_" + currentContact.getName() + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageWithMediaStore(bitmap, fileName);
        } else {
            saveImageToLegacyStorage(bitmap, fileName);
        }
    }

    /**
     * 使用MediaStore API保存图片（Android 10及以上）
     * 
     * @param bitmap 要保存的位图
     * @param fileName 文件名
     */
    private void saveImageWithMediaStore(Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ContactHub");

        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContext().getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(getContext(), "二维码已保存到相册", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "保存二维码失败", e);
            }
        } else {
            Toast.makeText(getContext(), "无法创建文件", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存图片到旧版存储（Android 9及以下）
     * 
     * @param bitmap 要保存的位图
     * @param fileName 文件名
     */
    private void saveImageToLegacyStorage(Bitmap bitmap, String fileName) {
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "ContactHub");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // 通知媒体扫描器扫描新图片
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            getContext().sendBroadcast(mediaScanIntent);

            Toast.makeText(getContext(), "二维码已保存到相册", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "保存二维码失败", e);
        }
    }
}
