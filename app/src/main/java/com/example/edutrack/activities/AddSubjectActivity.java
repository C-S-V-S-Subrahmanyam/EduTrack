package com.example.edutrack.activities;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Subject;

public class AddSubjectActivity extends AppCompatActivity {

    EditText etSubjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etSubjectName = findViewById(R.id.etSubjectName);

        findViewById(R.id.btnSaveSubject).setOnClickListener(v -> saveSubject());
    }

    private void saveSubject() {
        String name = etSubjectName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter subject name", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject subject = new Subject();
        subject.name = name;
        subject.total = 0;
        subject.attended = 0;

        AppDatabase.getInstance(this).subjectDao().insert(subject);
        Toast.makeText(this, "Subject added successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
