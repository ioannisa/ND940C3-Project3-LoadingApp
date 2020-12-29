package com.udacity.customviews

import com.udacity.R

sealed class ButtonState(var labelResourceId: Int) {
    object Clicked : ButtonState(R.string.button_state_download)
    object Loading : ButtonState(R.string.button_state_loading)
    object Completed : ButtonState(R.string.button_state_download)
}

