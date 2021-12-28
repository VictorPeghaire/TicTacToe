package com.lisny.tictactoe

import android.view.View

fun View.visibleOrInvisible(value: Boolean) =
    (if (value) View.VISIBLE else View.INVISIBLE).also { visibility = it }