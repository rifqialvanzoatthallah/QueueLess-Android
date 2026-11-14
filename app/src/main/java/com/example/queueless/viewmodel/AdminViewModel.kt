package com.example.queueless.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.queueless.model.Queue
import com.example.queueless.model.Service
import com.example.queueless.model.User
import com.example.queueless.repository.AuthRepository
import com.example.queueless.repository.QueueRepository
import com.google.firebase.firestore.ListenerRegistration

class AdminViewModel : ViewModel() {

    private val queueRepository = QueueRepository()
    private val authRepository = AuthRepository()

    private var servicesListener: ListenerRegistration? = null
    private var queuesListener: ListenerRegistration? = null

    private var allServicesCache: List<Service> = emptyList()
    private var currentSearchQuery: String = ""

    private val _filteredServices = MutableLiveData<List<Service>>(emptyList())
    val filteredServices: LiveData<List<Service>> = _filteredServices

    private val _currentlyServing = MutableLiveData<Queue?>(null)
    val currentlyServing: LiveData<Queue?> = _currentlyServing

    private val _waitingQueues = MutableLiveData<List<Queue>>(emptyList())
    val waitingQueues: LiveData<List<Queue>> = _waitingQueues

    private val _historyQueues = MutableLiveData<List<Queue>>(emptyList())
    val historyQueues: LiveData<List<Queue>> = _historyQueues

    private val _adminProfile = MutableLiveData<User?>()
    val adminProfile: LiveData<User?> = _adminProfile

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastEvent = MutableLiveData<Pair<String, String?>?>()
    val toastEvent: LiveData<Pair<String, String?>?> = _toastEvent

    init {
        startObserving()
        loadAdminProfile()
    }

    private fun startObserving() {
        _isLoading.value = true

        servicesListener = queueRepository.observeAllServices(
            onUpdate = { list ->
                _isLoading.value = false
                allServicesCache = list
                applySearchFilter()
            },
            onError = { e ->
                _isLoading.value = false
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        )

        queuesListener = queueRepository.observeTodayQueues(
            onUpdate = { todayList ->
                _isLoading.value = false
                _currentlyServing.value = todayList.firstOrNull { it.status == "Dipanggil" }
                _waitingQueues.value = todayList.filter { it.status == "Menunggu" }
                _historyQueues.value = todayList.filter {
                    it.status == "Selesai" || it.status == "Dibatalkan"
                }.sortedByDescending { it.nomorUrut }
            },
            onError = { e ->
                _isLoading.value = false
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        )
    }

    private fun loadAdminProfile() {
        val uid = authRepository.getCurrentUserUid() ?: return
        authRepository.fetchUserProfile(uid) { result ->
            result.onSuccess { _adminProfile.value = it }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        applySearchFilter()
    }

    fun getCurrentSearchQuery(): String = currentSearchQuery

    private fun applySearchFilter() {
        val q = currentSearchQuery.trim().lowercase()
        _filteredServices.value = if (q.isEmpty()) {
            allServicesCache
        } else {
            allServicesCache.filter {
                it.namaLayanan.lowercase().contains(q) || it.kodePrefix.lowercase().contains(q)
            }
        }
    }

    fun saveService(service: Service) {
        _isLoading.value = true
        queueRepository.saveService(service) { result ->
            _isLoading.value = false
            result.onSuccess {
                _toastEvent.value = Pair("SERVICE_SAVED", null)
            }.onFailure { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        }
    }

    fun deleteService(serviceId: String) {
        _isLoading.value = true
        queueRepository.deleteService(serviceId) { result ->
            _isLoading.value = false
            result.onSuccess {
                _toastEvent.value = Pair("SERVICE_DELETED", null)
            }.onFailure { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        }
    }

    fun callNextQueue() {
        val current = _currentlyServing.value
        if (current != null) {
            _toastEvent.value = Pair("FINISH_CURRENT_FIRST", current.nomorAntrean)
            return
        }
        val nextQueue = _waitingQueues.value?.firstOrNull()
        if (nextQueue == null) {
            _toastEvent.value = Pair("NO_WAITING_QUEUE", null)
            return
        }
        queueRepository.updateQueueStatus(nextQueue.id, "Dipanggil") { result ->
            result.onSuccess {
                _toastEvent.value = Pair("QUEUE_CALLED", nextQueue.nomorAntrean)
            }.onFailure { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        }
    }

    fun finishCurrentQueue() {
        val current = _currentlyServing.value
        if (current == null) {
            _toastEvent.value = Pair("NO_SERVING_TO_FINISH", null)
            return
        }
        queueRepository.updateQueueStatus(current.id, "Selesai") { result ->
            result.onSuccess {
                _toastEvent.value = Pair("QUEUE_FINISHED", current.nomorAntrean)
            }.onFailure { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearToastEvent() {
        _toastEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        servicesListener?.remove()
        queuesListener?.remove()
    }
}