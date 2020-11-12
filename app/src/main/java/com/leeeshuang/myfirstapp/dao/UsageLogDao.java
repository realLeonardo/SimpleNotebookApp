package com.leeeshuang.myfirstapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.leeeshuang.myfirstapp.model.UsageLog;

import java.util.List;

@Dao
public interface UsageLogDao {
    @Query("SELECT * FROM usagelogs")
    List<UsageLog> getAll();

    @Query("SELECT * FROM usagelogs WHERE id IN (:userIds)")
    List<UsageLog> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM usagelogs WHERE name LIKE :first")
//    UsageLog findByName(String first, String last);

    @Query("DELETE FROM usagelogs")
    public void clearAll();

    @Insert
    void insertAll(UsageLog... usageLogs);

    @Insert
    void insert(UsageLog usageLog);
}
