package com.nextgen.indoorplanting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ImagePickerHelper {

    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 3;
    private final Activity activity;
    private final Fragment fragment;
    private Uri pictureUri;
    private File photoFile;

    public ImagePickerHelper(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public ImagePickerHelper(Fragment fragment) {
        this.fragment = fragment;
        this.activity = null;
    }

    public void showPicturePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity != null ? activity : fragment.getContext());
        builder.setTitle("Select Picture");
        builder.setItems(new CharSequence[]{"Select from Gallery", "Take Photo"}, (dialog, which) -> {
            if (which == 0) {
                selectImageFromGallery();
            } else if (which == 1) {
                takePhoto();
            }
        });
        builder.show();
    }

    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (activity != null) {
            activity.startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        } else {
            fragment.startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        }
    }

    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity((activity != null ? activity : fragment.getActivity()).getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                pictureUri = FileProvider.getUriForFile(activity != null ? activity : fragment.getContext(), "com.nextgen.indoorplanting.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (activity != null) {
                    activity.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    fragment.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = (activity != null ? activity : fragment.getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data, OnImagePickedListener listener) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            handleGalleryResult(data, listener);
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            handleCameraResult(listener);
        }
    }

    private void handleGalleryResult(@Nullable Intent data, OnImagePickedListener listener) {
        if (data != null) {
            pictureUri = data.getData();
            String fileName = getFileNameFromUri(pictureUri);
            copyImageToAppSpecificDirectory(pictureUri);
            listener.onImagePicked(pictureUri, fileName);
        }
    }

    private void handleCameraResult(OnImagePickedListener listener) {
        if (photoFile != null) {
            pictureUri = Uri.fromFile(photoFile);
            String fileName = photoFile.getName();
            copyImageToAppSpecificDirectory(pictureUri);
            listener.onImagePicked(pictureUri, fileName);
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = (activity != null ? activity : fragment.getContext()).getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }

    private void copyImageToAppSpecificDirectory(Uri imageUri) {
        try {
            InputStream inputStream = (activity != null ? activity : fragment.getContext()).getContentResolver().openInputStream(imageUri);
            File storageDir = new File((activity != null ? activity : fragment.getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File newFile = new File(storageDir, UUID.randomUUID().toString() + ".jpg");
            OutputStream outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            pictureUri = Uri.fromFile(newFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity != null ? activity : fragment.getContext(), "Failed to copy image.", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnImagePickedListener {
        void onImagePicked(Uri imageUri, String fileName);
    }
}
