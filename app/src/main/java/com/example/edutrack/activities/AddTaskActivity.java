package com.example.edutrack.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.AlarmReceiver;
import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    EditText etTitle, etDesc;
    Button btnDate, btnTime;
    Spinner spPriority;
    String selectedDate = "";
    String selectedTime = "";
    Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTaskTitle);
        etDesc = findViewById(R.id.etTaskDesc);
        btnDate = findViewById(R.id.btnPickDate);
        btnTime = findViewById(R.id.btnPickTime);
        spPriority = findViewById(R.id.spPriority);

        // Setup priority spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Low", "Medium", "High"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate = day + "/" + (month + 1) + "/" + year;
                    btnDate.setText("Date: " + selectedDate);
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hour, minute) -> {
                    selectedTime = hour + ":" + (minute < 10 ? "0" + minute : minute);
                    btnTime.setText("Time: " + selectedTime);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    selectedCalendar.set(Calendar.MINUTE, minute);
                    selectedCalendar.set(Calendar.SECOND, 0);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter task title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task();
        task.title = title;
        task.description = desc;
        task.dueDate = selectedDate + " " + selectedTime;
        task.priority = spPriority.getSelectedItem().toString();
        task.time = System.currentTimeMillis();
        task.isCompleted = false;
        task.hasAlarm = true;

        AppDatabase.getInstance(this).taskDao().insert(task);
        
        // Get the task ID after insertion
        List<Task> allTasks = AppDatabase.getInstance(this).taskDao().getAll();
        int taskId = allTasks.get(allTasks.size() - 1).id;
        
        // Save to Firebase
        saveTaskToFirebase(taskId, task);
        
        // Schedule alarm (30 minutes before due time)
        scheduleAlarm(taskId, title, desc);
        
        // Schedule recurring alarm for after due time (if task not completed)
        scheduleRecurringAlarm(taskId, title, desc);
        
        Toast.makeText(this, "Task added with reminders", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void scheduleRecurringAlarm(int taskId, String title, String desc) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("taskTitle", title);
        intent.putExtra("taskDesc", desc);
        intent.putExtra("taskId", taskId);
        intent.putExtra("isRecurring", true);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                taskId + 10000, // Different ID for recurring alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Start email reminder 1 minute after due time
        long firstAlarmTime = selectedCalendar.getTimeInMillis() + (1 * 60 * 1000);
        
        if (firstAlarmTime > System.currentTimeMillis()) {
            try {
                // One-time email reminder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstAlarmTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, firstAlarmTime, pendingIntent);
                }
            } catch (SecurityException e) {
                // Silent fail
            }
        }
    }
    
    private void scheduleAlarm(int taskId, String title, String desc) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("taskTitle", title);
        intent.putExtra("taskDesc", desc);
        intent.putExtra("taskId", taskId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Set alarm 5 minutes before the due time
        long alarmTime = selectedCalendar.getTimeInMillis() - (5 * 60 * 1000);
        
        // Only schedule if the time is in the future
        if (alarmTime > System.currentTimeMillis()) {
            try {
                // Use setExactAndAllowWhileIdle for better reliability even when device is in Doze mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                }
                Toast.makeText(this, "Reminder set for 5 min before due time", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(this, "Please enable alarm permission in settings", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Task time is in the past, no reminder set", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveTaskToFirebase(int taskId, Task task) {
        try {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("id", taskId);
            taskData.put("title", task.title);
            taskData.put("description", task.description);
            taskData.put("dueDate", task.dueDate);
            taskData.put("priority", task.priority);
            taskData.put("time", task.time);
            taskData.put("isCompleted", task.isCompleted);
            taskData.put("hasAlarm", task.hasAlarm);
            
            FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .child("tasks")
                .child(String.valueOf(taskId))
                .setValue(taskData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
