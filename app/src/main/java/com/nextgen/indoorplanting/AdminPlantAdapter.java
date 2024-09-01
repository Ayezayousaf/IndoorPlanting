package com.nextgen.indoorplanting;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdminPlantAdapter extends RecyclerView.Adapter<AdminPlantAdapter.PlantViewHolder> {

    private Context context;
    private List<PlantModel> plantList;
    private EditPlantListener editPlantListener;
    private DeletePlantListener deletePlantListener;
    private boolean isAdmin;

    public AdminPlantAdapter(Context context, List<PlantModel> plantList, EditPlantListener editPlantListener, DeletePlantListener deletePlantListener, boolean isAdmin) {
        this.context = context;
        this.plantList = plantList;
        this.editPlantListener = editPlantListener;
        this.deletePlantListener = deletePlantListener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantModel plant = plantList.get(position);
        holder.plantNameTextView.setText(plant.getName());
        holder.scientificNameTextView.setText(plant.getScientificName());
        if (isAdmin)
        {holder.AdminLayout.setVisibility(View.VISIBLE);}

        loadImage(holder, plant);

        holder.viewDetailsLayout.setOnClickListener(v -> viewPlantDetails(plant));
        holder.viewFeedbacksLayout.setOnClickListener(v -> viewUserFeedbacks(plant));
        holder.editDetailsLayout.setOnClickListener(v -> editPlantListener.onEditPlant(plant));
        holder.deletePlantLayout.setOnClickListener(v -> showDeleteConfirmationDialog(plant));
    }

    private void loadImage(PlantViewHolder holder, PlantModel plant) {
        if (plant.getImage() != null && !plant.getImage().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .timeout(10000);

            Glide.with(context)
                    .load(plant.getImage())
                    .apply(requestOptions)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.plantImageView);
        } else {
            holder.plantImageView.setImageResource(R.drawable.ic_launcher_background); // Placeholder image
        }
    }

    private void showDeleteConfirmationDialog(PlantModel plant) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Plant")
                .setMessage("Are you sure you want to delete the plant: " + plant.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deletePlant(plant))
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePlant(PlantModel plant) {
        ProgressDialogHelper.showProgressDialog(context, "Deleting Plant...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl(plant.getImage());

        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Image deleted successfully
            db.collection("plants").document(plant.getPlantID())
                    .delete()
                    .addOnSuccessListener(aVoid1 -> {
                        deletePlantListener.onDeletePlant(plant);
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(context, "Plant deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        ProgressDialogHelper.dismissProgressDialog();
                        Toast.makeText(context, "Failed to delete plant", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(exception -> {
            // An error occurred
            ProgressDialogHelper.dismissProgressDialog();
            Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
        });
    }

    private void viewPlantDetails(PlantModel plant) {
        Intent intent = new Intent(context, ViewAdminPlantDetailsActivity.class);
        intent.putExtra("plant", plant);
        context.startActivity(intent);
    }

    private void viewUserFeedbacks(PlantModel plant) {
        Intent intent = new Intent(context, DisplayUsersFeedbackActivity.class);
        intent.putExtra("plant", plant);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImageView;
        TextView plantNameTextView;
        TextView scientificNameTextView;
        View viewDetailsLayout;
        View viewFeedbacksLayout;
        View editDetailsLayout;
        View deletePlantLayout;
        View AdminLayout;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImageView = itemView.findViewById(R.id.item_image_view);
            plantNameTextView = itemView.findViewById(R.id.item_name_text_view);
            scientificNameTextView = itemView.findViewById(R.id.scientific_name_text_view);
            viewDetailsLayout = itemView.findViewById(R.id.ViewPlantingDetailsLayout);
            viewFeedbacksLayout = itemView.findViewById(R.id.ViewUsersFeedBackLayout);
            editDetailsLayout = itemView.findViewById(R.id.EditPlantingDetailsLayout);
            deletePlantLayout = itemView.findViewById(R.id.DeletePlantLayout);
            AdminLayout = itemView.findViewById(R.id.AdminLayout);

        }
    }

    public interface EditPlantListener {
        void onEditPlant(PlantModel plant);
    }

    public interface DeletePlantListener {
        void onDeletePlant(PlantModel plant);
    }
}
