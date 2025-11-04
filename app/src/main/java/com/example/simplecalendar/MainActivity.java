package com.example.simplecalendar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;
import androidx.appcompat.app.AlertDialog;  // 添加AlertDialog导入
import android.content.DialogInterface;     // 添加DialogInterface导入
import android.view.LayoutInflater;         // 添加LayoutInflater导入
import android.widget.AdapterView;          // 添加AdapterView导入
import org.json.JSONArray;



public class MainActivity extends AppCompatActivity {
    private TextView tvCurrentDate, tvMonthYear;
    private LinearLayout calendarContainer, headerDate;
    private GridView gvCalendar;
    private Button btnPrevious, btnNext, btnSave;
    private EditText etDiary;
    private EditText[] homeworkEdits = new EditText[8];

    private Calendar currentDate;
    private SimpleDateFormat dateFormat, monthYearFormat;
    private CalendarAdapter calendarAdapter;
    private boolean isCalendarVisible = false;

    // ↓↓↓ 在这里添加科目相关成员变量 ↓↓↓
    private SubjectInfo[] subjects;
    private ImageButton[] editButtons = new ImageButton[8];
    private int currentEditingSubjectIndex = -1;
    // ↑↑↑ 添加到这里 ↑↑↑

    // 科目颜色配置
    private final int[] subjectColors = {
            0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1, 0xFF96CEB4,
            0xFFFECA57, 0xFFFF9FF3, 0xFF54A0FF, 0xFF5F27CD
    };

    // 在MainActivity类开头添加科目数据类
    private static class SubjectInfo {
        String name;
        String hint;
        int color;

        SubjectInfo(String name, String hint, int color) {
            this.name = name;
            this.hint = hint;
            this.color = color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupEventListeners();
        updateCalendar();
        loadCurrentDateData();
    }
    private void initSubjects() {
        subjects = new SubjectInfo[]{
                new SubjectInfo("语文", "语文作业...", 0xFFFF6B6B),
                new SubjectInfo("数学", "数学作业...", 0xFF4ECDC4),
                new SubjectInfo("英语", "英语作业...", 0xFF45B7D1),
                new SubjectInfo("物理", "物理作业...", 0xFF96CEB4),
                new SubjectInfo("化学", "化学作业...", 0xFFFECA57),
                new SubjectInfo("生物", "生物作业...", 0xFFFF9FF3),
                new SubjectInfo("历史", "历史作业...", 0xFF54A0FF),
                new SubjectInfo("地理", "地理作业...", 0xFF5F27CD)
        };
    }
    // ↑↑↑ 添加到这里 ↑↑↑


    private void initViews() {
        tvCurrentDate = findViewById(R.id.tv_current_date);
        tvMonthYear = findViewById(R.id.tv_month_year);
        calendarContainer = findViewById(R.id.calendar_container);
        headerDate = findViewById(R.id.header_date);
        gvCalendar = findViewById(R.id.gv_calendar);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnSave = findViewById(R.id.btn_save);
        etDiary = findViewById(R.id.et_diary);

        // 初始化所有8个作业输入框
        homeworkEdits[0] = findViewById(R.id.et_homework_1);
        homeworkEdits[1] = findViewById(R.id.et_homework_2);
        homeworkEdits[2] = findViewById(R.id.et_homework_3);
        homeworkEdits[3] = findViewById(R.id.et_homework_4);
        homeworkEdits[4] = findViewById(R.id.et_homework_5);
        homeworkEdits[5] = findViewById(R.id.et_homework_6);
        homeworkEdits[6] = findViewById(R.id.et_homework_7);
        homeworkEdits[7] = findViewById(R.id.et_homework_8);

        // 初始化编辑按钮数组
        editButtons[0] = findViewById(R.id.btn_edit_1);
        editButtons[1] = findViewById(R.id.btn_edit_2);
        editButtons[2] = findViewById(R.id.btn_edit_3);
        editButtons[3] = findViewById(R.id.btn_edit_4);
        editButtons[4] = findViewById(R.id.btn_edit_5);
        editButtons[5] = findViewById(R.id.btn_edit_6);
        editButtons[6] = findViewById(R.id.btn_edit_7);
        editButtons[7] = findViewById(R.id.btn_edit_8);
    }

