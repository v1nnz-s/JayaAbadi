package com.example.jayaabadi.data.payment

import com.google.gson.annotations.SerializedName

data class MidtransRequest(
    val transaction_details: TransactionDetails,
    val customer_details: Customer
)

data class TransactionDetails(
    val order_id: String,
    val gross_amount: Double
)

data class Customer(
    @SerializedName("first_name")
    val first_name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String
)


data class MidtransResponse(
    val token: String,
    val redirect_url: String
)
