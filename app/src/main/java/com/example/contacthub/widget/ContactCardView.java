package com.example.contacthub.widget;

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

import com.example.contacthub.ui.contactDetail.ContactEditActivity;
import com.example.contacthub.R;
import com.example.contacthub.model.Contact;
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

public class ContactCardView extends FrameLayout {

    private static final String TAG = "ContactCardView";
    private static final int EDIT_CONTACT_REQUEST_CODE = 100;

    private MaterialButton btnCall;
    private MaterialButton btnMessage;
    private MaterialButton btnShare;
    private FloatingActionButton fabEdit;

    private TextView tvName;
    private TextView tvMobileNumber;
    private TextView tvTelephoneNumber;
    private TextView tvEmail;
    private TextView tvAddress;
    private ShapeableImageView contactAvatar;

    private QRCodeUtil qrCodeUtil;  // QRCodeUtils实例

    private Contact currentContact;
    private OnContactUpdatedListener contactUpdatedListener;

    // 回调接口用于通知联系人更新事件
    public interface OnContactUpdatedListener {
        void onContactUpdated(Contact updatedContact);
    }

    public void setOnContactUpdatedListener(OnContactUpdatedListener listener) {
        this.contactUpdatedListener = listener;
    }

    public ContactCardView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ContactCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public Contact getCurrentContact() {
        return currentContact;
    }

    public void setContact(Contact contact) {
        this.currentContact = contact;
        updateContactInfo();
    }

    private void updateContactInfo() {
        if (currentContact == null) {
            clearContactInfo();
            return;
        }

        tvName.setText(currentContact.getName());

        // 更新手机号码，如果为空则隐藏
        String mobileNumber = currentContact.getMobileNumber();
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            tvMobileNumber.setText(mobileNumber);
            tvMobileNumber.setVisibility(View.VISIBLE);
        } else {
            tvMobileNumber.setVisibility(View.GONE);
        }

        // 更新座机号码，如果为空则隐藏
        String telephoneNumber = currentContact.getTelephoneNumber();
        if (telephoneNumber != null && !telephoneNumber.isEmpty()) {
            tvTelephoneNumber.setText(telephoneNumber);
            tvTelephoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvTelephoneNumber.setVisibility(View.GONE);
        }

        // 更新电子邮件，如果为空则隐藏
        String email = currentContact.getEmail();
        if (email != null && !email.isEmpty()) {
            tvEmail.setText(email);
            tvEmail.setVisibility(View.VISIBLE);
        } else {
            tvEmail.setVisibility(View.GONE);
        }

        // 更新地址，如果为空则隐藏
        String address = currentContact.getAddress();
        if (address != null && !address.isEmpty()) {
            tvAddress.setText(address);
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }

