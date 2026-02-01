package com.example.jayaabadi.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.jayaabadi.data.Category
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.util.Resource
import com.example.jayaabadi.viewmodel.AllOrdersViewModel
import com.example.jayaabadi.viewmodel.CategoryViewModel
import com.example.jayaabadi.viewmodel.MainCategoryViewModel
import com.example.jayaabadi.viewmodel.factory.BaseCategoryViewModelFactoryFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SparePartsFragment: BaseCategoryFragment(), Searchable {

    @Inject
    lateinit var firestore: FirebaseFirestore
    private var allBestProducts: List<Product> = listOf()
    private var allOfferProducts: List<Product> = listOf()
    private val mainCategoryViewModel by viewModels<MainCategoryViewModel>()
    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactoryFactory(firestore, Category.SpareParts)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.offerProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showOfferLoading()
                    }
                    is Resource.Success -> {
                        allOfferProducts = it.data ?: listOf()
                        offerAdapter.differ.submitList(allOfferProducts)
                        hideOfferLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideOfferLoading()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mainCategoryViewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showBestProductsLoading()
                    }
                    is Resource.Success -> {
                        allBestProducts = it.data ?: listOf()
                        bestProductsAdapter.differ.submitList(allBestProducts)
                        hideBestProductsLoading()
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                        hideBestProductsLoading()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun filterProduk(query: String) {
        val filteredBest = if (query.isNotEmpty()) {
            allBestProducts.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            allBestProducts
        }

        val filteredOffer = if (query.isNotEmpty()) {
            allOfferProducts.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            allOfferProducts
        }

        bestProductsAdapter.differ.submitList(filteredBest)
        offerAdapter.differ.submitList(filteredOffer)
    }

    override fun onBestProductsPagingRequest() {

    }

    override fun onOfferPagingRequest() {

    }
}