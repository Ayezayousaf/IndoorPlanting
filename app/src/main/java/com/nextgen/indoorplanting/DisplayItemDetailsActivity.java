package com.nextgen.indoorplanting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DisplayItemDetailsActivity extends AppCompatActivity {

    private ImageView itemImageView;
    private TextView itemTypeTextView, itemNameTextView, itemDescriptionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_item_details);

        initializeViews();
        handleIntent();
    }

    private void initializeViews() {
        itemImageView = findViewById(R.id.item_image_view);
        itemTypeTextView = findViewById(R.id.Attribute_Type);
        itemNameTextView = findViewById(R.id.Attribute_Name);
        itemDescriptionTextView = findViewById(R.id.item_description);
    }

    private void setupToolbar(String plantName, String attributeType) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(plantName + " - " + attributeType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String attributeType, attributeName, plantName;
        if (intent != null && intent.hasExtra("ATTRIBUTE_TYPE") && intent.hasExtra("PLANT_NAME")) {
            attributeType = intent.getStringExtra("ATTRIBUTE_TYPE");
            attributeName = intent.getStringExtra("ATTRIBUTE_NAME");
            plantName = intent.getStringExtra("PLANT_NAME");
        } else {
            attributeType = "Soil";
            attributeName = "Loamy";
            plantName = "Plant";
        }
        setupToolbar(plantName, attributeType);
        displayPlantDetails(attributeType, attributeName, plantName);
    }

    private void displayPlantDetails(String attributeType, String attributeName, String plantName) {
        PlantsDataHelper.PlantDetails plantDetails = PlantsDataHelper.getPlantDetails(attributeName, plantName);

        itemTypeTextView.setText(attributeType + ": ");
        itemNameTextView.setText(attributeName);
        itemImageView.setImageResource(plantDetails.getImageResId());
        itemDescriptionTextView.setText(plantDetails.getDescription());
    }
}
