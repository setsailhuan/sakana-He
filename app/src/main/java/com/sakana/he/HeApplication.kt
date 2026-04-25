package com.sakana.he

import android.app.Application
import com.sakana.he.data.UserPreferences
import com.sakana.he.data.WaterDatabase
import com.sakana.he.data.WaterRepository

class HeApplication : Application() {
    val waterDatabase by lazy { WaterDatabase(this) }
    val repository by lazy { WaterRepository(waterDatabase) }
    val userPreferences by lazy { UserPreferences(this) }
}
