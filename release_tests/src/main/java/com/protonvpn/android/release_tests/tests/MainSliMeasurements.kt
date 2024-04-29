/*
 *
 *  * Copyright (c) 2023. Proton AG
 *  *
 *  * This file is part of ProtonVPN.
 *  *
 *  * ProtonVPN is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * ProtonVPN is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.protonvpn.android.release_tests.tests

import com.protonvpn.android.release_tests.BuildConfig
import com.protonvpn.android.release_tests.helpers.LokiClient
import com.protonvpn.android.release_tests.helpers.TestMonitoringHelper
import com.protonvpn.android.release_tests.robots.LoginRobot
import com.protonvpn.android.release_tests.rules.LaunchVpnAppRule
import com.protonvpn.android.release_tests.rules.SliTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import java.util.UUID

class MainSliMeasurements {

    private lateinit var loginRobot: LoginRobot

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(LaunchVpnAppRule())
        .around(SliTestRule())

    @Before
    fun setup() {
        LokiClient.id = UUID.randomUUID().toString()
        loginRobot = LoginRobot()
    }

    @Test
    fun loginSli() {
        LokiClient.sliGroup = "login"

        loginRobot.navigateToSignIn()
        loginRobot.enterCredentials("testas3", BuildConfig.TEST_ACCOUNT_PASSWORD)
        loginRobot.pressSignIn()
        TestMonitoringHelper.measureTime {
            loginRobot.waitUntilLoggedIn()
        }
        LokiClient.metrics["duration"] = "%.2f".format(TestMonitoringHelper.elapsedTimeSeconds)
    }
}