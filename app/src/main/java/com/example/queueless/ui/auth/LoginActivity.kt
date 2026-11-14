package com.example.queueless.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.queueless.R
import com.example.queueless.databinding.ActivityLoginBinding
import com.example.queueless.model.User
import com.example.queueless.ui.admin.AdminMainActivity
import com.example.queueless.ui.mahasiswa.MahasiswaMainActivity
import com.example.queueless.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkActiveSession()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
            binding.tvToRegister.isEnabled = !loading
        }

        viewModel.authResult.observe(this) { result ->
            result ?: return@observe
            result.onSuccess { user ->
                viewModel.clearResult()
                navigateByRole(user)
            }.onFailure { error ->
                viewModel.clearResult()
                val message = when (error.message) {
                    "NETWORK_ERROR" -> getString(R.string.err_network)
                    "INVALID_CREDENTIALS" -> getString(R.string.err_login_failed)
                    "USER_DATA_NOT_FOUND" -> getString(R.string.err_user_data_not_found)
                    else -> getString(R.string.err_login_failed)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.err_empty_field)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.err_empty_field)
            isValid = false
        }

        if (isValid) {
            viewModel.login(email, password)
        }
    }

    private fun navigateByRole(user: User) {
        val targetIntent = if (user.role == "admin") {
            Intent(this, AdminMainActivity::class.java)
        } else {
            Intent(this, MahasiswaMainActivity::class.java)
        }
        targetIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(targetIntent)
        finish()
    }
}