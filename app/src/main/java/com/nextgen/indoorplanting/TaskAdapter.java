package com.nextgen.indoorplanting;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskAdapterListener {
        void onTasksUpdated(List<Task> tasks);
    }

    private Context context;
    private List<Task> tasks;
    private SharedPreferences sharedPreferences;
    private TaskAdapterListener listener;

    public TaskAdapter(Context context, List<Task> tasks, TaskAdapterListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.sharedPreferences = context.getSharedPreferences("GardeningAppPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.plantNameTextView.setText(task.getPlantName());
        holder.taskDescriptionTextView.setText(task.getTaskDescription());
        holder.dateTimeTextView.setText(formatDateTime(task.getDateTime()));

        holder.editButton.setOnClickListener(v -> showEditDialog(task));
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasksToSharedPreferences();
        notifyDataSetChanged();
        listener.onTasksUpdated(tasks);
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == updatedTask.getId()) {
                cancelNotification(tasks.get(i)); // Cancel the existing notification
                tasks.set(i, updatedTask);
                scheduleNotification(updatedTask); // Schedule a new notification
                break;
            }
        }
        saveTasksToSharedPreferences();
        notifyDataSetChanged();
        listener.onTasksUpdated(tasks);
    }

    public void removeTask(Task taskToRemove) {
        tasks.removeIf(task -> task.getId() == taskToRemove.getId());
        cancelNotification(taskToRemove); // Cancel the existing notification
        saveTasksToSharedPreferences();
        notifyDataSetChanged();
        listener.onTasksUpdated(tasks);
    }

    private void showEditDialog(Task task) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText editTextPlantName = dialog.findViewById(R.id.editTextPlantName);
        Spinner spinnerTask = dialog.findViewById(R.id.spinnerTask);
        TextView textViewDate = dialog.findViewById(R.id.textViewDate);
        TextView textViewTime = dialog.findViewById(R.id.textViewTime);

        editTextPlantName.setText(task.getPlantName());
        String[] dateTime = task.getDateTime().split(" - ");
        textViewDate.setText(dateTime[0]);
        textViewTime.setText(dateTime[1]);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.task_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTask.setAdapter(adapter);

        String taskDescription = task.getTaskDescription();
        int spinnerPosition = adapter.getPosition(taskDescription);
        spinnerTask.setSelection(spinnerPosition);

        setupDatePicker(textViewDate, context);
        setupTimePicker(textViewTime, context);

        MaterialButton buttonUpdate = dialog.findViewById(R.id.buttonOk);
        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonUpdate.setText("Update");
        buttonUpdate.setOnClickListener(v -> {
            String plantName = editTextPlantName.getText().toString();
            String newTaskDescription = spinnerTask.getSelectedItem().toString();
            String date = textViewDate.getText().toString();
            String time = textViewTime.getText().toString();
            Task updatedTask = new Task(plantName, newTaskDescription, date + " - " + time);
            updatedTask.setId(task.getId());
            updateTask(updatedTask);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(context)
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    removeTask(task);
                    listener.onTasksUpdated(tasks);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void saveTasksToSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String tasksJson = gson.toJson(tasks);
        editor.putString("tasks", tasksJson);
        editor.apply();
    }

    private void setupDatePicker(TextView textView, Context context) {
        textView.setOnClickListener(view -> {
            Calendar currentDate = Calendar.getInstance();
            new DatePickerDialog(context, (datePicker, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                textView.setText(dateFormat.format(selectedDate.getTime()));
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTimePicker(TextView textView, Context context) {
        textView.setOnClickListener(view -> {
            Calendar currentTime = Calendar.getInstance();
            new TimePickerDialog(context, (timePicker, hourOfDay, minute) -> {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                textView.setText(timeFormat.format(selectedTime.getTime()));
            }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show();
        });
    }

    private void scheduleNotification(Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("plantName", task.getPlantName());
        intent.putExtra("taskDescription", task.getTaskDescription());
        intent.putExtra("dateTime", task.getDateTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(task.getDateTime()));
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelNotification(Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private String formatDateTime(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTime;
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView plantNameTextView;
        TextView taskDescriptionTextView;
        TextView dateTimeTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.PlantNameTxtView);
            taskDescriptionTextView = itemView.findViewById(R.id.TaskNameTxtView);
            dateTimeTextView = itemView.findViewById(R.id.ReminderDateAndTimeTxtView);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
