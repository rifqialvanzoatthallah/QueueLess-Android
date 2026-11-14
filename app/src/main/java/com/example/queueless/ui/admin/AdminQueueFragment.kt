package com.example.queueless.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.queueless.R
import com.example.queueless.adapter.QueueAdapter
import com.example.queueless.databinding.FragmentAdminQueueBinding
import com.example.queueless.viewmodel.AdminViewModel

class AdminQueueFragment : Fragment() {

    private var _binding: FragmentAdminQueueBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private lateinit var queueAdapter: QueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        queueAdapter = QueueAdapter()
        binding.rvWaitingQueues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWaitingQueues.adapter = queueAdapter

        binding.btnCallNext.setOnClickListener { viewModel.callNextQueue() }
        binding.btnFinishQueue.setOnClickListener { viewModel.finishCurrentQueue() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarQueue.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.currentlyServing.observe(viewLifecycleOwner) { queue ->
            if (queue != null) {
                binding.tvServingNumber.text = queue.nomorAntrean
                binding.tvServingNumber.textSize = 36f
                binding.tvServingDetail.text = getString(
                    R.string.label_student_queue_info,
                    "${queue.namaLayanan} • ${queue.namaMahasiswa}",
                    queue.nimMahasiswa
                )
                binding.btnFinishQueue.isEnabled = true
            } else {
                binding.tvServingNumber.text = getString(R.string.label_no_serving)
                binding.tvServingNumber.textSize = 20f
                binding.tvServingDetail.text = ""
                binding.btnFinishQueue.isEnabled = false
            }
        }

        viewModel.waitingQueues.observe(viewLifecycleOwner) { list ->
            queueAdapter.submitList(list)
            binding.layoutEmptyQueue.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvWaitingQueues.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}