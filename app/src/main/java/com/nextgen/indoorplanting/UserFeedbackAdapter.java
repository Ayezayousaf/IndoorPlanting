package com.nextgen.indoorplanting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserFeedbackAdapter extends RecyclerView.Adapter<UserFeedbackAdapter.UserFeedbackViewHolder> {

    private List<UserFeedbackModel> feedbackList;
    private OnEditFeedbackListener editFeedbackListener;
    private OnDeleteFeedbackListener deleteFeedbackListener;
    private String currentUserEmail;
    private boolean isAdmin;

    public UserFeedbackAdapter(List<UserFeedbackModel> feedbackList, OnEditFeedbackListener editFeedbackListener, OnDeleteFeedbackListener deleteFeedbackListener, String currentUserEmail, boolean isAdmin) {
        this.feedbackList = feedbackList;
        this.editFeedbackListener = editFeedbackListener;
        this.deleteFeedbackListener = deleteFeedbackListener;
        this.currentUserEmail = currentUserEmail;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public UserFeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_feedback, parent, false);
        return new UserFeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserFeedbackViewHolder holder, int position) {
        UserFeedbackModel feedback = feedbackList.get(position);
        holder.userNameTextView.setText(feedback.getUserName());
        holder.userFeedbackTextView.setText(feedback.getUserFeedback());
        holder.postedAgoTextView.setText(getTimeAgo(feedback.getTimestamp()));

        // Show edit and delete options only for the feedback created by the current user or if the user is an admin
        if (isAdmin || feedback.getUserEmail().equals(currentUserEmail)) {
            holder.editCommentLayout.setVisibility(View.VISIBLE);
            holder.deleteCommentLayout.setVisibility(View.VISIBLE);

            holder.editCommentLayout.setOnClickListener(v -> editFeedbackListener.onEditFeedback(feedback));
            holder.deleteCommentLayout.setOnClickListener(v -> deleteFeedbackListener.onDeleteFeedback(feedback.getFeedbackId()));
        } else {
            holder.editCommentLayout.setVisibility(View.GONE);
            holder.deleteCommentLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    private String getTimeAgo(Date date) {
        if (date == null) {
            return "Unknown time";
        }

        long time = date.getTime();
        long now = System.currentTimeMillis();

        long diff = now - time;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    static class UserFeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView userFeedbackTextView;
        TextView postedAgoTextView;
        LinearLayout editCommentLayout;
        LinearLayout deleteCommentLayout;

        UserFeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name_text_view);
            userFeedbackTextView = itemView.findViewById(R.id.user_feedback_text_view);
            postedAgoTextView = itemView.findViewById(R.id.posted_ago);
            editCommentLayout = itemView.findViewById(R.id.EditCommentLayout);
            deleteCommentLayout = itemView.findViewById(R.id.DeleteCommentLayout);
        }
    }

    public interface OnEditFeedbackListener {
        void onEditFeedback(UserFeedbackModel feedback);
    }

    public interface OnDeleteFeedbackListener {
        void onDeleteFeedback(String feedbackId);
    }
}
