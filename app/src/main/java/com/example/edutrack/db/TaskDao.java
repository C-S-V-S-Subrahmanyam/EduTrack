package com.example.edutrack.db;

import androidx.room.*;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert void insert(Task t);
    @Update void update(Task t);
    @Delete void delete(Task t);
    @Query("SELECT * FROM Task") List<Task> getAll();
    @Query("SELECT * FROM Task WHERE id = :taskId") Task getById(int taskId);
}
