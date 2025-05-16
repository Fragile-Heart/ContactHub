package com.example.contacthub.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.contacthub.databinding.FragmentSettingBinding;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图绑定初始化布局
        binding = FragmentSettingBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 联系人管理功能 - 布局改为LinearLayout，但功能不变
        binding.buttonImportContacts.setOnClickListener(v -> 
            showToast("导入联系人功能待实现")
        );
        binding.buttonExportContacts.setOnClickListener(v -> 
            showToast("导出联系人功能待实现")
        );

        // 外观设置功能
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("切换到黑夜模式");
                // TODO: 实现黑夜模式逻辑
            } else {
                showToast("切换到白天模式");
                // TODO: 实现白天模式逻辑
            }
        });

        // 其他设置功能
        binding.buttonOtherSettings.setOnClickListener(v -> 
            showToast("其他设置功能待实现")
        );
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
