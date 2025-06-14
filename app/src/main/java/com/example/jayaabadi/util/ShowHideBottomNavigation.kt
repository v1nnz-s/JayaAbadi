package com.example.jayaabadi.util

import androidx.fragment.app.Fragment
import com.example.jayaabadi.R
import com.example.jayaabadi.activities.ShoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.jayaabadi.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.jayaabadi.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}