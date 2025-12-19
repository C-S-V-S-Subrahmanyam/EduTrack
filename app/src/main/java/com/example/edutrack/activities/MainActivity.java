package com.example.edutrack.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edutrack.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.btnAttendance).setOnClickListener(v ->
                startActivity(new Intent(this, AttendanceActivity.class)));

        findViewById(R.id.btnTasks).setOnClickListener(v ->
                startActivity(new Intent(this, TaskListActivity.class)));
        
        findViewById(R.id.btnFocusMode).setOnClickListener(v ->
                startActivity(new Intent(this, FocusModeActivity.class)));
        
        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
