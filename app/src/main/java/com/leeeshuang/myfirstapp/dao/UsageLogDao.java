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

    @Query("DELETE FROM usagelogs")
    void clearAll();

    @Query("DELETE FROM usagelogs WHERE id=(:id)")
    void deleteById(int id);

    @Insert
    void insert(UsageLog usageLog);
}
