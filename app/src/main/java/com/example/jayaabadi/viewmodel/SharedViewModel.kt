package com.example.jayaabadi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jayaabadi.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _recommendations = MutableStateFlow<List<Product>>(emptyList())
    val recommendations: StateFlow<List<Product>> get() = _recommendations

    fun setRecommendations(products: List<Product>) {
        viewModelScope.launch {
            _recommendations.emit(products)
        }
    }
}
