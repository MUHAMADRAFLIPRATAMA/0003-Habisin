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
