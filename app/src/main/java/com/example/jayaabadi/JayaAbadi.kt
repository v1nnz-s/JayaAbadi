package com.example.jayaabadi

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JayaAbadi : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Midtrans SDK UI Kit
        UiKitApi.Builder()
            .withContext(this)
            .withMerchantClientKey("SB-Mid-client-hkL8Vvhkbf46iT0Y") // Ganti dengan client key kamu
            .withMerchantUrl("http://192.168.101.76:3000/") // Ganti dengan URL backend kamu
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .enableLog(true)
            .build()
    }

    // Fungsi ini dipanggil dari Activity ketika transaksi selesai
    fun handleTransactionResult(result: TransactionResult?) {
        if (result == null) {
            Log.d("Midtrans", "❌ Transaction result is null")
            return
        }

        val orderId = result.transactionId ?: "Unknown"
        val status = result.status ?: "UNKNOWN"

        // Tidak ada grossAmount di TransactionResult versi UI Kit
        // Kamu harus menyimpannya sebelumnya (di lokal atau backend)

        val grossAmount = 0.0 // Misalnya ambil dari tempat penyimpanan lokal

        when (status) {
            UiKitConstants.STATUS_SUCCESS -> {
                Log.d("Midtrans", "✅ Transaction success: $orderId")

                val orderData = hashMapOf(
                    "orderId" to orderId,
                    "orderStatus" to "Completed",
                    "totalPrice" to grossAmount,
                    "date" to System.currentTimeMillis()
                )

                FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(orderId)
                    .set(orderData)
                    .addOnSuccessListener {
                        Log.d("Midtrans", "✅ Order saved to Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Midtrans", "❌ Error saving order: ${e.message}")
                    }
            }

            UiKitConstants.STATUS_PENDING -> {
                Log.d("Midtrans", "⏳ Transaction pending: $orderId")
            }

            UiKitConstants.STATUS_FAILED -> {
                Log.d("Midtrans", "❌ Transaction failed: $orderId")
            }

            UiKitConstants.STATUS_CANCELED -> {
                Log.d("Midtrans", "🚫 Transaction canceled by user")
            }

            else -> {
                Log.d("Midtrans", "❓ Unknown status: $status")
            }
        }
    }
}
