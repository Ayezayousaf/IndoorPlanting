package com.nextgen.indoorplanting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.stream.Collectors;

public class PlantDetailsAdapter extends RecyclerView.Adapter<PlantDetailsAdapter.PlantDetailsViewHolder> {
    private Context context;
    private List<PlantNetResponse.Result> plantDetails;

    public PlantDetailsAdapter(Context context, List<PlantNetResponse.Result> plantDetails) {
        this.context = context;
        this.plantDetails = plantDetails;
    }

    @NonNull
    @Override
    public PlantDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.identified_plant_details, parent, false);
        return new PlantDetailsViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlantDetailsViewHolder holder, int position) {
        PlantNetResponse.Result plantDetail = plantDetails.get(position);

        holder.plantName.setText(plantDetail.species.scientificNameWithoutAuthor);
        holder.commonNames.setText(String.join(", ", plantDetail.species.commonNames));
        holder.family.setText(plantDetail.species.family.scientificNameWithoutAuthor);
        holder.genus.setText(plantDetail.species.genus.scientificNameWithoutAuthor);
        holder.confidence.setText(String.format("%.2f%%", plantDetail.score * 100));

        if (plantDetail.images != null && !plantDetail.images.isEmpty()) {
            holder.imagesRecyclerView.setVisibility(View.VISIBLE);
            List<String> imageUrls = plantDetail.images.stream()
                    .map(image -> image.url.originalUrl)
                    .collect(Collectors.toList());

            ImageAdapter imageAdapter = new ImageAdapter(context, imageUrls);
            holder.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.imagesRecyclerView.setAdapter(imageAdapter);
        } else {
            holder.imagesRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return plantDetails.size();
    }

    public static class PlantDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;
        TextView commonNames;
        TextView family;
        TextView genus;
        TextView confidence;
        RecyclerView imagesRecyclerView;

        public PlantDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plant_name);
            commonNames = itemView.findViewById(R.id.common_names);
            family = itemView.findViewById(R.id.family);
            genus = itemView.findViewById(R.id.genus);
            confidence = itemView.findViewById(R.id.confidence);
            imagesRecyclerView = itemView.findViewById(R.id.images_recycler_view);
        }
    }
}

