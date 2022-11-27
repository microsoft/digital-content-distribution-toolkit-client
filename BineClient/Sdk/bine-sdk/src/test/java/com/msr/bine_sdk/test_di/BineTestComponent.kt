package com.msr.bine_sdk.test_di

import com.msr.bine_sdk.AuthManagerTest
import com.msr.bine_sdk.CloudManagerTest
import com.msr.bine_sdk.base.BaseTest
import com.msr.bine_sdk.di.BineComponent
import com.msr.bine_sdk.di.BineModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            (BineModule::class)
        ]
)
interface BineTestComponent : BineComponent {
    fun into(baseTest: BaseTest)
    fun into(cloudManagerTest: CloudManagerTest)
    fun into(authManagerTest: AuthManagerTest)
}