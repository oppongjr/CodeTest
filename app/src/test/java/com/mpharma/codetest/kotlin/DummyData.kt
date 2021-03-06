package com.mpharma.codetest.kotlin

import com.mpharma.codetest.data.api.PriceModel
import com.mpharma.codetest.data.api.ProductModel
import com.mpharma.codetest.data.local.entities.PriceEntity
import com.mpharma.codetest.data.local.entities.ProductEntity
import java.util.*

internal object DummyData {
    val productEntity = ProductEntity(name = "Paracetamol")

    val priceEntity =
        PriceEntity(price = 19.0, date = Date(), productId = UUID.randomUUID().toString())

    val products = listOf(
        ProductEntity(name = "Paracetamol"),
        ProductEntity(name = "Exforge 10mg"),
        ProductEntity(name = "Exforge 20mg")
    )

    val productModel = listOf(
        ProductModel(
            "Paracetamol",
            listOf(
                PriceModel(1, 12.0, Date()),
                PriceModel(2, 12.0, Date()),
                PriceModel(3, 12.0, Date()),
                PriceModel(4, 12.0, Date())
            )
        ),
        ProductModel(
            "ORS",
            listOf(
                PriceModel(1, 12.0, Date()),
                PriceModel(2, 12.0, Date()),
                PriceModel(3, 12.0, Date()),
                PriceModel(4, 12.0, Date())
            )
        ),
        ProductModel(
            "Vitamins",
            listOf(
                PriceModel(1, 12.0, Date()),
                PriceModel(2, 12.0, Date()),
                PriceModel(3, 12.0, Date()),
                PriceModel(4, 12.0, Date())
            )
        )
    )
}