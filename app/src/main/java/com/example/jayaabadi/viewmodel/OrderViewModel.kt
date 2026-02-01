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
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())

            try {
                val uid = auth.uid ?: throw Exception("User not logged in")

                // 1️⃣ Ambil cart dulu (SEBELUM batch)
                val cartSnapshot = firestore.collection("user")
                    .document(uid)
                    .collection("cart")
                    .get()
                    .await()

                // 2️⃣ Baru batch
                firestore.runBatch { batch ->

                    val userOrderRef = firestore.collection("user")
                        .document(uid)
                        .collection("orders")
                        .document(order.orderId.toString())

                    val adminOrderRef = firestore.collection("orders")
                        .document(order.orderId.toString())

                    batch.set(userOrderRef, order)
                    batch.set(adminOrderRef, order)

                    cartSnapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                }

                _order.emit(Resource.Success(order))

            } catch (e: Exception) {
                _order.emit(Resource.Error(e.message ?: "Order failed"))
            }
        }
    }
}















