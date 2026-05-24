package com.leanmass.calculator.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leanmass.calculator.database.DatabaseHelper
import com.leanmass.calculator.database.HistoryItem
import com.leanmass.calculator.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onDelete: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.tvHistLbm.text = "LBM : %.2f kg".format(item.lbm)
            binding.tvHistDetails.text = "${item.gender} • ${item.weight}kg • ${item.height}cm"
            binding.tvHistDate.text = item.date

            val isMale = item.gender == "Homme"
            val isSatisfying = if (isMale) {
                item.lbm >= DatabaseHelper.LBM_MIN_MALE
            } else {
                item.lbm >= DatabaseHelper.LBM_MIN_FEMALE
            }

            binding.tvHistIcon.text = if (isSatisfying) "😊" else "😟"

            binding.btnDelete.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem) =
            oldItem == newItem
    }
}