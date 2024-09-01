package com.nextgen.indoorplanting;
public class Task {
    private String plantName;
    private String taskDescription;
    private String dateTime;
    private int id;

    public Task(String plantName, String taskDescription, String dateTime) {
        this.plantName = plantName;
        this.taskDescription = taskDescription;
        this.dateTime = dateTime;
        this.id = (int) System.currentTimeMillis(); // Generate unique ID
    }

    public String getPlantName() {
        return plantName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
