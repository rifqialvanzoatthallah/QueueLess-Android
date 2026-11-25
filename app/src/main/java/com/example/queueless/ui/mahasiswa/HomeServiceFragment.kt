package com.example.queueless.ui.mahasiswa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queueless.R
import com.example.queueless.adapter.ServiceAdapter
import com.example.queueless.databinding.FragmentHomeServiceBinding
import com.example.queueless.model.Service
import com.example.queueless.viewmodel.MahasiswaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeServiceFragment : Fragment() {

    private var _binding: FragmentHomeServiceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MahasiswaViewModel by activityViewModels()
    private lateinit var serviceAdapter: ServiceAdapter

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val nomor = result.data?.getStringExtra(DetailServiceActivity.RESULT_QUEUE_NUMBER).orEmpty()
            (activity as? MahasiswaMainActivity)?.onQueueTakenSuccess(nomor)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        binding.tvMhsBannerDate.text = dateFormat.format(Date())

        serviceAdapter = ServiceAdapter(
            isAdmin = false,
            onItemClick = { service -> openServiceDetail(service) }
        )
        binding.rvMhsServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMhsServices.adapter = serviceAdapter

        setupSearchView()
        observeViewModel()
    }

    private fun setupSearchView() {
        val savedQuery = viewModel.getCurrentSearchQuery()
        if (savedQuery.isNotEmpty()) {
            binding.searchViewMhsService.setQuery(savedQuery, false)
        }

        binding.searchViewMhsService.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        viewModel.studentProfile.observe(viewLifecycleOwner) { user ->
            val firstName = user?.nama?.trim()?.substringBefore(" ").orEmpty()
            binding.tvMhsGreeting.text = if (firstName.isNotEmpty()) {
                getString(R.string.greeting_hello_user, firstName)
            } else {
                getString(R.string.greeting_hello_default)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarMhsService.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.filteredActiveServices.observe(viewLifecycleOwner) { list ->
            serviceAdapter.submitList(list)
            binding.tvMhsServiceCountBadge.text =
                getString(R.string.badge_active_services_count, list.size)

            val isEmpty = list.isEmpty()
            binding.layoutEmptyMhsService.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvMhsServices.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvEmptyMhsServiceMsg.text = if (viewModel.getCurrentSearchQuery().isNotEmpty()) {
                getString(R.string.empty_search_service)
            } else {
                getString(R.string.empty_service)
            }
        }
    }

    private fun openServiceDetail(service: Service) {
        val intent = Intent(requireContext(), DetailServiceActivity::class.java).apply {
            putExtra(DetailServiceActivity.EXTRA_ID, service.id)
            putExtra(DetailServiceActivity.EXTRA_NAMA, service.namaLayanan)
            putExtra(DetailServiceActivity.EXTRA_PREFIX, service.kodePrefix)
            putExtra(DetailServiceActivity.EXTRA_DESKRIPSI, service.deskripsi)
            putExtra(DetailServiceActivity.EXTRA_ESTIMASI, service.estimasiMenit)
            putExtra(DetailServiceActivity.EXTRA_KUOTA, service.kuotaHarian)
            putExtra(DetailServiceActivity.EXTRA_ANTREAN_TERAKHIR, service.antreanTerakhir)
            putExtra(DetailServiceActivity.EXTRA_TANGGAL_TERAKHIR, service.tanggalTerakhir)
        }
        detailLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}