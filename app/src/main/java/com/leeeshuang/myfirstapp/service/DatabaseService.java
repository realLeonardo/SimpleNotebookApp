package com.leeeshuang.myfirstapp.service;

import android.content.Context;

import androidx.room.Room;

import com.leeeshuang.myfirstapp.model.UsageLog;

import java.util.ArrayList;
import java.util.Collections;
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

    public static void insert(UsageLog usageLog){
        db.usageLogDao().insert(usageLog);
    }

    public static void insertAll(UsageLog... usageLogs){
        db.usageLogDao().insertAll(usageLogs);
    }

    public static List<UsageLog> getAll(){
        if(usageLogs == null){
            usageLogs = db.usageLogDao().getAll();
        }

        return usageLogs;
    }

    public static void clearAll(){
        db.usageLogDao().clearAll();
    }

    public static List<UsageLog> getFrom(long timeStamp, boolean notSame){
        List<UsageLog> temp = usageLogs;

        temp = temp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= timeStamp)
                .collect(Collectors.toList());

        if(notSame){
            Collections.sort(temp, (u1, u2) -> {
                return (int) (u1.lastUsedAt - u2.lastUsedAt);
            });
            HashMap<String, UsageLog> map = new HashMap<String, UsageLog>();

            for (UsageLog ul: temp) {
                map.put(ul.name, ul);
            }

            temp = new ArrayList(map.values());
        }

        return temp;
    }

    public static List<UsageLog> getFromTo(long begin, long end){
        List<UsageLog> temp = usageLogs;

        temp = temp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= begin && ul.lastUsedAt <= end)
                .collect(Collectors.toList());

        return temp;
    }

    public static List<UsageLog> getByPackageName(String name){
        List<UsageLog> temp = usageLogs;

        temp = temp.stream()
                .filter((UsageLog ul) -> ul.name.equals(name))
                .collect(Collectors.toList());

        return temp;
    }

    public static List<UsageLog> getFromToAndName(long begin, long end, String name){
        List<UsageLog> temp = usageLogs;

        temp = temp.stream()
                .filter((UsageLog ul) -> ul.lastUsedAt >= begin && ul.lastUsedAt <= end && ul.name.equals(name))
                .collect(Collectors.toList());

        return temp;
    }
}


