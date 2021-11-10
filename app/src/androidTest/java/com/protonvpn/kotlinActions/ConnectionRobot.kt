/*
 * Copyright (c) 2021 Proton Technologies AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.protonvpn.kotlinActions

import com.protonvpn.android.R
import com.protonvpn.base.BaseRobot
import com.protonvpn.base.BaseVerify
import com.protonvpn.testsHelper.ConditionalActionsHelper
import com.protonvpn.testsHelper.ServiceTestHelper

class ConnectionRobot : BaseRobot() {

    fun disconnectFromVPN() : ConnectionRobot {
        ConditionalActionsHelper().clickOnDisconnectButtonUntilUserIsDisconnected()
        return waitUntilDisplayedByText(R.string.loaderNotConnected)
    }

    fun clickCancelConnectionButton(): ConnectionRobot = clickElementById(R.id.buttonCancel)

    fun clickCancelRetry() : ConnectionRobot = clickElementById(R.id.buttonCancelRetry)

    class Verify : BaseVerify(){

        fun isConnected(){
            ServiceTestHelper().checkIfConnectedToVPN()
            checkIfElementIsDisplayedById(R.id.buttonDisconnect)
        }

        fun isDisconnected(){
            ServiceTestHelper().checkIfDisconnectedFromVPN()
            checkIfElementIsDisplayedById(R.id.textNotConnectedSuggestion)
        }

        fun isDisconnectedServiceHelper(){
            ServiceTestHelper().checkIfDisconnectedFromVPN()
        }

        fun isNotReachableErrorDisplayed() =
            checkIfElementByIdContainsText(R.id.textError, R.string.error_server_unreachable)
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}