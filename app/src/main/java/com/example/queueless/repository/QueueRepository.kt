package com.example.queueless.repository

import com.example.queueless.model.Queue
import com.example.queueless.model.Service
import com.google.firebase.firestore.FirebaseFirestore
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

    fun updateQueueStatus(queueId: String, newStatus: String, onResult: (Result<Unit>) -> Unit) {
        db.collection("queues").document(queueId)
            .update("status", newStatus)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}