package com.example.jayaabadi.data.order

sealed class OrderStatus(val status: String) {

    // 🔹 PAYMENT STATUS
    object Pending : OrderStatus("PENDING")      // COD / Online belum dibayar
    object Paid : OrderStatus("PAID")            // Online sukses
    object Failed : OrderStatus("FAILED")        // Online gagal
    object Canceled : OrderStatus("CANCELED")    // Dibatalkan user/admin

    // 🔹 ORDER PROCESS STATUS
    object Confirmed : OrderStatus("CONFIRMED")  // Diproses admin
    object Shipped : OrderStatus("SHIPPED")      // Dikirim
    object Delivered : OrderStatus("DELIVERED")  // Sampai
    object Returned : OrderStatus("RETURNED")    // Retur
}

fun getOrderStatus(status: String): OrderStatus {
    return when (status.uppercase()) {
        "PENDING" -> OrderStatus.Pending
        "PAID" -> OrderStatus.Paid
        "FAILED" -> OrderStatus.Failed
        "CANCELED" -> OrderStatus.Canceled
        "CONFIRMED" -> OrderStatus.Confirmed
        "SHIPPED" -> OrderStatus.Shipped
        "DELIVERED" -> OrderStatus.Delivered
        else -> OrderStatus.Returned
    }
}
