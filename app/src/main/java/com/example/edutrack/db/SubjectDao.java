package com.example.edutrack.db;

import androidx.room.*;
import java.util.List;

@Dao
public interface SubjectDao {
    @Insert void insert(Subject s);
    @Update void update(Subject s);
    @Delete void delete(Subject s);
    @Query("SELECT * FROM Subject") List<Subject> getAll();
}
