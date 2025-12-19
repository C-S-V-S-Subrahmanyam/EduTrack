package com.example.edutrack.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final List<Task> allTasks;
    private final Context context;
    private boolean showCompleted = false;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.allTasks = new ArrayList<>(taskList);
        this.taskList = filterTasks(taskList);
        this.context = context;
    }

    private List<Task> filterTasks(List<Task> tasks) {
        if (showCompleted) {
            return tasks;
        }
        List<Task> filtered = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted) {
                filtered.add(task);
            }
        }
        return filtered;
    }

    public void toggleShowCompleted() {
        showCompleted = !showCompleted;
        taskList = filterTasks(allTasks);
        notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task t = taskList.get(position);
        holder.tvTitle.setText(t.title);

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDue.setText(sdf.format(new Date(t.time)));
        
        // Set checkbox state
        holder.cbCompleted.setChecked(t.isCompleted);
        
        // Apply strikethrough if completed
        if (t.isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(1.0f);
        }
        
        // Checkbox click listener
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            t.isCompleted = isChecked;
            AppDatabase.getInstance(context).taskDao().update(t);
            
            // Update in Firebase
            updateTaskInFirebase(t);
            
            if (isChecked) {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.itemView.setAlpha(0.6f);
                
                // Hide completed task after 500ms delay
                holder.itemView.postDelayed(() -> {
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, taskList.size());
                }, 500);
            } else {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                holder.itemView.setAlpha(1.0f);
            }
        });
        
        // Click on task to show details
        holder.itemView.setOnClickListener(v -> showTaskDetails(t));
        
        // Delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        AppDatabase.getInstance(context).taskDao().delete(t);
                        
                        // Delete from Firebase
                        deleteTaskFromFirebase(t.id);
                        
                        taskList.remove(position);
                        allTasks.remove(t);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskList.size());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showTaskDetails(Task task) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_task_details, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvDesc = dialogView.findViewById(R.id.tvDetailDesc);
        TextView tvDueDate = dialogView.findViewById(R.id.tvDetailDueDate);
        TextView tvPriority = dialogView.findViewById(R.id.tvDetailPriority);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);
        
        tvTitle.setText(task.title);
        tvDesc.setText(task.description != null ? task.description : "No description");
        tvDueDate.setText("Due: " + task.dueDate);
        tvPriority.setText("Priority: " + task.priority);
        tvStatus.setText(task.isCompleted ? "Status: Completed ✓" : "Status: Pending");
        
        // Set priority color
        int priorityColor = context.getColor(R.color.primary);
        if ("High".equals(task.priority)) {
            priorityColor = context.getColor(R.color.error);
        } else if ("Medium".equals(task.priority)) {
            priorityColor = context.getColor(R.color.accent);
        } else {
            priorityColor = context.getColor(R.color.success);
        }
        tvPriority.setTextColor(priorityColor);
        
        // Set status color
        if (task.isCompleted) {
            tvStatus.setTextColor(context.getColor(R.color.success));
        } else {
            tvStatus.setTextColor(context.getColor(R.color.error));
        }
        
        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void updateTaskInFirebase(Task task) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .child("tasks")
                .child(String.valueOf(task.id))
                .child("isCompleted")
                .setValue(task.isCompleted);
        }
    }
    
    private void deleteTaskFromFirebase(int taskId) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .child("tasks")
                .child(String.valueOf(taskId))
                .removeValue();
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDue;
        CheckBox cbCompleted;
        ImageButton btnDelete;

        TaskViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDue = itemView.findViewById(R.id.tvDue);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
