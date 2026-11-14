package com.example.queueless.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val nomor = message.data["nomorAntrean"]
            ?: message.notification?.title
            ?: return
        val layanan = message.data["namaLayanan"]
            ?: message.notification?.body
            ?: ""
        NotificationHelper.showQueueCalledNotification(applicationContext, nomor, layanan)
    }
}