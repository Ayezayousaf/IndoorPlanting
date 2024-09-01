package com.nextgen.indoorplanting;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PlantModel implements Serializable {

    private static final long serialVersionUID = 1L; // Unique ID for serialization

    private String plantID;
    private String name;
    private String scientificName;
    private String image;
    private PlantingDetails plantingDetails;
    private List<String> healthCareTips;
    private List<Map<String, String>> commonIssues;

    public PlantModel() {
        // Default constructor required for calls to DataSnapshot.getValue(PlantModel.class)
    }

    public PlantModel(String plantID, String name, String scientificName, String image, PlantingDetails plantingDetails, List<String> healthCareTips, List<Map<String, String>> commonIssues) {
        this.plantID = plantID;
        this.name = name;
        this.scientificName = scientificName;
        this.image = image;
        this.plantingDetails = plantingDetails;
        this.healthCareTips = healthCareTips;
        this.commonIssues = commonIssues;
    }

    public String getPlantID() {
        return plantID;
    }

    public void setPlantID(String plantID) {
        this.plantID = plantID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public PlantingDetails getPlantingDetails() {
        return plantingDetails;
    }

    public void setPlantingDetails(PlantingDetails plantingDetails) {
        this.plantingDetails = plantingDetails;
    }

    public List<String> getHealthCareTips() {
        return healthCareTips;
    }

    public void setHealthCareTips(List<String> healthCareTips) {
        this.healthCareTips = healthCareTips;
    }

    public List<Map<String, String>> getCommonIssues() {
        return commonIssues;
    }

    public void setCommonIssues(List<Map<String, String>> commonIssues) {
        this.commonIssues = commonIssues;
    }

    @Override
    public String toString() {
        return "PlantModel{" +
                "plantID='" + plantID + '\'' +
                ", name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", image='" + image + '\'' +
                ", plantingDetails=" + plantingDetails +
                ", healthCareTips=" + healthCareTips +
                ", commonIssues=" + commonIssues +
                '}';
    }

    public static class PlantingDetails implements Serializable {

        private static final long serialVersionUID = 1L; // Unique ID for serialization

        private String soilType;
        private String sunlight;
        private String watering;
        private String season;
        private String depth;

        public PlantingDetails() {
            // Default constructor required for calls to DataSnapshot.getValue(PlantingDetails.class)
        }

        public PlantingDetails(String soilType, String sunlight, String watering, String season, String depth) {
            this.soilType = soilType;
            this.sunlight = sunlight;
            this.watering = watering;
            this.season = season;
            this.depth = depth;
        }

        public String getSoilType() {
            return soilType;
        }

        public void setSoilType(String soilType) {
            this.soilType = soilType;
        }

        public String getSunlight() {
            return sunlight;
        }

        public void setSunlight(String sunlight) {
            this.sunlight = sunlight;
        }

        public String getWatering() {
            return watering;
        }

        public void setWatering(String watering) {
            this.watering = watering;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }

        public String getDepth() {
            return depth;
        }

        public void setDepth(String depth) {
            this.depth = depth;
        }

        @Override
        public String toString() {
            return "PlantingDetails{" +
                    "soilType='" + soilType + '\'' +
                    ", sunlight='" + sunlight + '\'' +
                    ", watering='" + watering + '\'' +
                    ", season='" + season + '\'' +
                    ", depth='" + depth + '\'' +
                    '}';
        }
    }
}
