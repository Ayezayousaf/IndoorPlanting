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
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlantCareReminderFragment extends Fragment implements TaskAdapter.TaskAdapterListener {
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private RelativeLayout emptyStateContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_care_reminder, container, false);

        initViews(view);
        setupRecyclerView();
        setupToolbar(view);

        List<Task> tasks = loadTasks();
        taskAdapter = new TaskAdapter(getContext(), tasks, this);
        tasksRecyclerView.setAdapter(taskAdapter);

        updateEmptyStateVisibility(tasks);

        setupAddTaskButton(view);

        return view;
    }

    private void initViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
    }

    private void setupRecyclerView() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Plant Care Reminders");
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
    }

    private void setupAddTaskButton(View view) {
        ImageView addTaskButton = view.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText editTextPlantName = dialog.findViewById(R.id.editTextPlantName);
        Spinner spinnerTask = dialog.findViewById(R.id.spinnerTask);
        TextView textViewDate = dialog.findViewById(R.id.textViewDate);
        TextView textViewTime = dialog.findViewById(R.id.textViewTime);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.task_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTask.setAdapter(adapter);

        setupDatePicker(textViewDate);
        setupTimePicker(textViewTime);

        com.google.android.material.button.MaterialButton buttonOk = dialog.findViewById(R.id.buttonOk);
        com.google.android.material.button.MaterialButton buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonOk.setOnClickListener(v -> {
            String plantName = editTextPlantName.getText().toString().trim();
            String task = spinnerTask.getSelectedItem().toString();
            String date = textViewDate.getText().toString();
            String time = textViewTime.getText().toString();

            if (validateFields(plantName, task, date, time)) {
                saveTask(plantName, task, date, time);
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean validateFields(String plantName, String task, String date, String time) {
        if (plantName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the plant name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (task.equals("Select Task")) {
            Toast.makeText(getContext(), "Please select a task", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(getContext(), "Please select a time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupDatePicker(TextView textView) {
        textView.setOnClickListener(view -> {
            Calendar currentDate = Calendar.getInstance();
            new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                textView.setText(dateFormat.format(selectedDate.getTime()));
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTimePicker(TextView textView) {
        textView.setOnClickListener(view -> {
            Calendar currentTime = Calendar.getInstance();
            new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                textView.setText(timeFormat.format(selectedTime.getTime()));
            }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show();
        });
    }

    private void saveTask(String plantName, String task, String date, String time) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GardeningAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String tasksJson = sharedPreferences.getString("tasks", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(tasksJson, type);

        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        Task newTask = new Task(plantName, task, date + " - " + time);
        tasks.add(newTask);

        tasksJson = gson.toJson(tasks);
        editor.putString("tasks", tasksJson);
        editor.apply();

        scheduleNotification(newTask);

        taskAdapter.addTask(newTask);
        updateEmptyStateVisibility(tasks);
        Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();
    }

    private void scheduleNotification(Task task) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getContext(), TaskReminderReceiver.class);
        intent.putExtra("plantName", task.getPlantName());
        intent.putExtra("taskDescription", task.getTaskDescription());
        intent.putExtra("dateTime", task.getDateTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(task.getDateTime()));
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private List<Task> loadTasks() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GardeningAppPrefs", Context.MODE_PRIVATE);
        String tasksJson = sharedPreferences.getString("tasks", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(tasksJson, type);
        return tasks != null ? tasks : new ArrayList<>();
    }

    void updateEmptyStateVisibility(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTasksUpdated(List<Task> tasks) {
        updateEmptyStateVisibility(tasks);
    }
}
