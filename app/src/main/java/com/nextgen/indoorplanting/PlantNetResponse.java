package com.nextgen.indoorplanting;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlantNetResponse {
    @SerializedName("results")
    public List<Result> results;

    public static class Result {
        @SerializedName("score")
        public float score;

        @SerializedName("species")
        public Species species;

        @SerializedName("images")
        public List<Image> images;
    }

    public static class Species {
        @SerializedName("scientificNameWithoutAuthor")
        public String scientificNameWithoutAuthor;

        @SerializedName("commonNames")
        public List<String> commonNames;

        @SerializedName("family")
        public Family family;

        @SerializedName("genus")
        public Genus genus;
    }

    public static class Family {
        @SerializedName("scientificNameWithoutAuthor")
        public String scientificNameWithoutAuthor;
    }

    public static class Genus {
        @SerializedName("scientificNameWithoutAuthor")
        public String scientificNameWithoutAuthor;
    }

    public static class Image {
        @SerializedName("url")
        public Url url;
    }

    public static class Url {
        @SerializedName("o")
        public String originalUrl;
    }
}

