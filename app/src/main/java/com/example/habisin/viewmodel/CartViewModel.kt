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
