package com.nextgen.indoorplanting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddPlantActivity extends AppCompatActivity implements ImagePickerHelper.OnImagePickedListener {

    private EditText plantNameEditText, scientificNameEditText;
    private Spinner soilTypeSpinner, sunlightSpinner, wateringSpinner, seasonSpinner, depthSpinner;
    private MaterialButton saveButton, addHealthCareTipButton, addCommonIssueButton;
    private Uri pictureUri;
    private ImageView selectImagePickerButton;
    private TextView selectedPictureText;

    private LinearLayout healthCareTipsLayout, commonIssuesLayout;
    private ArrayList<String> healthCareTips = new ArrayList<>();
    private ArrayList<Map<String, String>> commonIssues = new ArrayList<>();

    private ImagePickerHelper imagePickerHelper;
    private ImageCompressHelper imageCompressHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        initializeViews();
        setListeners();
        setupSpinners();

        imagePickerHelper = new ImagePickerHelper(this);
        imageCompressHelper = new ImageCompressHelper(this);
    }

    private void initializeViews() {
        plantNameEditText = findViewById(R.id.plant_name);
        scientificNameEditText = findViewById(R.id.scientific_name);
        soilTypeSpinner = findViewById(R.id.soil_type_spinner);
        sunlightSpinner = findViewById(R.id.sunlight_spinner);
        wateringSpinner = findViewById(R.id.watering_spinner);
        seasonSpinner = findViewById(R.id.season_spinner);
        depthSpinner = findViewById(R.id.depth_spinner);
        selectImagePickerButton = findViewById(R.id.select_image_picker_button);
        selectedPictureText = findViewById(R.id.selected_picture_text);
        saveButton = findViewById(R.id.save_button);
        addHealthCareTipButton = findViewById(R.id.add_health_care_tip_button);
        addCommonIssueButton = findViewById(R.id.add_common_issue_button);
        healthCareTipsLayout = findViewById(R.id.health_care_tips_layout);
        commonIssuesLayout = findViewById(R.id.common_issues_layout);
    }

    private void setListeners() {
        saveButton.setOnClickListener(v -> savePlantToFirestore());
        selectImagePickerButton.setOnClickListener(v -> imagePickerHelper.showPicturePickerDialog());
        addHealthCareTipButton.setOnClickListener(v -> addHealthCareTip());
        addCommonIssueButton.setOnClickListener(v -> addCommonIssue());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> soilTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.soil_types, android.R.layout.simple_spinner_item);
        soilTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soilTypeSpinner.setAdapter(soilTypeAdapter);

        ArrayAdapter<CharSequence> sunlightAdapter = ArrayAdapter.createFromResource(this,
                R.array.sunlight_types, android.R.layout.simple_spinner_item);
        sunlightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sunlightSpinner.setAdapter(sunlightAdapter);

        ArrayAdapter<CharSequence> wateringAdapter = ArrayAdapter.createFromResource(this,
                R.array.watering_types, android.R.layout.simple_spinner_item);
        wateringAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wateringSpinner.setAdapter(wateringAdapter);

        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(this,
                R.array.planting_seasons, android.R.layout.simple_spinner_item);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(seasonAdapter);

        ArrayAdapter<CharSequence> depthAdapter = ArrayAdapter.createFromResource(this,
                R.array.planting_depths, android.R.layout.simple_spinner_item);
        depthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        depthSpinner.setAdapter(depthAdapter);
    }

    private void addHealthCareTip() {
        LinearLayout newTipLayout = createTipLayout("");
        healthCareTipsLayout.addView(newTipLayout);
    }

    private void addCommonIssue() {
        LinearLayout newIssueLayout = createIssueLayout("", "");
        commonIssuesLayout.addView(newIssueLayout);
    }

    private LinearLayout createTipLayout(String tip) {
        LinearLayout tipLayout = new LinearLayout(this);
        tipLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextInputEditText tipEditText = new TextInputEditText(this);
        tipEditText.setText(tip);
        tipEditText.setHint("Health Care Tip");
        tipLayout.addView(tipEditText);

        MaterialButton deleteButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        deleteButton.setText("Delete");
        deleteButton.setBackgroundColor(getResources().getColor(R.color.green));
        deleteButton.setCornerRadius(6);
        deleteButton.setOnClickListener(v -> healthCareTipsLayout.removeView(tipLayout));
        tipLayout.addView(deleteButton);

        return tipLayout;
    }

    private LinearLayout createIssueLayout(String issue, String solution) {
        LinearLayout issueLayout = new LinearLayout(this);
        issueLayout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout issueInputLayout = new TextInputLayout(this);
        TextInputEditText issueEditText = new TextInputEditText(this);
        issueEditText.setText(issue);
        issueInputLayout.setHint("Issue");
        issueInputLayout.addView(issueEditText);

        TextInputLayout solutionInputLayout = new TextInputLayout(this);
        TextInputEditText solutionEditText = new TextInputEditText(this);
        solutionEditText.setText(solution);
        solutionInputLayout.setHint("Solution");
        solutionInputLayout.addView(solutionEditText);

        MaterialButton deleteButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        deleteButton.setText("Delete");
        deleteButton.setBackgroundColor(getResources().getColor(R.color.green));
        deleteButton.setCornerRadius(6);
        deleteButton.setOnClickListener(v -> commonIssuesLayout.removeView(issueLayout));
        issueLayout.addView(issueInputLayout);
        issueLayout.addView(solutionInputLayout);
        issueLayout.addView(deleteButton);

        return issueLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerHelper.handleActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onImagePicked(Uri imageUri, String fileName) {
        Uri compressedUri = imageCompressHelper.compressImage(imageUri);
        if (compressedUri != null) {
            pictureUri = compressedUri;
            selectedPictureText.setText(fileName);
            selectedPictureText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Failed to compress image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePlantToFirestore() {
        String name = plantNameEditText.getText().toString();
        String soilType = soilTypeSpinner.getSelectedItem().toString();
        String scientificName = scientificNameEditText.getText().toString();
        String sunlight = sunlightSpinner.getSelectedItem().toString();
        String watering = wateringSpinner.getSelectedItem().toString();
        String season = seasonSpinner.getSelectedItem().toString();
        String depth = depthSpinner.getSelectedItem().toString();

        if (!validateInput(name, scientificName, soilType, sunlight, watering, season, depth)) {
            return;
        }
        if (pictureUri == null) {
            showAlertDialog("Picture Required", "Please select a picture for the plant.");
            return;
        }
        if (!updateHealthCareTipsList() || !updateCommonIssuesList()) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> plant = createPlantMap(name, scientificName, soilType, sunlight, watering, season, depth);

        if (pictureUri != null) {
            uploadImageToFirebaseStorage(pictureUri, plant, db);
        } else {
            savePlantToFirestore(plant, db);
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean validateInput(String name, String scientificName, String soilType, String sunlight, String watering, String season, String depth) {
        if (name.isEmpty()) {
            plantNameEditText.setError("Plant name is required");
            plantNameEditText.requestFocus();
            return false;
        }
        if (scientificName.isEmpty()) {
            scientificNameEditText.setError("Scientific name is required");
            scientificNameEditText.requestFocus();
            return false;
        }
        if (soilType.isEmpty() || sunlight.isEmpty() || watering.isEmpty() || season.isEmpty() || depth.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Map<String, Object> createPlantMap(String name, String scientificName, String soilType, String sunlight, String watering, String season, String depth) {
        Map<String, Object> plant = new HashMap<>();
        plant.put("name", name);
        plant.put("scientificName", scientificName);
        Map<String, Object> plantingDetails = new HashMap<>();
        plantingDetails.put("soilType", soilType);
        plantingDetails.put("sunlight", sunlight);
        plantingDetails.put("watering", watering);
        plantingDetails.put("season", season);
        plantingDetails.put("depth", depth);
        plant.put("plantingDetails", plantingDetails);
        plant.put("healthCareTips", healthCareTips);
        plant.put("commonIssues", commonIssues);
        return plant;
    }

    private boolean updateHealthCareTipsList() {
        healthCareTips.clear();
        for (int i = 0; i < healthCareTipsLayout.getChildCount(); i++) {
            LinearLayout tipLayout = (LinearLayout) healthCareTipsLayout.getChildAt(i);
            TextInputEditText tipEditText = (TextInputEditText) tipLayout.getChildAt(0);
            String tip = tipEditText.getText().toString().trim();
            if (tip.isEmpty()) {
                tipEditText.setError("This field cannot be empty");
                tipEditText.requestFocus();
                return false;
            }
            healthCareTips.add(tip);
        }
        return true;
    }

    private boolean updateCommonIssuesList() {
        commonIssues.clear();
        for (int i = 0; i < commonIssuesLayout.getChildCount(); i++) {
            LinearLayout issueLayout = (LinearLayout) commonIssuesLayout.getChildAt(i);
            TextInputEditText issueEditText = (TextInputEditText) ((TextInputLayout) issueLayout.getChildAt(0)).getEditText();
            TextInputEditText solutionEditText = (TextInputEditText) ((TextInputLayout) issueLayout.getChildAt(1)).getEditText();

            String issue = issueEditText.getText().toString().trim();
            String solution = solutionEditText.getText().toString().trim();

            if (issue.isEmpty()) {
                issueEditText.setError("This field cannot be empty");
                issueEditText.requestFocus();
                return false;
            }
            if (solution.isEmpty()) {
                solutionEditText.setError("This field cannot be empty");
                solutionEditText.requestFocus();
                return false;
            }

            Map<String, String> issueMap = new HashMap<>();
            issueMap.put("issue", issue);
            issueMap.put("solution", solution);
            commonIssues.add(issueMap);
        }
        return true;
    }

    private void uploadImageToFirebaseStorage(Uri fileUri, Map<String, Object> plant, FirebaseFirestore db) {
        ProgressDialogHelper.showProgressDialog(this,"Saving Plants Details...");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images/" + UUID.randomUUID().toString());

        UploadTask uploadTask = imagesRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            plant.put("image", downloadUri.toString());
            savePlantToFirestore(plant, db);
            deleteImageFile(fileUri);
        })).addOnFailureListener(exception -> Toast.makeText(AddPlantActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void deleteImageFile(Uri fileUri) {
        File file = new File(fileUri.getPath());
        if (file.exists()) {
            if (!file.delete()) {
                Log.e("Delete Image", "Failed to delete image file");
            }
        }
    }

    private void savePlantToFirestore(Map<String, Object> plant, FirebaseFirestore db) {
        String plantID = UUID.randomUUID().toString();

        plant.put("plantID", plantID);

        db.collection("plants").document(plantID)
                .set(plant)
                .addOnSuccessListener(documentReference -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Toast.makeText(AddPlantActivity.this, "Plant added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Log.w("Firestore", "Error adding document", e);
                    Toast.makeText(AddPlantActivity.this, "Failed to add plant", Toast.LENGTH_SHORT).show();
                });
    }
}
