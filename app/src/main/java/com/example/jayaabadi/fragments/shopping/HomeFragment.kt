package com.example.jayaabadi.fragments.shopping

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.jayaabadi.R
import com.example.jayaabadi.adapters.HomeViewpagerAdapter
import com.example.jayaabadi.adapters.SliderAdapter
import com.example.jayaabadi.databinding.FragmentHomeBinding
import com.example.jayaabadi.fragments.categories.*
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.g_orange))

        // Mengatur slider untuk ViewPager2
        val imageList = listOf(
            R.drawable.banner,
            R.drawable.banner,
            R.drawable.banner
        )
        val sliderAdapter = SliderAdapter(imageList)
        binding.sliderHome.adapter = sliderAdapter

        // Optional: Mengatur waktu perubahan otomatis slide
        val sliderHandler = android.os.Handler()
        val sliderRunnable = object : Runnable {
            override fun run() {
                if (binding.sliderHome.currentItem < imageList.size - 1) {
                    binding.sliderHome.currentItem = binding.sliderHome.currentItem + 1
                } else {
                    binding.sliderHome.currentItem = 0
                }
                sliderHandler.postDelayed(this, 3000) // Ganti slide setiap 3 detik
            }
        }
        sliderHandler.postDelayed(sliderRunnable, 3000)

        // Untuk menghentikan perubahan otomatis saat user melakukan swipe
        binding.sliderHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })

        // Mengatur adapter untuk ViewPager2
        val categoriesFragments = arrayListOf(
            MainCategoryFragment(),
            MesinFragment(),
            SparePartsFragment(),
            BukuFragment(),
            KertasFragment(),
            FolderFragment()

        )
        val viewPager2Adapter =
            HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter

        // Menyambungkan TabLayout dengan ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Main"
                1 -> tab.text = "Mesin"
                2 -> tab.text = "Spare Parts"
                3 -> tab.text = "Buku"
                4 -> tab.text = "Kertas"
                5 -> tab.text = "Folder"
            }
        }.attach()

        // Menambahkan aksi klik ke tombol pencarian
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
        val query = binding.searchBar.text.toString().trim()

    }
}
