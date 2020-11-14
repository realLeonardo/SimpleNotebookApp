package com.leeeshuang.myfirstapp.service;

import android.app.usage.UsageStats;
import android.content.Context;

import androidx.room.Room;

import com.leeeshuang.myfirstapp.model.UsageLog;
import com.leeeshuang.myfirstapp.util.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseService {
    public static AppDatabase db;
    public static List<UsageLog> usageLogs;

    public static AppDatabase create(final Context context){
        db = Room.databaseBuilder(
                context,
                AppDatabase.class,
                "my-roomdb")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        return db;
    }

    public static void setData(List<UsageLog> uls){
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : uls) {
            // NOTE: app filter
            if(p.name.equals("com.leeeshuang.myfirstapp")){
                continue;
            }
            usageLogsTemp.add(p.clone());
        }

        usageLogs = usageLogsTemp;
    }

    public static void insert(UsageLog usageLog){
        deleteSame(usageLog);
        db.usageLogDao().insert(usageLog);
    }

    public static void deleteSame(UsageLog usageLog) {
        List<UsageLog> list = getByPackageName(usageLog.name);

        String s = "" + (usageLog.lastUsedAt - usageLog.duration);
        for(UsageLog t: list){
            String q = "" + (t.lastUsedAt - t.duration);

            if(("" + (t.lastUsedAt - t.duration)).equals("" + (usageLog.lastUsedAt - usageLog.duration))){
                db.usageLogDao().deleteById(t.id);
            }
        }
    }

    public static List<UsageLog> getAll(){
        usageLogs = db.usageLogDao().getAll();

        return usageLogs;
    }

    public static void clearAll(){
        db.usageLogDao().clearAll();
    }

    public static List<UsageLog> getFrom(long timeStamp, boolean notSame){
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : usageLogs) {
            usageLogsTemp.add(p.clone());
        }

        usageLogsTemp = usageLogsTemp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= timeStamp)
                .collect(Collectors.toList());

        if(notSame){
            Collections.sort(usageLogsTemp, (u1, u2) -> {
                return (int) (u1.lastUsedAt - u2.lastUsedAt);
            });
            HashMap<String, UsageLog> map = new HashMap<String, UsageLog>();

            for (UsageLog ul: usageLogsTemp) {
                map.put(ul.name, ul);
            }

            usageLogsTemp = new ArrayList(map.values());
        }

        return usageLogsTemp;
    }

    public static List<UsageLog> getFromTo(long begin, long end){
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : usageLogs) {
            usageLogsTemp.add(p.clone());
        }

        usageLogsTemp = usageLogsTemp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= begin && ul.lastUsedAt <= end)
                .collect(Collectors.toList());

        return usageLogsTemp;
    }

    public static List<UsageLog> getFromToWithHour(int begin, int end) {
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : usageLogs) {
            usageLogsTemp.add(p.clone());
        }

        usageLogsTemp = usageLogsTemp.stream()
                .filter((UsageLog ul) -> (new Date(ul.lastUsedAt)).getHours() >= begin && (new Date(ul.lastUsedAt)).getHours() <= end)
                .collect(Collectors.toList());

        return usageLogsTemp;
    }

    public static List<UsageLog> getByPackageName(String name){
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : usageLogs) {
            usageLogsTemp.add(p.clone());
        }

        usageLogsTemp = usageLogsTemp.stream()
                .filter((UsageLog ul) -> ul.name.equals(name))
                .collect(Collectors.toList());

        return usageLogsTemp;
    }

    public static List<UsageLog> getFromToAndName(long begin, long end, String name){
        List<UsageLog> usageLogsTemp = new ArrayList<>();

        for(UsageLog p : usageLogs) {
            usageLogsTemp.add(p.clone());
        }

        usageLogsTemp = usageLogsTemp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= begin && ul.lastUsedAt <= end && ul.name.equals(name))
                .collect(Collectors.toList());

        return usageLogsTemp;
    }
}


