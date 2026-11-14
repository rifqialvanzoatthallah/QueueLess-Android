package com.example.queueless.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.queueless.R
import com.example.queueless.databinding.ActivityRegisterBinding
import com.example.queueless.ui.mahasiswa.MahasiswaMainActivity
import com.example.queueless.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
            binding.tvToLogin.isEnabled = !loading
            binding.btnBack.isEnabled = !loading
        }

        viewModel.authResult.observe(this) { result ->
            result ?: return@observe
            result.onSuccess {
                viewModel.clearResult()
                val intent = Intent(this, MahasiswaMainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { error ->
                viewModel.clearResult()
                val msg = if (error.message == "NETWORK_ERROR") {
                    getString(R.string.err_network)
                } else {
                    getString(R.string.err_register_failed, error.localizedMessage.orEmpty())
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateAndRegister() {
        binding.tilNama.error = null
        binding.tilNim.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        val nama = binding.etNama.text?.toString()?.trim().orEmpty()
        val nim = binding.etNim.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim().orEmpty()

        var isValid = true

        if (nama.isEmpty()) {
            binding.tilNama.error = getString(R.string.err_empty_field)
            isValid = false
        }
        if (nim.isEmpty()) {
            binding.tilNim.error = getString(R.string.err_empty_field)
            isValid = false
        }
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
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.err_password_short)
            isValid = false
        }
        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.err_password_mismatch)
            isValid = false
        }

        if (isValid) {
            viewModel.register(nama, nim, email, password)
        }
    }
}