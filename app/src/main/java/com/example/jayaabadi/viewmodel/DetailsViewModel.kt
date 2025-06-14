package com.example.jayaabadi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jayaabadi.data.CartProduct
import com.example.jayaabadi.firebase.FirebaseCommon
import com.example.jayaabadi.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct) {
        viewModelScope.launch {
            _addToCart.emit(Resource.Loading())
        }

        val userUid = auth.uid
        if (userUid == null) {
            viewModelScope.launch {
                _addToCart.emit(Resource.Error("User not authenticated"))
            }
            return
        }

        firestore.collection("user").document(userUid).collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.let { documents ->
                    if (documents.isEmpty()) {
                        // Add new product
                        addNewProduct(cartProduct)
                    } else {
                        val documentSnapshot = documents.firstOrNull()
                        val product = documentSnapshot?.toObject(CartProduct::class.java)

                        if (product != null) {
                            if (product.product == cartProduct.product &&
                                product.selectedColor == cartProduct.selectedColor &&
                                product.selectedSize == cartProduct.selectedSize) {
                                // Increase the quantity
                                val documentId = documentSnapshot.id
                                increaseQuantity(documentId, cartProduct)
                            } else {
                                // Add new product
                                addNewProduct(cartProduct)
                            }
                        } else {
                            // Add new product in case of null conversion
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    _addToCart.emit(Resource.Error(exception.message.toString()))
                }
            }
    }


    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}










