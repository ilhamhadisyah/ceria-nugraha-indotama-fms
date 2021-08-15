package org.traccar.client.ui.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.traccar.client.R
import org.traccar.client.data.model.ActivityModel
import org.traccar.client.databinding.LogItemBinding
import org.traccar.client.utils.StringBuilder

class LogAdapter(val data : ArrayList<ActivityModel>) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {
    inner class ViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {

        private val binding = LogItemBinding.bind(itemView)

        fun bind(activity : ActivityModel){
            binding.logText.text = StringBuilder.log(activity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}