/***********************************************************************************
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Robin Chutaux
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.andexert.calendarlistview.library;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import static com.andexert.calendarlistview.library.CalendarUtils.DEFAULT_WEEK_DAYS;

/**
 * @author WeiDeng
 * @date 16/5/20
 * @description 显示一个月的控件，（包括年，月，日的绘制）
 */
class SimpleMonthView extends View {

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String VIEW_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String VIEW_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String VIEW_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    private static final int SELECTED_CIRCLE_ALPHA = 128;
    protected static int DEFAULT_HEIGHT = 32;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_SELECTED_CIRCLE_SIZE;                                          //选中日期的背景半径
    protected static int DAY_SEPARATOR_WIDTH = 20;                                           //每日的文字间距
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;                                         //每日的文字大小Size值
    protected static int MIN_HEIGHT = 10;                                                   //每列最小间距
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;                                         //星期文字的Size值
    protected static int MONTH_HEADER_SIZE;                                                 //头部月份的大小  (包含月份和星期的总高度)
    protected static int MONTH_LABEL_TEXT_SIZE;                                             //头部月份文字的Size值
    protected static int VACATION_LABEL_TEXT_SIZE;                                          //节假日文字Size值

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mMonthDayLabelPaint;
    protected Paint mMonthNumPaint;
    protected Paint mMonthTitleBGPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mSelectedCirclePaint;                                                   //选中的背景画笔
    protected Paint mSelectedContainsPaint;
    protected int mCurrentDayTextColor;
    protected int mMonthTextColor;
    protected int mDayTextColor;
    protected int mDayWeekTextColor;                                                        //星期模块的文字颜色
    protected int mDayNumColor;                                                             //日期模块的文字颜色（未选中）
    protected int mMonthTitleBGColor;                                                       //日期模块的文字颜色 (选中)
    protected int mPreviousDayColor;                                                        //过期的日期文字颜色
    protected int mSelectedDaysColor;
    protected int mSelectedContainColor;                                                    //选中开始与结束日期中间部分背景
    protected int mVacationDayColor;                                                        //节假日文字颜色

    private final StringBuilder mStringBuilder;

    protected boolean mHasToday = false;
    protected boolean mIsPrev = false;
    protected int mSelectedBeginDay = -1;
    protected int mSelectedLastDay = -1;
    protected int mSelectedBeginMonth = -1;
    protected int mSelectedLastMonth = -1;
    protected int mSelectedBeginYear = -1;
    protected int mSelectedLastYear = -1;
    protected int mToday = -1;
    protected int mWeekStart = 1;                                                           //一周是从星期几开始 （但国外的1代表周天）
    protected int mNumDays = 7;                                                             //一列显示多少天
    protected int mNumCells = mNumDays;                                                     //这个月绘制多少天
    private int mDayOfWeekStart = 0;                                                        //这个月是星期几开始
    protected int mMonth;
    protected Boolean mDrawRect;                                                            //选中时是否是矩形
    protected int mRowHeight = DEFAULT_HEIGHT;
    protected int mWidth;
    protected int mYear;
    protected int mSelectedPadding;
    protected int mMonthSpacing;                                                            //每月之间的间距
    final Time today;
    final TimeZone timeZone;                                                                //时区

    private final Calendar mCalendar;
    private final Calendar mDayLabelCalendar;
    private final Boolean isPrevDayEnabled;                                                 //是否高亮以前的日期

    private int mNumRows = DEFAULT_NUM_ROWS;

    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();

    private OnDayClickListener mOnDayClickListener;
    private Time time;

