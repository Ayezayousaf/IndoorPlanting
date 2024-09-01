package com.nextgen.indoorplanting;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "Task Reminder Notifications";
    private static final String CHANNEL_DESC = "Shows notifications for task reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context);
        } else {
            showNotification(context, intent);
        }
    }

    private void showNotification(Context context, Intent intent) {
        String plantName = intent.getStringExtra("plantName");
        String taskDescription = intent.getStringExtra("taskDescription");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Plant Care Reminder")
                .setContentText("It's time to " + taskDescription.toLowerCase() + " your " + plantName + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void rescheduleAlarms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GardeningAppPrefs", Context.MODE_PRIVATE);
        String tasksJson = sharedPreferences.getString("tasks", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(tasksJson, type);

        if (tasks != null) {
            for (Task task : tasks) {
                scheduleNotification(context, task);
            }
        }
    }

    private void scheduleNotification(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("plantName", task.getPlantName());
        intent.putExtra("taskDescription", task.getTaskDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy - HH:mm", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(task.getDateTime()));
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
