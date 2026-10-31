package com.vital.data.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class OpenFoodFactsClient(private val client: OkHttpClient, private val gson: Gson) {

    data class Product(
        val code: String,
        val product_name: String?,
        val nutriments: Nutriments?
    )

    data class Nutriments(
        val proteins_100g: Double?,
        val carbohydrates_100g: Double?,
        val fat_100g: Double?,
        val energy_kcal_100g: Double?
    )

    data class SearchResult(
        val products: List<Product>,
        val count: Int
    )

    @Suppress("UNUSED_PARAMETER")
    suspend fun searchByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = JsonParser.parseString(body).asJsonObject
            if (json.get("status").asInt == 1) {
                gson.fromJson(json.get("product"), Product::class.java)
            } else null
        } catch (e: IOException) {
            null
        }
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        val encodedQuery = query.replace(" ", "+")
        val request = Request.Builder()
            .url("https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encodedQuery&search_simple=1&action=process&json=1&page_size=20")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JsonParser.parseString(body).asJsonObject
            val productsArray = json.getAsJsonArray("products")
            productsArray.asList().mapNotNull { gson.fromJson(it, Product::class.java) }
                .filter { it.nutriments != null && it.product_name != null }
        } catch (e: IOException) {
            emptyList()
        }
    }
}