package com.example.queueless.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queueless.R
import com.example.queueless.adapter.ServiceAdapter
import com.example.queueless.databinding.FragmentAdminServiceBinding
import com.example.queueless.model.Service
import com.example.queueless.viewmodel.AdminViewModel

class AdminServiceFragment : Fragment() {

    private var _binding: FragmentAdminServiceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()

        binding.fabAddService.setOnClickListener {
            startActivity(Intent(requireContext(), FormServiceActivity::class.java))
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(
            isAdmin = true,
            onItemClick = { service -> openEditForm(service) },
            onDeleteClick = { service -> confirmDeleteService(service) }
        )
        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServices.adapter = serviceAdapter
    }

    private fun setupSearchView() {
        val savedQuery = viewModel.getCurrentSearchQuery()
        if (savedQuery.isNotEmpty()) {
            binding.searchViewService.setQuery(savedQuery, false)
        }

        binding.searchViewService.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarService.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.filteredServices.observe(viewLifecycleOwner) { list ->
            serviceAdapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.layoutEmptyService.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvServices.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvEmptyServiceMsg.text = if (viewModel.getCurrentSearchQuery().isNotEmpty()) {
                getString(R.string.empty_search_service)
            } else {
                getString(R.string.empty_service)
            }
        }
    }

    private fun openEditForm(service: Service) {
        val intent = Intent(requireContext(), FormServiceActivity::class.java).apply {
            putExtra(FormServiceActivity.EXTRA_ID, service.id)
            putExtra(FormServiceActivity.EXTRA_NAMA, service.namaLayanan)
            putExtra(FormServiceActivity.EXTRA_PREFIX, service.kodePrefix)
            putExtra(FormServiceActivity.EXTRA_DESKRIPSI, service.deskripsi)
            putExtra(FormServiceActivity.EXTRA_ESTIMASI, service.estimasiMenit)
            putExtra(FormServiceActivity.EXTRA_KUOTA, service.kuotaHarian)
            putExtra(FormServiceActivity.EXTRA_IS_AKTIF, service.isAktif)
            putExtra(FormServiceActivity.EXTRA_ANTREAN_TERAKHIR, service.antreanTerakhir)
            putExtra(FormServiceActivity.EXTRA_TANGGAL_TERAKHIR, service.tanggalTerakhir)
        }
        startActivity(intent)
    }

    private fun confirmDeleteService(service: Service) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_msg, service.namaLayanan))
            .setPositiveButton(getString(R.string.btn_yes_delete)) { _, _ ->
                viewModel.deleteService(service.id)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}