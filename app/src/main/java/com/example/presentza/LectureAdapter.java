package com.example.presentza;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class LectureAdapter extends RecyclerView.Adapter<LectureAdapter.LectureViewHolder> {
    private List<Lecture> lectures;
    private final LectureClickListener lectureClickListener;

    public LectureAdapter(List<Lecture> lectures, LectureClickListener lectureClickListener) {
        this.lectures = lectures;
        this.lectureClickListener = lectureClickListener;
    }

    @NonNull
    @Override
    public LectureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecture_item, parent, false);
        return new LectureViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureViewHolder holder, int position) {
        Lecture lecture = lectures.get(position);
        holder.lectureNameTextView.setText(lecture.getLectureName());
        holder.itemView.setOnClickListener(v -> lectureClickListener.onLectureClick(lecture));
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLectures(List<Lecture> newLectures) {
        this.lectures = newLectures;
        notifyDataSetChanged();
    }

    public static class LectureViewHolder extends RecyclerView.ViewHolder {
        TextView lectureNameTextView;

        public LectureViewHolder(@NonNull View itemView) {
            super(itemView);
            lectureNameTextView = itemView.findViewById(R.id.lecturenametextview);
        }
    }

    public interface LectureClickListener {
        void onLectureClick(Lecture lecture);
    }
}

