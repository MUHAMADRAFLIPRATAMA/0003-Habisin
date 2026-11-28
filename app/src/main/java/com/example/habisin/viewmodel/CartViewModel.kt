package com.example.habisin.viewmodel

import com.example.habisin.ui.util.UiText
import com.example.habisin.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habisin.data.api.ApiClient
import com.example.habisin.data.api.ApiError
import com.example.habisin.data.api.CartInsightResponse
import com.example.habisin.data.api.CartQuestionRequest
import com.example.habisin.data.api.CartRequest
import com.example.habisin.data.api.CartResponse
import com.example.habisin.data.api.CartUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartResponse>>(emptyList())
    val items = _items.asStateFlow()
    private val _message = MutableStateFlow<UiText?>(null)
    val message = _message.asStateFlow()
    private val _checkoutQr = MutableStateFlow<com.example.habisin.data.api.CheckoutQrResponse?>(null)
    val checkoutQr = _checkoutQr.asStateFlow()

    // Fitur AI keranjang: deteksi isi keranjang dan jawaban pertanyaan pembeli.
    private val _aiInsight = MutableStateFlow<CartInsightResponse?>(null)
    val aiInsight = _aiInsight.asStateFlow()
    private val _aiAnswer = MutableStateFlow<String?>(null)
    val aiAnswer = _aiAnswer.asStateFlow()
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading = _aiLoading.asStateFlow()

    fun loadAiInsight() =
        viewModelScope.launch {
            runCatching { ApiClient.aiApi.cartInsight() }
                .onSuccess { _aiInsight.value = it }
                // Biarkan data terakhir tetap tampil bila permintaan gagal sesaat.
                .onFailure { /* deteksi bersifat opsional, galat tidak perlu mengganggu layar */ }
        }

    fun tanyaAi(pertanyaan: String) =
        viewModelScope.launch {
            val isi = pertanyaan.trim()
            if (isi.isBlank()) return@launch
            _aiLoading.value = true
            runCatching { ApiClient.aiApi.askCart(CartQuestionRequest(isi)) }
                .onSuccess { jawab ->
                    _aiAnswer.value = jawab.jawaban
                    jawab.ringkasan?.let { ringkas ->
                        _aiInsight.value =
                            CartInsightResponse(
                                item = jawab.item,
                                ringkasan = ringkas,
                                dari_gemini = jawab.dari_gemini,
                                // Ikuti status model dari server, jangan dipaksa true.
                                model_tersedia = jawab.dari_gemini || _aiInsight.value?.model_tersedia == true,
                            )
                    }
                }.onFailure { _message.value = ApiError.message(it, R.string.msg_cart_ai_failed) }
            _aiLoading.value = false
        }

    fun clearAiAnswer() {
        _aiAnswer.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    fun checkout() =
        viewModelScope.launch {
            runCatching { ApiClient.cartApi.checkout() }
                .onSuccess { sesi ->
                    load()
                    loadQr(sesi.session_id)
                }.onFailure {
                    _message.value = ApiError.message(it, R.string.msg_checkout_failed)
                }
        }

    fun loadQr(id: Long) =
        viewModelScope.launch {
            runCatching { ApiClient.cartApi.checkoutQr(id) }
                .onSuccess { _checkoutQr.value = it }
                .onFailure { _message.value = ApiError.message(it, R.string.msg_qr_failed) }
        }

    fun refreshQr() =
        viewModelScope.launch {
            val id = _checkoutQr.value?.session_id ?: return@launch
            runCatching { ApiClient.cartApi.refreshCheckout(id) }
                .onSuccess { _checkoutQr.value = it }
                .onFailure { _message.value = ApiError.message(it, R.string.msg_qr_refresh_failed) }
        }

    fun pollCheckoutStatus() =
        viewModelScope.launch {
            val id = _checkoutQr.value?.session_id ?: return@launch
            runCatching { ApiClient.cartApi.checkoutStatus(id) }.onSuccess {
                if (it.status == STATUS_LUNAS) {
                    _checkoutQr.value = null
                    _message.value = UiText.Res(R.string.msg_payment_success)
                    load()
                }
            }
        }

    fun clearCheckout() {
        _checkoutQr.value = null
    }

    fun load(query: String = "") =
        viewModelScope.launch {
            runCatching { ApiClient.cartApi.list(query) }
                .onSuccess {
                    _items.value = it
                    // Jawaban lama tidak relevan lagi setelah isi keranjang berubah.
                    clearAiAnswer()
                    loadAiInsight()
                }.onFailure { _message.value = ApiError.message(it, R.string.msg_cart_failed) }
        }

    fun add(
        productId: Long,
        quantity: Int = 1,
    ) = viewModelScope.launch {
        runCatching { ApiClient.cartApi.add(CartRequest(productId, quantity)) }
            .onSuccess {
                _message.value = UiText.Res(R.string.msg_cart_added)
            }.onFailure { _message.value = ApiError.message(it, R.string.msg_cart_add_failed) }
    }

    fun update(
        id: Long,
        quantity: Int,
    ) = viewModelScope.launch {
        runCatching { ApiClient.cartApi.update(id, CartUpdateRequest(quantity)) }
            .onSuccess { load() }
            .onFailure { _message.value = ApiError.message(it, R.string.msg_cart_update_failed) }
    }

    fun remove(id: Long) =
        viewModelScope.launch {
            runCatching { ApiClient.cartApi.delete(id) }
                .onSuccess { _items.value = _items.value.filterNot { it.id == id } }
                .onFailure { _message.value = ApiError.message(it, R.string.msg_cart_delete_failed) }
        }

    companion object {
        /** Nilai `status` pada respons sesi checkout ketika pembayaran sudah masuk. */
        const val STATUS_LUNAS = "dibayar"
    }
}
