package com.leeeshuang.myfirstapp;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.leeeshuang.myfirstapp.model.UsageLog;
import com.leeeshuang.myfirstapp.service.DatabaseService;
import com.leeeshuang.myfirstapp.service.MPChartHelper;
import com.leeeshuang.myfirstapp.util.AppUtils;
import com.leeeshuang.myfirstapp.view.ScrollListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AppItem {
    public String name;
    public long duration;
    public String durationStr;
    public Drawable icon;
    public String packageName;
    int rate;

    AppItem(String name, long duration, Drawable icon, int rate, String packageName) {
        this.name = name;
        this.duration = duration;
        this.icon = icon;
        this.rate = rate;
        this.packageName = packageName;

        // NOTE: 在这里进行转换
        // duration 毫秒
        if(duration / 1000 < 60) {
            this.durationStr = "less than 1 min";
        } else {
            this.durationStr = (int) duration/1000/60 + " mins";
        }
    }
}

class AppListAdapter extends BaseAdapter {
    Context context;
    ArrayList<AppItem> data;
    private static LayoutInflater inflater = null;

    public AppListAdapter(Context context, ArrayList<AppItem> data) {
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
            vi = inflater.inflate(R.layout.app_item_view, null);
        }

        TextView nameText = vi.findViewById(R.id.appName);
        TextView durationText = vi.findViewById(R.id.duration);
        ImageView icon = vi.findViewById(R.id.icon);
        ProgressBar progressBar = vi.findViewById(R.id.progressBar);
        TextView packageNameText = vi.findViewById(R.id.packageName);

        nameText.setText(data.get(position).name);
        durationText.setText(data.get(position).durationStr);
        // NOTE: 图标
        icon.setImageDrawable(data.get(position).icon);
        progressBar.setProgress(data.get(position).rate);
        packageNameText.setText(data.get(position).packageName);
        return vi;
    }
}

public class HomeActivity extends AppCompatActivity {
    private int shownStatus;

    private final ArrayList<AppItem> appsDataArr = new ArrayList<>();

    private View dashboardContainer;
    private View overviewContainer;
    private TextView dashboardButton;
    private TextView overviewButton;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dashboardContainer = findViewById(R.id.dashboardContainer);
        overviewContainer = findViewById(R.id.overviewContainer);
        ScrollListView appListContainer = findViewById(R.id.appList);

        AppListAdapter appsAdapter = new AppListAdapter(this, appsDataArr);
        appListContainer.setAdapter(appsAdapter);

        // NOTE: initialize room db
        // DatabaseService.create(getApplicationContext());
        this.initData();
        this.drawAppList();
        this.drawCharts();

        dashboardButton = findViewById(R.id.dashboardBtn);
        overviewButton = findViewById(R.id.overviewBtn);

        this.initOverviewData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("SetTextI18n")
    public void toggleAppListView(View view) {
        final int DEFAULT_HEIGHT = 750;
        LinearLayout appListLayout = findViewById(R.id.appListLayout);
        TextView toggleBtnText = findViewById(R.id.toggleButton);

        if(appListLayout.getHeight() > DEFAULT_HEIGHT){
            appListLayout.setLayoutParams(new LinearLayout.LayoutParams(appListLayout.getWidth(), DEFAULT_HEIGHT));
            toggleBtnText.setText("show more");
        } else {
            appListLayout.setLayoutParams(new LinearLayout.LayoutParams(appListLayout.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT));
            toggleBtnText.setText("hide detail");
        }
    }

    @SuppressLint("ResourceAsColor")
    public void showDashboardContainer(View view) {
        if (shownStatus == 0) {
            return;
        }

        shownStatus = 0;
        dashboardButton.setTextColor(getColor(R.color.black));
        overviewButton.setTextColor(getColor(R.color.primary));

        overviewContainer.setVisibility(View.GONE);
        dashboardContainer.setAlpha(0f);
        dashboardContainer.setVisibility(View.VISIBLE);
        int ANIMATE_DURATION = 300;
        dashboardContainer.animate()
                .alpha(1f)
                .setDuration(ANIMATE_DURATION)
                .setListener(null);
    }