    private void initData() {
        currentDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        monthYearFormat = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
        calendarAdapter = new CalendarAdapter();
        gvCalendar.setAdapter(calendarAdapter);

        // 设置科目标签颜色
        for (int i = 0; i < homeworkEdits.length && homeworkEdits[i] != null; i++) {
            ViewGroup parent = (ViewGroup) homeworkEdits[i].getParent();
            if (parent != null && parent.getChildCount() > 0) {
                TextView subjectLabel = (TextView) parent.getChildAt(0);
                subjectLabel.setTextColor(subjectColors[i]);
            }
        }
        initSubjects();
        loadSubjectsFromPreferences();
    }

    private void setupEventListeners() {
        // 顶部日期栏点击事件 - 展开/收起日历
        headerDate.setOnClickListener(v -> toggleCalendar());

        // 日历月份导航
        // 月份导航 - 添加点击动画反馈
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加点击动画
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            }
                        }).start();

                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();

                // 添加切换提示
                Toast.makeText(MainActivity.this,
                        "切换到 " + monthYearFormat.format(currentDate.getTime()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加点击动画
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            }
                        }).start();

                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();

                // 添加切换提示
                Toast.makeText(MainActivity.this,
                        "切换到 " + monthYearFormat.format(currentDate.getTime()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // 为每个编辑按钮设置点击事件
        for (int i = 0; i < editButtons.length; i++) {
            final int subjectIndex = i;
            editButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditSubjectDialog(subjectIndex);
                }
            });
        }

        // 日期点击事件
        gvCalendar.setOnItemClickListener((parent, view, position, id) -> {
            CalendarDateItem dateItem = calendarAdapter.getItem(position);
            if (dateItem != null && dateItem.isCurrentMonth) {
                // 更新当前选中日期
                currentDate.set(Calendar.DAY_OF_MONTH, dateItem.day);
                tvCurrentDate.setText(dateFormat.format(currentDate.getTime()));

                // 收起日历
                toggleCalendar();

                // 加载选中日期的数据
                loadCurrentDateData();
            }
        });

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> saveCurrentDateData());
    }

    // ↓↓↓ 在setupEventListeners方法后添加科目编辑相关方法 ↓↓↓
    // 显示科目编辑对话框
    private void showEditSubjectDialog(final int subjectIndex) {
        currentEditingSubjectIndex = subjectIndex;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_subject, null);
        builder.setView(dialogView);

        final EditText etSubjectName = dialogView.findViewById(R.id.et_subject_name);
        final EditText etSubjectHint = dialogView.findViewById(R.id.et_subject_hint);
        GridView gvColors = dialogView.findViewById(R.id.gv_colors);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save_subject);

        // 设置当前值
        SubjectInfo currentSubject = subjects[subjectIndex];
        etSubjectName.setText(currentSubject.name);
        etSubjectHint.setText(currentSubject.hint);

        // 设置颜色选择器
        final int[] colorOptions = {
                0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1, 0xFF96CEB4,
                0xFFFECA57, 0xFFFF9FF3, 0xFF54A0FF, 0xFF5F27CD,
                0xFFFF5252, 0xFF448AFF, 0xFFE040FB, 0xFF18FFFF,
                0xFFFFAB00, 0xFF69F0AE, 0xFFFF4081, 0xFF7C4DFF
        };

        gvColors.setAdapter(new ColorAdapter(colorOptions, currentSubject.color));
        gvColors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSubject.color = colorOptions[position];
                ((ColorAdapter) gvColors.getAdapter()).setSelectedColor(colorOptions[position]);
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = etSubjectName.getText().toString().trim();
                String newHint = etSubjectHint.getText().toString().trim();

                if (!newName.isEmpty()) {
                    subjects[subjectIndex].name = newName;
                    subjects[subjectIndex].hint = newHint.isEmpty() ? newName + "作业..." : newHint;
                    updateSubjectDisplay(subjectIndex);
                    saveSubjectsToPreferences();
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "科目已更新", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "科目名称不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除这个科目吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 这里可以实现科目删除逻辑
                                Toast.makeText(MainActivity.this, "科目删除功能待实现", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        dialog.show();
    }

    // 颜色适配器
    private class ColorAdapter extends BaseAdapter {
        private int[] colors;
        private int selectedColor;

        ColorAdapter(int[] colors, int selectedColor) {
            this.colors = colors;
            this.selectedColor = selectedColor;
        }

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_color, parent, false);
            }

            View colorView = view.findViewById(R.id.color_view);
            View selectionIndicator = view.findViewById(R.id.selection_indicator);

            int color = colors[position];
            colorView.setBackgroundColor(color);

            // 显示选中状态
            boolean isSelected = (color == selectedColor);
            selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

            return view;
        }

        public void setSelectedColor(int color) {
            this.selectedColor = color;
            notifyDataSetChanged();
        }
    }

    private void toggleCalendar() {
        isCalendarVisible = !isCalendarVisible;
        calendarContainer.setVisibility(isCalendarVisible ? View.VISIBLE : View.GONE);
        updateCalendar();
    }

    private void updateCalendar() {
        tvMonthYear.setText(monthYearFormat.format(currentDate.getTime()));
        tvCurrentDate.setText(dateFormat.format(currentDate.getTime()));
        if (calendarAdapter != null) {
            calendarAdapter.updateCalendar();
        }
    }

    private void loadCurrentDateData() {
        String dateKey = getDateKey(currentDate);
        String savedData = PreferenceHelper.getString(this, dateKey, "");

        if (!TextUtils.isEmpty(savedData)) {
            try {
                JSONObject jsonData = new JSONObject(savedData);

                // 加载作业数据
                JSONObject homeworkData = jsonData.optJSONObject("homework");
                if (homeworkData != null) {
                    for (int i = 0; i < homeworkEdits.length && homeworkEdits[i] != null; i++) {
                        String subjectKey = "subject" + (i + 1);
                        String homework = homeworkData.optString(subjectKey, "");
                        homeworkEdits[i].setText(homework);
                    }
                }

                // 加载日记数据
                String diary = jsonData.optString("diary", "");
                etDiary.setText(diary);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // 清除所有输入框
            for (EditText editText : homeworkEdits) {
                if (editText != null) {
                    editText.setText("");
                }
            }
            etDiary.setText("");
        }
    }

    private void saveCurrentDateData() {
        try {
            JSONObject jsonData = new JSONObject();
            JSONObject homeworkData = new JSONObject();

            // 保存作业数据
            for (int i = 0; i < homeworkEdits.length && homeworkEdits[i] != null; i++) {
                String subjectKey = "subject" + (i + 1);
                String homework = homeworkEdits[i].getText().toString().trim();
                homeworkData.put(subjectKey, homework);
            }

            jsonData.put("homework", homeworkData);

            // 保存日记数据
            String diary = etDiary.getText().toString().trim();
            jsonData.put("diary", diary);

            // 保存到SharedPreferences
            String dateKey = getDateKey(currentDate);
            PreferenceHelper.putString(this, dateKey, jsonData.toString());

            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDateKey(Calendar calendar) {
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return keyFormat.format(calendar.getTime());
    }

    // 日期数据类
    private static class CalendarDateItem {
        int day;
        boolean isCurrentMonth;
        boolean hasEvent;

        CalendarDateItem(int day, boolean isCurrentMonth, boolean hasEvent) {
            this.day = day;
            this.isCurrentMonth = isCurrentMonth;
            this.hasEvent = hasEvent;
        }
    }

    // 保存科目设置到SharedPreferences
    private void saveSubjectsToPreferences() {
        try {
            JSONArray subjectsArray = new JSONArray();
            for (SubjectInfo subject : subjects) {
                JSONObject subjectJson = new JSONObject();
                subjectJson.put("name", subject.name);
                subjectJson.put("hint", subject.hint);
                subjectJson.put("color", subject.color);
                subjectsArray.put(subjectJson);
            }
            PreferenceHelper.putString(this, "subjects", subjectsArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 从SharedPreferences加载科目设置
    private void loadSubjectsFromPreferences() {
        String subjectsJson = PreferenceHelper.getString(this, "subjects", "");
        if (!subjectsJson.isEmpty()) {
            try {
                JSONArray subjectsArray = new JSONArray(subjectsJson);
                for (int i = 0; i < subjectsArray.length() && i < subjects.length; i++) {
                    JSONObject subjectJson = subjectsArray.getJSONObject(i);
                    subjects[i].name = subjectJson.getString("name");
                    subjects[i].hint = subjectJson.getString("hint");
                    subjects[i].color = subjectJson.getInt("color");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 更新单个科目显示
    private void updateSubjectDisplay(int subjectIndex) {
        if (subjectIndex >= 0 && subjectIndex < homeworkEdits.length) {
            SubjectInfo subject = subjects[subjectIndex];

            // 找到科目标签TextView
            ViewGroup parent = (ViewGroup) homeworkEdits[subjectIndex].getParent();
            if (parent != null) {
                TextView subjectLabel = (TextView) parent.getChildAt(0);
                subjectLabel.setText(subject.name);
                subjectLabel.setTextColor(subject.color);
            }

            // 更新提示文字
            homeworkEdits[subjectIndex].setHint(subject.hint);
        }
    }

    // 更新所有科目显示
    private void updateAllSubjectsDisplay() {
        for (int i = 0; i < subjects.length; i++) {
            updateSubjectDisplay(i);
        }
    }

    // ↓↓↓ 添加这个方法到MainActivity类中 ↓↓↓
    private boolean hasDataForDate(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        String dateKey = getDateKey(date);
        String savedData = PreferenceHelper.getString(this, dateKey, "");

        if (!TextUtils.isEmpty(savedData)) {
            try {
                JSONObject jsonData = new JSONObject(savedData);
                // 检查是否有作业或日记数据
                JSONObject homeworkData = jsonData.optJSONObject("homework");
                String diary = jsonData.optString("diary", "");

                // 如果有任何作业或日记内容，返回true
                if (homeworkData != null) {
                    for (int i = 0; i < 8; i++) {
                        String homework = homeworkData.optString("subject" + (i + 1), "");
                        if (!TextUtils.isEmpty(homework)) {
                            return true;
                        }
                    }
                }

                return !TextUtils.isEmpty(diary);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    // ↑↑↑ 添加到这里 ↑↑↑

    // 日历适配器类 - 这就是缺失的CalendarAdapter
    private class CalendarAdapter extends BaseAdapter {
        private List<CalendarDateItem> dateItems;

        CalendarAdapter() {
            dateItems = new ArrayList<>();
        }

        void updateCalendar() {
            dateItems.clear();
            calculateDays();  // ← 这里调用 calculateDays 方法
            notifyDataSetChanged();
        }

        private void calculateDays() {
            dateItems.clear();

            // 创建当前月份的日历副本
            Calendar calendar = (Calendar) currentDate.clone();

            // 设置到当前月份的第一天
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // 计算需要显示的上个月天数
            int prevMonthDays = firstDayOfWeek - 1; // 周日=1, 周一=2, 所以要-1

            // 添加上个月的日期
            calendar.add(Calendar.MONTH, -1);
            int daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = daysInPrevMonth - prevMonthDays + 1; i <= daysInPrevMonth; i++) {
                dateItems.add(new CalendarDateItem(i, false, false));
            }

            // 回到当前月，添加当前月的日期
            calendar.add(Calendar.MONTH, 1);
            for (int i = 1; i <= daysInMonth; i++) {
                // ↓↓↓ 修改这里：检查该日期是否有保存的数据 ↓↓↓
                boolean hasEvent = hasDataForDate(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), i);
                dateItems.add(new CalendarDateItem(i, true, hasEvent));
            }
            // 添加下个月的日期
            int totalCells = 42;
            int remainingCells = totalCells - dateItems.size();
            for (int i = 1; i <= remainingCells; i++) {
                dateItems.add(new CalendarDateItem(i, false, false));
            }

        }


        @Override
        public int getCount() {
            return dateItems.size();
        }

        @Override
        public CalendarDateItem getItem(int position) {
            return dateItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_date, parent, false);
                holder = new ViewHolder();
                holder.tvDate = convertView.findViewById(R.id.tv_date);
                holder.tvEventIndicator = convertView.findViewById(R.id.tv_event_indicator);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CalendarDateItem dateItem = getItem(position);

            // 设置日期文本
            holder.tvDate.setText(String.valueOf(dateItem.day));

            // 设置样式：当前月 vs 非当前月
            if (dateItem.isCurrentMonth) {
                holder.tvDate.setTextColor(getColor(android.R.color.black));
                holder.tvDate.setAlpha(1.0f);
            } else {
                holder.tvDate.setTextColor(getResources().getColor(android.R.color.darker_gray));
                holder.tvDate.setAlpha(0.5f);
            }

            // 高亮显示今天
            Calendar today = Calendar.getInstance();
            if (dateItem.isCurrentMonth &&
                    currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    dateItem.day == today.get(Calendar.DAY_OF_MONTH)) {
                holder.tvDate.setSelected(true);
                holder.tvDate.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                holder.tvDate.setSelected(false);
            }

            // 显示事件标记
            if (holder.tvEventIndicator != null) {
                holder.tvEventIndicator.setVisibility(dateItem.hasEvent ? View.VISIBLE : View.INVISIBLE);
            }

            return convertView;
        }


        class ViewHolder {
            TextView tvDate;
            TextView tvEventIndicator;
        }
    }
}