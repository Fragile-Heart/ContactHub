package com.example.contacthub;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.contacthub.databinding.ActivityMainBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.contactDetail.ContactEditActivity;
import com.example.contacthub.utils.QRCodeUtils;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private QRCodeUtils qrCodeUtils;
    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        processQRCodeImage(selectedImageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<ScanOptions> qrCodeScanLauncher = QRCodeUtils.createQRScannerLauncher(
            this, new QRCodeUtils.QRScanResultCallback() {
                @Override
                public void onScanSuccess(String qrContent) {
                    processQRCodeResult(qrContent);
                }

                @Override
                public void onScanCancelled() {
                    Toast.makeText(MainActivity.this, "扫描取消", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化QRCodeUtils实例
        qrCodeUtils = new QRCodeUtils(this);

        // 适配状态栏
        adjustTopBarToStatusBar();

        // 设置二维码扫描按钮点击事件
        binding.btnScanQrcode.setOnClickListener(v -> {
            // 显示选择对话框：相机或相册
            new AlertDialog.Builder(this)
                    .setTitle("选择扫描方式")
                    .setItems(new CharSequence[]{"使用相机扫描", "从相册选择图片"}, (dialog, which) -> {
                        if (which == 0) {
                            // 使用相机扫描
                            qrCodeUtils.launchQRCodeScanner(qrCodeScanLauncher);
                        } else {
                            // 从相册选择
                            openGallery();
                        }
                    })
                    .show();
        });

        initializeData();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 设置导航监听更新标题
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getLabel() != null) {
                    binding.tvTitle.setText(destination.getLabel());
                }
            });

            NavigationUI.setupWithNavController(binding.navView, navController);

            // 修复多次点击同一导航项导致返回栈问题
            binding.navView.setOnItemReselectedListener(item -> {
                // 什么都不做，防止重复导航添加到回退栈
            });

            // 添加后退导航处理
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!navController.popBackStack()) {
                        finish();
                    }
                }
            });
        }
    }

    // 适配状态栏高度
    private void adjustTopBarToStatusBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.tvTitle, (view, windowInsets) -> {
            // 获取状态栏高度
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            // 调整顶部栏布局参数
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.title_bar_height) + statusBarHeight;
            // 设置顶部内边距，防止内容被状态栏遮挡
            view.setPadding(view.getPaddingLeft(), statusBarHeight,
                           view.getPaddingRight(), view.getPaddingBottom());
            view.setLayoutParams(params);

            // 同时调整扫描按钮的位置，确保垂直居中（相对于文本部分）
           ViewGroup.MarginLayoutParams btnParams =
                (ViewGroup.MarginLayoutParams) binding.btnScanQrcode.getLayoutParams();
            // 设置顶部边距使按钮与标题文字对齐
            btnParams.topMargin = statusBarHeight;
            // 移除底部边距的设置，防止影响垂直居中
            btnParams.bottomMargin = 0;
            binding.btnScanQrcode.setLayoutParams(btnParams);

            return windowInsets;
        });
    }

    private void initializeData() {

        // 添加联系人数据
        String contactsJson = "[{\"id\":1,\"name\":\"小明\",\"mobileNumber\":\"123456789\",\"telephoneNumber\":\"123-456-7890\",\"email\":\"xiaoming@example.com\",\"address\":\"无\",\"groupIds\":[1,2,3]},{\"id\":2,\"name\":\"小红\",\"mobileNumber\":\"987654321\",\"telephoneNumber\":\"987-654-3210\",\"email\":\"xiaohong@example.com\",\"address\":\"无\",\"groupIds\":[1,4]},{\"id\":3,\"name\":\"张三\",\"mobileNumber\":\"111111111\",\"telephoneNumber\":\"111-111-1111\",\"email\":\"zhangsan@example.com\",\"address\":\"无\",\"groupIds\":[2,3]},{\"id\":4,\"name\":\"李四\",\"mobileNumber\":\"222222222\",\"telephoneNumber\":\"222-222-2222\",\"email\":\"lisi@example.com\",\"address\":\"无\",\"groupIds\":[1,3]},{\"id\":5,\"name\":\"王五\",\"mobileNumber\":\"333333333\",\"telephoneNumber\":\"333-333-3333\",\"email\":\"wangwu@example.com\",\"address\":\"无\",\"groupIds\":[2,4]},{\"id\":6,\"name\":\"赵六\",\"mobileNumber\":\"444444444\",\"telephoneNumber\":\"444-444-4444\",\"email\":\"zhaoliu@example.com\",\"address\":\"无\",\"groupIds\":[1,2]},{\"id\":7,\"name\":\"孙七\",\"mobileNumber\":\"555555555\",\"telephoneNumber\":\"555-555-5555\",\"email\":\"sunqi@example.com\",\"address\":\"无\",\"groupIds\":[3,4]},{\"id\":8,\"name\":\"周八\",\"mobileNumber\":\"666666666\",\"telephoneNumber\":\"666-666-6666\",\"email\":\"zhouba@example.com\",\"address\":\"无\",\"groupIds\":[1,4]},{\"id\":9,\"name\":\"吴九\",\"mobileNumber\":\"777777777\",\"telephoneNumber\":\"777-777-7777\",\"email\":\"wujie@example.com\",\"address\":\"无\",\"groupIds\":[2,3]},{\"id\":10,\"name\":\"郑十\",\"mobileNumber\":\"888888888\",\"telephoneNumber\":\"888-888-8888\",\"email\":\"zhengshi@example.com\",\"address\":\"无\",\"groupIds\":[1,4]},{\"id\":11,\"name\":\"冯十一\",\"mobileNumber\":\"999999999\",\"telephoneNumber\":\"999-999-9999\",\"email\":\"fengshiyi@example.com\",\"address\":\"无\",\"groupIds\":[1,2]},{\"id\":12,\"name\":\"陈十二\",\"mobileNumber\":\"000000000\",\"telephoneNumber\":\"000-000-0000\",\"email\":\"chenshiyi@example.com\",\"address\":\"无\",\"groupIds\":[3,4]},{\"id\":13,\"name\":\"刘十三\",\"mobileNumber\":\"123123123\",\"telephoneNumber\":\"123-123-1234\",\"email\":\"liushisan@example.com\",\"address\":\"无\",\"groupIds\":[1,3]},{\"id\":14,\"name\":\"黄十四\",\"mobileNumber\":\"321321321\",\"telephoneNumber\":\"321-321-3210\",\"email\":\"huangshisi@example.com\",\"address\":\"无\",\"groupIds\":[2,4]},{\"id\":15,\"name\":\"周十五\",\"mobileNumber\":\"456456456\",\"telephoneNumber\":\"456-456-4560\",\"email\":\"zhoushiwu@example.com\",\"address\":\"无\",\"groupIds\":[1,2]},{\"id\":16,\"name\":\"吴十六\",\"mobileNumber\":\"654654654\",\"telephoneNumber\":\"654-654-6540\",\"email\":\"wushiliu@example.com\",\"address\":\"无\",\"groupIds\":[3,4]},{\"id\":17,\"name\":\"郑十七\",\"mobileNumber\":\"789789789\",\"telephoneNumber\":\"789-789-7890\",\"email\":\"zhengshiqi@example.com\",\"address\":\"无\",\"groupIds\":[1,4]},{\"id\":18,\"name\":\"王十八\",\"mobileNumber\":\"987987987\",\"telephoneNumber\":\"987-987-9870\",\"email\":\"wangshiba@example.com\",\"address\":\"无\",\"groupIds\":[2,3]},{\"id\":19,\"name\":\"曾辉\",\"mobileNumber\":\"123456789\",\"telephoneNumber\":\"123-456-7890\",\"email\":\"zenghui@example.com\",\"address\":\"无\",\"groupIds\":[1,2]}]";
        try {
            FileOutputStream fosContacts = openFileOutput("contacts.json", Context.MODE_PRIVATE);
            fosContacts.write(contactsJson.getBytes());
            fosContacts.close();
            Log.d("MainActivity", "联系人数据初始化成功");
        } catch (IOException e) {
            Log.e("MainActivity", "初始化联系人数据失败", e);
        }

        // 添加分组数据
        String groupsJson = "[{\"id\":1,\"name\":\"朋友\"},{\"id\":2,\"name\":\"大学同学\"},{\"id\":3,\"name\":\"学生会\"},{\"id\":4,\"name\":\"家人\"}]";
        try {
            FileOutputStream fosGroups = openFileOutput("groups.json", Context.MODE_PRIVATE);
            fosGroups.write(groupsJson.getBytes());
            fosGroups.close();
            Log.d("MainActivity", "分组数据初始化成功");
        } catch (IOException e) {
            Log.e("MainActivity", "初始化分组数据失败", e);
        }
    }

    /**
     * 打开图库选择二维码图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        selectImageLauncher.launch(intent);
    }

    /**
     * 处理从图库选择的二维码图片
     */
    private void processQRCodeImage(Uri imageUri) {
        String qrCodeResult = qrCodeUtils.decodeQRCodeFromUri(imageUri);
        if (qrCodeResult != null) {
            processQRCodeResult(qrCodeResult);
        } else {
            Toast.makeText(this, "无法识别图片中的二维码", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理二维码扫描结果
     */
    private void processQRCodeResult(String qrContent) {
        try {
            Contact newContact = qrCodeUtils.jsonToContact(qrContent);
            newContact.generateNewId(this);

            Intent intent = new Intent(this, ContactEditActivity.class);
            intent.putExtra("contact", newContact);
            startActivity(intent);

            Toast.makeText(this, "已扫描联系人信息，请补充完善", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e(TAG, "二维码内容不是有效的JSON格式", e);
            Toast.makeText(this, "无效的二维码格式", Toast.LENGTH_SHORT).show();
        }
    }
}
