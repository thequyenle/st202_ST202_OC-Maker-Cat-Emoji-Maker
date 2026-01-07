package com.oc.maker.cat.emoji.ui.add_character.adapter

import android.annotation.SuppressLint
import android.content.Context
import com.oc.maker.cat.emoji.R
import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.extensions.setFont
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.data.model.SelectedModel
import com.oc.maker.cat.emoji.databinding.ItemFontBinding

class TextFontAdapter(val context: Context) : BaseAdapter<SelectedModel, ItemFontBinding>(ItemFontBinding::inflate) {
    var onTextFontClick: ((Int, Int) -> Unit) = { _, _ -> }
    private var currentSelected = 0

    override fun onBind(binding: ItemFontBinding, item: SelectedModel, position: Int) {
        binding.apply {
            tvFont.setFont(item.color)

            if (item.isSelected) {
                // Selected state - set selected background and change text color
                cvMain.setBackgroundResource(R.drawable.bg_circle_white_)

                tvFont.setTextColor(android.graphics.Color.parseColor("#000000")) // White text
            } else {
                // Not selected state - white circle background
                cvMain.setBackgroundResource(R.drawable.bg_circle_white)

                tvFont.setTextColor(android.graphics.Color.parseColor("#000000")) // Black text
            }

            root.tap { onTextFontClick.invoke(item.color, position) }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        if (position != currentSelected) {
            items.clear()
            items.addAll(list)

            notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitListReset(list: ArrayList<SelectedModel>){
        items.clear()
        items.addAll(list)
        currentSelected = 0
        notifyDataSetChanged()
    }
}