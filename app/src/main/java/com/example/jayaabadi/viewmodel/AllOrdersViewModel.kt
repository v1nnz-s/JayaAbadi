package com.example.jayaabadi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jayaabadi.data.order.Order
import com.example.jayaabadi.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AllOrdersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    private val _frequentItemsets = MutableStateFlow<Resource<List<Set<String>>>>(Resource.Unspecified())
    val frequentItemsets = _frequentItemsets.asStateFlow()

    init {
        getAllOrders()
    }

    private fun getAllOrders() {
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
            try {
                val orders = firestore.collection("user")
                    .document(auth.uid ?: throw IllegalStateException("User ID is null"))
                    .collection("orders")
                    .get()
                    .await()
                    .toObjects(Order::class.java)

                _allOrders.emit(Resource.Success(orders))

                // Melakukan analisis keranjang pasar
                performMarketBasketAnalysis(orders)

            } catch (e: Exception) {
                _allOrders.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    internal fun performMarketBasketAnalysis(orders: List<Order>) {
        viewModelScope.launch {
            // Mengonversi produk dari CartProduct menjadi daftar string (misalnya ID produk)
            val transactions = orders.map { order ->
                Transaction(
                    transactionId = order.orderId.toString(), // Menggunakan orderId sebagai ID transaksi
                    products = order.products.map { it.product.id } // Mengambil ID produk dari CartProduct
                )
            }

            val minSupport = 0.1 // Tetapkan nilai minimum support Anda
            val recommendations = apriori(transactions, minSupport)

            // Emit atau tangani rekomendasi sesuai kebutuhan
            _frequentItemsets.emit(Resource.Success(recommendations))
        }
    }


    private fun apriori(transactions: List<Transaction>, minSupport: Double): List<Set<String>> {
        val productCounts = mutableMapOf<String, Int>()
        val transactionCount = transactions.size

        for (transaction in transactions) {
            for (product in transaction.products) {
                val count = productCounts[product] ?: 0
                productCounts[product] = count + 1
            }
        }

        val frequentItemsets = mutableListOf<Set<String>>()
        val minSupportCount = (minSupport * transactionCount).toInt()

        for ((product, count) in productCounts) {
            if (count >= minSupportCount) {
                frequentItemsets.add(setOf(product))
            }
        }

        var k = 2
        while (true) {
            val candidateItemsets = mutableSetOf<Set<String>>()
            for (i in 0 until frequentItemsets.size) {
                for (j in i + 1 until frequentItemsets.size) {
                    val candidate = frequentItemsets[i] union frequentItemsets[j]
                    if (candidate.size == k) {
                        candidateItemsets.add(candidate)
                    }
                }
            }

            val validItemsets = mutableListOf<Set<String>>()
            for (candidate in candidateItemsets) {
                var count = 0
                for (transaction in transactions) {
                    if (candidate.all { it in transaction.products }) {
                        count++
                    }
                }
                if (count >= minSupportCount) {
                    validItemsets.add(candidate)
                }
            }

            if (validItemsets.isEmpty()) break

            frequentItemsets.addAll(validItemsets)
            k++
        }

        return frequentItemsets
    }
}

data class Transaction(
    val transactionId: String,
    val products: List<String>
)
