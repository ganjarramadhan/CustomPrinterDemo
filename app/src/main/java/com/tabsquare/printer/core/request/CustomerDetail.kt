package com.tabsquare.printer.core.request

data class CustomerDetail(
    var name: String?,
    var address: String?,
    var tin: String?,
    var businessType: String?,
    var printCustomerNameAsOrderNo: Boolean?
)
