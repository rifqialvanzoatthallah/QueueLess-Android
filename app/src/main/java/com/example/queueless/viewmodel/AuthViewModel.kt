package com.example.queueless.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.queueless.model.User
import com.example.queueless.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> = _authResult

    fun checkActiveSession() {
        val uid = repository.getCurrentUserUid() ?: return
        _isLoading.value = true
        repository.fetchUserProfile(uid) { result ->
            _isLoading.value = false
            _authResult.value = result
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        repository.login(email, password) { result ->
            _isLoading.value = false
            _authResult.value = result
        }
    }

    fun register(nama: String, nim: String, email: String, password: String) {
        _isLoading.value = true
        repository.registerMahasiswa(nama, nim, email, password) { result ->
            _isLoading.value = false
            _authResult.value = result
        }
    }

    fun clearResult() {
        _authResult.value = null
    }
}