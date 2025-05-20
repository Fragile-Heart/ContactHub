package com.example.contacthub.ui.group;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacthub.R;
import com.example.contacthub.adapter.GroupAdapter;
import com.example.contacthub.databinding.FragmentGroupBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.model.Group;
import com.example.contacthub.utils.FileUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment {

    private FragmentGroupBinding binding;
    private FileUtil fileUtil;
    private GroupAdapter groupAdapter;
    private List<Group> groups;
    // 用于跟踪删除按钮的矩形区域
    private Map<Integer, RectF> deleteButtonsMap = new HashMap<>();
    // 用于跟踪管理按钮的矩形区域
    private Map<Integer, RectF> manageButtonsMap = new HashMap<>();
    // 最大滑动距离比例（增加到二分之一以容纳两个按钮）
    private final float MAX_SWIPE_RATIO = 0.5f;
    // 保存当前滑动展开的项位置
    private int currentOpenPosition = -1;
    // 保存ItemTouchHelper实例以便手动控制
    private ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用视图绑定初始化布局
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化文件工具类
        fileUtil = new FileUtil(requireContext());

        // 设置浮动按钮点击事件
        binding.fabAddGroup.setOnClickListener(v -> showAddGroupDialog());

        // 加载数据并更新UI
        loadDataAndUpdateUI();

        // 设置滑动删除
        setupSwipeToDelete();
    }

    // 设置滑动删除功能
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.RED);
            private final Paint textPaint = new Paint();

            {
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(48);
                textPaint.setTextAlign(Paint.Align.CENTER);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // 如果之前有打开的项，且与当前项不同，则关闭之前的项
                if (currentOpenPosition != -1 && currentOpenPosition != position) {
                    closeItem(currentOpenPosition);
                }

                // 更新当前打开的项
                currentOpenPosition = position;

                // 不再立即恢复原来的视图，让按钮保持显示
                // 我们在其他地方处理关闭操作
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                // 防止在短距离滑动时触发onSwiped
                return 0.4f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                // 增加滑动逃逸速度，防止轻微滑动就触发
                return defaultValue * 10;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int position = viewHolder.getAdapterPosition();

                // 计算最大滑动距离
                float maxSwipeDistance = itemView.getWidth() * MAX_SWIPE_RATIO * -1;

                // 限制最大滑动距离
                if (dX < maxSwipeDistance) {
                    dX = maxSwipeDistance;
                }

                if (dX < 0) {
                    // 每个按钮的宽度
                    float buttonWidth = Math.abs(dX) / 2;

                    // 绘制删除按钮（右侧按钮，红色）
                    Paint deleteBgPaint = new Paint();
                    deleteBgPaint.setColor(Color.parseColor("#FF5252")); // 红色
                    deleteBgPaint.setAntiAlias(true);

                    RectF deleteBackground = new RectF(
                            itemView.getRight() - buttonWidth,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom());
                    c.drawRoundRect(deleteBackground, 24, 24, deleteBgPaint);

                    // 绘制管理按钮（左侧按钮，蓝色）
                    Paint manageBgPaint = new Paint();
                    manageBgPaint.setColor(Color.parseColor("#3F51B5")); // 蓝色
                    manageBgPaint.setAntiAlias(true);

                    RectF manageBackground = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop(),
                            itemView.getRight() - buttonWidth,
                            itemView.getBottom());
                    c.drawRoundRect(manageBackground, 24, 24, manageBgPaint);

                    // 绘制删除文字
                    textPaint.setTextSize(itemView.getHeight() * 0.2f);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setFakeBoldText(true);

                    float deleteTextX = itemView.getRight() - buttonWidth / 2;
                    float textY = itemView.getTop() + (itemView.getHeight() / 2) + (textPaint.getTextSize() / 2);
                    c.drawText("删除", deleteTextX, textY, textPaint);

                    // 绘制管理文字
                    float manageTextX = itemView.getRight() - buttonWidth - buttonWidth / 2;
                    c.drawText("管理", manageTextX, textY, textPaint);

                    // 保存按钮区域用于点击检测
                    deleteButtonsMap.put(position, deleteBackground);
                    manageButtonsMap.put(position, manageBackground);
                } else {
                    // 如果不是左滑，则清除对应位置的按钮区域
                    deleteButtonsMap.remove(position);
                    manageButtonsMap.remove(position);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerGroups);

        // 添加触摸监听器来检测按钮的点击和其他区域的点击
        binding.recyclerGroups.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    // 检查是否点击了删除按钮
                    for (Map.Entry<Integer, RectF> entry : deleteButtonsMap.entrySet()) {
                        int position = entry.getKey();
                        RectF buttonRect = entry.getValue();

                        // 只有当位置是当前打开的项，并且是有效的索引范围时，才允许触发删除操作
                        if (position == currentOpenPosition && position >= 0 && position < groups.size() && buttonRect.contains(e.getX(), e.getY())) {
                            // 用户点击了删除按钮
                            Group groupToDelete = groups.get(position);
                            showDeleteConfirmDialog(groupToDelete, position);
                            return true;
                        }
                    }

                    // 检查是否点击了管理按钮
                    for (Map.Entry<Integer, RectF> entry : manageButtonsMap.entrySet()) {
                        int position = entry.getKey();
                        RectF buttonRect = entry.getValue();

                        // 只有当位置是当前打开的项，并且是有效的索引范围时，才允许触发管理操作
                        if (position == currentOpenPosition && position >= 0 && position < groups.size() && buttonRect.contains(e.getX(), e.getY())) {
                            // 用户点击了管理按钮
                            Group groupToManage = groups.get(position);
                            showManageGroupDialog(groupToManage, position);
                            return true;
                        }
                    }

                    // 如果点击的不是按钮，且有打开的项，则关闭它
                    if (currentOpenPosition != -1) {
                        // 在视图上关闭滑动项
                        closeItem(currentOpenPosition);
                        // 完全清除所有按钮映射，防止误触发
                        deleteButtonsMap.clear();
                        manageButtonsMap.clear();
                        // 重置当前打开项标记
                        currentOpenPosition = -1;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    // 关闭已滑动打开的项
    private void closeItem(int position) {
        if (position >= 0 && position < groups.size()) {
            groupAdapter.notifyItemChanged(position);
            deleteButtonsMap.remove(position);
            manageButtonsMap.remove(position);
        }
    }

    // 显示删除确认对话框
    private void showDeleteConfirmDialog(Group groupToDelete, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除分组")
                .setMessage("确定要删除分组 '" + groupToDelete.getName() + "' 吗？这不会删除联系人。")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 删除分组
                    deleteGroup(groupToDelete, position);
                    // 重置当前打开项
                    currentOpenPosition = -1;
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 取消删除，恢复原来的视图
                    closeItem(position);
                    currentOpenPosition = -1;
                })
                .setCancelable(false)
                .show();
    }

    // 显示管理分组对话框
    private void showManageGroupDialog(Group group, int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_group_bottom_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etGroupName = view.findViewById(R.id.et_group_name);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnCreate = view.findViewById(R.id.btn_create);

        // 设置当前分组名称
        etGroupName.setText(group.getName());

        // 修改按钮文本
        btnCreate.setText("保存");

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            closeItem(position);
        });

        btnCreate.setOnClickListener(v -> {
            String newGroupName = etGroupName.getText().toString().trim();
            if (!newGroupName.isEmpty()) {
                updateGroupName(group, newGroupName, position);
                dialog.dismiss();
                closeItem(position);
            } else {
                etGroupName.setError("分组名称不能为空");
            }
        });

        dialog.setOnDismissListener(dialog1 -> closeItem(position));
        dialog.show();
    }

    // 更新分组名称
    private void updateGroupName(Group group, String newName, int position) {
        try {
            // 更新内存中的分组名称
            group.setName(newName);
            groupAdapter.notifyItemChanged(position);

            // 保存更新后的分组列表到文件
            Group[] groupArray = groups.toArray(new Group[0]);
            String json = new Gson().toJson(groupArray);

            FileOutputStream fos = requireContext().openFileOutput("groups.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();

            Toast.makeText(requireContext(), "分组名称已更新", Toast.LENGTH_SHORT).show();
            Log.d("GroupFragment", "分组名称已更新: " + newName + ", ID: " + group.getId());
        } catch (Exception e) {
            Log.e("GroupFragment", "更新分组名称失败", e);
            Toast.makeText(requireContext(), "更新分组名称失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 删除分组
    private void deleteGroup(Group group, int position) {
        try {
            int groupIdToDelete = group.getId();

            // 从列表中移除分组
            groups.remove(position);
            groupAdapter.notifyItemRemoved(position);

            // 保存更新后的分组列表到文件
            Group[] groupArray = groups.toArray(new Group[0]);
            String json = new Gson().toJson(groupArray);

            FileOutputStream fos = requireContext().openFileOutput("groups.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();

            // 更新联系人数据，移除已删除分组的引用
            List<Contact> contacts = loadContacts();
            boolean contactsUpdated = false;

            for (Contact contact : contacts) {
                List<Integer> groupIds = contact.getGroupIds();
                if (groupIds != null && groupIds.contains(groupIdToDelete)) {
                    // 从联系人的分组列表中移除此分组ID
                    groupIds.remove(Integer.valueOf(groupIdToDelete));
                    contactsUpdated = true;
                }
            }

            // 如果有联系人被更新，保存联系人数据
            if (contactsUpdated) {
                Contact[] contactArray = contacts.toArray(new Contact[0]);
                String contactsJson = new Gson().toJson(contactArray);

                FileOutputStream contactsFos = requireContext().openFileOutput("contacts.json", Context.MODE_PRIVATE);
                contactsFos.write(contactsJson.getBytes());
                contactsFos.close();

                Log.d("GroupFragment", "已更新关联联系人的分组引用");
            }

            Toast.makeText(requireContext(), "分组 '" + group.getName() + "' 已删除", Toast.LENGTH_SHORT).show();
            Log.d("GroupFragment", "分组已删除: " + group.getName() + ", ID: " + group.getId());
        } catch (Exception e) {
            Log.e("GroupFragment", "删除分组失败", e);
            Toast.makeText(requireContext(), "删除分组失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // 发生错误时恢复视图
            groupAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在Fragment恢复可见状态时重新加载数据
        // 确保当联系人数据被修改后，分组视图也能反映最新变化
        loadDataAndUpdateUI();
        Log.d("GroupFragment", "onResume: 重新加载分组数据");
    }

    // 显示添加分组对话框
    private void showAddGroupDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_group_bottom_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etGroupName = view.findViewById(R.id.et_group_name);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnCreate = view.findViewById(R.id.btn_create);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                addNewGroup(groupName);
                dialog.dismiss();
            } else {
                etGroupName.setError("分组名称不能为空");
            }
        });

        dialog.show();
    }

    // 添加新分组
    private void addNewGroup(String groupName) {
      try {
                // 创建新分组对象 (使用默认ID 0，后续会生成正确的ID)
                Group newGroup = new Group(0, false, groupName);
                newGroup.generateNewId(requireContext());

                // 读取现有分组并添加新分组
                List<Group> groups = new ArrayList<>(loadGroups());
                groups.add(newGroup);

                // 保存到文件
                Group[] groupArray = groups.toArray(new Group[0]);
                String json = new Gson().toJson(groupArray);

                FileOutputStream fos = requireContext().openFileOutput("groups.json", Context.MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.close();

                // 刷新UI显示新的分组
                loadDataAndUpdateUI();

                Toast.makeText(requireContext(), "分组 '" + groupName + "' 创建成功", Toast.LENGTH_SHORT).show();
                Log.d("GroupFragment", "新分组已创建: " + groupName + ", ID: " + newGroup.getId());
            } catch (Exception e) {
                Log.e("GroupFragment", "创建分组失败", e);
                Toast.makeText(requireContext(), "创建分组失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
      }

    // 加载数据并更新UI的方法
    private void loadDataAndUpdateUI() {
        // 加载数据
        List<Contact> contacts = loadContacts();
        groups = new ArrayList<>(loadGroups());  // 将groups保存为类成员变量

        // 设置RecyclerView
        binding.recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupAdapter = new GroupAdapter(groups, contacts);
        binding.recyclerGroups.setAdapter(groupAdapter);
    }

    // 从JSON文件加载联系人数据
    private List<Contact> loadContacts() {
        try {
            Contact[] contacts = fileUtil.readFile("contacts.json", Contact[].class);
            return Arrays.asList(contacts);
        } catch (Exception e) {
            Log.e("GroupFragment", "加载联系人失败", e);
            return new ArrayList<>();
        }
    }

    // 从JSON文件加载分组数据
    private List<Group> loadGroups() {
        try {
            Group[] groups = fileUtil.readFile("groups.json", Group[].class);
            return Arrays.asList(groups);
        } catch (Exception e) {
            Log.e("HomeFragment", "加载分组失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
