package com.nextgen.indoorplanting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlantDetectorFragment extends Fragment implements ImagePickerHelper.OnImagePickedListener {
    private static final String BASE_URL = "https://my-api.plantnet.org/v2/";
    private static final String API_KEY = "2b10aFzqTMyc2ociL0IwLaAiyu";
    private static final int TIMEOUT_DURATION = 30000; // 30 seconds

    private Call<PlantNetResponse> apiCall;
    private RecyclerView recyclerView;
    private PlantDetailsAdapter adapter;
    private ImagePickerHelper imagePickerHelper;
    private ImageCompressHelper imageCompressHelper;
    private ImageView selectedImageView;
    private MaterialButton selectPlantImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_detector, container, false);

        initViews(view);
        setupRecyclerView();
        setupImagePicker();
        setupToolbar(view);
        setupSelectImageButton();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        selectedImageView = view.findViewById(R.id.selected_image);
        selectPlantImageButton = view.findViewById(R.id.Select_Plant_Image);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupImagePicker() {
        imagePickerHelper = new ImagePickerHelper(this);
        imageCompressHelper = new ImageCompressHelper(getContext());
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Plant Identifier");
    }

    private void setupSelectImageButton() {
        selectPlantImageButton.setOnClickListener(v -> imagePickerHelper.showPicturePickerDialog());
    }

    private void identifyPlant(File imageFile) {
        ProgressDialogHelper.showProgressDialog(getContext(), "Identifying plant, please wait...");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlantNetApi api = retrofit.create(PlantNetApi.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", imageFile.getName(), requestFile);

        apiCall = api.identifyPlant(body, API_KEY, true);
        apiCall.enqueue(new Callback<PlantNetResponse>() {
            @Override
            public void onResponse(Call<PlantNetResponse> call, Response<PlantNetResponse> response) {
                ProgressDialogHelper.dismissProgressDialog();
                if (response.isSuccessful() && response.body() != null) {
                    displayPlantDetails(response.body().results);
                } else {
                    Toast.makeText(getContext(), "Identification failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlantNetResponse> call, Throwable t) {
                ProgressDialogHelper.dismissProgressDialog();
                Toast.makeText(getContext(), "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        setupTimeout();
    }

    private void setupTimeout() {
        new Handler().postDelayed(() -> {
            if (apiCall != null && !apiCall.isExecuted()) {
                apiCall.cancel();
                ProgressDialogHelper.dismissProgressDialog();
                Toast.makeText(getContext(), "Identification timed out", Toast.LENGTH_SHORT).show();
            }
        }, TIMEOUT_DURATION);
    }

    private void displayPlantDetails(List<PlantNetResponse.Result> results) {
        adapter = new PlantDetailsAdapter(getContext(), results);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerHelper.handleActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onImagePicked(Uri imageUri, String fileName) {
        Uri compressedUri = imageCompressHelper.compressImage(imageUri);
        if (compressedUri != null) {
            selectedImageView.setImageURI(compressedUri);
            selectedImageView.setVisibility(View.VISIBLE);
            File imageFile = new File(compressedUri.getPath());
            identifyPlant(imageFile);
            selectPlantImageButton.setText("Scan Another Picture");
        } else {
            Toast.makeText(getContext(), "Failed to compress image.", Toast.LENGTH_SHORT).show();
        }
    }
}
