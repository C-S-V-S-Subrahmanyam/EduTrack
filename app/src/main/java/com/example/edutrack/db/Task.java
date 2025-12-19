package com.example.edutrack.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public long time;
    public String dueDate;
    public String priority;
    public boolean isCompleted;
    public boolean hasAlarm;
    
    public Task() {
        super();
        // Default constructor required by Room
        this.isCompleted = false;
        this.hasAlarm = false;
    }
}
