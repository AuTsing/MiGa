package com.autsing.miga.tile

import androidx.wear.protolayout.LayoutElementBuilders

fun column(builder: LayoutElementBuilders.Column.Builder.() -> Unit) =
    LayoutElementBuilders.Column.Builder().apply(builder).build()
