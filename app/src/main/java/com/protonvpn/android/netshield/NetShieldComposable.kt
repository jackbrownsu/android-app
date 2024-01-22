/*
 * Copyright (c) 2023. Proton Technologies AG
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
package com.protonvpn.android.netshield

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.protonvpn.android.R
import com.protonvpn.android.utils.ConnectionTools
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.defaultSmallStrongNorm
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.overlineWeak
import me.proton.core.presentation.R as CoreR

@Composable
fun NetShieldComposable(
    netShieldViewState: NetShieldViewState,
    navigateToUpgrade: () -> Unit,
    onNavigateToSubsetting: () -> Unit
) {
    when (netShieldViewState) {
        is NetShieldViewState.NetShieldState -> NetShieldView(
            state = netShieldViewState,
            onNavigateToSubsetting = onNavigateToSubsetting
        )
        NetShieldViewState.UpgradePlusBanner -> UpgradeNetShieldFree(navigateToUpgrade)
        NetShieldViewState.UpgradeBusinessBanner -> UpgradeNetShieldBusiness()
    }
}

@Preview
@Composable
private fun UpgradeNetShieldFree(
    navigateToUpgrade: () -> Unit = {}
) = UpgradePromo(
    R.string.netshield_free_title,
    R.string.netshield_free_description,
    navigateToUpgrade = navigateToUpgrade
)

@Preview
@Composable
private fun UpgradeNetShieldBusiness() =
    UpgradePromo(
        titleRes = R.string.netshield_business_title,
        descriptionRes = R.string.netshield_business_description,
        badgeIconRes = R.drawable.vpn_business_badge,
        navigateToUpgrade = null
    )

@Composable
fun UpgradePromo(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @DrawableRes iconRes: Int = R.drawable.ic_netshield_free,
    @DrawableRes badgeIconRes: Int = ResourcesCompat.ID_NULL,
    navigateToUpgrade: (() -> Unit)?
) {
    val rowClickModifier = if (navigateToUpgrade != null) {
        Modifier
            .clickable(onClick = navigateToUpgrade)
            .semantics(mergeDescendants = true, properties = {})
    } else {
        Modifier
    }
    Row(
        modifier = rowClickModifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 4.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(titleRes),
                style = ProtonTheme.typography.default,
            )
            Text(
                text = stringResource(descriptionRes),
                style = ProtonTheme.typography.defaultWeak,
            )
        }

        if (badgeIconRes != ResourcesCompat.ID_NULL) {
            Image(
                painter = painterResource(id = badgeIconRes),
                contentDescription = null
            )
        }
        if (navigateToUpgrade != null) {
            Icon(
                painter = painterResource(id = CoreR.drawable.ic_proton_chevron_right),
                tint = ProtonTheme.colors.iconHint,
                contentDescription = null,
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}

@Composable
private fun NetShieldView(state: NetShieldViewState.NetShieldState, onNavigateToSubsetting: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onNavigateToSubsetting)
    ) {
        Row(
            modifier = Modifier
                .semantics(mergeDescendants = true, properties = {})
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = state.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = stringResource(R.string.netshield_feature_name),
                style = ProtonTheme.typography.captionNorm,
                modifier = Modifier.weight(1f)
                )
            Text(
                text = stringResource(state.titleRes),
                style = ProtonTheme.typography.captionWeak,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Icon(
                painter = painterResource(id = CoreR.drawable.ic_proton_chevron_right),
                contentDescription = null,
                tint = ProtonTheme.colors.iconHint,
                modifier = Modifier.size(24.dp)
            )
        }
        AnimatedVisibility(state.bandwidthShown) {
            BandwidthStatsRow(
                stats = state.netShieldStats,
                isGreyedOut = state.isGreyedOut
            )
        }
    }
}

@Composable
fun BandwidthStatsRow(isGreyedOut: Boolean, stats: NetShieldStats) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .semantics(mergeDescendants = true, properties = {})
    ) {
        val adsCount = stats.adsBlocked
        val trackerCount = stats.trackersBlocked
        val dataSaved = stats.savedBytes
        val modifier = Modifier
            .weight(1f)
            .padding(2.dp)
        BandwidthColumn(
            isDisabledStyle = isGreyedOut || adsCount == 0L,
            title = pluralStringResource(id = R.plurals.netshield_ads_blocked, count = adsCount.toInt()),
            content = if (adsCount == 0L) "-" else adsCount.toString(),
            modifier = modifier.testTag("adsBlocked")
        )
        BandwidthColumn(
            isDisabledStyle = isGreyedOut || trackerCount == 0L,
            title = pluralStringResource(id = R.plurals.netshield_trackers_stopped, count = trackerCount.toInt()),
            content = if (trackerCount == 0L) "-" else trackerCount.toString(),
            modifier = modifier.testTag("trackersStopped")
        )
        BandwidthColumn(
            isDisabledStyle = isGreyedOut || dataSaved == 0L,
            title = stringResource(id = R.string.netshield_data_saved),
            content = if (dataSaved == 0L) "-" else ConnectionTools.bytesToSize(dataSaved),
            modifier = modifier.testTag("bandwidthSaved")
        )
    }
}

@Composable
private fun BandwidthColumn(
    isDisabledStyle: Boolean,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = content,
            style = if (isDisabledStyle) ProtonTheme.typography.defaultSmallWeak else ProtonTheme.typography.defaultSmallStrongNorm,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("value")
        )
        Text(
            text = title,
            style = ProtonTheme.typography.overlineWeak,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun NetShieldOnPreview() {
    NetShieldComposable(
        netShieldViewState =
            NetShieldViewState.NetShieldState(
                protocol = NetShieldProtocol.ENABLED_EXTENDED,
                netShieldStats = NetShieldStats(
                    adsBlocked = 3,
                    trackersBlocked = 0,
                    savedBytes = 2000
                )
            ),
        navigateToUpgrade = {},
        onNavigateToSubsetting = {}
    )
}

@Preview
@Composable
private fun NetShieldOffPreview() {
    NetShieldComposable(
        netShieldViewState =
            NetShieldViewState.NetShieldState(
                protocol = NetShieldProtocol.DISABLED,
                netShieldStats = NetShieldStats(
                    adsBlocked = 3,
                    trackersBlocked = 5,
                )
            ),
        navigateToUpgrade = {},
        onNavigateToSubsetting = {}
    )
}
