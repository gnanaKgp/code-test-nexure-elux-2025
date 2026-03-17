package io.nexure.discount.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.nexure.discount.model.Discount
import io.nexure.discount.model.Product
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class ProductRepository(mongoClient: MongoClient, databaseName: String = "productdb") {
    private val database: MongoDatabase = mongoClient.getDatabase(databaseName)
    private val collection = database.getCollection<Product>("products")
    
    suspend fun init() {
        collection.createIndex(
            Indexes.ascending("id"),
            IndexOptions().unique(true)
        )
    }
    
    suspend fun save(product: Product): Product {
        val filter = Filters.eq("id", product.id)
        collection.replaceOne(filter, product, com.mongodb.client.model.ReplaceOptions().upsert(true))
        return product
    }
    
    suspend fun findById(id: String): Product? {
        return collection.find(Filters.eq("id", id)).firstOrNull()
    }
    
    suspend fun findByCountry(country: String): List<Product> {
        return collection.find(Filters.eq("country", country)).toList()
    }
    
    suspend fun applyDiscount(productId: String, discount: Discount): Product? {
        // Ensure the product exists
        val exists = findById(productId) ?: return null

        // Atomically push the discount only if no element with the same discountId exists.
        // This single round-trip to MongoDB prevents race conditions entirely.
        val filter = Filters.and(
            Filters.eq("id", productId),
            Filters.not(Filters.elemMatch("discounts", Filters.eq("discountId", discount.discountId)))
        )
        val update = Updates.push("discounts", discount)
        val options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)

        val updated = collection.findOneAndUpdate(filter, update, options)

        // If the filter didn't match (discount already existed) return the current product state
        return updated ?: exists
    }
    
    suspend fun deleteAll() {
        collection.drop()
    }
}

