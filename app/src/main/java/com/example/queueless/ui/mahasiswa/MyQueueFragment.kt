package com.example.queueless.ui.mahasiswa

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.queueless.R
import com.example.queueless.databinding.FragmentMyQueueBinding
import com.example.queueless.viewmodel.MahasiswaViewModel

class MyQueueFragment : Fragment() {

    private var _binding: FragmentMyQueueBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MahasiswaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelMyQueue.setOnClickListener { confirmCancelQueue() }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.activeQueue.observe(viewLifecycleOwner) { queue ->
            if (queue == null) {
                binding.layoutEmptyMyQueue.visibility = View.VISIBLE
                binding.layoutActiveQueueCard.visibility = View.GONE
            } else {
                binding.layoutEmptyMyQueue.visibility = View.GONE
                binding.layoutActiveQueueCard.visibility = View.VISIBLE

                binding.tvMyQueueServiceName.text = queue.namaLayanan
                binding.tvMyQueueNumber.text = queue.nomorAntrean
                binding.tvMyQueueStatus.text =
                    getString(R.string.label_status_antrean_saya, queue.status)

                val ctx = requireContext()
                if (queue.status == "Dipanggil") {
                    binding.cardYourTurnBanner.visibility = View.VISIBLE
                    binding.tvMyQueueStatus.setTextColor(
                        ContextCompat.getColor(ctx, R.color.status_called)
                    )
                    binding.tvMyQueueStatus.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.status_called_bg))
                } else {
                    binding.cardYourTurnBanner.visibility = View.GONE
                    binding.tvMyQueueStatus.setTextColor(
                        ContextCompat.getColor(ctx, R.color.status_waiting)
                    )
                    binding.tvMyQueueStatus.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.status_waiting_bg))
                }
            }
        }

        viewModel.currentlyServingNumber.observe(viewLifecycleOwner) { serving ->
            binding.tvNowServingValue.text = serving
        }

        viewModel.queuesAheadCount.observe(viewLifecycleOwner) { ahead ->
            binding.tvQueuesAhead.text = getString(R.string.label_queues_ahead, ahead)
        }

        viewModel.estimatedWaitMinutes.observe(viewLifecycleOwner) { minutes ->
            binding.tvEstWaitMinutes.text = getString(R.string.label_est_wait, minutes)
        }
    }

    private fun confirmCancelQueue() {
        val nomor = viewModel.activeQueue.value?.nomorAntrean ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_cancel_queue_title))
            .setMessage(getString(R.string.dialog_cancel_queue_msg, nomor))
            .setPositiveButton(getString(R.string.btn_yes_cancel)) { _, _ ->
                viewModel.cancelActiveQueue()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}