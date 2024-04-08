package com.login.pdtracker;

import android.content.Context;
import android.widget.Toast;

public class AbnormalConditionAlert {

    private Context context;

    public AbnormalConditionAlert(Context context) {
        this.context = context;
    }

    // Method to view the alert of patient's abnormal condition
    public void viewAlert(String patientName, String condition) {
        String alertMessage = "Alert: Abnormal condition detected for patient " + patientName + ". Condition: " + condition;
        Toast.makeText(context, alertMessage, Toast.LENGTH_LONG).show();
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
        // Code to send notification to patient
    }

    // Method to send notification to caregiver
    private void sendNotificationToCaregiver(String notification) {
        // Code to send notification to caregiver
    }
}

