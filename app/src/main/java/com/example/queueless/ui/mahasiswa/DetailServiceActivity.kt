package com.example.queueless.ui.mahasiswa

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.queueless.R
import com.example.queueless.databinding.ActivityDetailServiceBinding
import com.example.queueless.model.Service
import com.example.queueless.utils.applyStatusBarTopPadding
import com.example.queueless.viewmodel.MahasiswaViewModel
import com.google.android.material.snackbar.Snackbar

class DetailServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailServiceBinding
    private val viewModel: MahasiswaViewModel by viewModels()
    private lateinit var currentService: Service
    private var remainingQuota: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerDetailService.applyStatusBarTopPadding()

        currentService = Service(
            id = intent.getStringExtra(EXTRA_ID).orEmpty(),
            namaLayanan = intent.getStringExtra(EXTRA_NAMA).orEmpty(),
            kodePrefix = intent.getStringExtra(EXTRA_PREFIX).orEmpty(),
            deskripsi = intent.getStringExtra(EXTRA_DESKRIPSI).orEmpty(),
            estimasiMenit = intent.getIntExtra(EXTRA_ESTIMASI, 10),
            kuotaHarian = intent.getIntExtra(EXTRA_KUOTA, 50),
            isAktif = true,
            antreanTerakhir = intent.getIntExtra(EXTRA_ANTREAN_TERAKHIR, 0),
            tanggalTerakhir = intent.getStringExtra(EXTRA_TANGGAL_TERAKHIR).orEmpty()
        )

        bindServiceDetails(currentService)
        setupObservers()

        binding.btnBackDetail.setOnClickListener { finish() }
        binding.btnTakeQueue.setOnClickListener {
            if (remainingQuota <= 0) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.msg_quota_full),
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            viewModel.takeQueue(currentService)
        }
    }

    private fun bindServiceDetails(service: Service) {
        binding.tvDetailPrefix.text = getString(R.string.label_prefix_code, service.kodePrefix)
        binding.tvDetailServiceName.text = service.namaLayanan
        binding.tvDetailEstimasi.text =
            getString(R.string.label_estimasi_format, service.estimasiMenit)
        binding.tvDetailDesc.text = service.deskripsi

        val usedToday = if (service.tanggalTerakhir == viewModel.getTodayDateString()) {
            service.antreanTerakhir
        } else {
            0
        }
        remainingQuota = (service.kuotaHarian - usedToday).coerceAtLeast(0)
        binding.tvDetailQuota.text =
            getString(R.string.label_sisa_kuota_hari_ini, remainingQuota, service.kuotaHarian)

        updateTakeQueueButtonState(isAvailable = remainingQuota > 0)
    }

    private fun updateTakeQueueButtonState(isAvailable: Boolean) {
        binding.btnTakeQueue.isEnabled = true
        if (isAvailable) {
            binding.btnTakeQueue.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue))
            binding.btnTakeQueue.setTextColor(ContextCompat.getColor(this, R.color.text_white))
        } else {
            binding.btnTakeQueue.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.divider_gray))
            binding.btnTakeQueue.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        }
    }

    private fun setupObservers() {
        viewModel.filteredActiveServices.observe(this) { list ->
            val updated = list.firstOrNull { it.id == currentService.id }
            if (updated != null) {
                currentService = updated
                bindServiceDetails(updated)
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBarDetail.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.btnTakeQueue.isEnabled = false
            } else {
                updateTakeQueueButtonState(isAvailable = remainingQuota > 0)
            }
        }

        viewModel.takeQueueResult.observe(this) { result ->
            result ?: return@observe
            viewModel.clearTakeQueueResult()

            result.onSuccess { nomorAntrean ->
                val resultIntent = Intent().putExtra(RESULT_QUEUE_NUMBER, nomorAntrean)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }.onFailure { error ->
                updateTakeQueueButtonState(isAvailable = remainingQuota > 0)
                val msg = when (error.message) {
                    "ACTIVE_QUEUE_EXISTS" -> getString(R.string.msg_active_queue_exists)
                    "QUOTA_FULL" -> getString(R.string.msg_quota_full)
                    "SERVICE_CLOSED" -> getString(R.string.msg_service_closed)
                    else -> getString(R.string.err_general, error.localizedMessage.orEmpty())
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_PREFIX = "extra_prefix"
        const val EXTRA_DESKRIPSI = "extra_deskripsi"
        const val EXTRA_ESTIMASI = "extra_estimasi"
        const val EXTRA_KUOTA = "extra_kuota"
        const val EXTRA_ANTREAN_TERAKHIR = "extra_antrean_terakhir"
        const val EXTRA_TANGGAL_TERAKHIR = "extra_tanggal_terakhir"
        const val RESULT_QUEUE_NUMBER = "result_queue_number"
    }
}