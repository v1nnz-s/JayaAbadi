package com.example.jayaabadi.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jayaabadi.R
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.databinding.ProductRvItemBinding
import com.example.jayaabadi.helper.getProductPrice
import java.text.NumberFormat
import java.util.*

class BestProductsAdapter : RecyclerView.Adapter<BestProductsAdapter.BestProductsViewHolder>() {

    inner class BestProductsViewHolder(private val binding: ProductRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                // Format harga ke dalam rupiah
                fun formatRupiah(price: Float): String {
                    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    return formatRupiah.format(price)
                }

                // Cek apakah daftar gambar tidak kosong sebelum memuatnya
                if (product.images.isNotEmpty()) {
                    Glide.with(itemView).load(product.images[0]).into(imgProduct)
                } else {
                    // Anda bisa menampilkan placeholder jika tidak ada gambar
                    imgProduct.setImageResource(R.drawable.ic_gambar)
                }

                // Menampilkan nama produk dan harga yang sudah diformat
                tvPrice.text = formatRupiah(product.price)
                tvName.text = product.name
            }
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestProductsViewHolder {
        return BestProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BestProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null

}