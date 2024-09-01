package com.nextgen.indoorplanting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditPlantsDetailsActivity extends AppCompatActivity implements ImagePickerHelper.OnImagePickedListener {

    private TextInputEditText plantNameEditText, scientificNameEditText;
    private Spinner soilTypeSpinner, sunlightSpinner, wateringSpinner, seasonSpinner, depthSpinner;
    private MaterialButton saveButton, addHealthCareTipButton, addCommonIssueButton, changePictureButton;
    private ImageView plantImageView;
    private LinearLayout healthCareTipsLayout, commonIssuesLayout;
    private PlantModel plant;
    private Uri pictureUri;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ImagePickerHelper imagePickerHelper;
    private ImageCompressHelper imageCompressHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plants_details);

        plant = (PlantModel) getIntent().getSerializableExtra("plant");

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();
        setupSpinners();
        populateData();

        imagePickerHelper = new ImagePickerHelper(this);
        imageCompressHelper = new ImageCompressHelper(this);

        saveButton.setOnClickListener(v -> showChangesDialog());
        addHealthCareTipButton.setOnClickListener(v -> addHealthCareTip());
        addCommonIssueButton.setOnClickListener(v -> addCommonIssue());
        changePictureButton.setOnClickListener(v -> imagePickerHelper.showPicturePickerDialog());
    }

    private void initializeViews() {
        plantNameEditText = findViewById(R.id.plant_name);
        scientificNameEditText = findViewById(R.id.scientific_name);
        soilTypeSpinner = findViewById(R.id.soil_type_spinner);
        sunlightSpinner = findViewById(R.id.sunlight_spinner);
        wateringSpinner = findViewById(R.id.watering_spinner);
        seasonSpinner = findViewById(R.id.season_spinner);
        depthSpinner = findViewById(R.id.depth_spinner);
        saveButton = findViewById(R.id.save_button);
        addHealthCareTipButton = findViewById(R.id.add_health_care_tip_button);
        addCommonIssueButton = findViewById(R.id.add_common_issue_button);
        changePictureButton = findViewById(R.id.edit_picture_button);
        plantImageView = findViewById(R.id.plant_image);
        healthCareTipsLayout = findViewById(R.id.health_care_tips_layout);
        commonIssuesLayout = findViewById(R.id.common_issues_layout);

        customizeButton(addHealthCareTipButton);
        customizeButton(addCommonIssueButton);
        customizeButton(saveButton);
        customizeButton(changePictureButton);
    }

    private void customizeButton(MaterialButton button) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setCornerRadius(4);
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

    private void populateData() {
        if (plant != null) {
            plantNameEditText.setText(plant.getName());
            scientificNameEditText.setText(plant.getScientificName());

            setSpinnerValue(soilTypeSpinner, plant.getPlantingDetails().getSoilType());
            setSpinnerValue(sunlightSpinner, plant.getPlantingDetails().getSunlight());
            setSpinnerValue(wateringSpinner, plant.getPlantingDetails().getWatering());
            setSpinnerValue(seasonSpinner, plant.getPlantingDetails().getSeason());
            setSpinnerValue(depthSpinner, plant.getPlantingDetails().getDepth());

            Glide.with(this)
                    .load(plant.getImage())
                    .into(plantImageView);

            populateHealthCareTips(plant.getHealthCareTips());
            populateCommonIssues(plant.getCommonIssues());
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        spinner.setSelection(position);
    }

    private void populateHealthCareTips(List<String> tips) {
        healthCareTipsLayout.removeAllViews();
        for (String tip : tips) {
            LinearLayout tipLayout = createTipLayout(tip);
            healthCareTipsLayout.addView(tipLayout);
        }
    }

    private void populateCommonIssues(List<Map<String, String>> issues) {
        commonIssuesLayout.removeAllViews();
        for (Map<String, String> issue : issues) {
            LinearLayout issueLayout = createIssueLayout(issue.get("issue"), issue.get("solution"));
            commonIssuesLayout.addView(issueLayout);
        }
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
        deleteButton.setCornerRadius(4);
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
        deleteButton.setCornerRadius(4);
        deleteButton.setOnClickListener(v -> commonIssuesLayout.removeView(issueLayout));
        issueLayout.addView(issueInputLayout);
        issueLayout.addView(solutionInputLayout);
        issueLayout.addView(deleteButton);

        return issueLayout;
    }

    private void showChangesDialog() {
        String updatedName = plantNameEditText.getText().toString();
        String updatedScientificName = scientificNameEditText.getText().toString();
        String updatedSoilType = soilTypeSpinner.getSelectedItem().toString();
        String updatedSunlight = sunlightSpinner.getSelectedItem().toString();
        String updatedWatering = wateringSpinner.getSelectedItem().toString();
        String updatedSeason = seasonSpinner.getSelectedItem().toString();
        String updatedDepth = depthSpinner.getSelectedItem().toString();

        List<String> updatedHealthCareTips = new ArrayList<>();
        List<String> originalHealthCareTips = plant.getHealthCareTips();
        boolean healthCareTipsChanged = false;
        for (int i = 0; i < healthCareTipsLayout.getChildCount(); i++) {
            LinearLayout tipLayout = (LinearLayout) healthCareTipsLayout.getChildAt(i);
            TextInputEditText tipEditText = (TextInputEditText) tipLayout.getChildAt(0);
            if (tipEditText.getText().toString().trim().isEmpty()) {
                tipEditText.setError("This field cannot be empty");
                tipEditText.requestFocus();
                return;
            }
            String tip = tipEditText.getText().toString();
            updatedHealthCareTips.add(tip);
            if (!originalHealthCareTips.contains(tip)) {
                healthCareTipsChanged = true;
            }
        }
        healthCareTipsChanged = healthCareTipsChanged || updatedHealthCareTips.size() != originalHealthCareTips.size();

        List<Map<String, String>> updatedCommonIssues = new ArrayList<>();
        List<Map<String, String>> originalCommonIssues = plant.getCommonIssues();
        boolean commonIssuesChanged = false;
        for (int i = 0; i < commonIssuesLayout.getChildCount(); i++) {
            LinearLayout issueLayout = (LinearLayout) commonIssuesLayout.getChildAt(i);
            TextInputEditText issueEditText = (TextInputEditText) ((TextInputLayout) issueLayout.getChildAt(0)).getEditText();
            TextInputEditText solutionEditText = (TextInputEditText) ((TextInputLayout) issueLayout.getChildAt(1)).getEditText();

            if (issueEditText.getText().toString().trim().isEmpty()) {
                issueEditText.setError("This field cannot be empty");
                issueEditText.requestFocus();
                return;
            }
            if (solutionEditText.getText().toString().trim().isEmpty()) {
                solutionEditText.setError("This field cannot be empty");
                solutionEditText.requestFocus();
                return;
            }

            Map<String, String> issueMap = new HashMap<>();
            issueMap.put("issue", issueEditText.getText().toString());
            issueMap.put("solution", solutionEditText.getText().toString());
            updatedCommonIssues.add(issueMap);
            if (!originalCommonIssues.contains(issueMap)) {
                commonIssuesChanged = true;
            }
        }
        commonIssuesChanged = commonIssuesChanged || updatedCommonIssues.size() != originalCommonIssues.size();

        boolean isChanged = !updatedName.equals(plant.getName()) ||
                !updatedScientificName.equals(plant.getScientificName()) ||
                !updatedSoilType.equals(plant.getPlantingDetails().getSoilType()) ||
                !updatedSunlight.equals(plant.getPlantingDetails().getSunlight()) ||
                !updatedWatering.equals(plant.getPlantingDetails().getWatering()) ||
                !updatedSeason.equals(plant.getPlantingDetails().getSeason()) ||
                !updatedDepth.equals(plant.getPlantingDetails().getDepth()) ||
                healthCareTipsChanged ||
                commonIssuesChanged ||
                pictureUri != null;

        if (!isChanged) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder changes = new StringBuilder("The following changes will be made:<br>");
        if (!updatedName.equals(plant.getName())) changes.append("<b>Plant Name:</b> ").append(updatedName).append("<br>");
        if (!updatedScientificName.equals(plant.getScientificName())) changes.append("<b>Scientific Name:</b> ").append(updatedScientificName).append("<br>");
        if (!updatedSoilType.equals(plant.getPlantingDetails().getSoilType())) changes.append("<b>Soil Type:</b> ").append(updatedSoilType).append("<br>");
        if (!updatedSunlight.equals(plant.getPlantingDetails().getSunlight())) changes.append("<b>Sunlight:</b> ").append(updatedSunlight).append("<br>");
        if (!updatedWatering.equals(plant.getPlantingDetails().getWatering())) changes.append("<b>Watering:</b> ").append(updatedWatering).append("<br>");
        if (!updatedSeason.equals(plant.getPlantingDetails().getSeason())) changes.append("<b>Planting Season:</b> ").append(updatedSeason).append("<br>");
        if (!updatedDepth.equals(plant.getPlantingDetails().getDepth())) changes.append("<b>Planting Depth:</b> ").append(updatedDepth).append("<br>");
        if (healthCareTipsChanged) changes.append("<b>Health Care Tips:</b> ").append("Edited/Added/Deleted").append("<br>");
        if (commonIssuesChanged) changes.append("<b>Common Issues:</b> ").append("Edited/Added/Deleted").append("<br>");
        if (pictureUri != null) changes.append("<b>Image:</b> New picture selected");

        new AlertDialog.Builder(this)
                .setTitle("Confirm Changes")
                .setMessage(android.text.Html.fromHtml(changes.toString()))
                .setPositiveButton("Confirm", (dialog, which) -> saveChanges(
                        updatedName, updatedScientificName, updatedSoilType, updatedSunlight,
                        updatedWatering, updatedSeason, updatedDepth,
                        updatedHealthCareTips, updatedCommonIssues))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void saveChanges(String updatedName, String updatedScientificName, String updatedSoilType, String updatedSunlight,
                             String updatedWatering, String updatedSeason, String updatedDepth,
                             List<String> updatedHealthCareTips, List<Map<String, String>> updatedCommonIssues) {
        plant.setName(updatedName);
        plant.setScientificName(updatedScientificName);
        plant.getPlantingDetails().setSoilType(updatedSoilType);
        plant.getPlantingDetails().setSunlight(updatedSunlight);
        plant.getPlantingDetails().setWatering(updatedWatering);
        plant.getPlantingDetails().setSeason(updatedSeason);
        plant.getPlantingDetails().setDepth(updatedDepth);
        plant.setHealthCareTips(updatedHealthCareTips);
        plant.setCommonIssues(updatedCommonIssues);

        ProgressDialogHelper.showProgressDialog(this, "Saving changes...");

        // Delete old picture if new one is selected
        if (pictureUri != null) {
            String oldImageUrl = plant.getImage();
            StorageReference oldImageRef = storage.getReferenceFromUrl(oldImageUrl);
            oldImageRef.delete().addOnSuccessListener(aVoid -> {
                // Upload new picture and save changes
                uploadImageToFirebaseStorage(pictureUri, plant);
            }).addOnFailureListener(e -> {
                ProgressDialogHelper.dismissProgressDialog();
                Toast.makeText(EditPlantsDetailsActivity.this, "Failed to delete old image", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Save changes without uploading a new picture
            savePlantToFirestore(plant);
        }
    }

    private void uploadImageToFirebaseStorage(Uri fileUri, PlantModel plant) {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imagesRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            imagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                plant.setImage(downloadUri.toString());
                savePlantToFirestore(plant);
            }).addOnFailureListener(e -> {
                ProgressDialogHelper.dismissProgressDialog();
                Toast.makeText(EditPlantsDetailsActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            ProgressDialogHelper.dismissProgressDialog();
            Toast.makeText(EditPlantsDetailsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void savePlantToFirestore(PlantModel plant) {
        db.collection("plants").document(plant.getPlantID())
                .set(plant)
                .addOnSuccessListener(aVoid -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Toast.makeText(EditPlantsDetailsActivity.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedPlant", plant);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    ProgressDialogHelper.dismissProgressDialog();
                    Toast.makeText(EditPlantsDetailsActivity.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerHelper.handleActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onImagePicked(Uri imageUri, String fileName) {
        Uri compressedUri = imageCompressHelper.compressImage(imageUri);
        if (compressedUri != null) {
            pictureUri = compressedUri;
            changePictureButton.setText("New picture selected");
            Glide.with(this).load(pictureUri).into(plantImageView);
        } else {
            Toast.makeText(this, "Failed to compress image.", Toast.LENGTH_SHORT).show();
        }
    }
}