    @SuppressLint("ResourceAsColor")
    public void showOverviewContainer(View view) {
        if(shownStatus == 1) {
            return;
        }

        shownStatus = 1;
        overviewButton.setTextColor(getColor(R.color.black));
        dashboardButton.setTextColor(getColor(R.color.primary));

        dashboardContainer.setVisibility(View.GONE);
        overviewContainer.setAlpha(0f);
        overviewContainer.setVisibility(View.VISIBLE);
        int ANIMATE_DURATION = 300;
        overviewContainer.animate()
                .alpha(1f)
                .setDuration(ANIMATE_DURATION)
                .setListener(null);
    }

    public void appDetailBtnClickHandler(View view) {
        TextView t = view.findViewById(R.id.packageName);
        String pn = (String) t.getText();
        // app item clicked
        Intent intent = new Intent(this, AppUsageDetailActivity.class);
        intent.putExtra("PACKAGE_NAME", pn);

        startActivityForResult(intent, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initData(){
        // NOTE: 获取权限
//         Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//         startActivity(intent);

        List<UsageLog> usageLogs = new ArrayList<>();

        UsageStatsManager manager = (UsageStatsManager)getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, AppUtils.get7DaysAgoTimestamp(), System.currentTimeMillis());

        usageStatsList = usageStatsList.stream()
                .filter((UsageStats us) -> us.getTotalTimeInForeground() != 0 && !us.getPackageName().equals("com.leeeshuang.myfirstapp"))
                .collect(Collectors.toList());

        for(UsageStats us: usageStatsList){
            long duration = us.getTotalTimeInForeground();
            UsageLog ul = new UsageLog(us.getPackageName(), us.getLastTimeVisible(), duration);
            usageLogs.add(ul);
        }

        DatabaseService.setData(usageLogs);
    }

    private void drawAppList() {
        List<UsageLog> usageLogs = DatabaseService.getFrom(AppUtils.getDayTimestamp());

        PackageManager pm = getApplicationContext().getPackageManager();
        HashMap<String, Long> durationMap = new HashMap<>();

        for(UsageLog ul: usageLogs) {
            String name = ul.name;
            long duration = durationMap.containsKey(name) ? durationMap.get(name) : 0;
            durationMap.put(name, duration + ul.duration);
        }

        usageLogs.sort((u1, u2) -> {
            return (int) (u1.lastUsedAt - u2.lastUsedAt);
        });

        HashMap<String, UsageLog> map = new HashMap<String, UsageLog>();

        for (UsageLog ul: usageLogs) {
            map.put(ul.name, ul);
        }

        usageLogs = new ArrayList(map.values());

        for(UsageLog ul: usageLogs) {
            if(durationMap.containsKey(ul.name)){
                ul.duration = durationMap.get(ul.name);
            }
        }

        usageLogs = usageLogs.stream()
                .filter((UsageLog ul) -> ul.duration >= 1000*10)
                .collect(Collectors.toList());
        usageLogs.sort((u1, u2) -> (int) (u2.duration - u1.duration));

        long count = 0;

        // NOTE: display app list
        for(UsageLog ul: usageLogs) {
            try {
                ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(ul.name, 0);

                Drawable icon = pm.getApplicationIcon(appInfo);
                String name = (String) pm.getApplicationLabel(appInfo);
                int rate = 100;
                if(count == 0){
                    count = ul.duration;
                } else {
                    rate = (int) (((double) ul.duration / (double) count) * 100);

                    if(rate < 5){
                        rate = 5;
                    }
                }

                appsDataArr.add(new AppItem(name, ul.duration, icon, rate, ul.name));
            } catch (PackageManager.NameNotFoundException e) {
                // e.printStackTrace();
            }
        }
    }

    private void drawCharts() {
        drawLineChart();
        drawPieChart();
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
            List<UsageLog> usageLogs = DatabaseService.getFromTo(timeList.get(count), timeList.get(count+1));

            long amount = 0;
            for(UsageLog us: usageLogs){
                amount+=us.duration;
            }

            allDuration += amount;

            yLAxisValues.add((float)(amount / 1000 / 60));
        }

        yLAxisValues.add((float)(0));

        TextView allDurationTextView = findViewById(R.id.AllDataTitle);
        String allDurationText;
        allDuration = allDuration / 1000 / 60;
        if (allDuration < 60) {
            allDurationText = allDuration + " mins";
        } else {
            allDurationText = allDuration /60 + " hrs " + allDuration%60 + " mins";
        }

        allDurationTextView.setText(allDurationText);

        LineChart lineChart = findViewById(R.id.allDataLineChart);
        lineChart.setTouchEnabled(false);

        MPChartHelper.setLineChart(lineChart, xLAxisValues, yLAxisValues, "");
    }

