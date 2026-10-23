package com.byga.flowmanager

import android.app.Application
import androidx.room.Room
import com.byga.flowmanager.data.PocDatabase
import com.byga.flowmanager.data.PocPreferences
import com.byga.flowmanager.data.PocRepository
import com.byga.flowmanager.launcher.FlowLauncher
import com.byga.flowmanager.security.MetadataCipher

class BygaApplication : Application() {
    val repository by lazy {
        PocRepository(Room.databaseBuilder(this, PocDatabase::class.java, "byga-poc.db").build(), MetadataCipher())
    }
    val preferences by lazy { PocPreferences(this) }
    val launcher by lazy { FlowLauncher(this) }
}
