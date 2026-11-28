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
