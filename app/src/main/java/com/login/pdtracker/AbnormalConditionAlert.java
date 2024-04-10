package com.login.pdtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class AbnormalConditionAlert{

        private Context context; // initialise context
        private static final String CHANNEL_ID = "abnormal_condition_notification_channel";
        //initialise final channel id
        private static final int NOTIFICATION_ID_PATIENT = 1;
        //initialise final notification id for patient as 1
        private static final int NOTIFICATION_ID_CAREGIVER = 2;
        //initialise final notification id for caregiver as 2

    public AbnormalConditionAlert(Context context) {
        this.context =context;
    }

    //method used to view the alert of patient's abnormal condition
    public void viewAlert(String patientName, String condition ) {
        String alertMessage = "Alert: Abnormal condition detected for patient " + patientName + ". Condition: " +condition;
        //display alert message using toast
        //Toast.makeText(context, alertMessage, Toast.LENGTH_LONG).show();
    }

    // Method to send notification to patient and caregiver
    public void sendNotification(String patientName, String caregiverName) {
        // Code to send notification to patient
        String patientNotification = "Dear " + patientName + ", an abnormal condition has been detected. Please take necessary action.";
        sendNotificationToPatient(patientNotification);

        // Code to send notification to caregiver
        String caregiverNotification = "Dear " + caregiverName + ", an abnormal condition has been detected for patient " + patientName + ". Please take necessary action.";
        sendNotificationToCaregiver(caregiverNotification);
    }

    // Method to send notification to patient
    private void sendNotificationToPatient(String notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Patient Notification";
            String description = "Notification channel for patient";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Abnormal Condition Alert")
                    .setContentText(notification)
                    .setPriority(Notification.PRIORITY_HIGH);
        }

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID_PATIENT, builder.build());
    }

    // Method to send notification to caregiver
    private void sendNotificationToCaregiver(String notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Caregiver Notification";
            String description = "Notification channel for caregiver";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }


    }

}