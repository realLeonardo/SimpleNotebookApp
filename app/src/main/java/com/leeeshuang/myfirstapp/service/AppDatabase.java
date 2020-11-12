package com.leeeshuang.myfirstapp.service;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.leeeshuang.myfirstapp.dao.UsageLogDao;
import com.leeeshuang.myfirstapp.model.UsageLog;

@Database(entities = {UsageLog.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsageLogDao usageLogDao();
}


