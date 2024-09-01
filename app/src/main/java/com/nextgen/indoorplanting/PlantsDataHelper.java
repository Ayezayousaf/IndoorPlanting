package com.nextgen.indoorplanting;

import java.util.HashMap;
import java.util.Map;

public class PlantsDataHelper {

    private static final Map<String, PlantDetails> plantData = new HashMap<>();

    static {
        // Soil Types
        plantData.put("Loamy", new PlantDetails(R.drawable.loam_soil, "%s thrives in loamy soil due to its excellent drainage and nutrient retention. This soil type ensures that %s receives the right balance of moisture and nutrients, promoting healthy growth."));
        plantData.put("Sandy", new PlantDetails(R.drawable.sandy_soil, "For %s, sandy soil is ideal as it drains quickly, preventing root rot. This soil type keeps %s's roots healthy by avoiding waterlogging, making it perfect for plants that prefer drier conditions."));
        plantData.put("Clay", new PlantDetails(R.drawable.clay_soil, "%s benefits from clay soil's moisture retention properties. This soil type keeps %s well-hydrated, which is crucial for plants that require consistent moisture levels."));
        plantData.put("Silty", new PlantDetails(R.drawable.silty_soil, "Silty soil's high nutrient content supports %s's robust growth. This soil type is ideal for %s as it holds moisture well and provides essential nutrients, ensuring lush and healthy development."));
        plantData.put("Peaty", new PlantDetails(R.drawable.peat_soil, "%s flourishes in peaty soil, which retains moisture and is rich in organic matter. This environment is perfect for %s, especially if the plant requires a lot of moisture and slightly acidic conditions."));
        plantData.put("Chalky", new PlantDetails(R.drawable.chilky_soil, "Chalky soil's alkaline nature is suited for %s if the plant prefers less acidic conditions. This soil type ensures that %s gets the necessary lime content, supporting its growth and health."));
        plantData.put("Saline", new PlantDetails(R.drawable.saline_soil, "%s can tolerate saline soil, which is challenging for most plants. This soil type supports %s's growth by managing high salt levels, making it suitable for salt-tolerant plants."));

        // Sunlight Types
        plantData.put("Full Sun", new PlantDetails(R.drawable.full_sun, "%s needs full sun to thrive. At least 6 hours of direct sunlight each day helps %s grow and bloom, providing the necessary energy for photosynthesis."));
        plantData.put("Partial Sun", new PlantDetails(R.drawable.partial_sun, "Partial sun is perfect for %s, which requires 3-6 hours of direct sunlight. This amount of sun helps %s grow without getting scorched, ideal for plants that need moderate light."));
        plantData.put("Partial Shade", new PlantDetails(R.drawable.partial_shade, "Partial shade suits %s well, offering 3-6 hours of sunlight. This lighting condition protects %s from harsh sun, preventing leaf burn and promoting healthy growth."));
        plantData.put("Full Shade", new PlantDetails(R.drawable.full_shade, "%s thrives in full shade, needing less than 3 hours of sunlight. This environment keeps %s's leaves lush and green, ideal for plants that prefer indirect light."));

        // Watering Types
        plantData.put("Light", new PlantDetails(R.drawable.light_watering, "Light watering is best for %s, which prefers drier conditions. This watering schedule ensures %s's roots don't rot, keeping the plant healthy and vibrant."));
        plantData.put("Moderate", new PlantDetails(R.drawable.moderate_watering, "Moderate watering keeps %s's soil consistently moist, ideal for most plants. This schedule helps %s stay hydrated without being waterlogged, promoting steady growth."));
        plantData.put("Frequent", new PlantDetails(R.drawable.frequent_watering, "%s requires frequent watering to thrive. Regular moisture keeps %s's roots healthy, essential for tropical and moisture-loving plants."));

        // Planting Seasons
        plantData.put("Spring", new PlantDetails(R.drawable.spring, "Spring is the best time to plant %s, with moderate temperatures and increased rainfall. This season provides %s with optimal growing conditions, leading to vibrant growth."));
        plantData.put("Summer", new PlantDetails(R.drawable.summer, "Summer planting is suitable for heat-tolerant %s. Ensure adequate watering and shade to help %s thrive in the hot weather, supporting its growth and health."));
        plantData.put("Autumn", new PlantDetails(R.drawable.autumn, "Autumn is perfect for planting %s, helping it establish strong roots before winter. The cooler temperatures and increased rainfall support %s's robust growth."));
        plantData.put("Winter", new PlantDetails(R.drawable.winter, "Winter is challenging for most plants, but %s can survive with adequate protection. Mulching and covering %s during cold snaps help it thrive through winter."));

        // Planting Depths
        plantData.put("2 inches", new PlantDetails(R.drawable.depth_2_inches, "Planting %s at a depth of 2 inches ensures it receives adequate warmth and moisture. This shallow depth is perfect for %s, promoting quick germination and growth."));
        plantData.put("4 inches", new PlantDetails(R.drawable.depth_4_inches, "%s benefits from being planted at 4 inches deep, providing stability and moisture. This depth helps %s's roots develop strong and healthy, supporting its growth."));
        plantData.put("6 inches", new PlantDetails(R.drawable.depth_6_inches, "A planting depth of 6 inches is recommended for %s, allowing strong root development. This depth supports %s's growth by providing the necessary conditions for its roots."));
        plantData.put("8 inches", new PlantDetails(R.drawable.depth_8_inches, "Planting %s at a depth of 8 inches is ideal for large bulbs and tubers. This deep planting ensures stability for %s, providing the necessary support for its growth."));
    }

    public static PlantDetails getPlantDetails(String attributeType, String plantName) {
        PlantDetails details = plantData.getOrDefault(attributeType, new PlantDetails(R.drawable.ic_image_select, "No description available."));
        return new PlantDetails(details.getImageResId(), String.format(details.getDescription(), plantName, plantName));
    }

    public static class PlantDetails {
        private int imageResId;
        private String description;

        public PlantDetails(int imageResId, String description) {
            this.imageResId = imageResId;
            this.description = description;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getDescription() {
            return description;
        }
    }
}
