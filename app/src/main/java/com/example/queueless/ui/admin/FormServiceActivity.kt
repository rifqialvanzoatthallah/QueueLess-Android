package com.example.queueless.ui.admin

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.queueless.R
import com.example.queueless.databinding.ActivityFormServiceBinding
import com.example.queueless.model.Service
import com.example.queueless.utils.applyStatusBarTopPadding
import com.example.queueless.viewmodel.AdminViewModel
import com.google.android.material.snackbar.Snackbar

class FormServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormServiceBinding
    private val viewModel: AdminViewModel by viewModels()

    private var existingServiceId: String = ""
    private var existingAntreanTerakhir: Int = 0
    private var existingTanggalTerakhir: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.headerForm.applyStatusBarTopPadding()

        loadIntentData()
        setupObservers()
        setupListeners()
    }

    private fun loadIntentData() {
        existingServiceId = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (existingServiceId.isNotEmpty()) {
            binding.tvFormTitle.text = getString(R.string.title_edit_service)
            binding.etServiceName.setText(intent.getStringExtra(EXTRA_NAMA).orEmpty())
            binding.etServicePrefix.setText(intent.getStringExtra(EXTRA_PREFIX).orEmpty())
            binding.etServiceDesc.setText(intent.getStringExtra(EXTRA_DESKRIPSI).orEmpty())
            binding.etServiceEstimasi.setText(
                intent.getIntExtra(EXTRA_ESTIMASI, 10).toString()
            )
            binding.etServiceKuota.setText(
                intent.getIntExtra(EXTRA_KUOTA, 50).toString()
            )
            binding.switchServiceActive.isChecked =
                intent.getBooleanExtra(EXTRA_IS_AKTIF, true)
            existingAntreanTerakhir = intent.getIntExtra(EXTRA_ANTREAN_TERAKHIR, 0)
            existingTanggalTerakhir = intent.getStringExtra(EXTRA_TANGGAL_TERAKHIR).orEmpty()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBarForm.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSaveService.isEnabled = !loading
        }

        viewModel.toastEvent.observe(this) { event ->
            event ?: return@observe
            val (code, detail) = event
            viewModel.clearToastEvent()
            if (code == "SERVICE_SAVED") {
                finish()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.err_general, detail.orEmpty()),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackForm.setOnClickListener { finish() }
        binding.btnSaveService.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        binding.tilServiceName.error = null
        binding.tilServicePrefix.error = null
        binding.tilServiceDesc.error = null
        binding.tilServiceEstimasi.error = null
        binding.tilServiceKuota.error = null

        val nama = binding.etServiceName.text?.toString()?.trim().orEmpty()
        val prefix = binding.etServicePrefix.text?.toString()?.trim()?.uppercase().orEmpty()
        val deskripsi = binding.etServiceDesc.text?.toString()?.trim().orEmpty()
        val estimasi = binding.etServiceEstimasi.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val kuota = binding.etServiceKuota.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val isAktif = binding.switchServiceActive.isChecked

        var isValid = true

        if (nama.isEmpty()) {
            binding.tilServiceName.error = getString(R.string.err_empty_field)
            isValid = false
        }
        if (prefix.isEmpty()) {
            binding.tilServicePrefix.error = getString(R.string.err_empty_field)
            isValid = false
        }
        if (deskripsi.isEmpty()) {
            binding.tilServiceDesc.error = getString(R.string.err_empty_field)
            isValid = false
        }
        if (estimasi <= 0) {
            binding.tilServiceEstimasi.error = getString(R.string.err_number_invalid)
            isValid = false
        }
        if (kuota <= 0) {
            binding.tilServiceKuota.error = getString(R.string.err_number_invalid)
            isValid = false
        }

        if (isValid) {
            val service = Service(
                id = existingServiceId,
                namaLayanan = nama,
                kodePrefix = prefix,
                deskripsi = deskripsi,
                estimasiMenit = estimasi,
                kuotaHarian = kuota,
                isAktif = isAktif,
                antreanTerakhir = existingAntreanTerakhir,
                tanggalTerakhir = existingTanggalTerakhir
            )
            viewModel.saveService(service)
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_PREFIX = "extra_prefix"
        const val EXTRA_DESKRIPSI = "extra_deskripsi"
        const val EXTRA_ESTIMASI = "extra_estimasi"
        const val EXTRA_KUOTA = "extra_kuota"
        const val EXTRA_IS_AKTIF = "extra_is_aktif"
        const val EXTRA_ANTREAN_TERAKHIR = "extra_antrean_terakhir"
        const val EXTRA_TANGGAL_TERAKHIR = "extra_tanggal_terakhir"
    }
}