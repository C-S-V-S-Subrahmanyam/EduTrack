package com.example.edutrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.edutrack.R;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final List<Subject> subjectList;
    private final Context context;
    private final AppDatabase db;

    public SubjectAdapter(List<Subject> subjectList, Context context) {
        this.subjectList = subjectList;
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        Subject s = subjectList.get(position);

        holder.tvSubjectName.setText(s.name);
        holder.tvPercent.setText(getPercentText(s));

        holder.btnPresent.setOnClickListener(v -> {
            s.total++;
            s.attended++;
            db.subjectDao().update(s);
            holder.tvPercent.setText(getPercentText(s));
        });

        holder.btnAbsent.setOnClickListener(v -> {
            s.total++;
            db.subjectDao().update(s);
            holder.tvPercent.setText(getPercentText(s));
        });
    }

    private String getPercentText(Subject s) {
        if (s.total == 0) return "0%";
        int percent = (int) ((s.attended * 100.0f) / s.total);
        return percent + "%";
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvPercent;
        Button btnPresent, btnAbsent;

        SubjectViewHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            btnPresent = itemView.findViewById(R.id.btnPresent);
            btnAbsent = itemView.findViewById(R.id.btnAbsent);
        }
    }
}
