package com.example.edutrack.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.FocusSession;
import com.example.edutrack.db.Task;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FocusModeActivity extends AppCompatActivity {

    private TextView tvTimer, tvSessionType, tvSessionCount, tvTodayFocus, tvTotalFocus;
    private MaterialButton btnStartPause, btnReset, btnSelectTask;
    private ProgressBar progressBar;
    
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 minutes default
    private boolean isTimerRunning = false;
    private String currentSessionType = "focus"; // focus, short_break, long_break
    private int pomodoroCount = 0;
    private long sessionStartTime = 0;
    
    private int selectedTaskId = -1;
    private String selectedTaskTitle = "No task selected";
    
    // Pomodoro settings
    private static final long FOCUS_TIME = 25 * 60 * 1000; // 25 minutes
    private static final long SHORT_BREAK = 5 * 60 * 1000; // 5 minutes
    private static final long LONG_BREAK = 15 * 60 * 1000; // 15 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTimer = findViewById(R.id.tvTimer);
        tvSessionType = findViewById(R.id.tvSessionType);
        tvSessionCount = findViewById(R.id.tvSessionCount);
        tvTodayFocus = findViewById(R.id.tvTodayFocus);
        tvTotalFocus = findViewById(R.id.tvTotalFocus);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnSelectTask = findViewById(R.id.btnSelectTask);
        progressBar = findViewById(R.id.progressBar);

        updateTimerText();
        updateSessionTypeDisplay();
        loadStatistics();

        btnStartPause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());
        
        btnSelectTask.setOnClickListener(v -> showTaskSelector());
        
        updateProgressBar();
    }

    private void showTaskSelector() {
        List<Task> allTasks = AppDatabase.getInstance(this).taskDao().getAll();
        
        // Filter only incomplete tasks
        List<Task> incompleteTasks = new java.util.ArrayList<>();
        for (Task task : allTasks) {
            if (!task.isCompleted) {
                incompleteTasks.add(task);
            }
        }
        
        // Create task titles array
        String[] taskTitles = new String[incompleteTasks.size() + 1];
        taskTitles[0] = "No task (Free study)";
        
        for (int i = 0; i < incompleteTasks.size(); i++) {
            taskTitles[i + 1] = incompleteTasks.get(i).title;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Task to Focus On");
        builder.setItems(taskTitles, (dialog, which) -> {
            if (which == 0) {
                selectedTaskId = -1;
                selectedTaskTitle = "Free study";
            } else {
                selectedTaskId = incompleteTasks.get(which - 1).id;
                selectedTaskTitle = incompleteTasks.get(which - 1).title;
            }
            btnSelectTask.setText(selectedTaskTitle);
        });
        builder.show();
    }

    private void startTimer() {
        sessionStartTime = System.currentTimeMillis();
        
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnStartPause.setText("START");
                btnStartPause.setIcon(getDrawable(android.R.drawable.ic_media_play));
                
                // Save session
                saveSession();
                
                // Show completion dialog
                showSessionCompleteDialog();
            }
        }.start();

        isTimerRunning = true;
        btnStartPause.setText("PAUSE");
        btnStartPause.setIcon(getDrawable(android.R.drawable.ic_media_pause));
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnStartPause.setText("START");
        btnStartPause.setIcon(getDrawable(android.R.drawable.ic_media_play));
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        
        if (currentSessionType.equals("focus")) {
            timeLeftInMillis = FOCUS_TIME;
        } else if (currentSessionType.equals("short_break")) {
            timeLeftInMillis = SHORT_BREAK;
        } else {
            timeLeftInMillis = LONG_BREAK;
        }
        
        updateTimerText();
        updateProgressBar();
        btnStartPause.setText("START");
        btnStartPause.setIcon(getDrawable(android.R.drawable.ic_media_play));
    }

    private void saveSession() {
        long endTime = System.currentTimeMillis();
        int durationMinutes = (int) ((endTime - sessionStartTime) / 1000 / 60);
        
        FocusSession session = new FocusSession();
        session.startTime = sessionStartTime;
        session.endTime = endTime;
        session.durationMinutes = durationMinutes;
        session.sessionType = currentSessionType;
        session.taskId = selectedTaskId;
        session.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        AppDatabase.getInstance(this).focusSessionDao().insert(session);
        
        if (currentSessionType.equals("focus")) {
            pomodoroCount++;
            tvSessionCount.setText("Pomodoros: " + pomodoroCount);
        }
        
        loadStatistics();
    }

    private void showSessionCompleteDialog() {
        String message;
        if (currentSessionType.equals("focus")) {
            message = "🎉 Great job! You completed a focus session!\n\nTime for a ";
            if (pomodoroCount % 4 == 0) {
                message += "long break (15 min)";
                currentSessionType = "long_break";
                timeLeftInMillis = LONG_BREAK;
            } else {
                message += "short break (5 min)";
                currentSessionType = "short_break";
                timeLeftInMillis = SHORT_BREAK;
            }
        } else {
            message = "✨ Break time over!\n\nReady for another focus session?";
            currentSessionType = "focus";
            timeLeftInMillis = FOCUS_TIME;
        }
        
        updateSessionTypeDisplay();
        updateTimerText();
        updateProgressBar();
        
        new AlertDialog.Builder(this)
            .setTitle("Session Complete!")
            .setMessage(message)
            .setPositiveButton("Start Next", (dialog, which) -> startTimer())
            .setNegativeButton("Later", null)
            .show();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
    }

    private void updateSessionTypeDisplay() {
        switch (currentSessionType) {
            case "focus":
                tvSessionType.setText("🎯 FOCUS MODE");
                break;
            case "short_break":
                tvSessionType.setText("☕ SHORT BREAK");
                break;
            case "long_break":
                tvSessionType.setText("🌴 LONG BREAK");
                break;
        }
    }

    private void updateProgressBar() {
        long totalTime = currentSessionType.equals("focus") ? FOCUS_TIME :
                        currentSessionType.equals("short_break") ? SHORT_BREAK : LONG_BREAK;
        int progress = (int) ((totalTime - timeLeftInMillis) * 100 / totalTime);
        progressBar.setProgress(progress);
    }

    private void loadStatistics() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        Integer todayMinutes = AppDatabase.getInstance(this).focusSessionDao().getTotalFocusTimeByDate(today);
        Integer totalMinutes = AppDatabase.getInstance(this).focusSessionDao().getTotalFocusTime();
        
        int todayTime = todayMinutes != null ? todayMinutes : 0;
        int totalTime = totalMinutes != null ? totalMinutes : 0;
        
        tvTodayFocus.setText("Today: " + todayTime + " min");
        tvTotalFocus.setText("Total: " + (totalTime / 60) + " hrs " + (totalTime % 60) + " min");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
