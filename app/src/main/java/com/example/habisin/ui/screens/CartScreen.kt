package com.example.habisin.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.habisin.R
import com.example.habisin.ui.util.asString
import com.example.habisin.ui.components.HabisinBottomBar
import com.example.habisin.ui.components.HabisinButton
import com.example.habisin.ui.components.HabisinIcon
import com.example.habisin.ui.components.HabisinIconKind
import com.example.habisin.ui.components.HabisinQtyStepper
import com.example.habisin.ui.components.HabisinTextField
import com.example.habisin.ui.components.HabisinSearchField
import com.example.habisin.ui.components.HabisinTopBar
import com.example.habisin.ui.theme.HabisinColor
import com.example.habisin.ui.util.rupiah
import com.example.habisin.viewmodel.CartViewModel

@Composable
fun CartScreen(
    nav: NavHostController,
    vm: CartViewModel = viewModel(),
) {
    val cartItems by vm.items.collectAsState()
    val checkoutQr by vm.checkoutQr.collectAsState()
    val aiInsight by vm.aiInsight.collectAsState()
    val aiAnswer by vm.aiAnswer.collectAsState()
    val aiLoading by vm.aiLoading.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var query by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(PAYMENT_FALLBACK_SECONDS) }
    val messageText = message?.asString()
    LaunchedEffect(messageText) {
        messageText?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }
    // Hitung mundur mengikuti kode QR yang sedang tampil; saat habis, minta QR baru
    // (kode lama otomatis batal di backend sehingga tidak ada pembayaran ganda).
    LaunchedEffect(checkoutQr?.session_id, checkoutQr?.payment_code) {
        val qr = checkoutQr ?: return@LaunchedEffect
        secondsLeft = qr.detik_berlaku.takeIf { it > 0 } ?: PAYMENT_FALLBACK_SECONDS
        while (secondsLeft > 0) {
            delay(COUNTDOWN_TICK_MILLIS)
            secondsLeft--
            // Status tidak perlu ditanyakan tiap detik; cukup berkala agar hemat jaringan.
            if (secondsLeft % STATUS_POLL_INTERVAL_SECONDS == 0) vm.pollCheckoutStatus()
        }
        if (vm.checkoutQr.value != null) vm.refreshQr()
    }
    LaunchedEffect(Unit) { vm.load() }
    val total = cartItems.sumOf { (it.product?.discountPrice ?: 0) * it.jumlah }
    val totalItem = cartItems.sumOf { it.jumlah }

    Scaffold(
        containerColor = HabisinColor.Cream,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            HabisinTopBar(
                title = stringResource(R.string.cart_title),
                onBack = { nav.popBackStack() },
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                HabisinBottomBar {
                    HabisinButton(
                        text = stringResource(R.string.cart_pay_qr),
