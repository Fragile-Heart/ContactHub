package com.example.contacthub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.contacthub.databinding.ActivityMainBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.ui.view.contactDetail.ContactEditActivity;
import com.example.contacthub.utils.FileUtil;
import com.example.contacthub.utils.QRCodeUtil;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private QRCodeUtil qrCodeUtil;
    private FileUtil fileUtil;
    
    /**
     * 用于从图库选择二维码图片的启动器
     */
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

    /**
     * 用于扫描二维码的启动器
     */
    private final ActivityResultLauncher<ScanOptions> qrCodeScanLauncher = QRCodeUtil.createQRScannerLauncher(
            this, new QRCodeUtil.QRScanResultCallback() {
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
        // 强制使用日间模式（浅色主题）
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置状态栏颜色和样式
        setupStatusBar();
        
        qrCodeUtil = new QRCodeUtil(this);
        fileUtil = new FileUtil(this);

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
                            qrCodeUtil.launchQRCodeScanner(qrCodeScanLauncher);
                        } else {
                            // 从相册选择
                            openGallery();
                        }
                    })
                    .show();
        });

        initializeData();
        setupNavigation();
    }

    /**
     * 设置状态栏颜色和样式
     */
    private void setupStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.status_bar_color));

        // 设置状态栏图标为暗色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+, 使用WindowInsetsController
            Objects.requireNonNull(window.getInsetsController()).setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            // Android 6.0 - 10
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
    
    /**
     * 设置导航和返回键行为
     */
    private void setupNavigation() {
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

    /**
     * 适配状态栏高度，调整UI布局
     */
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

    /**
     * 初始化示例数据
     * 包括联系人列表、分组信息和个人名片
     */
    private void initializeData() {        // 添加联系人数据
            String contactsJson = "[{\"id\":1,\"name\":\"王志强\",\"mobileNumber\":\"13812345678\",\"telephoneNumber\":\"01087654321\",\"email\":\"zhiqiang.wang@example.com\",\"address\":\"北京市海淀区中关村南大街5号\",\"groupIds\":[1,2]},{\"id\":2,\"name\":\"李晓红\",\"mobileNumber\":\"13987654321\",\"telephoneNumber\":\"02112345678\",\"email\":\"xiaohong.li@example.com\",\"address\":\"上海市浦东新区张江高科技园区\",\"groupIds\":[1,4]},{\"id\":3,\"name\":\"张伟\",\"mobileNumber\":\"13511112222\",\"telephoneNumber\":\"075533221100\",\"email\":\"wei.zhang@example.com\",\"address\":\"深圳市南山区科技园路8号\",\"groupIds\":[2,3]},{\"id\":4,\"name\":\"刘芳\",\"mobileNumber\":\"13622223333\",\"telephoneNumber\":\"02087654321\",\"email\":\"fang.liu@example.com\",\"address\":\"广州市天河区体育西路123号\",\"groupIds\":[1,3]},{\"id\":5,\"name\":\"陈明\",\"mobileNumber\":\"13733334444\",\"telephoneNumber\":\"057112345678\",\"email\":\"ming.chen@example.com\",\"address\":\"杭州市西湖区文三路478号\",\"groupIds\":[2,4]},{\"id\":6,\"name\":\"赵阳\",\"mobileNumber\":\"13844445555\",\"telephoneNumber\":\"02887654321\",\"email\":\"yang.zhao@example.com\",\"address\":\"成都市高新区天府大道1199号\",\"groupIds\":[1,2]},{\"id\":7,\"name\":\"孙丽\",\"mobileNumber\":\"13955556666\",\"telephoneNumber\":\"02312345678\",\"email\":\"li.sun@example.com\",\"address\":\"重庆市渝中区解放碑步行街88号\",\"groupIds\":[3,4]},{\"id\":8,\"name\":\"周健\",\"mobileNumber\":\"13666667777\",\"telephoneNumber\":\"02487654321\",\"email\":\"jian.zhou@example.com\",\"address\":\"沈阳市和平区南京街5号\",\"groupIds\":[1,4]},{\"id\":9,\"name\":\"吴婷\",\"mobileNumber\":\"13777778888\",\"telephoneNumber\":\"041112345678\",\"email\":\"ting.wu@example.com\",\"address\":\"大连市中山区人民路25号\",\"groupIds\":[2,3]},{\"id\":10,\"name\":\"郑远\",\"mobileNumber\":\"13888889999\",\"telephoneNumber\":\"053287654321\",\"email\":\"yuan.zheng@example.com\",\"address\":\"青岛市市南区香港中路66号\",\"groupIds\":[1,4]},{\"id\":11,\"name\":\"冯强\",\"mobileNumber\":\"13999990000\",\"telephoneNumber\":\"02512345678\",\"email\":\"qiang.feng@example.com\",\"address\":\"南京市鼓楼区中山北路123号\",\"groupIds\":[1,2]},{\"id\":12,\"name\":\"陈佳\",\"mobileNumber\":\"15012345678\",\"telephoneNumber\":\"02787654321\",\"email\":\"jia.chen@example.com\",\"address\":\"武汉市武昌区珞瑜路205号\",\"groupIds\":[3,4]},{\"id\":13,\"name\":\"刘洋\",\"mobileNumber\":\"15123456789\",\"telephoneNumber\":\"037112345678\",\"email\":\"yang.liu@example.com\",\"address\":\"郑州市金水区花园路123号\",\"groupIds\":[1,3]},{\"id\":14,\"name\":\"黄晓明\",\"mobileNumber\":\"15234567890\",\"telephoneNumber\":\"043187654321\",\"email\":\"xiaoming.huang@example.com\",\"address\":\"长春市南关区人民大街2088号\",\"groupIds\":[2,4]},{\"id\":15,\"name\":\"周梅\",\"mobileNumber\":\"15345678901\",\"telephoneNumber\":\"045112345678\",\"email\":\"mei.zhou@example.com\",\"address\":\"哈尔滨市南岗区红旗大街235号\",\"groupIds\":[1,2]},{\"id\":16,\"name\":\"吴鹏\",\"mobileNumber\":\"15456789012\",\"telephoneNumber\":\"059187654321\",\"email\":\"peng.wu@example.com\",\"address\":\"福州市鼓楼区五一北路123号\",\"groupIds\":[3,4]},{\"id\":17,\"name\":\"郑晨\",\"mobileNumber\":\"15567890123\",\"telephoneNumber\":\"059212345678\",\"email\":\"chen.zheng@example.com\",\"address\":\"厦门市思明区湖滨南路358号\",\"groupIds\":[1,4]},{\"id\":18,\"name\":\"王辉\",\"mobileNumber\":\"15678901234\",\"telephoneNumber\":\"089887654321\",\"email\":\"hui.wang@example.com\",\"address\":\"海口市龙华区国贸大道56号\",\"groupIds\":[2,3]},{\"id\":19,\"name\":\"曾琳\",\"mobileNumber\":\"15789012345\",\"telephoneNumber\":\"087112345678\",\"email\":\"lin.zeng@example.com\",\"address\":\"昆明市盘龙区北京路155号\",\"groupIds\":[1,2]}]";        try {
            fileUtil.saveRawJSON("contacts.json", contactsJson);
            Log.d(TAG, "联系人数据初始化成功");
        } catch (IOException e) {
            Log.e(TAG, "初始化联系人数据失败", e);
        }        // 添加分组数据
        String groupsJson = "[{\"id\":1,\"name\":\"密切联系人\"},{\"id\":2,\"name\":\"大学同学\"},{\"id\":3,\"name\":\"工作伙伴\"},{\"id\":4,\"name\":\"家人亲友\"}]";
        try {
            fileUtil.saveRawJSON("groups.json", groupsJson);
            Log.d(TAG, "分组数据初始化成功");
        } catch (IOException e) {
            Log.e(TAG, "初始化分组数据失败", e);
        }        // 添加我的名片
            String myCardJson = "{\"name\":\"张明远\",\"mobileNumber\":\"13912345678\",\"telephoneNumber\":\"01087654321\",\"email\":\"mingyuan.zhang@company.com\",\"address\":\"北京市朝阳区建国路88号现代城5层\",\"photo\":\"\",\"qq\":\"123456789\",\"wechat\":\"zhang_my\",\"website\":\"https://mingyuan.dev\",\"birthday\":\"1990-05-15\",\"company\":\"未来科技有限公司\",\"postalCode\":\"100022\",\"notes\":\"软件工程师，专注于移动应用开发\"}";        try {
            fileUtil.saveRawJSON("my.json", myCardJson);
            Log.d(TAG, "我的名片数据初始化成功");
        } catch (IOException e) {
            Log.e(TAG, "初始化我的名片数据失败", e);
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
     * 
     * @param imageUri 选中图片的URI
     */
    private void processQRCodeImage(Uri imageUri) {
        String qrCodeResult = qrCodeUtil.decodeQRCodeFromUri(imageUri);
        if (qrCodeResult != null) {
            processQRCodeResult(qrCodeResult);
        } else {
            Toast.makeText(this, "无法识别图片中的二维码", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理二维码扫描结果，解析为联系人信息并打开编辑界面
     * 
     * @param qrContent 二维码内容字符串
     */
    private void processQRCodeResult(String qrContent) {
        try {
            Contact newContact = qrCodeUtil.jsonToContact(qrContent);
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
