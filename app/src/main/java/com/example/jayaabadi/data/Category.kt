package com.example.jayaabadi.data

sealed class Category(val category: String) {

    object Chair: Category("Chair")
    object Cupboard: Category("Cupboard")
    object Table: Category("Table")
    object Accessory: Category("Accessory")
    object Furniture: Category("Furniture")
    object Mesin: Category("Mesin")
    object SpareParts: Category("SpareParts")
    object Buku: Category("Buku")
    object Kertas: Category("Kertas")
    object Folder: Category("Folder")
}