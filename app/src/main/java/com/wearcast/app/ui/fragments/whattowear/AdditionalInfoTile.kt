package com.wearcast.app.ui.fragments.whattowear

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.wearcast.app.R
import com.wearcast.app.databinding.AdditionalInfoTileBinding

class AdditionalInfoTile(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var binding: AdditionalInfoTileBinding

    init {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AdditionalInfoTileBinding.inflate(layoutInflater)
        inflate(context, R.layout.additional_info_tile, this)
        addView(binding.root)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AdditionalInfoTile,
            0, 0).apply {
            try {
                val title_text = getString(R.styleable.AdditionalInfoTile_title)
                val value_text = getString(R.styleable.AdditionalInfoTile_value)
                val icon_res = getResourceId(R.styleable.AdditionalInfoTile_iconResource, 0)

                binding.additionalInfoTitle.text = title_text
                binding.additionalInfoValue.text = value_text
                binding.additionalInfoIcon.setImageResource(icon_res)

            }
            finally {
                recycle()
            }
        }
    }

    fun setValue(value: String) {
        binding.additionalInfoValue.text = value
    }

    fun setImg(res: Int) {
        binding.additionalInfoIcon.setImageResource(res)
    }
}