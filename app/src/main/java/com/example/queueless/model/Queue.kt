package com.example.queueless.model

import com.google.firebase.Timestamp

data class Queue(
    val id: String = "",
    val userId: String = "",
    val namaMahasiswa: String = "",
    val nimMahasiswa: String = "",
    val serviceId: String = "",
    val namaLayanan: String = "",
    val nomorAntrean: String = "",
    val nomorUrut: Int = 0,
    val status: String = "Menunggu",
    val tanggal: String = "",
    val waktuAmbil: Timestamp? = null
)