package com.example.edutrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.edutrack.R;
import com.example.edutrack.adapters.SubjectAdapter;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Subject;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_attendance);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvSubjects);
        rv.setLayoutManager(new LinearLayoutManager(this));

        rv.setAdapter(new SubjectAdapter(
                AppDatabase.getInstance(this).subjectDao().getAll(),
                this
        ));

        findViewById(R.id.fabAddSubject).setOnClickListener(v ->
                startActivity(new Intent(this, AddSubjectActivity.class)));
        
        updateOverallAttendance();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateOverallAttendance();
    }
    
    private void updateOverallAttendance() {
        List<Subject> subjects = AppDatabase.getInstance(this).subjectDao().getAll();
        int totalClasses = 0;
        int attendedClasses = 0;
        
        for (Subject subject : subjects) {
            totalClasses += subject.total;
            attendedClasses += subject.attended;
        }
        
        TextView tvPercentage = findViewById(R.id.tvOverallPercentage);
        TextView tvPrediction = findViewById(R.id.tvPrediction);
        
        if (totalClasses == 0) {
            tvPercentage.setText("0%");
            tvPrediction.setText("No attendance data yet");
            return;
        }
        
        double percentage = (attendedClasses * 100.0) / totalClasses;
        tvPercentage.setText(String.format("%.1f%%", percentage));
        
        // Calculate 75% prediction
        if (percentage >= 75) {
            // Calculate how many classes can be skipped
            int canSkip = (int) Math.floor((attendedClasses - 0.75 * totalClasses) / 0.75);
            tvPrediction.setText("You can skip " + canSkip + " more classes to maintain 75%");
            tvPercentage.setTextColor(getColor(R.color.success));
        } else {
            // Calculate how many classes needed to attend
            int needed = (int) Math.ceil((0.75 * totalClasses - attendedClasses) / 0.25);
            tvPrediction.setText("Attend " + needed + " more consecutive classes to reach 75%");
            tvPercentage.setTextColor(getColor(R.color.error));
        }
    }
}
