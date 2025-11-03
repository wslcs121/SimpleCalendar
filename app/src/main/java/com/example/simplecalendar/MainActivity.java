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

    // 科目颜色配置
    private final int[] subjectColors = {
            0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1, 0xFF96CEB4,
            0xFFFECA57, 0xFFFF9FF3, 0xFF54A0FF, 0xFF5F27CD
    };

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
                // 简单模拟：在特定日期显示事件标记
                boolean hasEvent = (i == 1 || i == 8 || i == 15 || i == 20 || i == 25);
                dateItems.add(new CalendarDateItem(i, true, hasEvent));
            }

            // 计算需要显示的下个月天数（凑满6行42格）
            int totalCells = 42;
            int nextMonthDays = totalCells - dateItems.size();
            for (int i = 1; i <= nextMonthDays; i++) {
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