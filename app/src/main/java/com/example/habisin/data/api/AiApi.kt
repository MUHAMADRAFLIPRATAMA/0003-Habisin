package com.example.habisin.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AiApi {
    @POST("ai/chat")
    suspend fun chat(
        @Body body: ChatRequest,
    ): ChatResponse

    @POST("ai/saran-harga")
    suspend fun suggestPrice(
        @Body body: PriceAiRequest,
    ): PriceAiResponse

    @POST("ai/deskripsi-produk")
    suspend fun productDescription(
        @Body body: DescriptionAiRequest,
    ): DescriptionAiResponse

    @GET("ai/ringkasan-penjualan")
    suspend fun salesSummary(): SalesSummaryResponse

    @POST("ai/cari")
    suspend fun smartSearch(
        @Body body: SmartSearchRequest,
    ): SmartSearchResponse

    @GET("ai/dampakku")
    suspend fun myImpact(): ImpactResponse

    @POST("ai/tanya-produk")
    suspend fun askProduct(
        @Body body: AskProductRequest,
    ): AskProductResponse

    @GET("ai/keranjang")
    suspend fun cartInsight(): CartInsightResponse

    @POST("ai/tanya-keranjang")
    suspend fun askCart(
        @Body body: CartQuestionRequest,
    ): CartAnswerResponse
}

data class ChatRequest(
    val pesan: String,
)

data class ChatResponse(
    val jawaban: String,
    val dari_gemini: Boolean,
    val produk: List<ChatProduct> = emptyList(),
)

data class ChatProduct(
    val id: Long,
    val nama: String,
    val deskripsi: String? = null,
    val harga_diskon: Int,
    val stok: Int,
    val jenis_makanan: String? = null,
)

data class PriceAiRequest(
    val nama: String,
    val harga_normal: Int,
    val batas_konsumsi: String? = null,
)

data class PriceAiResponse(
    val harga_saran: Int,
    val alasan: String,
)

data class DescriptionAiRequest(
    val nama: String,
    val jenis_makanan: String,
)

data class DescriptionAiResponse(
    val deskripsi: String,
)

data class SalesSummaryResponse(
    val jumlah_pesanan: Int,
    val jumlah_porsi: Int,
    val pendapatan: Int,
    val ringkasan: String,
)

data class SmartSearchRequest(
    val pertanyaan: String,
)

