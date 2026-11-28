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
                        onClick = { vm.checkout() },
                    )
                }
            }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = dimensionResource(R.dimen.space_20))) {
            HabisinSearchField(
                value = query,
                onValueChange = {
                    query = it
                    vm.load(it)
                },
                placeholder = stringResource(R.string.cart_search_hint),
                modifier = Modifier.padding(top = dimensionResource(R.dimen.space_12)),
            )
            if (cartItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.cart_empty), style = MaterialTheme.typography.titleMedium, color = HabisinColor.Muted)
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).padding(top = dimensionResource(R.dimen.space_14)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_12)),
                ) {
                    item {
                        CartAiCard(
                            insight = aiInsight,
                            answer = aiAnswer,
                            loading = aiLoading,
                            onAsk = { pertanyaan -> vm.tanyaAi(pertanyaan) },
                        )
                    }
                    items(cartItems, key = { it.id }) { item ->
                        val product = item.product
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(dimensionResource(R.dimen.radius_16)))
                                .border(dimensionResource(R.dimen.stroke_1), HabisinColor.Border, RoundedCornerShape(dimensionResource(R.dimen.radius_16)))
                                .padding(dimensionResource(R.dimen.space_12)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_12)),
                        ) {
                            Box(
                                Modifier
                                    .size(dimensionResource(R.dimen.empty_state_icon_box))
                                    .background(HabisinColor.GreenSoft, RoundedCornerShape(dimensionResource(R.dimen.radius_12))),
                                contentAlignment = Alignment.Center,
                            ) {
                                HabisinIcon(HabisinIconKind.Image, size = dimensionResource(R.dimen.icon_24), color = HabisinColor.Mint)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_2))) {
                                Text(
                                    product?.name ?: stringResource(R.string.cart_product_fallback),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = HabisinColor.Ink,
                                )
                                Text(
                                    product?.shop.orEmpty(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HabisinColor.Muted,
                                )
                                Text(
                                    rupiah(product?.discountPrice ?: 0),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HabisinColor.Green,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_6))) {
                                Box(
                                    Modifier.size(dimensionResource(R.dimen.icon_24)).clickable { vm.remove(item.id) },
                                    contentAlignment = Alignment.Center,
                                ) { HabisinIcon(HabisinIconKind.Trash, size = dimensionResource(R.dimen.icon_16), color = HabisinColor.Faint) }
                                HabisinQtyStepper(
                                    quantity = item.jumlah,
                                    onChange = { value ->
                                        if (product == null || value <= product.stock) vm.update(item.id, value)
                                    },
                                    min = 1,
                                    max = product?.stock?.coerceAtLeast(1) ?: 99,
                                )
                            }
                        }
                    }
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(dimensionResource(R.dimen.radius_18)))
                                .border(dimensionResource(R.dimen.stroke_1), HabisinColor.Border, RoundedCornerShape(dimensionResource(R.dimen.radius_18)))
                                .padding(dimensionResource(R.dimen.space_16)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_6)),
                        ) {
                            Text(stringResource(R.string.cart_summary), style = MaterialTheme.typography.titleMedium)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    stringResource(R.string.cart_item_count, totalItem),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HabisinColor.Muted,
                                )
                                Text(rupiah(total), style = MaterialTheme.typography.bodySmall, color = HabisinColor.Ink)
                            }
                            Box(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.stroke_1)).background(HabisinColor.BorderSoft))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.cart_total), style = MaterialTheme.typography.titleMedium)
                                Text(rupiah(total), style = MaterialTheme.typography.titleLarge, color = HabisinColor.Green)
                            }
                        }
                    }
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(HabisinColor.GreenSoft, RoundedCornerShape(dimensionResource(R.dimen.radius_14)))
                                .padding(horizontal = dimensionResource(R.dimen.space_14), vertical = dimensionResource(R.dimen.space_12)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_10)),
                        ) {
                            HabisinIcon(HabisinIconKind.Clock, size = dimensionResource(R.dimen.icon_18), color = HabisinColor.Green)
                            Text(
                                stringResource(R.string.cart_saved_note),
                                style = MaterialTheme.typography.labelLarge,
                                color = HabisinColor.Green,
                            )
                        }
                    }
                }
            }
        }
    }

    checkoutQr?.let { qr ->
        val bytes = remember(qr.gambar_qr) { Base64.decode(qr.gambar_qr.substringAfter(","), Base64.DEFAULT) }
        val bitmap = remember(qr.gambar_qr) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
        AlertDialog(
            onDismissRequest = { vm.clearCheckout() },
            title = { Text(stringResource(R.string.payment_qr_title)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_8))) {
                    if (bitmap != null) {
                        Image(
                            bitmap,
                            stringResource(R.string.payment_qr_alt),
                            Modifier.size(dimensionResource(R.dimen.qr_image_size)),
                        )
                    }
                    Text(
                        stringResource(R.string.payment_qr_total, rupiah(qr.total_harga)),
                        style = MaterialTheme.typography.titleLarge,
                        color = HabisinColor.Green,
                    )
                    Text(stringResource(R.string.payment_time_left, secondsLeft), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.payment_qr_scan_other_phone), style = MaterialTheme.typography.bodySmall, color = HabisinColor.Muted)
                    Text(stringResource(R.string.payment_qr_code, qr.payment_code), style = MaterialTheme.typography.labelMedium, color = HabisinColor.Muted)
                    Text(qr.url, style = MaterialTheme.typography.labelSmall, color = HabisinColor.Faint)
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.refreshQr() }) { Text(stringResource(R.string.payment_qr_new)) }
            },
            dismissButton = {
                TextButton(onClick = { vm.clearCheckout() }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

@Composable
private fun CartAiCard(
    insight: com.example.habisin.data.api.CartInsightResponse?,
    answer: String?,
    loading: Boolean,
    onAsk: (String) -> Unit,
) {
    var pertanyaan by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxWidth()
