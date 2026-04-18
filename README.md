# 🛒 Jaya Abadi E-Commerce App

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/language-Kotlin-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/backend-Firebase-orange?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/payment-Midtrans-red?style=for-the-badge"/>
</p>

<p align="center">
  🚀 Modern Android E-Commerce App with Smart Recommendation System  
</p>

---

## 🎥 Demo Preview (Animated)

<p align="center">
  <img src="docs/demo.gif" width="250"/>
</p>

---

## ✨ Features

* 🔐 Authentication (Firebase Auth)
* 🛍️ Product Catalog & Detail
* 🛒 Cart & Checkout System
* 💳 Midtrans Payment Gateway
* 📦 Order Management
* 🧠 Smart Product Recommendation (Market Basket Analysis)

---

## 🏗️ Architecture

* MVVM (Model - View - ViewModel)
* Clean Architecture
* Repository Pattern

---

## 🛠️ Tech Stack

| Category       | Technology            |
| -------------- | --------------------- |
| Language       | Kotlin                |
| Backend        | Firebase Firestore    |
| Storage        | Firebase Storage      |
| Authentication | Firebase Auth         |
| Payment        | Midtrans              |
| UI             | XML + Material Design |

---

## 📸 Screenshots

<p align="center">
  <img src="docs/home.png" width="200"/>
  <img src="docs/product.png" width="200"/>
  <img src="docs/checkout.png" width="200"/>
</p>

---

## 🧠 Market Basket Analysis (MBA)

Fitur ini menggunakan teknik **Market Basket Analysis** untuk memberikan rekomendasi produk berdasarkan pola pembelian pelanggan.

### 📌 Konsep

Market Basket Analysis adalah metode dalam data mining yang digunakan untuk menemukan hubungan antar produk dalam transaksi.

---

### ⚙️ Metode yang Digunakan

* Association Rule Mining
* Support
* Confidence
* Lift Ratio

---

### 📊 Rumus Dasar

**Support**

```
Support(A) = (Jumlah transaksi yang mengandung A) / (Total transaksi)
```

**Confidence**

```
Confidence(A → B) = (Jumlah transaksi mengandung A dan B) / (Jumlah transaksi mengandung A)
```

**Lift**

```
Lift(A → B) = Confidence(A → B) / Support(B)
```

---

### 🔄 Alur Sistem

1. Ambil data transaksi dari Firebase Firestore (`orders`)
2. Ekstrak produk dari setiap transaksi
3. Bentuk kombinasi itemset
4. Hitung Support, Confidence, dan Lift
5. Filter rule dengan threshold
6. Simpan ke Firebase
7. Tampilkan di aplikasi:

    * 🔥 Sering Dibeli Bersama
    * 💡 Rekomendasi Untuk Anda

---

### 💻 Implementasi (Kotlin)

```kotlin
fun performMarketBasketAnalysis(orders: List<Order>): List<Recommendation> {
    val pairCount = mutableMapOf<Pair<String, String>, Int>()
    val productCount = mutableMapOf<String, Int>()

    for (order in orders) {
        val products = order.products.map { it.name }.distinct()

        for (product in products) {
            productCount[product] = productCount.getOrDefault(product, 0) + 1
        }

        for (i in products.indices) {
            for (j in i + 1 until products.size) {
                val pair = Pair(products[i], products[j])
                pairCount[pair] = pairCount.getOrDefault(pair, 0) + 1
            }
        }
    }

    val recommendations = mutableListOf<Recommendation>()

    for ((pair, count) in pairCount) {
        val support = count.toDouble() / orders.size
        val confidence = count.toDouble() / productCount[pair.first]!!

        if (confidence > 0.3) {
            recommendations.add(
                Recommendation(
                    productA = pair.first,
                    productB = pair.second,
                    support = support,
                    confidence = confidence
                )
            )
        }
    }

    return recommendations
}
```

---

### 🎯 Manfaat

* 📈 Meningkatkan penjualan (cross-selling)
* 🛍️ Rekomendasi produk cerdas
* 📊 Analisis perilaku pelanggan
* 🚀 User experience lebih baik

---

### 🔮 Pengembangan Selanjutnya

* Implementasi algoritma Apriori
* Real-time recommendation
* Visualisasi grafik asosiasi produk
* AI-based recommendation system

---

## 📦 Download APK

👉 Coming soon

---

## 📈 Future Improvements

* 🔔 Push Notification
* 📊 Advanced Analytics
* 🤖 AI Recommendation System

---

## 👨‍💻 Author

**Davin Surya Saputra**

<p align="center">
  ⭐ Jangan lupa kasih star kalau project ini membantu!
</p>
