package com.example.queueless.ui.admin

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
import com.example.queueless.databinding.FragmentAdminHistoryBinding
import com.example.queueless.ui.auth.LoginActivity
import com.example.queueless.viewmodel.AdminViewModel

class AdminHistoryFragment : Fragment() {

    private var _binding: FragmentAdminHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var historyAdapter: QueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = QueueAdapter()
        binding.rvAdminHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminHistory.adapter = historyAdapter

        binding.btnAdminLogout.setOnClickListener { confirmLogout() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.adminProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvAdminName.text = user.nama
                binding.tvAdminEmail.text = user.email
            }
        }

        viewModel.historyQueues.observe(viewLifecycleOwner) { list ->
            historyAdapter.submitList(list)
            binding.layoutEmptyAdminHistory.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvAdminHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
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