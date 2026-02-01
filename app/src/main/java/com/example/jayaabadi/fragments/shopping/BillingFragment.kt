package com.example.jayaabadi.fragments.shopping

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jayaabadi.R
import com.example.jayaabadi.adapters.AddressAdapter
import com.example.jayaabadi.adapters.BillingProductsAdapter
import com.example.jayaabadi.data.Address
import com.example.jayaabadi.data.CartProduct
import com.example.jayaabadi.data.order.Order
import com.example.jayaabadi.data.order.OrderStatus
import com.example.jayaabadi.data.payment.Customer
import com.example.jayaabadi.data.payment.MidtransRequest
import com.example.jayaabadi.data.payment.TransactionDetails
import com.example.jayaabadi.databinding.FragmentBillingBinding
import com.example.jayaabadi.network.RetrofitInstance
import com.example.jayaabadi.util.HorizontalItemDecoration
import com.example.jayaabadi.util.Resource
import com.example.jayaabadi.viewmodel.BillingViewModel
import com.example.jayaabadi.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class BillingFragment : Fragment() {

    private lateinit var binding: FragmentBillingBinding

    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }

    private val billingViewModel by viewModels<BillingViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()
    private val args by navArgs<BillingFragmentArgs>()

    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var selectedAddress: Address? = null

    // 🔹 Order sementara (disimpan setelah payment sukses)
    private var pendingOrder: Order? = null

    private enum class PaymentMethod {
        COD, ONLINE
    }

    private var selectedPaymentMethod = PaymentMethod.COD

    // 🔹 Launcher Snap UI
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val transactionResult =
                    result.data?.getParcelableExtra<TransactionResult>(
                        UiKitConstants.KEY_TRANSACTION_RESULT
                    )

                transactionResult?.let {
                    Log.d("Midtrans", "Status: ${it.status}")
                    Log.d("Midtrans", "TransactionId: ${it.transactionId}")

                    when (it.status) {
                        "success", "pending" -> {
                            pendingOrder?.let { order ->
                                orderViewModel.placeOrder(order)
                            }
                        }

                        "failed" -> {
                            Toast.makeText(requireContext(), "Pembayaran gagal", Toast.LENGTH_LONG).show()
                        }

                        "canceled" -> {
                            Toast.makeText(requireContext(), "Pembayaran dibatalkan", Toast.LENGTH_LONG).show()
                        }

                        else -> {
                            Toast.makeText(requireContext(), "Status tidak dikenal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice

        // 🔹 Init Midtrans UiKit
        UiKitApi.Builder()
            .withContext(requireContext().applicationContext)
            .withMerchantUrl("http://10.0.2.2:3000")
            .withMerchantClientKey("SB-Mid-client-hkL8Vvhkbf46iT0Y")
            .enableLog(true)
            .withColorTheme(
                CustomColorTheme(
                    "#FFE51255",
                    "#B61548",
                    "#FFE51255"
                )
            )
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        billingProductsAdapter.differ.submitList(products)

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvTotalPrice.text = formatRupiah.format(totalPrice)

        binding.imageCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        addressAdapter.onClick = {
            selectedAddress = it
        }

        // 🔹 Observe alamat
        lifecycleScope.launchWhenStarted {
            billingViewModel.address.collectLatest {
                when (it) {
                    is Resource.Loading -> binding.progressbarAddress.visibility = View.VISIBLE
                    is Resource.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        // 🔹 Observe order result
        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> binding.buttonPlaceOrder.startAnimation()
                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Snackbar.make(requireView(), "Pesanan berhasil dibuat", Snackbar.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.rbCod.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPaymentMethod = PaymentMethod.COD
            }
        }

        binding.rbOnlinePayment.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPaymentMethod = PaymentMethod.ONLINE
            }
        }

        // 🔹 BUTTON BAYAR
        binding.buttonPlaceOrder.setOnClickListener {

            val address = selectedAddress ?: run {
                Toast.makeText(requireContext(), "Pilih alamat terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val orderId = System.currentTimeMillis()

            when (selectedPaymentMethod) {

                PaymentMethod.COD -> {
                    // ✅ COD → langsung simpan order
                    val order = Order(
                        orderId = orderId,
                        orderStatus = OrderStatus.Pending.status,
                        totalPrice = totalPrice,
                        products = products,
                        address = address
                    )

                    orderViewModel.placeOrder(order)
                }

                PaymentMethod.ONLINE -> {
                    // ✅ Online → Midtrans
                    pendingOrder = Order(
                        orderId = orderId,
                        orderStatus = OrderStatus.Pending.status,
                        totalPrice = totalPrice,
                        products = products,
                        address = address
                    )

                    val request = MidtransRequest(
                        transaction_details = TransactionDetails(
                            order_id = orderId.toString(),
                            gross_amount = totalPrice.toDouble()
                        ),
                        customer_details = Customer(
                            first_name = address.fullName,
                            email = user.email ?: "default@mail.com",
                            phone = address.phone
                        )
                    )

                    lifecycleScope.launch {
                        try {
                            val response = RetrofitInstance.api.createTransaction(request)
                            if (response.isSuccessful) {
                                response.body()?.token?.let { token ->
                                    UiKitApi.getDefaultInstance().startPaymentUiFlow(
                                        activity = requireActivity(),
                                        launcher = launcher,
                                        snapToken = token
                                    )
                                }
                            } else {
                                Toast.makeText(requireContext(), "Gagal membuat transaksi", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }
}
