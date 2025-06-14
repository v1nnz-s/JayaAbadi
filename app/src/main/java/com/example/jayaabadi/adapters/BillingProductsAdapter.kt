package com.example.jayaabadi.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.jayaabadi.data.CartProduct
import com.example.jayaabadi.databinding.BillingProductsRvItemBinding
import com.example.jayaabadi.helper.getProductPrice
import java.text.NumberFormat
import java.util.*

class BillingProductsAdapter: Adapter<BillingProductsAdapter.BillingProductsViewHolder>() {

    inner class BillingProductsViewHolder(val binding: BillingProductsRvItemBinding): ViewHolder(binding.root) {

        fun bind(billingProduct: CartProduct) {
            binding.apply {
                // Cek apakah ada gambar
                if (!billingProduct.product.images.isNullOrEmpty()) {
                    Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                } else {
                    imageCartProduct.setImageDrawable(ColorDrawable(Color.LTGRAY)) // atau placeholder default
                }

                tvProductCartName.text = billingProduct.product.name
                tvBillingProductQuantity.text = billingProduct.quantity.toString()

                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                tvProductCartPrice.text = formatRupiah.format(billingProduct.product.price)

                imageCartProductColor.setImageDrawable(ColorDrawable(billingProduct.selectedColor ?: Color.TRANSPARENT))
                tvCartProductSize.text = billingProduct.selectedSize ?: ""
                if (billingProduct.selectedSize == null) {
                    imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>(){
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductsViewHolder {
        return BillingProductsViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BillingProductsViewHolder, position: Int) {
        if (position < differ.currentList.size) {
            val billingProduct = differ.currentList[position]
            holder.bind(billingProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}
