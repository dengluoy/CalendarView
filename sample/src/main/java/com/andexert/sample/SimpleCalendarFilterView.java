package com.andexert.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andexert.calendarlistview.library.CalendarUtils;
import com.andexert.calendarlistview.library.DatePickerController;
import com.andexert.calendarlistview.library.DayPickerView;
import com.andexert.calendarlistview.library.SimpleMonthAdapter.CalendarDay;
import com.andexert.calendarlistview.library.SimpleMonthAdapter.SelectedDays;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author WeiDeng
 * @date 16/6/14
 * @description     日历筛选控件
 */
public class SimpleCalendarFilterView extends RelativeLayout implements DatePickerController {

    private LayoutInflater mLayoutInflater;
    private DayPickerView mPickerView;
    private TextView mDateTv;
    private TextView mHowManyDaysTv;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
    private static LinkedHashMap<String, String> vacationMap;

    public SimpleCalendarFilterView(Context context) {
        this(context, null);
    }

    public SimpleCalendarFilterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleCalendarFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        internalInit();
    }

    /**
     * 初始化
     */
    private void internalInit() {
        mLayoutInflater = LayoutInflater.from(getContext());
        View rootView = mLayoutInflater.inflate(R.layout.calendar_filter_layout, this, true);
        mPickerView = (DayPickerView) rootView.findViewById(R.id.pickerView);
        mDateTv = (TextView) rootView.findViewById(R.id.filter_date_tv);
        mHowManyDaysTv = (TextView) rootView.findViewById(R.id.filter_how_many_days_tv);
        mPickerView.setController(this);
    }

    /**
     * 设置节假日
     * @param holidayMap
     */
    public void setHoliday(Map<String, String> holidayMap) {
        mPickerView.setHoliday(holidayMap);
    }

    @Override
    public int getMaxYear() {
        return 2017;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
    }

    @Override
    public void onDateRangeSelected(SelectedDays<CalendarDay> selectedDays) {
        CalendarDay firstDay = selectedDays.getFirst();
        CalendarDay lastDay = selectedDays.getLast();
        int compareDate = CalendarUtils.compareDate(firstDay.getDate(), lastDay.getDate());

        int days = 0;
        String firstDateStr;
        String lastDateStr;
        if(compareDate > 0) {
            firstDateStr = dateFormat.format(firstDay.getDate());
            lastDateStr = dateFormat.format(lastDay.getDate());
            days = firstDay.computeDays(lastDay);
        } else {
            firstDateStr = dateFormat.format(lastDay.getDate());
            lastDateStr = dateFormat.format(firstDay.getDate());
            days = lastDay.computeDays(firstDay);
        }
        mDateTv.setText(String.valueOf(firstDateStr + " - " + lastDateStr));
        mHowManyDaysTv.setText(String.valueOf("共" + (days + 1) + "天" + days+ "晚"));
        mHowManyDaysTv.setVisibility(View.VISIBLE);
    }

}
