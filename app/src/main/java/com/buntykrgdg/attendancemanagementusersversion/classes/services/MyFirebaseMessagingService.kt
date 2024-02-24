package com.buntykrgdg.attendancemanagementusersversion.classes.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.buntykrgdg.attendancemanagementusersversion.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("TAG", remoteMessage.toString())
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "ChannelId",
            "My Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, "ChannelId")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .build()
        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
        val instituteId = sharedPref.getString("EmpInstituteId", "")
        val empPhNo = sharedPref.getString("PhoneNumber", "PhoneNumber")

        if (instituteId.isNullOrEmpty() || empPhNo.isNullOrEmpty()) {
            Log.d(ContentValues.TAG, "No institute ID/employee phone number found")
            return
        }
        val map = mapOf("fcmToken" to token)
        val databaseRef = FirebaseFirestore.getInstance()
            .collection("Institutions")
            .document(instituteId)
            .collection("Employees")
            .document(empPhNo)

        databaseRef.set(map, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "FCM token added to database successfully")
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "FCM token could not be added to database", e)
            }
    }
}