package com.example.jayaabadi.data.order

import android.os.Parcelable
import com.example.jayaabadi.data.Address
import com.example.jayaabadi.data.CartProduct
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Order(
    val orderStatus: String = "Ordered",
    val totalPrice: Float = 0f,
    val products: List<CartProduct> = emptyList(),
    val address: Address = Address(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = 0L
) : Parcelable

