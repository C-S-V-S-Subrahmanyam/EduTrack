package com.example.edutrack.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {
    @Insert
    void insert(FocusSession session);
    
    @Query("SELECT * FROM FocusSession ORDER BY startTime DESC")
    List<FocusSession> getAll();
    
    @Query("SELECT * FROM FocusSession WHERE date = :date")
    List<FocusSession> getSessionsByDate(String date);
    
    @Query("SELECT * FROM FocusSession WHERE taskId = :taskId")
    List<FocusSession> getSessionsByTask(int taskId);
    
    @Query("SELECT SUM(durationMinutes) FROM FocusSession WHERE date = :date AND sessionType = 'focus'")
    int getTotalFocusTimeByDate(String date);
    
    @Query("SELECT SUM(durationMinutes) FROM FocusSession WHERE sessionType = 'focus'")
    int getTotalFocusTime();
}
