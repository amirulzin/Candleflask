package com.candleflask.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.candleflask.android.R
import com.candleflask.android.databinding.MainActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var binding: MainActivityBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = MainActivityBinding.inflate(layoutInflater)
    binding.navHostContainerView
    setContentView(binding.root)
  }


  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.navHostContainerView)
    return navController.navigateUp() || super.onSupportNavigateUp()
  }
}