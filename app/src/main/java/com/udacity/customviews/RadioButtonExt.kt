package com.udacity.customviews

import android.content.Context
import android.util.AttributeSet
import com.udacity.R

/**
 * Define a custom RadioButton that extends the classic RadioButton functionality by
 * holding a "string item value"
 *
 * This allows to map directly two string values to the existing radio button
 * allowing for a very basic key/value relationship
 *
 * This reason this custom control was made, was to demonstrate another wy to create custom controls
 * by extending from an existing control
 *
 * Source:
 * https://developer.android.com/training/custom-views/create-view
 */
class RadioButtonExt(context: Context, attrs: AttributeSet): androidx.appcompat.widget.AppCompatRadioButton(context, attrs) {
    val sourceUrl: String
    val targetFilename: String

    init {
        context.theme.obtainStyledAttributes(
            attrs,
                R.styleable.RadioButtonExt, 0, 0).apply {
            try {
                sourceUrl = getString(R.styleable.RadioButtonExt_sourceUrl) ?: ""
                targetFilename = getString(R.styleable.RadioButtonExt_targetFilename) ?: ""
            } finally {
                recycle()
            }
        }
    }
}