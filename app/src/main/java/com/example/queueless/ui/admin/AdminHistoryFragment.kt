package com.example.queueless.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queueless.R
import com.example.queueless.adapter.QueueAdapter
import com.example.queueless.databinding.DialogAdminSettingsBinding
import com.example.queueless.databinding.DialogAppGuideBinding
import com.example.queueless.databinding.DialogHistorySheetBinding
import com.example.queueless.databinding.FragmentAdminHistoryBinding
import com.example.queueless.ui.auth.LoginActivity
import com.example.queueless.utils.ProfilePhotoHelper
import com.example.queueless.utils.SettingsHelper
import com.example.queueless.viewmodel.AdminViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar

class AdminHistoryFragment : Fragment() {

    private var _binding: FragmentAdminHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val ctx = requireContext()
        if (ProfilePhotoHelper.saveAvatarFromUri(ctx, uri)) {
            ProfilePhotoHelper.loadAvatarInto(ctx, binding.ivAdminAvatar)
            Snackbar.make(binding.root, getString(R.string.msg_avatar_updated), Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, getString(R.string.msg_avatar_failed), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivAdminAvatar.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(ShapeAppearanceModel.PILL)
            .build()
        ProfilePhotoHelper.loadAvatarInto(requireContext(), binding.ivAdminAvatar)
        binding.frameAdminAvatar.setOnClickListener {
            pickAvatarLauncher.launch("image/*")
        }

        binding.rowAdminHistory.setOnClickListener { showAdminHistorySheet() }
        binding.rowAdminTtsSettings.setOnClickListener { showAdminSettingsSheet() }
        binding.rowAdminGuide.setOnClickListener { showAdminGuideSheet() }
        binding.rowAdminAbout.setOnClickListener { showAboutDialog() }
        binding.btnAdminLogout.setOnClickListener { confirmLogout() }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        ProfilePhotoHelper.loadAvatarInto(requireContext(), binding.ivAdminAvatar)
    }

    private fun observeViewModel() {
        viewModel.adminProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvAdminName.text = user.nama
                binding.tvAdminEmail.text = user.email
            }
        }

        viewModel.historyQueues.observe(viewLifecycleOwner) { list ->
            binding.tvAdminHistoryCountPill.text =
                getString(R.string.badge_history_count, list.size)
        }
    }

    private fun showAdminHistorySheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        val sheetBinding = DialogHistorySheetBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)

        val historyList = viewModel.historyQueues.value.orEmpty()
        sheetBinding.tvHistorySheetTitle.text = getString(R.string.label_today_history)
        sheetBinding.tvHistorySheetBadge.text =
            getString(R.string.badge_history_count, historyList.size)

        val adapter = QueueAdapter()
        sheetBinding.rvHistorySheet.layoutManager = LinearLayoutManager(ctx)
        sheetBinding.rvHistorySheet.adapter = adapter
        adapter.submitList(historyList)

        sheetBinding.layoutEmptyHistorySheet.visibility =
            if (historyList.isEmpty()) View.VISIBLE else View.GONE
        sheetBinding.rvHistorySheet.visibility =
            if (historyList.isEmpty()) View.GONE else View.VISIBLE

        sheetDialog.show()
    }

    private fun showAdminGuideSheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        val guideBinding = DialogAppGuideBinding.inflate(layoutInflater)
        sheetDialog.setContentView(guideBinding.root)

        guideBinding.tvGuideTitle.text = getString(R.string.guide_admin_title)
        guideBinding.tvGuideSubtitle.text = getString(R.string.guide_admin_subtitle)
        guideBinding.tvGuideStep1Title.text = getString(R.string.guide_admin_step1_title)
        guideBinding.tvGuideStep1Desc.text = getString(R.string.guide_admin_step1_desc)
        guideBinding.tvGuideStep2Title.text = getString(R.string.guide_admin_step2_title)
        guideBinding.tvGuideStep2Desc.text = getString(R.string.guide_admin_step2_desc)
        guideBinding.tvGuideStep3Title.text = getString(R.string.guide_admin_step3_title)
        guideBinding.tvGuideStep3Desc.text = getString(R.string.guide_admin_step3_desc)
        guideBinding.tvGuideStep4Title.text = getString(R.string.guide_admin_step4_title)
        guideBinding.tvGuideStep4Desc.text = getString(R.string.guide_admin_step4_desc)

        guideBinding.btnCloseGuide.setOnClickListener { sheetDialog.dismiss() }
        sheetDialog.show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about_app_title))
            .setMessage(getString(R.string.about_app_message))
            .setPositiveButton(getString(R.string.btn_close), null)
            .show()
    }

    private fun showAdminSettingsSheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        val sheetBinding = DialogAdminSettingsBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)

        sheetBinding.switchAdminTts.isChecked = SettingsHelper.isAdminTtsEnabled(ctx)
        sheetBinding.etAdminCounterName.setText(SettingsHelper.getAdminCounterName(ctx))
        sheetBinding.btnAdminDeletePhotoSheet.isEnabled = ProfilePhotoHelper.hasCustomAvatar(ctx)

        val currentRate = SettingsHelper.getAdminSpeechRate(ctx)
        val initialCheckedId = when {
            currentRate <= 0.80f -> R.id.btnSpeedSlow
            currentRate >= 1.10f -> R.id.btnSpeedFast
            else -> R.id.btnSpeedNormal
        }
        sheetBinding.toggleTtsSpeed.check(initialCheckedId)

        fun getSelectedRate(): Float {
            return when (sheetBinding.toggleTtsSpeed.checkedButtonId) {
                R.id.btnSpeedSlow -> 0.75f
                R.id.btnSpeedFast -> 1.20f
                else -> 0.95f
            }
        }

        sheetBinding.btnTestAdminTts.setOnClickListener {
            val counterText = sheetBinding.etAdminCounterName.text?.toString().orEmpty()
            (activity as? AdminMainActivity)?.testVoiceAnnouncement(counterText, getSelectedRate())
        }

        sheetBinding.btnSaveAdminSettings.setOnClickListener {
            SettingsHelper.setAdminTtsEnabled(ctx, sheetBinding.switchAdminTts.isChecked)
            SettingsHelper.setAdminCounterName(
                ctx,
                sheetBinding.etAdminCounterName.text?.toString().orEmpty()
            )
            SettingsHelper.setAdminSpeechRate(ctx, getSelectedRate())
            sheetDialog.dismiss()
            Snackbar.make(binding.root, getString(R.string.msg_settings_saved), Snackbar.LENGTH_SHORT).show()
        }

        sheetBinding.btnAdminChangePhotoSheet.setOnClickListener {
            sheetDialog.dismiss()
            pickAvatarLauncher.launch("image/*")
        }

        sheetBinding.btnAdminDeletePhotoSheet.setOnClickListener {
            ProfilePhotoHelper.deleteAvatar(ctx)
            ProfilePhotoHelper.loadAvatarInto(ctx, binding.ivAdminAvatar)
            sheetDialog.dismiss()
            Snackbar.make(binding.root, getString(R.string.msg_avatar_removed), Snackbar.LENGTH_SHORT).show()
        }

        sheetBinding.btnAdminAboutApp.setOnClickListener {
            sheetDialog.dismiss()
            showAboutDialog()
        }

        sheetDialog.show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_logout_title))
            .setMessage(getString(R.string.dialog_logout_msg))
            .setPositiveButton(getString(R.string.btn_yes_logout)) { _, _ ->
                viewModel.logout()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}