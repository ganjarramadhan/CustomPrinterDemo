package com.tabsquare.printer.core.request

import android.graphics.Bitmap

data class Restaurant(
    var merchantKey: String,
    var name: String?,
    var address: String?,
    var phone: String?,
    var moreInfo: String?,
    var topImage: Bitmap?,
    var bottomImage: Bitmap?
)
