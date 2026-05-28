package com.teamconfused.planmyplate

import android.app.Application
import com.teamconfused.planmyplate.di.appModule
import com.teamconfused.planmyplate.di.repositoryModule
import com.teamconfused.planmyplate.di.useCaseModule
import com.teamconfused.planmyplate.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PlanMyPlateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@PlanMyPlateApplication)
            modules(appModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
