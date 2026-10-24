package com.zeeshanbhatti.puzzlegame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(
    private val images: List<Int>,
    private var unlockedCount: Int,
    private val onItemClick: (position: Int, resId: Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgItem)
        val lockView: ImageView = itemView.findViewById(R.id.imgLock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageRes = images[position]
        holder.imageView.setImageResource(imageRes)

        val isUnlocked = position < unlockedCount

        if (isUnlocked) {
            holder.imageView.alpha = 1f
            holder.lockView.visibility = View.GONE
            holder.itemView.isClickable = true
        } else {
            holder.imageView.alpha = 0.4f
            holder.lockView.visibility = View.VISIBLE
            holder.itemView.isClickable = false
        }

        holder.itemView.setOnClickListener {
            if (isUnlocked) {
                onItemClick(position, imageRes)
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun updateUnlockedCount(newCount: Int) {
        unlockedCount = newCount
        notifyDataSetChanged()
    }
}
