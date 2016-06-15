package com.andexert.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class MainActivity extends Activity {

    private SimpleCalendarFilterView mCalendarFilterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCalendarFilterView = (SimpleCalendarFilterView) findViewById(R.id.main_content_calendar);
        //这边节假日数据采用本地解析的方式，如果大家有什么好的建议可以提出，一起交流。多谢。~
        mCalendarFilterView.setHoliday(toHolidayMap(this));
    }

    /**
     * 解析节假日文件
     * @param context
     * @throws IOException
     * @throws JSONException
     */
    private LinkedHashMap<String, String> toHolidayMap(Context context) {
        LinkedHashMap<String, String> holidayMap = new LinkedHashMap<>();

        InputStream inputStream = null;
        JSONObject json = null;
        try {
            inputStream = context.getResources().getAssets().open("Date.json");
            byte[] buf = new byte[inputStream.available()];
            inputStream.read(buf);
            String content = new String(buf);
            json = new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        for(Iterator<String> iterator = json.keys(); iterator.hasNext();) {
            String key = iterator.next().trim();
            String values = json.optString(key);
            if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(values)) {
                holidayMap.put(key, values);
            }
        }

        return holidayMap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
