package com.example.jayaabadi.fragments.shopping

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jayaabadi.R
import com.example.jayaabadi.adapters.ProductAdapter
import com.example.jayaabadi.data.Product
import com.example.jayaabadi.databinding.FragmentSearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var recentSearchAdapter: ArrayAdapter<String>
    private lateinit var recommendedAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val recentSearches = mutableListOf<String>()

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadSearches()
        setupRecentSearchRecycler()
        setupRecommendedRecycler()
        fetchPopularProducts()
        setupSearchView()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                addRecentSearch(query)
                saveSearches()

                val action = SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(query)
                findNavController().navigate(action)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })
    }

    private fun addRecentSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        // Hapus jika sudah ada, lalu tambahkan ke posisi teratas
        recentSearches.remove(trimmedQuery)
        recentSearches.add(0, trimmedQuery)

        // Batasi maksimal 5 item
        if (recentSearches.size > 5) {
            recentSearches.removeLast()
        }

        // Update tampilan & simpan ke SharedPreferences
        binding.rvRecentSearch.adapter?.notifyDataSetChanged()
        saveSearches()
    }

    private fun setupRecentSearchRecycler() {
        binding.rvRecentSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentSearch.adapter = object : RecyclerView.Adapter<RecentViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                return RecentViewHolder(view)
            }

            override fun getItemCount() = recentSearches.size

            override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
                holder.bind(recentSearches[position])
            }
        }
    }

    private fun setupRecommendedRecycler() {
        recommendedAdapter = ProductAdapter()
        binding.rvRecommended.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecommended.adapter = recommendedAdapter
    }

    private fun fetchPopularProducts() {
        firestore.collection("Products")
            .limit(6)
            .get()
            .addOnSuccessListener {
                val products = it.toObjects(Product::class.java)
                recommendedAdapter.submitList(products)
            }
    }

    private fun saveSearches() {
        val shared = requireContext().getSharedPreferences("search", Context.MODE_PRIVATE)
        shared.edit().putStringSet("history", recentSearches.toSet()).apply()
    }

    private fun loadSearches() {
        val shared = requireContext().getSharedPreferences("search", Context.MODE_PRIVATE)
        val set = shared.getStringSet("history", emptySet())
        recentSearches.clear()
        recentSearches.addAll(set ?: emptySet())
    }

    inner class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(text: String) {
            (itemView as TextView).text = text
            itemView.setOnClickListener {
                binding.searchView.setQuery(text, true)
            }
        }
    }
}
