package com.example.queueless.ui.admin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.queueless.R
import com.example.queueless.databinding.ActivityAdminMainBinding
import com.example.queueless.utils.VoiceAnnouncer
import com.example.queueless.utils.applyStatusBarTopPadding
import com.example.queueless.viewmodel.AdminViewModel
import com.google.android.material.snackbar.Snackbar

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var voiceAnnouncer: VoiceAnnouncer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.headerAdmin.applyStatusBarTopPadding()

        voiceAnnouncer = VoiceAnnouncer(this)

        setupBottomNav()
        observeGlobalToasts()

        if (savedInstanceState == null) {
            loadFragment(AdminQueueFragment(), getString(R.string.title_admin_queue))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_queue -> {
                    loadFragment(AdminQueueFragment(), getString(R.string.title_admin_queue))
                    true
                }
                R.id.nav_admin_service -> {
                    loadFragment(AdminServiceFragment(), getString(R.string.title_admin_service))
                    true
                }
                R.id.nav_admin_history -> {
                    loadFragment(AdminHistoryFragment(), getString(R.string.title_admin_history))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        binding.tvAdminToolbarTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerAdmin, fragment)
            .commit()
    }

    private fun observeGlobalToasts() {
        viewModel.toastEvent.observe(this) { event ->
            event ?: return@observe
            val (code, param) = event
            viewModel.clearToastEvent()

            val message = when (code) {
                "SERVICE_DELETED" -> getString(R.string.msg_service_deleted)
                "FINISH_CURRENT_FIRST" -> getString(R.string.msg_finish_current_first, param.orEmpty())
                "NO_WAITING_QUEUE" -> getString(R.string.msg_no_waiting_queue)
                "NO_SERVING_TO_FINISH" -> getString(R.string.msg_no_serving_to_finish)
                "QUEUE_CALLED" -> {
                    val nomorAntrean = param.orEmpty()
                    voiceAnnouncer.announceQueue(nomorAntrean)
                    getString(R.string.msg_queue_called, nomorAntrean)
                }
                "QUEUE_FINISHED" -> getString(R.string.msg_queue_finished, param.orEmpty())
                else -> getString(R.string.err_general, param.orEmpty())
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::voiceAnnouncer.isInitialized) {
            voiceAnnouncer.shutdown()
        }
        super.onDestroy()
    }
}