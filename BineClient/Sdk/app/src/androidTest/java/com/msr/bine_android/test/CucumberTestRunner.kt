package com.msr.bine_android.test

import io.cucumber.android.runner.CucumberAndroidJUnitRunner
import io.cucumber.junit.CucumberOptions

@CucumberOptions(glue = ["com.msr.bine_android.test"],  features = ["features/1_login.feature",
    "features/2_tnc.feature",
    "features/3_permissions.feature",
    "features/4_home.feature"])
class CucumberTestRunner: CucumberAndroidJUnitRunner()