package com.example.queueless.repository

import com.example.queueless.model.Queue
import com.example.queueless.model.Service
import com.example.queueless.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QueueRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun observeAllServices(
        onUpdate: (List<Service>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Service::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.kodePrefix } ?: emptyList()
                onUpdate(list)
            }
    }

    fun saveService(service: Service, onResult: (Result<Unit>) -> Unit) {
        val docRef = if (service.id.isEmpty()) {
            db.collection("services").document()
        } else {
            db.collection("services").document(service.id)
        }
        val finalService = service.copy(id = docRef.id)
        docRef.set(finalService)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteService(serviceId: String, onResult: (Result<Unit>) -> Unit) {
        db.collection("services").document(serviceId)
            .delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeTodayQueues(
        onUpdate: (List<Queue>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val today = getTodayDateString()
        return db.collection("queues")
            .whereEqualTo("tanggal", today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Queue::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.nomorUrut } ?: emptyList()
                onUpdate(list)
            }
    }

    fun observeStudentTodayQueues(
        userId: String,
        onUpdate: (List<Queue>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val today = getTodayDateString()
        return db.collection("queues")
            .whereEqualTo("userId", userId)
            .whereEqualTo("tanggal", today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Queue::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.nomorUrut } ?: emptyList()
                onUpdate(list)
            }
    }

    fun observeServiceTodayQueues(
        serviceId: String,
        onUpdate: (List<Queue>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val today = getTodayDateString()
        return db.collection("queues")
            .whereEqualTo("serviceId", serviceId)
            .whereEqualTo("tanggal", today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Queue::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.nomorUrut } ?: emptyList()
                onUpdate(list)
            }
    }

    fun takeQueueTransaction(
        user: User,
        service: Service,
        onResult: (Result<String>) -> Unit
    ) {
        val today = getTodayDateString()
        db.collection("queues")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("tanggal", today)
            .get()
            .addOnSuccessListener { existingSnapshot ->
                val hasActive = existingSnapshot.documents.any { doc ->
                    val status = doc.getString("status")
                    status == "Menunggu" || status == "Dipanggil"
                }
                if (hasActive) {
                    onResult(Result.failure(Exception("ACTIVE_QUEUE_EXISTS")))
                    return@addOnSuccessListener
                }

                val serviceRef = db.collection("services").document(service.id)
                val newQueueRef = db.collection("queues").document()

                db.runTransaction { transaction ->
                    val snap = transaction.get(serviceRef)
                    val isAktif = snap.getBoolean("isAktif") ?: false
                    if (!isAktif) {
                        throw FirebaseFirestoreException(
                            "SERVICE_CLOSED",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }

                    val kuotaHarian = (snap.getLong("kuotaHarian") ?: 0L).toInt()
                    val lastDate = snap.getString("tanggalTerakhir").orEmpty()
                    val lastNumber = if (lastDate == today) {
                        (snap.getLong("antreanTerakhir") ?: 0L).toInt()
                    } else {
                        0
                    }

                    if (lastNumber >= kuotaHarian) {
                        throw FirebaseFirestoreException(
                            "QUOTA_FULL",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }

                    val nextNumber = lastNumber + 1
                    val prefix = snap.getString("kodePrefix").orEmpty()
                    val namaLayanan = snap.getString("namaLayanan").orEmpty()
                    val formattedNumber = String.format(Locale.US, "%s-%03d", prefix, nextNumber)

                    transaction.update(
                        serviceRef,
                        mapOf(
                            "antreanTerakhir" to nextNumber,
                            "tanggalTerakhir" to today
                        )
                    )

                    val newQueue = Queue(
                        id = newQueueRef.id,
                        userId = user.uid,
                        namaMahasiswa = user.nama,
                        nimMahasiswa = user.nim,
                        serviceId = service.id,
                        namaLayanan = namaLayanan,
                        nomorAntrean = formattedNumber,
                        nomorUrut = nextNumber,
                        status = "Menunggu",
                        tanggal = today,
                        waktuAmbil = Timestamp.now()
                    )
                    transaction.set(newQueueRef, newQueue)
                    formattedNumber
                }.addOnSuccessListener { nomorAntrean ->
                    onResult(Result.success(nomorAntrean))
                }.addOnFailureListener { e ->
                    onResult(Result.failure(e))
                }
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }

    fun updateQueueStatus(queueId: String, newStatus: String, onResult: (Result<Unit>) -> Unit) {
        db.collection("queues").document(queueId)
            .update("status", newStatus)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}