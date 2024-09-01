package com.nextgen.indoorplanting;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.MultipartBody;

public interface PlantNetApi {
    @Multipart
    @POST("identify/all")
    Call<PlantNetResponse> identifyPlant(
            @Part MultipartBody.Part image,
            @Query("api-key") String apiKey,
            @Query("include-related-images") boolean includeRelatedImages
    );
}
