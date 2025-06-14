package com.example.jayaabadi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jayaabadi.data.Category
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.data.order.Order
import com.example.jayaabadi.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: Category
) : ViewModel() {

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts = _offerProducts.asStateFlow()

    init {
        fetchOfferProducts()
    }

    fun fetchOfferProducts() {
        viewModelScope.launch {
            _offerProducts.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("category", category.category)
            .get()
            .addOnSuccessListener {
                val products = it.toObjects(Product::class.java)
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Success(products))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

}
