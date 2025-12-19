package com.example.edutrack.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Subject;
import com.example.edutrack.db.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_profile);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        EditText etName = findViewById(R.id.etName);
        EditText etRoll = findViewById(R.id.etRoll);
        EditText etBranch = findViewById(R.id.etBranch);

        // Local storage (offline)
        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);
        etName.setText(sp.getString("name", ""));
        etRoll.setText(sp.getString("roll", ""));
        etBranch.setText(sp.getString("branch", ""));

        // Load statistics
        loadStatistics();

        findViewById(R.id.btnSave).setOnClickListener(v -> {

            // Save locally
            sp.edit()
                    .putString("name", etName.getText().toString())
                    .putString("roll", etRoll.getText().toString())
                    .putString("branch", etBranch.getText().toString())
                    .apply();

            // Save to Firebase Realtime Database
            String uid = FirebaseAuth.getInstance().getUid();

            if (uid != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("name", etName.getText().toString());
                profile.put("roll", etRoll.getText().toString());
                profile.put("branch", etBranch.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("students")
                        .child(uid)
                        .child("profile")
                        .setValue(profile);
            }

            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        TextView tvAttendance = findViewById(R.id.tvStatsAttendance);
        TextView tvSubjects = findViewById(R.id.tvStatsSubjects);
        TextView tvTasksCompleted = findViewById(R.id.tvStatsTasksCompleted);
        TextView tvTasksTotal = findViewById(R.id.tvStatsTasksTotal);

        // Calculate overall attendance
        List<Subject> subjects = AppDatabase.getInstance(this).subjectDao().getAll();
        int totalClasses = 0;
        int attendedClasses = 0;

        for (Subject subject : subjects) {
            totalClasses += subject.total;
            attendedClasses += subject.attended;
        }

        if (totalClasses > 0) {
            double percentage = (attendedClasses * 100.0) / totalClasses;
            tvAttendance.setText(String.format("%.1f%%", percentage));
            if (percentage >= 75) {
                tvAttendance.setTextColor(getColor(R.color.success));
            } else {
                tvAttendance.setTextColor(getColor(R.color.error));
            }
        } else {
            tvAttendance.setText("0%");
        }

        // Total subjects
        tvSubjects.setText(String.valueOf(subjects.size()));

        // Tasks statistics
        List<Task> tasks = AppDatabase.getInstance(this).taskDao().getAll();
        int completedTasks = 0;
        for (Task task : tasks) {
            if (task.isCompleted) {
                completedTasks++;
            }
        }

        tvTasksCompleted.setText(String.valueOf(completedTasks));
        tvTasksTotal.setText(String.valueOf(tasks.size()));
    }
}
