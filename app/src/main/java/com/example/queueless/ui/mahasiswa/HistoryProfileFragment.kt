package com.example.queueless.ui.mahasiswa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queueless.R
import com.example.queueless.adapter.QueueAdapter
import com.example.queueless.databinding.FragmentHistoryProfileBinding
import com.example.queueless.ui.auth.LoginActivity
import com.example.queueless.viewmodel.MahasiswaViewModel

class HistoryProfileFragment : Fragment() {

    private var _binding: FragmentHistoryProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MahasiswaViewModel by activityViewModels()
    private lateinit var historyAdapter: QueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = QueueAdapter()
        binding.rvMhsHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMhsHistory.adapter = historyAdapter

        binding.btnMhsLogout.setOnClickListener { confirmLogout() }
        observeViewModel()
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
            historyAdapter.submitList(list)
            binding.layoutEmptyMhsHistory.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMhsHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
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