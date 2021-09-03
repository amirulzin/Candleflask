package com.candleflask.android.ui

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.candleflask.android.R

class ThemedTypedValues(themedContext: Context) {
  val textColorPositive = resolveFor(R.attr.dayNightTextColorPositive, themedContext)
  val textColorNegative = resolveFor(R.attr.dayNightTextColorNegative, themedContext)
  val textColorNeutral = resolveFor(android.R.attr.textColorPrimary, themedContext)

  private fun resolveFor(@AttrRes id: Int, context: Context) = with(TypedValue()) {
    require(context.theme.resolveAttribute(id, this, true)) {
      "Either attribute can't be found, or this attribute is invalid for a ${TypedValue::class.java.simpleName} "
    }
    return@with data
  }

}