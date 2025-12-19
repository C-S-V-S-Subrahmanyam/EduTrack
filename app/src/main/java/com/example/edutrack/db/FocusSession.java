package com.example.edutrack.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FocusSession {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public long startTime;
    public long endTime;
    public int durationMinutes;
    public String sessionType; // "focus", "short_break", "long_break"
    public int taskId; // -1 if no task associated
    public String date; // Format: "2025-12-18"
    
    public FocusSession() {
        // Default constructor required by Room
    }
}
