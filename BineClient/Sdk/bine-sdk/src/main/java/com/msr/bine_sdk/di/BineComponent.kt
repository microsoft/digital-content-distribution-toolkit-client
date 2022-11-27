package com.msr.bine_sdk.di

import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.download.Downloader
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [BineModule::class])
interface BineComponent {

    /*@Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): BineComponent
    }*/

    fun inject(bineAPI: BineAPI)
    fun inject(downloader: Downloader)
}