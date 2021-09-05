package com.candleflask.android.ui

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.candleflask.android.R

class ThemedTypedValues(themedContext: Context) {
  @ColorInt
  val textColorPositive = resolveFor(R.attr.dayNightTextColorPositive, themedContext)

  @ColorInt
  val textColorNegative = resolveFor(R.attr.dayNightTextColorNegative, themedContext)

  @ColorInt
  val textColorNeutral = resolveFor(android.R.attr.textColorPrimary, themedContext)

  @ColorInt
  val colorPrimary = resolveFor(R.attr.colorPrimary, themedContext)

  @ColorInt
  val colorSecondary = resolveFor(R.attr.colorSecondary, themedContext)

  @ColorInt
  val backgroundColor = resolveFor(R.attr.colorOnPrimary, themedContext)

  private fun resolveFor(@AttrRes id: Int, context: Context) = with(TypedValue()) {
    require(context.theme.resolveAttribute(id, this, true)) {
      "Either attribute can't be found, or this attribute is invalid for a ${TypedValue::class.java.simpleName} "
    }
    return@with data
  }

}