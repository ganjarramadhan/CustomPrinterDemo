package com.tabsquare.printer.demo

import android.app.Application
import com.tabqsquare.log.TabsquareLog
import com.tabqsquare.log.TabsquareLogImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

class PrinterDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // declare Android context
            androidContext(this@PrinterDemoApplication)
            modules(
                module {
                    single {
                        TabsquareLogImpl(this@PrinterDemoApplication, get(), get())
                    } bind TabsquareLog::class
                }
            )
        }
    }
}