    @SuppressLint("SetTextI18n")
    private void drawPieChart() {
        PieChart pieChart = findViewById(R.id.pieChart);

        List<UsageLog> usageLogs = DatabaseService.getFrom(AppUtils.getDayTimestamp());
        PackageManager pm = getApplicationContext().getPackageManager();
        HashMap<String, Integer> map = new HashMap<>();

        for(UsageLog ul: usageLogs) {
            try {
                ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(ul.name, 0);
                String name = (String) pm.getApplicationLabel(appInfo);

                int count = map.containsKey(name) ? map.get(name) : 0;
                map.put(name, count + 1);
            } catch (PackageManager.NameNotFoundException e) {
                // e.printStackTrace();
            }
        }

        TextView countTextView = findViewById(R.id.CountDataTitle);
        countTextView.setText("App Opened " + usageLogs.size() + " times");

        Map<String, Integer> pieValues = new LinkedHashMap<>();
        int count = 0;

        for (String i : map.keySet()) {
            pieValues.put(i, map.get(i));

            if(count++ >= 3){
                break;
            }
        }

        MPChartHelper.setPieChart(pieChart, pieValues,"",true);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initOverviewData() {
        this.initUsageRanking();
        this.initUsageDurationRanking();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initUsageRanking() {
        UsageStatsManager manager = (UsageStatsManager)getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
        List<UsageLog> usageLogs = new ArrayList<>();

        List<UsageStats> usageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, AppUtils.get7DaysAgoTimestamp(), System.currentTimeMillis());

        usageStatsList = usageStatsList.stream()
                .filter((UsageStats us) -> us.getTotalTimeInForeground() != 0 && !us.getPackageName().equals("com.leeeshuang.myfirstapp"))
                .collect(Collectors.toList());

        for(UsageStats us: usageStatsList){
            long duration = us.getTotalTimeInForeground();
            UsageLog ul = new UsageLog(us.getPackageName(), us.getLastTimeVisible(), duration);
            usageLogs.add(ul);
        }

        PackageManager pm = getApplicationContext().getPackageManager();
        HashMap<String, Long> durationMap = new HashMap<>();

        for(UsageLog ul: usageLogs) {
            String name = ul.name;
            long duration = durationMap.containsKey(name) ? durationMap.get(name) : 0;
            durationMap.put(name, duration + ul.duration);
        }

        usageLogs.sort((u1, u2) -> {
            return (int) (u1.lastUsedAt - u2.lastUsedAt);
        });

        HashMap<String, UsageLog> map = new HashMap<String, UsageLog>();

        for (UsageLog ul: usageLogs) {
            map.put(ul.name, ul);
        }

        usageLogs = new ArrayList(map.values());

        for(UsageLog ul: usageLogs) {
            if(durationMap.containsKey(ul.name)){
                ul.duration = durationMap.get(ul.name);
            }
        }

        usageLogs = usageLogs.stream()
                .filter((UsageLog ul) -> ul.duration >= 1000*10 && !ul.name.equals("com.leeeshuang.myfirstapp"))
                .collect(Collectors.toList());
        usageLogs.sort((u1, u2) -> (int) (u2.duration - u1.duration));

        int count = 1;
        // NOTE: display app list
        for(UsageLog ul: usageLogs) {
            try {
                ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(ul.name, 0);

                Drawable icon = pm.getApplicationIcon(appInfo);
                String name = (String) pm.getApplicationLabel(appInfo);
                
                LinearLayout container = findViewById(R.id.no1usedApp);
                switch (count) {
                    case 2: {
                        container = findViewById(R.id.no2usedApp);
                        break;
                    }
                    case 3: {
                        container = findViewById(R.id.no3usedApp);
                        break;
                    }
                }

                ImageView appIcon = container.findViewById(R.id.appIcon);
                TextView appName = container.findViewById(R.id.appName);
                TextView appDuration = container.findViewById(R.id.appDuration);

                appIcon.setImageDrawable(icon);
                appName.setText(name);

                int dayCount = (int) (ul.duration / 1000 / 3600 / 24);
                int hourCount = (int) (ul.duration / 1000 / 3600 % 24);
                int minCount = (int) (ul.duration / 1000 / 60 % 60);

                String durationText = (dayCount>0?dayCount+" days ":"") + (hourCount>0?hourCount+" hrs ":"") + (minCount>0?minCount+" mins":"");

                if (count == 1) {
                    durationText = "Has been running for " + durationText + "!";

                    TextView openItBtn = container.findViewById(R.id.openItBtn);

                    openItBtn.setOnClickListener(v -> {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(ul.name);

                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        }
                    });
                }
                appDuration.setText(durationText);
            } catch (PackageManager.NameNotFoundException e) {
                // e.printStackTrace();
            }

            count++;
            if(count > 3){
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initUsageDurationRanking() {
        UsageStatsManager manager = (UsageStatsManager)getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
        List<UsageLog> usageLogs = new ArrayList<>();

        List<UsageStats> usageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, AppUtils.get7DaysAgoTimestamp(), System.currentTimeMillis());

        usageStatsList = usageStatsList.stream()
                .filter((UsageStats us) -> us.getTotalTimeInForeground() != 0 && !us.getPackageName().equals("com.leeeshuang.myfirstapp"))
                .collect(Collectors.toList());

        for(UsageStats us: usageStatsList){
            long duration = us.getTotalTimeInForeground();
            UsageLog ul = new UsageLog(us.getPackageName(), us.getLastTimeVisible(), duration);
            usageLogs.add(ul);
        }

        HashMap<String, Long> durationMap = new HashMap<>();

        long allAmount = 0;

        for(UsageLog ul: usageLogs) {
            String name = ul.name;
            long duration = durationMap.containsKey(name) ? durationMap.get(name) : 0;
            durationMap.put(name, duration + ul.duration);
            allAmount += ul.duration;
        }

        String allDurationText = "";
        allAmount = allAmount / 1000 / 60 / 7;

        if (allAmount < 60) {
            allDurationText = allAmount + " mins";
        } else {
            allDurationText = allAmount / 60 % 24 + " hrs " + allAmount % 60 + " mins";
        }

        TextView dailyUsageText = findViewById(R.id.dailyUsageText);
        dailyUsageText.setText(allDurationText);

        usageLogs.sort((u1, u2) -> (int) (u1.lastUsedAt - u2.lastUsedAt));

        List<Integer> hoursList = new ArrayList<>();
        hoursList.add(0);
        hoursList.add(3);
        hoursList.add(6);
        hoursList.add(9);
        hoursList.add(12);
        hoursList.add(15);
        hoursList.add(18);
        hoursList.add(21);
        hoursList.add(24);

        List<Long> durationList = new ArrayList<>();

        for(int count = 0; count<hoursList.size()-1; count++){
            List<UsageLog> usageLogsTemp = DatabaseService.getFromToWithHour(hoursList.get(count), hoursList.get(count+1));

            long amount = 0;
            for(UsageLog us: usageLogsTemp){
                amount+=us.duration;
            }

            durationList.add(amount);
        }

        int maxIndex = 0;
        long maxValue = 0;
        for(int index = 0; index<durationList.size(); index++){
            if(durationList.get(index) > maxValue){
                maxIndex = index;
            }
        }

        String words;
        String suggestion;

        if(maxIndex > durationList.size() - 1){
            words = "23:00 - 01:00";
        } else {
            words = hoursList.get(maxIndex) + ":00 - " + hoursList.get(maxIndex + 1) + ":00";
        }

        if (hoursList.get(maxIndex) >= 9 && hoursList.get(maxIndex) <= 21) {
            suggestion = "Not bad, please keep it up";
        } else {
            suggestion = "Bad habit, please change it quickly";
        }

        TextView maxWordsEl = findViewById(R.id.maxDurationText);
        TextView suggestionEl = findViewById(R.id.suggestionText);

        maxWordsEl.setText(words);
        suggestionEl.setText(suggestion);
    }
}