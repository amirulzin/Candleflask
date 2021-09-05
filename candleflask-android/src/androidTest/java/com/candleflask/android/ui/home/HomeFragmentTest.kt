package com.candleflask.android.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.candleflask.android.R
import com.candleflask.android.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentTest {


  @get:Rule
  var activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun whenClickSearchFab_launchSearchFragment() {
    onView(withId(R.id.fabAddTicker))
      .perform(click())

    onView(withId(R.id.searchTickersFragmentRoot))
      .check(matches(isDisplayed()))
  }

  @Test
  fun whenClickSettingFab_launchUpdateTokenDialogFragment() {
    onView(withId(R.id.fabSetting))
      .perform(click())

    onView(withId(R.id.updateTokenDialogFragmentRoot))
      .check(matches(isDisplayed()))
  }
}