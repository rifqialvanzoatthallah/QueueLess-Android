package com.example.queueless.repository

import com.example.queueless.model.User
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun login(email: String, password: String, onResult: (Result<User>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    fetchUserProfile(uid, onResult)
                } else {
                    onResult(Result.failure(Exception("UID_NOT_FOUND")))
                }
            }
            .addOnFailureListener { exception ->
                val mappedError = when (exception) {
                    is FirebaseNetworkException -> Exception("NETWORK_ERROR")
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException -> Exception("INVALID_CREDENTIALS")
                    else -> exception
                }
                onResult(Result.failure(mappedError))
            }
    }

    fun registerMahasiswa(
        nama: String,
        nim: String,
        email: String,
        password: String,
        onResult: (Result<User>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: run {
                    onResult(Result.failure(Exception("UID_NOT_FOUND")))
                    return@addOnSuccessListener
                }
                val newUser = User(
                    uid = uid,
                    nama = nama,
                    nim = nim,
                    email = email,
                    role = "mahasiswa",
                    fcmToken = ""
                )
                db.collection("users").document(uid)
                    .set(newUser)
                    .addOnSuccessListener {
                        onResult(Result.success(newUser))
                    }
                    .addOnFailureListener { e ->
                        onResult(Result.failure(e))
                    }
            }
            .addOnFailureListener { exception ->
                val mappedError = when (exception) {
                    is FirebaseNetworkException -> Exception("NETWORK_ERROR")
                    else -> exception
                }
                onResult(Result.failure(mappedError))
            }
    }

    fun fetchUserProfile(uid: String, onResult: (Result<User>) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onResult(Result.success(user))
                    } else {
                        onResult(Result.failure(Exception("USER_DATA_NOT_FOUND")))
                    }
                } else {
                    auth.signOut()
                    onResult(Result.failure(Exception("USER_DATA_NOT_FOUND")))
                }
            }
            .addOnFailureListener { exception ->
                val mappedError = when (exception) {
                    is FirebaseNetworkException -> Exception("NETWORK_ERROR")
                    else -> exception
                }
                onResult(Result.failure(mappedError))
            }
    }

    fun logout() {
        auth.signOut()
    }
}