    public SimpleMonthView(Context context, TypedArray typedArray, TimeZone timeZone) {
        super(context);

        Resources resources = context.getResources();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        this.timeZone = timeZone;
        today = new Time(timeZone.getID());
        today.setToNow();
        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, resources.getColor(R.color.normal_day));
        mMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorMonthName, resources.getColor(R.color.normal_day));
        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorDayName, resources.getColor(R.color.normal_day));
        mDayNumColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDay, resources.getColor(R.color.black_333333));
        mPreviousDayColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDay, resources.getColor(R.color.color_c1c1c1));
        mDayWeekTextColor = typedArray.getColor(R.styleable.DayPickerView_colorWeek, resources.getColor(R.color.normal_day));
        mSelectedDaysColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_background));
        mVacationDayColor = typedArray.getColor(R.styleable.DayPickerView_colorVacation, resources.getColor(R.color.selected_day_background));
        mSelectedContainColor = typedArray.getColor(R.styleable.DayPickerView_colorSeelctedContainsBackgound, resources.getColor(R.color.selected_day_contains_background));
        mMonthTitleBGColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, resources.getColor(R.color.white_FFFFFF));


        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, false);

        mStringBuilder = new StringBuilder(50);
        mSelectedPadding = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_calendarDayTextSpacing, resources.getDimensionPixelOffset(R.dimen.day_text_spacing));

        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));
        MONTH_DAY_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDayName, resources.getDimensionPixelSize(R.dimen.text_size_day_name));
        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_SELECTED_CIRCLE_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));
        VACATION_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarVacationTextSize, resources.getDimensionPixelSize(R.dimen.vacation_text_size));

        //（每列的高度根据控件总高度 - 头部高度）/ 6
        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight, resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE) / 6);
        mMonthSpacing = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_calendarMonthSpacing, resources.getDimensionPixelOffset(R.dimen.month_text_spacing));
        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, true);

        MONTH_HEADER_SIZE = MONTH_LABEL_TEXT_SIZE + MONTH_DAY_LABEL_TEXT_SIZE + mMonthSpacing * 2;

        initView();
    }

    /**
     * 计算每列的高度
     *
     * @return
     */
    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 绘制星期
     *
     * @param canvas
     */
    private void drawMonthDayLabels(Canvas canvas) {

        int y = mMonthSpacing * 2 + (MONTH_DAY_LABEL_TEXT_SIZE / 2) + (MONTH_LABEL_TEXT_SIZE / 2);
        //宽度减去两边Padding / (一列的天数 * 2) 间距和文字的间距是一样的。目前按14等分
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            //星期几
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(DEFAULT_WEEK_DAYS[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK) - 1].toUpperCase(Locale.getDefault()), x, y, mMonthDayLabelPaint);
        }
    }

    /**
     * 绘制月份
     *
     * @param canvas
     */
    private void drawMonthTitle(Canvas canvas) {
        //中间位置
        int x = (mWidth + 2 * mPadding) / 2;
        int monthSpacing = mMonthSpacing;
        int y = monthSpacing + (MONTH_LABEL_TEXT_SIZE / 2);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, mMonthTitlePaint);
    }

    /**
     * 查找从第一几个列开始绘制
     *
     * @return
     */
    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart) - mWeekStart;
    }

    /**
     * 获取年月的文字String
     *
     * @return
     */
    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        if (mOnDayClickListener != null && (isPrevDayEnabled || !((calendarDay.month == today.month) && (calendarDay.year == today.year) && calendarDay.day < today.monthDay))
                && (!sameDay(calendarDay.day, mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay) || (mSelectedBeginDay != -1 && mSelectedLastDay != -1))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }

    /**
     * 是否是同一天
     *
     * @param monthDay=
     * @param time
     * @return
     */
    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    /**
     * 是否是同一天
     * @param monthDay
     * @param compYear
     * @param compMonth
     * @param compDay
     * @return
     */
    private boolean sameDay(int monthDay, int compYear, int compMonth, int compDay) {
        return (mYear == compYear) && (mMonth == compMonth) && (monthDay == compDay);
    }

    /**
     * 是否是小于这一天
     *
     * @param monthDay
     * @param time
     * @return
     */
    private boolean prevDay(int monthDay, Time time) {
        this.time = time;
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) || (mMonth == time.month && monthDay < time.monthDay);
    }

    /**
     * 渲染日期 （单位：日）
     *
     * @param canvas
     */
    protected void drawMonthNums(Canvas canvas) {
        // 每日的Y间距 + (文字的大小 / 2) - xx + 头部的月份的高度
        int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
        // 每列开始x位置
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        //从第几列开始
        int dayOffset = findDayOffset();
        int day = 1;
        int textY = 0;
        boolean isSelectedDay;
        boolean isVacation;
        while (day <= mNumCells) {
            isSelectedDay = false;
            isVacation = false;
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            String daysVacation = CalendarUtils.getDaysHoliday(getContext(), mYear, mMonth + 1, day);
            textY = y;

            mMonthNumPaint.setColor(mDayNumColor);
            mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
            mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

            //选中了开始或者结束的任意一天则显示MonthTitleBgColor
            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                isSelectedDay = true;
            }

            //选中的开始日 != -1 && 选中的结束日 != -1 && 选中的开始年 == 选中的结束年 && 选中的开始日 == 选中的结束日 && 本日 == 选中的开始日 && 选中开始约 == 本月 && 选中的开始年 == 本年
            //如果是同时选择了开始和结束且在同一年中，开始和结束在同一天则显示mSeelctDaysColor
            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear &&
                    mSelectedBeginMonth == mSelectedLastMonth &&
                    mSelectedBeginDay == mSelectedLastDay &&
                    day == mSelectedBeginDay &&
                    mMonth == mSelectedBeginMonth &&
                    mYear == mSelectedBeginYear)) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
            }

            //是否是节假日
            if(!TextUtils.isEmpty(daysVacation) && !isSelectedDay) {
                mMonthNumPaint.setColor(mVacationDayColor);
                isVacation = true;
            }

            //处理开始和结束中间位置的渲染问题

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear == mYear) &&
                    (((mMonth == mSelectedBeginMonth && mSelectedLastMonth == mSelectedBeginMonth) && ((mSelectedBeginDay < mSelectedLastDay && day > mSelectedBeginDay && day < mSelectedLastDay) || (mSelectedBeginDay > mSelectedLastDay && day < mSelectedBeginDay && day > mSelectedLastDay))) ||
                            ((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                RectF rectF = new RectF(x - paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.f, 0.f, mSelectedContainsPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear && ((mSelectedBeginYear == mYear && mMonth == mSelectedBeginMonth) || (mSelectedLastYear == mYear && mMonth == mSelectedLastMonth)) &&
                    (((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay))))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                RectF rectF = new RectF(x - paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.f, 0.f, mSelectedContainsPaint);
            }

            //考虑跨月问题其中包括了开始大于结束月的情况下
            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mYear == mSelectedBeginYear) &&
                    ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth && mSelectedBeginMonth < mSelectedLastMonth) ||
                            (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth && mSelectedBeginMonth > mSelectedLastMonth))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                RectF rectF = new RectF(x - paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.f, 0.f, mSelectedContainsPaint);
            }

            //开始年小于结束年,
            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear) &&
                    ((mSelectedBeginYear < mSelectedLastYear && ((mMonth > mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth < mSelectedLastMonth && mYear == mSelectedLastYear))) ||
                            (mSelectedBeginYear > mSelectedLastYear && ((mMonth < mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth > mSelectedLastMonth && mYear == mSelectedLastYear))))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                RectF rectF = new RectF(x - paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.f, 0.f, mSelectedContainsPaint);
            }



            if (!isPrevDayEnabled && prevDay(day, today) && today.month == mMonth && today.year == mYear) {
                mMonthNumPaint.setColor(mPreviousDayColor);
            }

            // 选中时的
            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                if(mSelectedBeginDay != -1 && mSelectedLastDay != -1) {
                    int filter = CalendarUtils.filterDate(mYear, mMonth, day, mSelectedBeginYear, mSelectedBeginMonth, mSelectedBeginDay, mSelectedLastYear, mSelectedLastMonth, mSelectedLastDay);
                    RectF rectF;
                    if(filter == 1) {
                        rectF = new RectF(x, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                    } else {
                        rectF = new RectF(x - paddingDay, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                    }
                    canvas.drawRoundRect(rectF, 0.f, 0.f, mSelectedContainsPaint);
                }
                if (mDrawRect) {
                    RectF rectF = new RectF(x - DAY_SELECTED_CIRCLE_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + DAY_SELECTED_CIRCLE_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                    canvas.drawRoundRect(rectF, 10.0f, 10.0f, mSelectedCirclePaint);
                } else {
                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                }

            }

            if(isVacation) {
                mMonthNumPaint.setTextSize(VACATION_LABEL_TEXT_SIZE);
                canvas.drawText(daysVacation, x, textY, mMonthNumPaint);
            } else {
                canvas.drawText(String.format("%d", day), x, textY, mMonthNumPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        //点击渲染位置以外 则不返回NULL
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        return new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day, timeZone);
    }

    protected void initView() {
        /**
         * 绘制月份画笔
         */
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        /**
         * 选中时的文字画笔
         */
        mMonthTitleBGPaint = new Paint();
        mMonthTitleBGPaint.setFakeBoldText(true);
        mMonthTitleBGPaint.setAntiAlias(true);
        mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
        mMonthTitleBGPaint.setTextAlign(Align.CENTER);
        mMonthTitleBGPaint.setStyle(Style.FILL);

        /**
         *  选中时的文字背景画笔
         */
        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mSelectedDaysColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);

        /**
         *  开始和结束时的中间段的背景画笔
         */
        mSelectedContainsPaint = new Paint();
        mSelectedContainsPaint.setFakeBoldText(true);
        mSelectedContainsPaint.setAntiAlias(true);
        mSelectedContainsPaint.setColor(mSelectedContainColor);
        mSelectedContainsPaint.setTextAlign(Align.CENTER);
        mSelectedContainsPaint.setStyle(Style.FILL);

        /**
         *  绘制星期的画笔
         */
        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mDayWeekTextColor);
        mMonthDayLabelPaint.setStyle(Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(false);

        /**
         * 绘制日期的画笔（未选中）
         */
        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);

    }

    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                onDayClick(calendarDay);
            }
        }
        return true;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DAY)) {
            mSelectedLastDay = params.get(VIEW_PARAMS_SELECTED_LAST_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_MONTH)) {
            mSelectedLastMonth = params.get(VIEW_PARAMS_SELECTED_LAST_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_YEAR)) {
            mSelectedLastYear = params.get(VIEW_PARAMS_SELECTED_LAST_YEAR);
        }

        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            // 判断是否是今天
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }

            mIsPrev = prevDay(day, today);
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public static interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }
}