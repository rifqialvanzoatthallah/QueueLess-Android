package com.example.queueless.ui.mahasiswa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.queueless.R
import com.example.queueless.databinding.ActivityMahasiswaMainBinding
import com.example.queueless.utils.NotificationHelper
import com.example.queueless.utils.SettingsHelper
import com.example.queueless.utils.applyStatusBarTopPadding
import com.example.queueless.viewmodel.MahasiswaViewModel
import com.google.android.material.snackbar.Snackbar

class MahasiswaMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMahasiswaMainBinding
    private val viewModel: MahasiswaViewModel by viewModels()
    private var lastNotifiedQueueId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Snackbar.make(
                binding.root,
                getString(R.string.msg_notif_permission_denied),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        binding.headerMahasiswa.applyStatusBarTopPadding()
        NotificationHelper.createNotificationChannel(this)
        checkNotificationPermission()

        setupBottomNav()
        observeRealtimeQueueNotification()

        if (savedInstanceState == null) {
            loadFragment(HomeServiceFragment(), getString(R.string.title_mhs_home))
        } else {
            restoreToolbarTitle(binding.bottomNavMahasiswa.selectedItemId)
        }
    }

    private fun restoreToolbarTitle(selectedItemId: Int) {
        binding.tvMhsToolbarTitle.text = when (selectedItemId) {
            R.id.nav_mhs_queue -> getString(R.string.title_my_queue)
            R.id.nav_mhs_profile -> getString(R.string.title_settings)
            else -> getString(R.string.title_mhs_home)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavMahasiswa.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mhs_home -> {
                    loadFragment(HomeServiceFragment(), getString(R.string.title_mhs_home))
                    true
                }
                R.id.nav_mhs_queue -> {
                    loadFragment(MyQueueFragment(), getString(R.string.title_my_queue))
                    true
                }
                R.id.nav_mhs_profile -> {
                    loadFragment(HistoryProfileFragment(), getString(R.string.title_settings))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        binding.tvMhsToolbarTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerMahasiswa, fragment)
            .commit()
    }

    fun onQueueTakenSuccess(nomorAntrean: String) {
        binding.bottomNavMahasiswa.selectedItemId = R.id.nav_mhs_queue
        Snackbar.make(
            binding.root,
            getString(R.string.msg_queue_taken, nomorAntrean),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun observeRealtimeQueueNotification() {
        viewModel.activeQueue.observe(this) { queue ->
            if (queue != null && queue.status == "Dipanggil" && queue.id != lastNotifiedQueueId) {
                lastNotifiedQueueId = queue.id
                NotificationHelper.showQueueCalledNotification(
                    this,
                    queue.nomorAntrean,
                    queue.namaLayanan
                )
                if (SettingsHelper.isMhsNotifVibrateEnabled(this)) {
                    triggerCallVibration()
                }
            }
        }

        viewModel.toastEvent.observe(this) { event ->
            event ?: return@observe
            val (code, detail) = event
            viewModel.clearToastEvent()
            val message = when (code) {
                "QUEUE_CANCELLED" -> getString(R.string.msg_queue_cancelled)
                else -> getString(R.string.err_general, detail.orEmpty())
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun triggerCallVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(450, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: SecurityException) {
            // Aman jika izin getar tidak dideklarasikan di manifest
        }
    }
}