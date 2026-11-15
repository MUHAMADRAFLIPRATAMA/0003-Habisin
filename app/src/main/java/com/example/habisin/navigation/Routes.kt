package com.example.habisin.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SELLER = "seller"
    const val ORDERS = "orders"
    const val AI = "ai"
    const val AI_PRODUCT = "ai_product/{product}"
    const val FAVORITES = "favorites"
    const val CART = "cart"
    const val SHOPS = "shops"
    const val SHOP_DETAIL = "shop/{id}"

    fun shopDetail(id: Long) = "shop/$id"
}
