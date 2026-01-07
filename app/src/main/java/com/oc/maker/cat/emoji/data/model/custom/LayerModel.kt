package com.oc.maker.cat.emoji.data.model.custom

import com.oc.maker.cat.emoji.data.model.custom.ColorModel

data class LayerModel(
    val image: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf()
)