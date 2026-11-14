package com.example.queueless.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.queueless.R
import com.example.queueless.databinding.ItemQueueBinding
import com.example.queueless.model.Queue

class QueueAdapter : ListAdapter<Queue, QueueAdapter.QueueViewHolder>(DIFF_CALLBACK) {

    inner class QueueViewHolder(private val binding: ItemQueueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(queue: Queue) {
            val ctx = binding.root.context
            binding.tvQueueNumber.text = queue.nomorAntrean
            binding.tvQueueServiceName.text = queue.namaLayanan
            binding.tvQueueStudentInfo.text = ctx.getString(
                R.string.label_student_queue_info,
                queue.namaMahasiswa,
                queue.nimMahasiswa
            )
            binding.tvQueueStatus.text = queue.status

            val (textColor, bgColor) = when (queue.status) {
                "Dipanggil" -> Pair(R.color.status_called, R.color.status_called_bg)
                "Selesai" -> Pair(R.color.status_done, R.color.status_done_bg)
                "Dibatalkan" -> Pair(R.color.status_cancelled, R.color.status_cancelled_bg)
                else -> Pair(R.color.status_waiting, R.color.status_waiting_bg)
            }
            binding.tvQueueStatus.setTextColor(ContextCompat.getColor(ctx, textColor))
            binding.tvQueueStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, bgColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val binding = ItemQueueBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QueueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Queue>() {
            override fun areItemsTheSame(oldItem: Queue, newItem: Queue): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Queue, newItem: Queue): Boolean =
                oldItem == newItem
        }
    }
}