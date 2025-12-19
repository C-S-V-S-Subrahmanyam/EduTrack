package com.example.edutrack.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edutrack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etRollNumber, etBranch, etEmail, etPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etRollNumber = findViewById(R.id.etRollNumber);
        etBranch = findViewById(R.id.etBranch);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String rollNumber = etRollNumber.getText().toString().trim();
            String branch = etBranch.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (name.isEmpty() || rollNumber.isEmpty() || branch.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (pass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {

                        String uid = auth.getCurrentUser().getUid();

                        // Store user info in Realtime DB
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email);
                        userMap.put("name", name);
                        userMap.put("rollNumber", rollNumber);
                        userMap.put("branch", branch);

                        FirebaseDatabase.getInstance()
                                .getReference("students")
                                .child(uid)
                                .setValue(userMap);

                        // Also save locally
                        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);
                        sp.edit()
                                .putString("name", name)
                                .putString("roll", rollNumber)
                                .putString("branch", branch)
                                .apply();

                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
        
        // Add click listener for tvLogin to navigate back to login
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }
}
