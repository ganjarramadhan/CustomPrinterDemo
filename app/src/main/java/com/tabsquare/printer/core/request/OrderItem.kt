package com.tabsquare.printer.core.request

data class OrderItem(
    var customisationId: Int?,
    var skuId: Int?,
    var dishId: Int?,
    var dishName: String?,
    var skuName: String?,
    var isPrintSku: Boolean = true,
    var quantity: Int = 1,
    var pricePerUnit: Double?,
    var discount: Double?,
    var tax: Double?,
    var totalPrice: Double?,
    var remarks: String?,
    var modifiers: List<OrderItem>?
) {

    val modifiersId: String
    get() {
        var modifiersId = "|##$customisationId-$dishId-{$skuId##"

        modifiers?.forEach { nested ->
            modifiersId += "*${nested.customisationId}-${nested.dishId}-{${nested.skuId}*"
            modifiersId += "|"
        }
        modifiersId += "|"
        return modifiersId
    }

    fun getModifierIds(): String {
        var modifiersId = ""

        modifiers?.forEach {
            modifiersId += "|##${it.customisationId}-${it.dishId}-{${it.skuId}##"
            it.modifiers?.forEach { nested ->
                modifiersId += "*${nested.customisationId}-${nested.dishId}-{${nested.skuId}*"
            }
            modifiersId += "|"
        }

        return modifiersId
    }
}
