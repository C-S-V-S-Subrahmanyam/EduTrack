package com.example.edutrack.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Subject {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int total;
    public int attended;
    
    public Subject() {
        super();
        // Default constructor required by Room
    }
}
