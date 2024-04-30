package com.example.ibusiti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ibusiti.R
import com.example.ibusiti.Model.MenuModel

class ItemAdapter(private val itemList: ArrayList<MenuModel>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private lateinit var iListerner : onItemClickListener

    interface  onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        iListerner = clickListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ItemViewHolder(view, iListerner)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.tvjudul.text = currentItem.judul
        "Rp.${currentItem.harga}".also { holder.tvharga.text = it }
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .placeholder(R.drawable.random) // placeholder jika gambar belum dimuat
            .into(holder.ivGambar)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ItemViewHolder(itemView: View , clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvjudul: TextView = itemView.findViewById(R.id.tvJudulmenu)
        val tvharga: TextView = itemView.findViewById(R.id.tvHarga)
        val ivGambar: ImageView = itemView.findViewById(R.id.imgItem)
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
}
