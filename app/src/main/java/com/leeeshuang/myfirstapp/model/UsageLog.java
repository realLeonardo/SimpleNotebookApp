package com.leeeshuang.myfirstapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usagelogs")
public class UsageLog {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;
    // 应用名称
    @ColumnInfo(name="name")
    public String name;
    // 上次使用时间
    @ColumnInfo(name="last_used_at")
    public long lastUsedAt;
    // 使用总时间
    @ColumnInfo(name="duration")
    public long duration;

    public UsageLog(String name, long lastUsedAt, long duration){
        this.name = name;
        this.lastUsedAt = lastUsedAt;
        this.duration = duration;
    }
}
