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

class MahasiswaViewModel : ViewModel() {

    private val queueRepository = QueueRepository()
    private val authRepository = AuthRepository()

    private var servicesListener: ListenerRegistration? = null
    private var studentQueuesListener: ListenerRegistration? = null
    private var serviceQueuesListener: ListenerRegistration? = null
    private var observedServiceIdForQueue: String? = null

    private var activeServicesCache: List<Service> = emptyList()
    private var currentSearchQuery: String = ""

    private val _studentProfile = MutableLiveData<User?>()
    val studentProfile: LiveData<User?> = _studentProfile

    private val _filteredActiveServices = MutableLiveData<List<Service>>(emptyList())
    val filteredActiveServices: LiveData<List<Service>> = _filteredActiveServices

    private val _activeQueue = MutableLiveData<Queue?>(null)
    val activeQueue: LiveData<Queue?> = _activeQueue

    private val _historyQueues = MutableLiveData<List<Queue>>(emptyList())
    val historyQueues: LiveData<List<Queue>> = _historyQueues

    private val _currentlyServingNumber = MutableLiveData("-")
    val currentlyServingNumber: LiveData<String> = _currentlyServingNumber

    private val _queuesAheadCount = MutableLiveData(0)
    val queuesAheadCount: LiveData<Int> = _queuesAheadCount

    private val _estimatedWaitMinutes = MutableLiveData(0)
    val estimatedWaitMinutes: LiveData<Int> = _estimatedWaitMinutes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _takeQueueResult = MutableLiveData<Result<String>?>()
    val takeQueueResult: LiveData<Result<String>?> = _takeQueueResult

    private val _toastEvent = MutableLiveData<Pair<String, String?>?>()
    val toastEvent: LiveData<Pair<String, String?>?> = _toastEvent

    init {
        loadStudentProfile()
        observeServices()
        observeMyQueues()
    }

    fun getTodayDateString(): String = queueRepository.getTodayDateString()

    private fun loadStudentProfile() {
        val uid = authRepository.getCurrentUserUid() ?: return
        authRepository.fetchUserProfile(uid) { result ->
            result.onSuccess { _studentProfile.value = it }
        }
    }

    private fun observeServices() {
        _isLoading.value = true
        servicesListener = queueRepository.observeAllServices(
            onUpdate = { list ->
                _isLoading.value = false
                activeServicesCache = list.filter { it.isAktif }
                applySearchFilter()
                recalculateWaitEstimate()
            },
            onError = { e ->
                _isLoading.value = false
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        )
    }

    private fun observeMyQueues() {
        val uid = authRepository.getCurrentUserUid() ?: return
        studentQueuesListener = queueRepository.observeStudentTodayQueues(
            userId = uid,
            onUpdate = { list ->
                val currentActive = list.firstOrNull {
                    it.status == "Menunggu" || it.status == "Dipanggil"
                }
                _activeQueue.value = currentActive
                _historyQueues.value = list.filter {
                    it.status == "Selesai" || it.status == "Dibatalkan"
                }

                if (currentActive != null) {
                    ensureServiceQueueObserver(currentActive.serviceId)
                } else {
                    serviceQueuesListener?.remove()
                    serviceQueuesListener = null
                    observedServiceIdForQueue = null
                    _currentlyServingNumber.value = "-"
                    _queuesAheadCount.value = 0
                    _estimatedWaitMinutes.value = 0
                }
            },
            onError = { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        )
    }

    private fun ensureServiceQueueObserver(serviceId: String) {
        if (observedServiceIdForQueue == serviceId && serviceQueuesListener != null) {
            return
        }
        serviceQueuesListener?.remove()
        observedServiceIdForQueue = serviceId

        serviceQueuesListener = queueRepository.observeServiceTodayQueues(
            serviceId = serviceId,
            onUpdate = { serviceQueues ->
                val serving = serviceQueues.firstOrNull { it.status == "Dipanggil" }
                _currentlyServingNumber.value = serving?.nomorAntrean ?: "-"

                val myQueue = _activeQueue.value
                if (myQueue != null) {
                    val ahead = serviceQueues.count {
                        (it.status == "Menunggu" || it.status == "Dipanggil") &&
                                it.nomorUrut < myQueue.nomorUrut
                    }
                    _queuesAheadCount.value = ahead
                    recalculateWaitEstimate()
                }
            },
            onError = { }
        )
    }

    private fun recalculateWaitEstimate() {
        val myQueue = _activeQueue.value ?: return
        val service = activeServicesCache.firstOrNull { it.id == myQueue.serviceId }
        val perPerson = service?.estimasiMenit ?: 10
        val ahead = _queuesAheadCount.value ?: 0
        _estimatedWaitMinutes.value = ahead * perPerson
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        applySearchFilter()
    }

    fun getCurrentSearchQuery(): String = currentSearchQuery

    private fun applySearchFilter() {
        val q = currentSearchQuery.trim().lowercase()
        _filteredActiveServices.value = if (q.isEmpty()) {
            activeServicesCache
        } else {
            activeServicesCache.filter {
                it.namaLayanan.lowercase().contains(q) || it.kodePrefix.lowercase().contains(q)
            }
        }
    }

    fun takeQueue(service: Service) {
        val user = _studentProfile.value
        if (user == null) {
            _toastEvent.value = Pair("ERROR_GENERAL", "Profil mahasiswa belum dimuat")
            return
        }
        if (_activeQueue.value != null) {
            _takeQueueResult.value = Result.failure(Exception("ACTIVE_QUEUE_EXISTS"))
            return
        }

        _isLoading.value = true
        queueRepository.takeQueueTransaction(user, service) { result ->
            _isLoading.value = false
            _takeQueueResult.value = result
        }
    }

    fun cancelActiveQueue() {
        val myQueue = _activeQueue.value ?: return
        _isLoading.value = true
        queueRepository.updateQueueStatus(myQueue.id, "Dibatalkan") { result ->
            _isLoading.value = false
            result.onSuccess {
                _toastEvent.value = Pair("QUEUE_CANCELLED", null)
            }.onFailure { e ->
                _toastEvent.value = Pair("ERROR_GENERAL", e.localizedMessage)
            }
        }
    }

    fun clearTakeQueueResult() {
        _takeQueueResult.value = null
    }

    fun clearToastEvent() {
        _toastEvent.value = null
    }

    fun logout() {
        authRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        servicesListener?.remove()
        studentQueuesListener?.remove()
        serviceQueuesListener?.remove()
    }
}