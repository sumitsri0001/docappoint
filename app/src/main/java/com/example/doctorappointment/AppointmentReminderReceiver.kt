package com.example.doctorappointment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class AppointmentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentTitle = intent.getStringExtra("title") ?: "Doctor Appointment"
        val doctorLat = intent.getDoubleExtra("doctorLat", 0.0)
        val doctorLng = intent.getDoubleExtra("doctorLng", 0.0)
        val patientLat = intent.getDoubleExtra("patientLat", 0.0)
        val patientLng = intent.getDoubleExtra("patientLng", 0.0)
        val mapsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$patientLat,$patientLng&destination=$doctorLat,$doctorLng")
        val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mapsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "appointment_channel",
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "appointment_channel")
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Appointment Reminder")
            .setContentText("You have an appointment in 1 hour: $appointmentTitle \n Tap to see directions")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
