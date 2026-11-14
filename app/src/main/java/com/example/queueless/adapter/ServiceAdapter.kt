package com.example.queueless.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.queueless.R
import com.example.queueless.databinding.ItemServiceBinding
import com.example.queueless.model.Service

class ServiceAdapter(
    private val isAdmin: Boolean,
    private val onItemClick: (Service) -> Unit,
    private val onDeleteClick: ((Service) -> Unit)? = null
) : ListAdapter<Service, ServiceAdapter.ServiceViewHolder>(DIFF_CALLBACK) {

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            val ctx = binding.root.context
            binding.tvPrefixBadge.text = service.kodePrefix
            binding.tvServiceName.text = service.namaLayanan
            binding.tvServiceDesc.text = service.deskripsi
            binding.tvServiceEstimasi.text =
                ctx.getString(R.string.label_estimasi_format, service.estimasiMenit)
            binding.tvServiceKuota.text =
                ctx.getString(R.string.label_kuota_format, service.kuotaHarian)

            if (service.isAktif) {
                binding.tvServiceStatus.text = ctx.getString(R.string.label_service_active)
                binding.tvServiceStatus.setTextColor(
                    ContextCompat.getColor(ctx, R.color.status_called)
                )
            } else {
                binding.tvServiceStatus.text = ctx.getString(R.string.label_service_inactive)
                binding.tvServiceStatus.setTextColor(
                    ContextCompat.getColor(ctx, R.color.error_red)
                )
            }

            binding.btnDeleteService.visibility = if (isAdmin) View.VISIBLE else View.GONE
            binding.btnDeleteService.setOnClickListener {
                onDeleteClick?.invoke(service)
            }

            binding.root.setOnClickListener {
                onItemClick(service)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Service>() {
            override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
                oldItem == newItem
        }
    }
}