        contactAvatar.setImageResource(R.drawable.ic_person);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_contact_card, this, true);

        // 初始化QRCodeUtils实例
        qrCodeUtil = new QRCodeUtil(context);

        // 找到布局中的各个 View 元素
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnShare = findViewById(R.id.btn_share);
        fabEdit = findViewById(R.id.fab_edit);
        tvName = findViewById(R.id.tv_contact_name);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvTelephoneNumber = findViewById(R.id.tv_telephone_number);
        tvEmail = findViewById(R.id.tv_contact_email);
        tvAddress = findViewById(R.id.tv_location);
        contactAvatar = findViewById(R.id.contact_avatar);

        // 设置按钮点击监听器，在触发时传递 currentContact 对象
        btnCall.setOnClickListener(v -> {
            if (currentContact != null) {
                boolean hasMobile = currentContact.getMobileNumber() != null && !currentContact.getMobileNumber().isEmpty();
                boolean hasTelephone = currentContact.getTelephoneNumber() != null && !currentContact.getTelephoneNumber().isEmpty();

                if (hasMobile && hasTelephone) {
                    // 同时有手机和座机号码，显示选择对话框
                    String[] options = new String[]{"手机: " + currentContact.getMobileNumber(),
                            "座机: " + currentContact.getTelephoneNumber()};

                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
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
                    android.widget.Toast.makeText(getContext(), "无可用电话号码", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnMessage.setOnClickListener(v -> {
            if (currentContact != null && currentContact.getMobileNumber() != null
                    && !currentContact.getMobileNumber().isEmpty()) {
                // 直接实现发送短信逻辑
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + currentContact.getMobileNumber()));
                getContext().startActivity(intent);
            } else {
                // 提醒用户手机号码为空
                android.widget.Toast.makeText(getContext(), "无可用手机号",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            if (currentContact != null) {
                // 生成联系人信息的二维码并显示
                generateAndShowQRCode();
            } else {
                Toast.makeText(getContext(), "没有联系人可分享",
                        Toast.LENGTH_SHORT).show();
            }
        });

        fabEdit.setOnClickListener(v -> {
            if (currentContact != null) {
                // 启动编辑联系人页面
                Intent intent = new Intent(getContext(), ContactEditActivity.class);
                // 将当前联系人对象传递给编辑页面
                intent.putExtra("contact", currentContact);

                // 如果上下文是Activity，使用startActivityForResult
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE);
                } else {
                    // 非Activity上下文直接启动，但无法接收返回结果
                    context.startActivity(intent);
                    Log.w(TAG, "编辑联系人：当前上下文不是Activity，无法接收编辑结果");
                }
            } else {
                android.widget.Toast.makeText(getContext(), "没有联系人可编辑",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化时清空显示，或者显示默认占位符
        clearContactInfo();
    }

    public void clearContactInfo() {
        this.currentContact = null; // 清除存储的联系人对象

        // 清空所有信息
        tvName.setText(""); // 清空姓名
        tvMobileNumber.setText(""); // 清空手机号码
        tvTelephoneNumber.setText(""); // 清空座机号码
        tvEmail.setText(""); // 清空电子邮件
        tvAddress.setText(""); // 清空地址

        // 设置默认头像或清空头像
        contactAvatar.setImageResource(R.drawable.ic_person); // 设置默认头像
    }

    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("[^0-9]", "")));
        getContext().startActivity(intent);
    }

    // 供宿主Activity调用，处理编辑结果
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Contact updatedContact = (Contact) data.getSerializableExtra("updatedContact");
            if (updatedContact != null) {
                // 更新当前显示的联系人
                setContact(updatedContact);
                
                // 通知监听器联系人已更新
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
            // 使用QRCodeUtils实例生成联系人二维码
            Bitmap qrCodeBitmap = qrCodeUtil.generateContactQRCode(currentContact, 600);
            
            if (qrCodeBitmap != null) {
                // 显示包含二维码的对话框
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
     * @param qrCodeBitmap 二维码位图
     */
    private void showQRCodeDialog(Bitmap qrCodeBitmap) {
        // 创建对话框布局
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_code, null);
        
        // 设置二维码图片
        ImageView imgQrCode = dialogView.findViewById(R.id.img_qr_code);
        imgQrCode.setImageBitmap(qrCodeBitmap);
        
        // 设置标题
        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        tvDialogTitle.setText("扫描二维码添加 " + currentContact.getName() + " 的联系信息");
        
        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        
        // 设置保存按钮点击事件
        dialogView.findViewById(R.id.btn_save_qr_code).setOnClickListener(v -> {
            saveQRCodeToGallery(qrCodeBitmap);
        });
        
        // 设置关闭按钮点击事件
        dialogView.findViewById(R.id.btn_close).setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        // 显示对话框
        dialog.show();
    }

    /**
     * 保存二维码到相册
     * @param bitmap 要保存的二维码位图
     */
    private void saveQRCodeToGallery(Bitmap bitmap) {
        String fileName = "联系人_" + currentContact.getName() + "_" 
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";
        
        // 根据Android版本使用不同的保存方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageWithMediaStore(bitmap, fileName);
        } else {
            saveImageToLegacyStorage(bitmap, fileName);
        }
    }

    /**
     * 使用MediaStore API保存图片（Android 10及以上）
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
