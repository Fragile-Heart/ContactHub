package com.example.contacthub;

    import android.content.Context;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.ViewGroup;

    import androidx.core.view.WindowCompat;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.core.graphics.Insets;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.navigation.NavController;
    import androidx.navigation.fragment.NavHostFragment;
    import androidx.navigation.ui.NavigationUI;

    import com.example.contacthub.databinding.ActivityMainBinding;

    import java.io.FileOutputStream;
    import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 启用沉浸式模式（设置在布局膨胀之前）
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.container, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 为标题添加适当的顶部内边距
            binding.tvTitle.setPadding(0, systemBars.top, 0, 0);
            // 增加标题高度以适应状态栏
            ViewGroup.LayoutParams params = binding.tvTitle.getLayoutParams();
            params.height = systemBars.top + (int) getResources().getDimension(R.dimen.title_height);
            binding.tvTitle.setLayoutParams(params);

            return insets;
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
        }
    }

    private void initializeData() {
        //FileUtil fileUtil = new FileUtil(this);

        // 添加联系人数据
        String contactsJson = "[{\"id\":1,\"name\":\"小明\",\"phone\":\"123456789\",\"groupIds\":[1,2,3]},{\"id\":2,\"name\":\"小红\",\"phone\":\"987654321\",\"groupIds\":[1,4]},{\"id\":3,\"name\":\"张三\",\"phone\":\"111111111\",\"groupIds\":[2,3]},{\"id\":4,\"name\":\"李四\",\"phone\":\"222222222\",\"groupIds\":[1,3]},{\"id\":5,\"name\":\"王五\",\"phone\":\"333333333\",\"groupIds\":[2,4]},{\"id\":6,\"name\":\"赵六\",\"phone\":\"444444444\",\"groupIds\":[1,2]},{\"id\":7,\"name\":\"孙七\",\"phone\":\"555555555\",\"groupIds\":[3,4]},{\"id\":8,\"name\":\"周八\",\"phone\":\"666666666\",\"groupIds\":[1,4]},{\"id\":9,\"name\":\"吴九\",\"phone\":\"777777777\",\"groupIds\":[2,3]},{\"id\":10,\"name\":\"郑十\",\"phone\":\"888888888\",\"groupIds\":[1,4]},{\"id\":11,\"name\":\"冯十一\",\"phone\":\"999999999\",\"groupIds\":[1,2]},{\"id\":12,\"name\":\"陈十二\",\"phone\":\"000000000\",\"groupIds\":[3,4]},{\"id\":13,\"name\":\"刘十三\",\"phone\":\"123123123\",\"groupIds\":[1,3]},{\"id\":14,\"name\":\"黄十四\",\"phone\":\"321321321\",\"groupIds\":[2,4]},{\"id\":15,\"name\":\"周十五\",\"phone\":\"456456456\",\"groupIds\":[1,2]},{\"id\":16,\"name\":\"吴十六\",\"phone\":\"654654654\",\"groupIds\":[3,4]},{\"id\":17,\"name\":\"郑十七\",\"phone\":\"789789789\",\"groupIds\":[1,4]},{\"id\":18,\"name\":\"王十八\",\"phone\":\"987987987\",\"groupIds\":[2,3]},{\"id\":19,\"name\":\"曾辉\",\"phone\":\"123456789\",\"groupIds\":[1,2]}]";
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
}