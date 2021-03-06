package com.mpharma.codetest.domain

import android.util.Log
import androidx.room.withTransaction
import com.mpharma.codetest.data.api.ApiDataSource
import com.mpharma.codetest.data.local.ProductsDatabase
import com.mpharma.codetest.data.local.entities.PriceEntity
import com.mpharma.codetest.data.local.entities.ProductEntity
import com.mpharma.codetest.domain.mappers.EntityToPriceMapper
import com.mpharma.codetest.domain.mappers.EntityToProductMapper
import com.mpharma.codetest.domain.mappers.PriceToEntityMapper
import com.mpharma.codetest.domain.mappers.ProductToEntityMapper
import com.mpharma.codetest.domain.model.Price
import com.mpharma.codetest.domain.model.Product
import com.mpharma.codetest.domain.model.ProductAndPrices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    suspend fun getProductsWithPrices(): Flow<List<ProductAndPrices>>

    fun getProductWithPrices(productId: String): Flow<ProductAndPrices>

    suspend fun addNewProduct(productName: String, price: Double)

    suspend fun addNewPriceToProduct(price: Price)

    suspend fun deleteProductBy(id: String)

    suspend fun updateProduct(productId: String, productName: String, price: Double, withNewPrice: Boolean)
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val apiDataSource: ApiDataSource,
    private val database: ProductsDatabase,
    private val entityToPriceMapper: EntityToPriceMapper,
    private val entityToProductMapper: EntityToProductMapper,
    private val priceToEntityMapper: PriceToEntityMapper,
    private val productToEntityMapper: ProductToEntityMapper
) : AppRepository {
    override suspend fun getProductsWithPrices(): Flow<List<ProductAndPrices>> = flow {
        database.productsDao().getProducts().collect { entities ->
            val productsAndPrices = entities.map {
                ProductAndPrices(
                    product = entityToProductMapper.map(it.product),
                    prices = entityToPriceMapper.mapInputList(it.prices)
                )
            }

            if (productsAndPrices.isEmpty()) {
                fetchProductsFromServer()
            }

            emit(productsAndPrices)
        }

    }

    override suspend fun addNewProduct(productName: String, price: Double) {
        val productEntity = productToEntityMapper.map(Product(name = productName))

        database.productsDao().insertProduct(productEntity)
        addNewPriceToProduct(Price(price = price, date = Date(), productId = productEntity.id))
    }

    override suspend fun addNewPriceToProduct(price: Price) {
        database.productsDao().insertPrice(priceToEntityMapper.map(price))
    }

    override suspend fun deleteProductBy(id: String) {
        database.productsDao().deleteProductBy(id)
    }

    override suspend fun updateProduct(
        productId: String,
        productName: String,
        price: Double,
        withNewPrice: Boolean
    ) {
        updateProduct(productId, productName)

        if(withNewPrice){
            addNewPriceToProduct(Price(price = price, date = Date(), productId = productId))
        }
    }

    private suspend fun updateProduct(productId: String, productName: String) {
        val productEntity =
            productToEntityMapper.map(Product(name = productName)).copy(id = productId)

        database.productsDao().insertProduct(productEntity)
    }

    override fun getProductWithPrices(productId: String): Flow<ProductAndPrices> {
        return database.productsDao().getProductWithPrices(productId).map {
            Log.e("InRepo", it.product.name)

            ProductAndPrices(
                product = entityToProductMapper.map(it.product),
                prices = entityToPriceMapper.mapInputList(it.prices)
            )
        }
    }

    private suspend fun fetchProductsFromServer() = withContext(Dispatchers.IO) {
        apiDataSource.fetchProducts().collect { products ->
            database.withTransaction {
                products.forEach { product ->
                    val productEntity = ProductEntity(name = product.name)
                    database.productsDao().insertProduct(productEntity)

                    val priceEntities = product.prices.map {
                        PriceEntity(
                            price = it.price,
                            date = it.date,
                            productId = productEntity.id
                        )
                    }

                    database.productsDao().insertPrices(priceEntities)
                }
            }
        }

    }

}