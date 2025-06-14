package com.example.jayaabadi.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.data.order.Order
import com.example.jayaabadi.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val specialProducts: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _bestDealsProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestDealsProducts: StateFlow<Resource<List<Product>>> = _bestDealsProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val pagingInfo = PagingInfo()

    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchOrders()
        fetchRecommendations()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            firestore.collection("orders").get().addOnSuccessListener { result ->
                Log.d(TAG, "Successfully retrieved orders.")
                val ordersList = result.toObjects(Order::class.java)
                Log.d(TAG, "Total orders retrieved: ${ordersList.size}")
                val recommendations = performMarketBasketAnalysis(ordersList)
                saveRecommendationsToFirebase(recommendations)
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting orders: ", exception)
            }
        }
    }

    fun performMarketBasketAnalysis(orders: List<Order>): Map<String, List<String>> {
        // Tipe Map yang jelas
        val frequencyMap: MutableMap<String, MutableList<String>> = mutableMapOf()

        // Mengumpulkan item dari semua order
        for (order in orders) {
            // Ambil nama produk dari CartProduct
            val productNames: List<String> = order.products.map { it.product.name } // Akses nama produk dari CartProduct
            Log.d(TAG, "Order products: ${order.products}")

            for (product in productNames) {
                // Periksa jika produk sudah ada di frequencyMap
                if (!frequencyMap.containsKey(product)) {
                    frequencyMap[product] = mutableListOf() // Inisialisasi dengan mutableListOf()
                }

                // Menambahkan produk yang dibeli bersamaan
                for (otherProduct in productNames) {
                    if (otherProduct != product) {
                        // Hanya tambahkan jika produk belum ada dalam daftar
                        if (!frequencyMap[product]!!.contains(otherProduct)) {
                            frequencyMap[product]!!.add(otherProduct)
                        }
                    }
                }
            }
        }

        // Kembalikan map dengan daftar produk yang direkomendasikan
        return frequencyMap.mapValues { it.value.distinct() } // Hanya mengembalikan produk unik
    }




    fun saveRecommendationsToFirebase(recommendations: Map<String, List<String>>) {
        val db = firestore

        for ((product, recommendedProducts) in recommendations) {
            val recommendationData = hashMapOf(
                "recommendedProducts" to recommendedProducts
            )

            db.collection("recommendations")
                .document(product)
                .set(recommendationData)
                .addOnSuccessListener {
                    Log.d(TAG, "Recommendations for $product saved successfully!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving recommendations for $product", e)
                }
        }
    }



    fun fetchSpecialProducts() {
        viewModelScope.launch {
            _specialProducts.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("category", "Special Products").get().addOnSuccessListener { result ->
                val specialProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Success(specialProductsList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }


    fun fetchBestDeals() {
        viewModelScope.launch {
            _bestDealsProducts.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("category", "Best Deals").get()
            .addOnSuccessListener { result ->
                val bestDealsProducts = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestDealsProducts.emit(Resource.Success(bestDealsProducts))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestDealsProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchRecommendations() {
        viewModelScope.launch {
            _bestProducts.emit(Resource.Loading()) // Emit status loading

            // Ambil semua order dari Firestore
            firestore.collection("orders").get().addOnSuccessListener { result ->
                val orders = result.toObjects(Order::class.java) // Ambil daftar order
                Log.d(TAG, "Total orders retrieved for recommendations: ${orders.size}")
                val recommendations = performMarketBasketAnalysis(orders) // Lakukan analisis

                Log.d(TAG, "Recommendations after analysis: $recommendations")
                // Inisialisasi daftar untuk menyimpan produk yang direkomendasikan
                val recommendedProducts = mutableListOf<Product>()

                // Ambil detail produk untuk setiap nama produk yang direkomendasikan
                val fetchJobs = recommendations.flatMap { entry ->
                    entry.value.map { productName ->
                        firestore.collection("Products")
                            .whereEqualTo("name", productName)
                            .get()
                            .addOnSuccessListener { productResult ->
                                productResult.forEach { productDoc ->
                                    val product = productDoc.toObject(Product::class.java)
                                    recommendedProducts.add(product) // Tambahkan produk yang ditemukan ke daftar
                                }

                                // Emit hasil rekomendasi
                                viewModelScope.launch {
                                    _bestProducts.emit(Resource.Success(recommendedProducts.distinctBy { it.name })) // Emit hasil rekomendasi
                                }
                            }
                    }
                }

            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestProducts.emit(Resource.Error(it.message.toString())) // Emit error jika gagal
                }
            }
        }
    }




}

internal data class PagingInfo(
    var bestProductsPage: Long = 1,
    var oldBestProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)












