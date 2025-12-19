package com.example.edutrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.edutrack.R;
import com.example.edutrack.adapters.TaskAdapter;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {
    private TaskAdapter adapter;
    private TextView tvTaskStats;
    private TextView tvToggleLabel;
    
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_task_list);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTaskStats = findViewById(R.id.tvTaskStats);
        tvToggleLabel = findViewById(R.id.tvToggleLabel);
        SwitchMaterial switchShowCompleted = findViewById(R.id.switchShowCompleted);

        RecyclerView rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Task> tasks = AppDatabase.getInstance(this).taskDao().getAll();
        adapter = new TaskAdapter(tasks, this);
        rv.setAdapter(adapter);
        
        updateTaskStats(tasks);
        
        // Load tasks from Firebase for cross-device sync
        loadTasksFromFirebase();

        // Toggle switch listener
        switchShowCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.toggleShowCompleted();
            if (isChecked) {
                tvToggleLabel.setText("Hide Completed");
            } else {
                tvToggleLabel.setText("Show Completed");
            }
            updateTaskStats(AppDatabase.getInstance(this).taskDao().getAll());
        });

        findViewById(R.id.fabAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, AddTaskActivity.class)));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView rv = findViewById(R.id.rvTasks);
        List<Task> tasks = AppDatabase.getInstance(this).taskDao().getAll();
        adapter = new TaskAdapter(tasks, this);
        rv.setAdapter(adapter);
        updateTaskStats(tasks);
    }
    
    private void loadTasksFromFirebase() {
        try {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .child("tasks")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> firebaseTasks = new ArrayList<>();
                        AppDatabase db = AppDatabase.getInstance(TaskListActivity.this);
                        
                        for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                            try {
                                int id = taskSnapshot.child("id").getValue(Integer.class);
                                String title = taskSnapshot.child("title").getValue(String.class);
                                String description = taskSnapshot.child("description").getValue(String.class);
                                String dueDate = taskSnapshot.child("dueDate").getValue(String.class);
                                String priority = taskSnapshot.child("priority").getValue(String.class);
                                Long time = taskSnapshot.child("time").getValue(Long.class);
                                Boolean isCompleted = taskSnapshot.child("isCompleted").getValue(Boolean.class);
                                Boolean hasAlarm = taskSnapshot.child("hasAlarm").getValue(Boolean.class);
                                
                                // Check if task already exists in local database
                                Task existingTask = db.taskDao().getById(id);
                                if (existingTask == null) {
                                    // Insert new task from Firebase
                                    Task newTask = new Task();
                                    newTask.id = id;
                                    newTask.title = title;
                                    newTask.description = description;
                                    newTask.dueDate = dueDate;
                                    newTask.priority = priority;
                                    newTask.time = time != null ? time : System.currentTimeMillis();
                                    newTask.isCompleted = isCompleted != null ? isCompleted : false;
                                    newTask.hasAlarm = hasAlarm != null ? hasAlarm : false;
                                    db.taskDao().insert(newTask);
                                } else {
                                    // Update existing task with Firebase data
                                    existingTask.isCompleted = isCompleted != null ? isCompleted : false;
                                    db.taskDao().update(existingTask);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        // Refresh the task list
                        runOnUiThread(() -> {
                            List<Task> tasks = db.taskDao().getAll();
                            adapter = new TaskAdapter(tasks, TaskListActivity.this);
                            RecyclerView rv = findViewById(R.id.rvTasks);
                            rv.setAdapter(adapter);
                            updateTaskStats(tasks);
                        });
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        error.toException().printStackTrace();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateTaskStats(List<Task> tasks) {
        int pending = 0;
        int completed = 0;
        
        for (Task task : tasks) {
            if (task.isCompleted) {
                completed++;
            } else {
                pending++;
            }
        }
        
        tvTaskStats.setText(pending + " pending • " + completed + " completed");
    }
}
