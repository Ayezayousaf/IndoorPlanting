package com.nextgen.indoorplanting;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ViewAdminPlantDetailsActivity extends AppCompatActivity {

    private ImageView plantImageView;
    private TextView plantNameTextView;
    private TextView scientificNameTextView;
    private TextView soilTypeTextView;
    private TextView sunlightTextView;
    private TextView wateringTextView;
    private TextView seasonTextView;
    private TextView depthTextView;
    private TextView healthCareTipsTextView;
    private LinearLayout commonIssuesLayout;
    private ImageView soilTypeInfo;
    private ImageView sunlightInfo;
    private ImageView wateringInfo;
    private ImageView seasonInfo;
    private ImageView depthInfo;
    private PlantModel plant;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_admin_plant_details);

        plantImageView = findViewById(R.id.plant_image_view);
        plantNameTextView = findViewById(R.id.plant_name_text_view);
        scientificNameTextView = findViewById(R.id.scientific_name_text_view);
        soilTypeTextView = findViewById(R.id.soil_type_text_view);
        sunlightTextView = findViewById(R.id.sunlight_text_view);
        wateringTextView = findViewById(R.id.watering_text_view);
        seasonTextView = findViewById(R.id.season_text_view);
        depthTextView = findViewById(R.id.depth_text_view);
        healthCareTipsTextView = findViewById(R.id.health_care_tips_text_view);
        commonIssuesLayout = findViewById(R.id.common_issues_layout);
        soilTypeInfo = findViewById(R.id.soil_type_info);
        sunlightInfo = findViewById(R.id.sunlight_info);
        wateringInfo = findViewById(R.id.watering_info);
        seasonInfo = findViewById(R.id.season_info);
        depthInfo = findViewById(R.id.depth_info);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Receive the PlantModel object from intent
        plant = (PlantModel) getIntent().getSerializableExtra("plant");

        if (plant != null) {
            populatePlantDetails(plant);
        }

        setInfoButtonListeners();
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

    private void populatePlantDetails(PlantModel plant) {
        Glide.with(this).load(plant.getImage()).into(plantImageView);

        plantNameTextView.setText(plant.getName());
        scientificNameTextView.setText(plant.getScientificName());
        soilTypeTextView.setText(plant.getPlantingDetails().getSoilType());
        sunlightTextView.setText(plant.getPlantingDetails().getSunlight());
        wateringTextView.setText(plant.getPlantingDetails().getWatering());
        seasonTextView.setText(plant.getPlantingDetails().getSeason());
        depthTextView.setText(plant.getPlantingDetails().getDepth());

        StringBuilder healthCareTips = new StringBuilder();
        for (String tip : plant.getHealthCareTips()) {
            healthCareTips.append("• ").append(tip).append("\n");
        }
        healthCareTipsTextView.setText(healthCareTips.toString().trim());

        commonIssuesLayout.removeAllViews();
        for (Map<String, String> issue : plant.getCommonIssues()) {
            String issueText = issue.get("issue");
            String solutionText = issue.get("solution");

            LinearLayout issueLayout = new LinearLayout(this);
            issueLayout.setOrientation(LinearLayout.VERTICAL);
            issueLayout.setPadding(8, 8, 8, 8);

            TextView issueTextView = new TextView(this);
            issueTextView.setText("Issue: " + issueText);
            issueTextView.setTextSize(16);
            issueTextView.setTextColor(getResources().getColor(android.R.color.white));
            issueTextView.setTypeface(issueTextView.getTypeface(), Typeface.BOLD);

            TextView solutionTextView = new TextView(this);
            solutionTextView.setText("Solution: " + solutionText);
            solutionTextView.setTextSize(16);
            solutionTextView.setTextColor(getResources().getColor(android.R.color.white));
            solutionTextView.setPadding(8, 0, 0, 0);

            issueLayout.addView(issueTextView);
            issueLayout.addView(solutionTextView);

            commonIssuesLayout.addView(issueLayout);
        }
    }


    private void setInfoButtonListeners() {
        soilTypeInfo.setOnClickListener(view -> openDisplayItemDetails("Soil", soilTypeTextView.getText().toString()));
        sunlightInfo.setOnClickListener(view -> openDisplayItemDetails("Sunlight", sunlightTextView.getText().toString()));
        wateringInfo.setOnClickListener(view -> openDisplayItemDetails("Watering", wateringTextView.getText().toString()));
        seasonInfo.setOnClickListener(view -> openDisplayItemDetails("Season", seasonTextView.getText().toString()));
        depthInfo.setOnClickListener(view -> openDisplayItemDetails("Depth", depthTextView.getText().toString()));
    }

    private void openDisplayItemDetails(String attributeType, String attributeValue) {
        Intent intent = new Intent(this, DisplayItemDetailsActivity.class);
        intent.putExtra("ATTRIBUTE_NAME", attributeValue);
        intent.putExtra("PLANT_NAME", plant.getName());
        intent.putExtra("ATTRIBUTE_TYPE", attributeType);

        startActivity(intent);
    }

}
