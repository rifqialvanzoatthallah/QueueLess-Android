package com.example.queueless.ui.mahasiswa

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.queueless.databinding.DialogAppGuideBinding
import com.example.queueless.databinding.DialogHistorySheetBinding
import com.example.queueless.databinding.DialogMhsSettingsBinding
import com.example.queueless.databinding.FragmentHistoryProfileBinding
import com.example.queueless.ui.auth.LoginActivity
import com.example.queueless.utils.ProfilePhotoHelper
import com.example.queueless.utils.SettingsHelper
import com.example.queueless.viewmodel.MahasiswaViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar

class HistoryProfileFragment : Fragment() {

    private var _binding: FragmentHistoryProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MahasiswaViewModel by activityViewModels()
    private var currentSheet: BottomSheetDialog? = null

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val ctx = requireContext()
        if (ProfilePhotoHelper.saveAvatarFromUri(ctx, uri)) {
            ProfilePhotoHelper.loadAvatarInto(ctx, binding.ivMhsAvatar)
            Snackbar.make(binding.root, getString(R.string.msg_avatar_updated), Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, getString(R.string.msg_avatar_failed), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivMhsAvatar.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(ShapeAppearanceModel.PILL)
            .build()
        ProfilePhotoHelper.loadAvatarInto(requireContext(), binding.ivMhsAvatar)
        binding.frameMhsAvatar.setOnClickListener {
            pickAvatarLauncher.launch("image/*")
        }

        binding.rowMhsHistory.setOnClickListener { showMhsHistorySheet() }
        binding.rowMhsNotifSettings.setOnClickListener { showMhsSettingsSheet() }
        binding.rowMhsGuide.setOnClickListener { showMhsGuideSheet() }
        binding.rowMhsAbout.setOnClickListener { showAboutDialog() }
        binding.btnMhsLogout.setOnClickListener { confirmLogout() }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        ProfilePhotoHelper.loadAvatarInto(requireContext(), binding.ivMhsAvatar)
    }

    private fun observeViewModel() {
        viewModel.studentProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvMhsName.text = user.nama
                binding.tvMhsNim.text = getString(R.string.label_nim_format, user.nim)
                binding.tvMhsEmail.text = user.email
            }
        }

        viewModel.historyQueues.observe(viewLifecycleOwner) { list ->
            binding.tvMhsHistoryCountPill.text =
                getString(R.string.badge_history_count, list.size)
        }
    }

    private fun applyTransparentSheetContainer(sheetDialog: BottomSheetDialog) {
        sheetDialog.setOnShowListener {
            sheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun showMhsHistorySheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        currentSheet = sheetDialog
        val sheetBinding = DialogHistorySheetBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)
        applyTransparentSheetContainer(sheetDialog)

        val historyList = viewModel.historyQueues.value.orEmpty()
        sheetBinding.tvHistorySheetTitle.text = getString(R.string.label_my_history_title)
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

    private fun showMhsGuideSheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        currentSheet = sheetDialog
        val guideBinding = DialogAppGuideBinding.inflate(layoutInflater)
        sheetDialog.setContentView(guideBinding.root)
        applyTransparentSheetContainer(sheetDialog)

        guideBinding.tvGuideTitle.text = getString(R.string.guide_mhs_title)
        guideBinding.tvGuideSubtitle.text = getString(R.string.guide_mhs_subtitle)
        guideBinding.tvGuideStep1Title.text = getString(R.string.guide_mhs_step1_title)
        guideBinding.tvGuideStep1Desc.text = getString(R.string.guide_mhs_step1_desc)
        guideBinding.tvGuideStep2Title.text = getString(R.string.guide_mhs_step2_title)
        guideBinding.tvGuideStep2Desc.text = getString(R.string.guide_mhs_step2_desc)
        guideBinding.tvGuideStep3Title.text = getString(R.string.guide_mhs_step3_title)
        guideBinding.tvGuideStep3Desc.text = getString(R.string.guide_mhs_step3_desc)
        guideBinding.tvGuideStep4Title.text = getString(R.string.guide_mhs_step4_title)
        guideBinding.tvGuideStep4Desc.text = getString(R.string.guide_mhs_step4_desc)

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

    private fun showMhsSettingsSheet() {
        val ctx = requireContext()
        val sheetDialog = BottomSheetDialog(ctx)
        currentSheet = sheetDialog
        val sheetBinding = DialogMhsSettingsBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)
        applyTransparentSheetContainer(sheetDialog)

        sheetBinding.btnDeletePhotoSheet.isEnabled = ProfilePhotoHelper.hasCustomAvatar(ctx)
        sheetBinding.switchMhsVibrate.isChecked = SettingsHelper.isMhsNotifVibrateEnabled(ctx)
        sheetBinding.switchMhsSound.isChecked = SettingsHelper.isMhsNotifSoundEnabled(ctx)

        sheetBinding.btnChangePhotoSheet.setOnClickListener {
            sheetDialog.dismiss()
            pickAvatarLauncher.launch("image/*")
        }

        sheetBinding.btnDeletePhotoSheet.setOnClickListener {
            ProfilePhotoHelper.deleteAvatar(ctx)
            ProfilePhotoHelper.loadAvatarInto(ctx, binding.ivMhsAvatar)
            sheetDialog.dismiss()
            Snackbar.make(binding.root, getString(R.string.msg_avatar_removed), Snackbar.LENGTH_SHORT).show()
        }

        sheetBinding.switchMhsVibrate.setOnCheckedChangeListener { _, isChecked ->
            SettingsHelper.setMhsNotifVibrateEnabled(ctx, isChecked)
        }

        sheetBinding.switchMhsSound.setOnCheckedChangeListener { _, isChecked ->
            SettingsHelper.setMhsNotifSoundEnabled(ctx, isChecked)
        }

        sheetBinding.btnSystemNotifSettings.setOnClickListener {
            sheetDialog.dismiss()
            openSystemNotificationSettings()
        }

        sheetDialog.show()
    }

    private fun openSystemNotificationSettings() {
        val ctx = requireContext()
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", ctx.packageName, null)
            }
        }
        startActivity(intent)
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
        currentSheet?.dismiss()
        currentSheet = null
        super.onDestroyView()
        _binding = null
    }
}