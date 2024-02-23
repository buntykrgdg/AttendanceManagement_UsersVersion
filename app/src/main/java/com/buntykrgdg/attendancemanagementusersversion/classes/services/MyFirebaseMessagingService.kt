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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onCreate() {
        //getfcmtoken()
        super.onCreate()
    }

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


    private fun getFcmToken() = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Retrieve the FCM token in a non-blocking way
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            // Continue only if the shared preferences are available
            val sharedPref = getSharedPreferences("AttendanceManagementUV", Context.MODE_PRIVATE)
            val instituteId = sharedPref?.getString("EmpInstituteId", "")
            val employeePhno = sharedPref?.getString("PhoneNumber", "")
            if (instituteId.isNullOrEmpty() || employeePhno.isNullOrEmpty()) {
                Log.d(ContentValues.TAG, "No institute ID/employee ID found")
                return@launch
            }

            // Prepare the data to be updated in Firestore
            val map = mutableMapOf<String, Any>("fcmToken" to fcmToken)
            val databaseRef = FirebaseFirestore.getInstance().collection("Institutions")
                .document(instituteId)
                .collection("Employees")
                .document(employeePhno)

            // Merge the FCM token into the Firestore document
            databaseRef.set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "Error fetching FCM token or updating Firestore: ${e.message}")
        }
    }
}