package com.nextgen.indoorplanting;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class ImageCompressHelper {

    private Context context;

    public ImageCompressHelper(Context context) {
        this.context = context;
    }

    public Uri compressImage(Uri imageUri) {
        File originalFile = new File(getRealPathFromURI(imageUri));

        try {
            File compressedFile = new Compressor(context)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFile(originalFile);

            return Uri.fromFile(compressedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
