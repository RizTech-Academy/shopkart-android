package com.riztech.shopkart.data.remote

import com.riztech.shopkart.data.remote.dto.ApiEnvelope
import com.riztech.shopkart.data.remote.dto.CategoryDto
import com.riztech.shopkart.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShopKartApi {

    @GET("api/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
    ): ApiEnvelope<List<ProductDto>>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): ApiEnvelope<ProductDto>

    @GET("api/categories")
    suspend fun getCategories(): ApiEnvelope<List<CategoryDto>>
}
