package com.candleflask.framework.data.datasource.db

import android.app.Application
import androidx.room.Room
import javax.inject.Inject

class DatabaseController @Inject constructor(private val application: Application) {
  companion object {
    const val DB_NAME = "local_sqlite"
  }

  val database by lazy {
    Room.databaseBuilder(application, LocalDatabase::class.java, DB_NAME)
      .build()
  }
}