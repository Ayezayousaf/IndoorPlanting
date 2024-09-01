package com.nextgen.indoorplanting;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminHomeFragment extends Fragment implements AdminPlantAdapter.EditPlantListener, AdminPlantAdapter.DeletePlantListener {

    private static final int EDIT_PLANT_REQUEST_CODE = 1;
    private static final int ADD_PLANT_REQUEST_CODE = 2;

    private RecyclerView recyclerView;
    private AdminPlantAdapter adminPlantAdapter;
    private List<PlantModel> plantList;
    private List<PlantModel> filteredPlantList;
    private FirebaseFirestore db;
    private TextInputEditText searchInput;
    private ProgressBar progressBar;
    private View emptyStateContainer;
    private TextInputLayout Search_Card;
    private boolean isAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        searchInput = view.findViewById(R.id.search_input);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        ImageView fabImageView = view.findViewById(R.id.fab_image_view);
        Search_Card = view.findViewById(R.id.search_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        plantList = new ArrayList<>();
        filteredPlantList = new ArrayList<>();
        isAdmin = UserSessionManager.getInstance(getContext()).getUserType().equals("Administrator");

        adminPlantAdapter = new AdminPlantAdapter(getContext(), filteredPlantList, this, this,isAdmin);
        recyclerView.setAdapter(adminPlantAdapter);

        db = FirebaseFirestore.getInstance();
        loadPlantData();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        if(isAdmin)
        {
            fabImageView.setVisibility(View.VISIBLE);
        }
        fabImageView.setOnClickListener(v -> openAddPlantActivity());

        return view;
    }

    private void loadPlantData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("plants")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e != null) {
                            Log.w("Firestore", "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    PlantModel newPlant = dc.getDocument().toObject(PlantModel.class);
                                    plantList.add(newPlant);
                                    break;
                                case MODIFIED:
                                    PlantModel updatedPlant = dc.getDocument().toObject(PlantModel.class);
                                    updatePlantInList(updatedPlant);
                                    break;
                                case REMOVED:
                                    PlantModel removedPlant = dc.getDocument().toObject(PlantModel.class);
                                    deletePlantFromList(removedPlant);
                                    break;
                            }
                        }

                        sortPlantList();
                        filteredPlantList.clear();
                        filteredPlantList.addAll(plantList);
                        adminPlantAdapter.notifyDataSetChanged();
                        toggleEmptyState();
                    }
                });
    }

    private void sortPlantList() {
        Collections.sort(plantList, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
    }

    private void filterPlants(String query) {
        filteredPlantList.clear();
        for (PlantModel plant : plantList) {
            if (plant.getName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getScientificName().toLowerCase().contains(query.toLowerCase())) {
                filteredPlantList.add(plant);
            }
        }
        adminPlantAdapter.notifyDataSetChanged();
    }

    private void toggleEmptyState() {
        if (filteredPlantList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            Search_Card.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void openAddPlantActivity() {
        Intent intent = new Intent(getContext(), AddPlantActivity.class);
        startActivityForResult(intent, ADD_PLANT_REQUEST_CODE);
    }

    @Override
    public void onEditPlant(PlantModel plant) {
        Intent intent = new Intent(getContext(), EditPlantsDetailsActivity.class);
        intent.putExtra("plant", plant);
        startActivityForResult(intent, EDIT_PLANT_REQUEST_CODE);
    }

    @Override
    public void onDeletePlant(PlantModel plant) {
        deletePlantFromList(plant);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == EDIT_PLANT_REQUEST_CODE && data != null) {
                PlantModel updatedPlant = (PlantModel) data.getSerializableExtra("updatedPlant");
                if (updatedPlant != null) {
                    updatePlantInList(updatedPlant);
                }
            } else if (requestCode == ADD_PLANT_REQUEST_CODE && data != null) {
                PlantModel newPlant = (PlantModel) data.getSerializableExtra("newPlant");
                if (newPlant != null) {
                    addPlantToList(newPlant);
                }
            }
        }
    }

    private void updatePlantInList(PlantModel updatedPlant) {
        for (int i = 0; i < plantList.size(); i++) {
            if (plantList.get(i).getPlantID().equals(updatedPlant.getPlantID())) {
                plantList.set(i, updatedPlant);
                adminPlantAdapter.notifyItemChanged(i);
                break;
            }
        }
        sortPlantList();
        filterPlants(searchInput.getText().toString());
    }

    private void addPlantToList(PlantModel newPlant) {
        plantList.add(newPlant);
        sortPlantList();
        filterPlants(searchInput.getText().toString());
        toggleEmptyState();
    }

    public void deletePlantFromList(PlantModel plant) {
        plantList.remove(plant);
        filteredPlantList.remove(plant);
        adminPlantAdapter.notifyDataSetChanged();
        toggleEmptyState();
    }
}
