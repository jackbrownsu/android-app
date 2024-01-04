/*
 * Copyright (c) 2023. Proton AG
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

package com.protonvpn.android.redesign.recents.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Transition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.protonvpn.android.R
import com.protonvpn.android.base.ui.VpnSolidButton
import com.protonvpn.android.base.ui.VpnWeakSolidButton
import com.protonvpn.android.base.ui.theme.LightAndDarkPreview
import com.protonvpn.android.redesign.base.ui.FlagOrGatewayIndicator
import com.protonvpn.android.redesign.vpn.ui.ChangeServerViewState
import com.protonvpn.android.redesign.vpn.ui.ConnectIntentLabels
import com.protonvpn.android.redesign.vpn.ui.ConnectIntentPrimaryLabel
import com.protonvpn.android.redesign.vpn.ui.ConnectIntentSecondaryLabel
import com.protonvpn.android.redesign.vpn.ui.ConnectIntentViewState
import com.protonvpn.android.ui.home.vpn.ChangeServerButton
import me.proton.core.compose.component.VerticalSpacer
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.captionStrongUnspecified

enum class VpnConnectionState {
    Disconnected,
    Connecting,
    Connected
}

@Immutable
data class VpnConnectionCardViewState(
    @StringRes val cardLabelRes: Int,
    val connectIntentViewState: ConnectIntentViewState,
    val connectionState: VpnConnectionState,
)

@Suppress("LongParameterList")
@Composable
fun VpnConnectionCard(
    viewState: VpnConnectionCardViewState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenPanelClick: () -> Unit,
    modifier: Modifier = Modifier,
    changeServerButton: (@Composable ColumnScope.() -> Unit)? = null,
    itemIdsTransition: Transition<ItemIds>? = null
) {
    Column(
        modifier = modifier
            .animateContentSize()
    ) {
        ContainerLabelRow(
            viewState.cardLabelRes,
            Modifier.padding(vertical = 12.dp) // There's 4.dp padding on the help button.
        )
        val surfaceShape = ProtonTheme.shapes.large
        Surface(
            color = ProtonTheme.colors.backgroundSecondary,
            shape = surfaceShape,
            modifier = Modifier.border(1.dp, ProtonTheme.colors.separatorNorm, surfaceShape)
        ) {
            // The whole card can be clicked to open the panel but for accessibility this action is placed on the
            // chevron icon.
            val canOpenPanel = viewState.connectionState == VpnConnectionState.Connected
            val panelModifier = if (canOpenPanel) {
                Modifier.clickable(onClick = onOpenPanelClick)
            } else {
                Modifier
            }
            Column(
                modifier = panelModifier.padding(16.dp),
            ) {
                AnimatedContent(
                    targetState = viewState.connectIntentViewState,
                    transitionSpec = { getTransitionSpec(itemIdsTransition) },
                    label = "connect intent"
                ) { targetState ->
                    Row(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .heightIn(min = 42.dp)
                            .semantics(mergeDescendants = true) {},
                    ) {
                        with(targetState) {
                            FlagOrGatewayIndicator(primaryLabel)
                            ConnectIntentLabels(
                                primaryLabel,
                                secondaryLabel,
                                serverFeatures,
                                isConnected = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            )
                        }
                        if (canOpenPanel) {
                            OpenPanelButton(onOpenPanelClick, Modifier.align(Alignment.Top))
                        }
                    }
                }
                when (viewState.connectionState) {
                    VpnConnectionState.Disconnected ->
                        VpnSolidButton(text = stringResource(R.string.buttonConnect), onClick = onConnect)
                    VpnConnectionState.Connecting ->
                        VpnWeakSolidButton(text = stringResource(R.string.cancel), onClick = onDisconnect)
                    VpnConnectionState.Connected ->
                        VpnWeakSolidButton(text = stringResource(R.string.disconnect), onClick = onDisconnect)
                }
                if (changeServerButton != null) {
                    Spacer(Modifier.height(8.dp))
                    changeServerButton()
                }
            }
        }
    }
}

@Composable
private fun OpenPanelButton(
    onOpenPanelClick: () -> Unit,
    modifier: Modifier
) {
    val clickLabel = stringResource(R.string.accessibility_action_open)
    Icon(
        painterResource(id = R.drawable.ic_proton_chevron_up),
        tint = ProtonTheme.colors.iconWeak,
        contentDescription =
        stringResource(R.string.connection_card_accessbility_label_details),
        modifier = modifier.semantics {
            role = Role.Button
            onClick(label = clickLabel) {
                onOpenPanelClick()
                true
            }
        }
    )
}

private fun AnimatedContentTransitionScope<ConnectIntentViewState>.getTransitionSpec(
    ids: Transition<ItemIds>?
): ContentTransform {
    if (ids == null) return EnterTransition.None togetherWith ExitTransition.None

    val isSameThingOrIdentical =
        initialState == targetState || ids.currentState.connectionCard == ids.targetState.connectionCard
    val fromRecents = ids.currentState.recents.contains(ids.targetState.connectionCard)
    val toRecents = ids.targetState.recents.contains(ids.currentState.connectionCard)
    val enter = when (!isSameThingOrIdentical && fromRecents) {
        true -> slideInVertically { height -> height } + fadeIn()
        else -> EnterTransition.None
    }
    val exit = when (!isSameThingOrIdentical && toRecents) {
        true -> slideOutVertically { height -> height } + fadeOut()
        else -> ExitTransition.None
    }
    return enter togetherWith exit
}

@Composable
private fun ContainerLabelRow(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(labelRes),
            style = ProtonTheme.typography.captionNorm,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview
@Composable
private fun VpnConnectionCardFreeUserPreview() {
    LightAndDarkPreview {
        val connectIntentState = ConnectIntentViewState(
            ConnectIntentPrimaryLabel.FastestFreeServer,
            ConnectIntentSecondaryLabel.FastestFreeServer(4),
            emptySet(),
        )
        val state = VpnConnectionCardViewState(
            R.string.connection_card_label_last_connected,
            connectIntentState,
            VpnConnectionState.Disconnected,
        )
        VpnConnectionCard(
            viewState = state,
            onConnect = {},
            onDisconnect = {},
            onOpenPanelClick = {},
            modifier = Modifier
        )
    }
}
