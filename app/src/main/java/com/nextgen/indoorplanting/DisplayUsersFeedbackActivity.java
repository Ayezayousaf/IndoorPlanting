package com.nextgen.indoorplanting;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayUsersFeedbackActivity extends AppCompatActivity {

    private ImageView plantImageView;
    private RecyclerView userFeedbackRecyclerView;
    private ImageView icAddFeedback;
    private FirebaseFirestore db;
    private PlantModel plant;
    private UserFeedbackAdapter adapter;
    private List<UserFeedbackModel> feedbackList;
    private ProgressBar progressIndicator;
    private View emptyStateLayout;
    private String currentUserEmail;
    private boolean isAdmin;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_users_feedback);

        plantImageView = findViewById(R.id.plant_image_view);
        userFeedbackRecyclerView = findViewById(R.id.user_feedback_recyclerview);
        icAddFeedback = findViewById(R.id.ic_add_feedback);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyStateLayout = findViewById(R.id.empty_state_layout);

        db = FirebaseFirestore.getInstance();

        // Get the current user's email and type
        currentUserEmail = UserSessionManager.getInstance(this).getUserEmail();
        isAdmin = UserSessionManager.getInstance(this).getUserType().equals("Administrator");

        // Get the plant object from intent
        plant = (PlantModel) getIntent().getSerializableExtra("plant");
        TextView plantNameTextView = findViewById(R.id.plant_name_text_view);
        TextView scientificNameTextView = findViewById(R.id.scientific_name_text_view);
        if (plant != null) {
            plantNameTextView.setText("Plant Name : " + plant.getName());
            scientificNameTextView.setText("Scientific Name : " + plant.getScientificName());
            Glide.with(this).load(plant.getImage()).into(plantImageView);
            loadUserFeedbacks();
        }

        icAddFeedback.setOnClickListener(v -> openAddFeedbackDialog());

        feedbackList = new ArrayList<>();
        adapter = new UserFeedbackAdapter(feedbackList, this::editFeedback, this::deleteFeedback, currentUserEmail, isAdmin);
        userFeedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userFeedbackRecyclerView.setAdapter(adapter);
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadUserFeedbacks() {
        progressIndicator.setVisibility(View.VISIBLE);
        CollectionReference feedbackRef = db.collection("UsersFeedback");

        feedbackRef.whereEqualTo("plantID", plant.getPlantID())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading feedbacks", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error loading feedbacks", e);
                        progressIndicator.setVisibility(View.GONE);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        feedbackList.clear();
                        for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {
                            QueryDocumentSnapshot document = docChange.getDocument();
                            String feedbackId = document.getId();
                            String userName = document.getString("userName");
                            String userEmail = document.getString("userEmail");
                            String userFeedback = document.getString("feedback");
                            Date timestamp = document.getTimestamp("timestamp") != null ? document.getTimestamp("timestamp").toDate() : null;
                            UserFeedbackModel feedback = new UserFeedbackModel(feedbackId, userName, userEmail, userFeedback, timestamp);

                            switch (docChange.getType()) {
                                case ADDED:
                                    feedbackList.add(feedback);
                                    break;
                                case MODIFIED:
                                    updateFeedbackInList(feedback);
                                    break;
                                case REMOVED:
                                    removeFeedbackFromList(feedback);
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressIndicator.setVisibility(View.GONE);
                        updateEmptyState();
                    }
                });
    }

    private void updateFeedbackInList(UserFeedbackModel feedback) {
        for (int i = 0; i < feedbackList.size(); i++) {
            if (feedbackList.get(i).getFeedbackId().equals(feedback.getFeedbackId())) {
                feedbackList.set(i, feedback);
                return;
            }
        }
    }

    private void removeFeedbackFromList(UserFeedbackModel feedback) {
        for (int i = 0; i < feedbackList.size(); i++) {
            if (feedbackList.get(i).getFeedbackId().equals(feedback.getFeedbackId())) {
                feedbackList.remove(i);
                return;
            }
        }
    }

    private void updateEmptyState() {
        if (feedbackList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            userFeedbackRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            userFeedbackRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openAddFeedbackDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_feedback);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText editTextFeedback = dialog.findViewById(R.id.editTextFeedback);
        MaterialButton buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonSubmit.setOnClickListener(v -> {
            String feedback = editTextFeedback.getText().toString().trim();
            if (!feedback.isEmpty()) {
                String userName = UserSessionManager.getInstance(this).getUserName();
                String userEmail = UserSessionManager.getInstance(this).getUserEmail();
                addUserFeedback(userName, userEmail, feedback);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addUserFeedback(String userName, String userEmail, String feedback) {
        ProgressDialogHelper.showProgressDialog(this, "Adding your comment...");
        String plantID = plant.getPlantID();
        CollectionReference feedbackRef = db.collection("UsersFeedback");

        Map<String, Object> newFeedback = new HashMap<>();
        newFeedback.put("userName", userName);
        newFeedback.put("userEmail", userEmail);
        newFeedback.put("feedback", feedback);
        newFeedback.put("timestamp", FieldValue.serverTimestamp());
        newFeedback.put("plantID", plantID);

        feedbackRef.add(newFeedback)
                .addOnSuccessListener(documentReference -> {
                    // Delay to ensure timestamp is set before reloading
                    new Handler().postDelayed(() -> {
                        loadUserFeedbacks();
                        ProgressDialogHelper.dismissProgressDialog();
                    }, 500);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding feedback", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding feedback", e);
                    ProgressDialogHelper.dismissProgressDialog();
                });
    }

    private void editFeedback(UserFeedbackModel feedback) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_feedback);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText editTextFeedback = dialog.findViewById(R.id.editTextFeedback);
        editTextFeedback.setText(feedback.getUserFeedback());

        MaterialButton buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonSubmit.setOnClickListener(v -> {
            String updatedFeedback = editTextFeedback.getText().toString().trim();
            if (!updatedFeedback.isEmpty()) {
                updateUserFeedback(feedback.getFeedbackId(), updatedFeedback);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserFeedback(String feedbackId, String updatedFeedback) {
        ProgressDialogHelper.showProgressDialog(this, "Updating your comment...");
        CollectionReference feedbackRef = db.collection("UsersFeedback");

        feedbackRef.document(feedbackId)
                .update("feedback", updatedFeedback)
                .addOnSuccessListener(aVoid -> {
                    loadUserFeedbacks();
                    new Handler().postDelayed(ProgressDialogHelper::dismissProgressDialog, 1000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating feedback", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating feedback", e);
                    new Handler().postDelayed(ProgressDialogHelper::dismissProgressDialog, 1000);
                });
    }

    private void deleteFeedback(String feedbackId) {
        ProgressDialogHelper.showProgressDialog(this, "Deleting your comment...");
        CollectionReference feedbackRef = db.collection("UsersFeedback");

        feedbackRef.document(feedbackId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadUserFeedbacks();
                    new Handler().postDelayed(ProgressDialogHelper::dismissProgressDialog, 1000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting feedback", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error deleting feedback", e);
                    new Handler().postDelayed(ProgressDialogHelper::dismissProgressDialog, 1000);
                });
    }

}
