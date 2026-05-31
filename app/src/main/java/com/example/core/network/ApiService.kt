package com.example.core.network

import com.example.core.models.Category
import com.example.core.models.Product
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface ApiService {
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<Product>

    @GET("products/{id}")
    suspend fun getProduct(
        @Path("id") id: Int
    ): Product

    @GET("categories")
    suspend fun getCategories(): List<Category>

    @GET("categories/{id}/products")
    suspend fun getCategoryProducts(
        @Path("id") categoryId: Int,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<Product>

    @GET("products")
    suspend fun searchProducts(
        @Query("title") query: String
    ): List<Product>
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.escapi.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    // High fidelity offline fallback database for gorgeous, real-looking fashion catalog
    val fallbackCategories = listOf(
        Category(1, "Premium Apparel", "https://picsum.photos/seed/apparel/600/600"),
        Category(2, "Footwear & Kicks", "https://picsum.photos/seed/kicks/600/600"),
        Category(3, "Boutique Accessories", "https://picsum.photos/seed/accessories/600/600"),
        Category(4, "Luxury Streetwear", "https://picsum.photos/seed/streetwear/600/600"),
        Category(5, "Summer Collection", "https://picsum.photos/seed/summer/600/600")
    )

    val fallbackProducts = listOf(
        Product(101, "Velvet Gold Luxe Bomber Jacket", 149.99, "A stunning premium bomber jacket tailored from high-density Italian velvet, detailed with hand-worked metallic gold embroidery. Designed for high fashion expression.", fallbackCategories[3], listOf("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&q=80", "https://picsum.photos/seed/jacket2/600/800")),
        Product(102, "Drip Signature Silk Trench", 199.50, "Full-length draped trench coat made from luxury mulberry silk blend. Features matching gold belt buckles and high-collar wind flaps.", fallbackCategories[0], listOf("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=500&q=80", "https://picsum.photos/seed/trench2/600/800")),
        Product(103, "Aero Drip Retro Gold Low-Top", 120.00, "Classic 80s sneaker silhouette refined with genuine premium nubuck and metallic gold foil inlays. Air-cushioned rubber sole for elite comfort.", fallbackCategories[1], listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&q=80")),
        Product(104, "Gold Accent Oversized Hoodie", 85.00, "Ultra-heavy weight 480GSM loopback cotton hoodie in pure onyx black with golden metal aglets and embroidered 3D Drip signature emblem.", fallbackCategories[3], listOf("https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500&q=80")),
        Product(105, "Urban Monograph Cargo Pants", 95.00, "Modular layout lightweight waterproof nylon cargo pants featuring adjustable tactical straps and golden zip pulls. Relaxed fit.", fallbackCategories[3], listOf("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&q=80")),
        Product(106, "Chrono Premium Gold Watch", 250.00, "Elegant timepiece with custom gold-tinted dial face, scratch-proof sapphire crystal glass, and premium black crocodile-pattern real leather band.", fallbackCategories[2], listOf("https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=500&q=80")),
        Product(107, "Boutique Leather Handbag", 180.00, "Structured handbag constructed with premium full-grain Italian leather, featuring luxurious gold chain strap links and secure twin lock locks.", fallbackCategories[2], listOf("https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500&q=80")),
        Product(108, "Drip Elite High-Top Gold Edition", 145.00, "Limited release basketball-inspired sneaker featuring dynamic gold panels, custom laces, and lightweight EVA foam midsoles.", fallbackCategories[1], listOf("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&q=80")),
        Product(109, "Golden Coast Breeze Linen Shirt", 69.99, "Ethically-sourced organic linen button-down shirt. Breathable, light, and tailored for effortless elegant sizing on summer retreats.", fallbackCategories[4], listOf("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&q=80")),
        Product(110, "Sunset Gold Aviator Sunglasses", 55.00, "Double-bridged luxury stainless steel aviators finished with 18k gold plating. 100% UVA/UVB polarized anti-reflection lenses.", fallbackCategories[2], listOf("https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&q=80")),
        Product(111, "Pleated Luxury Midi Skirt - Beige", 79.00, "Symmetrical sharp pleated skirt styled with elegant high rise waistline and gold zipper details. Flows beautifully when walking.", fallbackCategories[0], listOf("https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500&q=80")),
        Product(112, "Tuxedo-cut Premium Slim Blazer", 160.00, "Tailored single breasted modern blazer with satin peaks. Finished with dynamic inner gold embroidery lining for subtle flex.", fallbackCategories[0], listOf("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=500&q=80"))
    )
}
