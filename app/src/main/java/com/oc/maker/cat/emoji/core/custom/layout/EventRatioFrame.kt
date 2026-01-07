package com.oc.maker.cat.emoji.core.custom.layout

import android.widget.ImageView
import com.oc.maker.cat.emoji.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}