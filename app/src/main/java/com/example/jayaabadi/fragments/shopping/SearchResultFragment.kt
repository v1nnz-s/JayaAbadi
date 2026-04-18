package com.example.jayaabadi.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jayaabadi.adapters.ProductAdapter
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.databinding.FragmentSearchResultBinding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultBinding
    private lateinit var adapter: ProductAdapter

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val args by navArgs<SearchResultFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val query = args.query.trim()
        adapter = ProductAdapter()

        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = adapter

        searchProduct(query)
    }

    private fun searchProduct(query: String) {
        val queryLower = query.trim().lowercase()
        firestore.collection("Products")
            .whereGreaterThanOrEqualTo("nameLowerCase", queryLower)
            .whereLessThanOrEqualTo("nameLowerCase", queryLower + "\uf8ff")
            .get()
            .addOnSuccessListener {
                val results = it.toObjects(Product::class.java)
                adapter.submitList(results)

                if (results.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.emptyLottie.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.emptyLottie.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

}
