package com.leeeshuang.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.leeeshuang.myfirstapp.model.UsageLog;
import com.leeeshuang.myfirstapp.service.DatabaseService;
import com.leeeshuang.myfirstapp.service.MPChartHelper;
import com.leeeshuang.myfirstapp.util.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class AppLogItem {
    public long duration;
    public long startAt;
    public String str;

    AppLogItem(long duration, long startAt) {
        this.duration = duration;
        this.startAt = startAt;

        // NOTE: 在这里进行转换
        // duration 毫秒
        String durationStr;
        if(duration / 1000 < 60) {
            durationStr = "less than 1 min";
        } else {
            durationStr = (int) duration/1000/60 + " mins";
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dateStr = dateFormat.format(new Date(this.startAt));

        this.str = "Run " + durationStr + ", in " + dateStr;
    }
}

class AppLogListAdapter extends BaseAdapter {

    Context context;
    ArrayList<AppLogItem> data;
    private static LayoutInflater inflater = null;

    public AppLogListAdapter(Context context, ArrayList<AppLogItem> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null){
            vi = inflater.inflate(R.layout.app_log_item_view, null);
        }

        TextView nameText = vi.findViewById(R.id.duration);

        nameText.setText(data.get(position).str);
        return vi;
    }
}


public class AppUsageDetailActivity extends AppCompatActivity {
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_detail);

        Intent intent = getIntent();

        // Default value is -1, means from create button
        packageName = intent.getStringExtra("PACKAGE_NAME");

        PackageManager pm = getApplicationContext().getPackageManager();
        // NOTE: display app list
        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(packageName, 0);
            Drawable icon = pm.getApplicationIcon(appInfo);
            String name = (String) pm.getApplicationLabel(appInfo);

            ImageView iconEl = findViewById(R.id.appIcon);
            TextView appNameEl = findViewById(R.id.appName);

            appNameEl.setText(name);
            iconEl.setImageDrawable(icon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initData();
    }

    private void initData() {
        final ArrayList<AppLogItem> appsDataArr = new ArrayList<>();

        AppLogListAdapter appsAdapter;
        ListView lv = findViewById(R.id.usageList);

        appsAdapter = new AppLogListAdapter(this, appsDataArr);
        lv.setAdapter(appsAdapter);

        List<UsageLog> usageLogs = DatabaseService.getByPackageName(packageName);

        usageLogs.sort((u1, u2) -> (int) (u2.lastUsedAt - u1.lastUsedAt));

        for(UsageLog ul: usageLogs){
            appsDataArr.add(new AppLogItem(ul.duration, ul.lastUsedAt));
        }

        drawLineChart();
    }

    private void drawLineChart() {
        List<String> xLAxisValues = new ArrayList<>();
        List<Float> yLAxisValues = new ArrayList<>();

        // 横坐标
        xLAxisValues.add("00:00");
        xLAxisValues.add("");
        xLAxisValues.add("");
        xLAxisValues.add("06:00");
        xLAxisValues.add("");
        xLAxisValues.add("");
        xLAxisValues.add("12:00");
        xLAxisValues.add("");
        xLAxisValues.add("");
        xLAxisValues.add("18:00");
        xLAxisValues.add("");
        xLAxisValues.add("");
        xLAxisValues.add("23:00");

        List<Long> timeList = AppUtils.getDayTimestamps();

        int allDuration = 0;
        int count = 0;
        for(; count<timeList.size()-1; count++){
            List<UsageLog> usageLogs = DatabaseService.getFromToAndName(timeList.get(count), timeList.get(count+1), packageName);

            long amount = 0;
            for(UsageLog us: usageLogs){
                amount+=us.duration;
            }

            allDuration += amount;

            yLAxisValues.add((float)(amount / 1000 / 60));
        }

        yLAxisValues.add((float)(0));

        TextView allDurationTextView = findViewById(R.id.duration);
        String allDurationText;
        allDuration = allDuration / 1000 / 60;
        if(allDuration<60){
            allDurationText = allDuration + " mins";
        }else {
            allDurationText = allDuration /60 + " hrs " + allDuration%60 + " mins";
        }

        allDurationTextView.setText(allDurationText);

        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.setTouchEnabled(false);

        MPChartHelper.setLineChart(lineChart, xLAxisValues, yLAxisValues, "");
    }

    // NOTE: back button clicked
    public void backButtonClickHandler(View view) {
        finish();
    }
}