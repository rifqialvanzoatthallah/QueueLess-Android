package com.example.queueless.model

data class User(
    val uid: String = "",
    val nama: String = "",
    val nim: String = "",
    val email: String = "",
    val role: String = "mahasiswa",
    val fcmToken: String = ""
)