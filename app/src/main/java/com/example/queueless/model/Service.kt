package com.example.queueless.model

import com.google.firebase.firestore.PropertyName

data class Service(
    val id: String = "",
    val namaLayanan: String = "",
    val kodePrefix: String = "",
    val deskripsi: String = "",
    val estimasiMenit: Int = 0,
    val kuotaHarian: Int = 0,
    @get:PropertyName("isAktif")
    @set:PropertyName("isAktif")
    var isAktif: Boolean = true,
    val antreanTerakhir: Int = 0,
    val tanggalTerakhir: String = ""
)