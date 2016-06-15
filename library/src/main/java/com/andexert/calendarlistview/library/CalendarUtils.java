/***********************************************************************************
 * The MIT License (MIT)

 * Copyright (c) 2014 Robin Chutaux

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class CalendarUtils {

    public static Map<String, String> holidayMap;
    public static final String[] DEFAULT_WEEK_DAYS = new String[]{"日","一","二","三", "四","五","六"};

	public static int getDaysInMonth(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
	}


    /**
     * 是否节假日
     * @param context
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String getDaysHoliday(Context context, int year, int month, int day) {
        if(holidayMap == null || holidayMap.size() <= 0) {
            return null;
        }

        Integer key = year * 10000 + month * 100 + day;
        String value = null;
        if(holidayMap.containsKey(key.toString())) {
            value = holidayMap.get(key.toString());
        }
        return value;
    }


    /**
     * 返回是入住还是离店
     * @param currentYear                   当前年
     * @param currentMonth                  当前月
     * @param currentDay                    当前日
     * @param selectBeginYear               选择开始的年
     * @param selectBeginMonth              选择开始的月
     * @param selectBeginDay                选择开始日
     * @param selectLastYear                选择结束的年
     * @param selectLastMonth               选择结束的月
     * @param selectLastDay                 选择结束的日
     * @return      1 = 入住 ，0 = 离店， -1 = 没有匹配到
     */
    public static int filterDate(int currentYear, int currentMonth, int currentDay,
                                     int selectBeginYear, int selectBeginMonth, int selectBeginDay,
                                     int selectLastYear, int selectLastMonth, int selectLastDay) {
        if(selectBeginYear == -1 && selectBeginMonth == -1 && selectBeginDay == -1 ) {
            return -1;
        }

        if((selectBeginYear != -1 && selectBeginMonth != -1 && selectBeginDay != -1) && (selectLastYear == -1 && selectLastMonth == -1 && selectLastDay == -1)) {
            return 1;
        }

        int currentComp = currentYear * 3000 + currentMonth * 100 + currentDay;
        int beginComp = selectBeginYear * 3000 + selectBeginMonth * 100 + selectBeginDay;
        int lastComp = selectLastYear * 3000 + selectLastMonth * 100 + selectLastDay;

        if(beginComp < lastComp) {
            if(currentComp == beginComp) {
                return 1;
            } else if(currentComp == lastComp) {
                return 0;
            }
        } else{
            if(currentComp == beginComp) {
                return 0;
            } else if(currentComp == lastComp) {
                return 1;
            }
        }
        return -1;
    }

    /**
     * 比较时间大小
     * @param date
     * @param oldDate
     * @return
     */
    public static int compareDate(Date date, Date oldDate) {
        if(oldDate.after(date)) {
            return 1;
        } else if(oldDate.equals(date)){
            return 0;
        } else {
            return -1;
        }
    }
}
