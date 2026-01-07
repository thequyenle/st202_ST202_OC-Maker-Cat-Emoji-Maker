package com.oc.maker.cat.emoji.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.data.model.custom.ItemColorModel
import com.oc.maker.cat.emoji.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            imvStroke.isVisible = item.isSelected

            //imvFocus.isVisible = item.isSelected
            root.tap { onItemClick.invoke(position) }
        }
    }
}