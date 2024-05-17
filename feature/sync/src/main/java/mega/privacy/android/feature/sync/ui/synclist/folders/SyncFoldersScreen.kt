package mega.privacy.android.feature.sync.ui.synclist.folders

import mega.privacy.android.shared.resources.R as sharedResR
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.feature.sync.R
import mega.privacy.android.feature.sync.domain.entity.SyncStatus
import mega.privacy.android.feature.sync.ui.model.SyncUiItem
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.CardExpanded
import mega.privacy.android.feature.sync.ui.views.SyncItemView
import mega.privacy.android.feature.sync.ui.views.TAG_SYNC_LIST_SCREEN_NO_ITEMS
import mega.privacy.android.shared.original.core.ui.controls.buttons.RaisedDefaultMegaButton
import mega.privacy.android.shared.original.core.ui.controls.text.MegaText
import mega.privacy.android.shared.original.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.shared.original.core.ui.theme.extensions.h6Medium
import mega.privacy.android.shared.original.core.ui.theme.tokens.TextColor
import mega.privacy.android.shared.theme.MegaAppTheme

@Composable
internal fun SyncFoldersScreen(
    syncUiItems: List<SyncUiItem>,
    cardExpanded: (CardExpanded) -> Unit,
    pauseRunClicked: (SyncUiItem) -> Unit,
    removeFolderClicked: (folderPairId: Long) -> Unit,
    addFolderClicked: () -> Unit,
    issuesInfoClicked: () -> Unit,
    isLowBatteryLevel: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = rememberLazyListState(), modifier = modifier
                .fillMaxSize()
        ) {
            if (syncUiItems.isEmpty()) {
                item {
                    SyncFoldersScreenEmptyState(
                        addFolderClicked = addFolderClicked,
                        modifier = Modifier
                            .fillParentMaxHeight(0.8f)
                            .fillParentMaxWidth()
                            .testTag(TAG_SYNC_LIST_SCREEN_NO_ITEMS)
                    )
                }
            } else {
                items(count = syncUiItems.size, key = {
                    syncUiItems[it].id
                }) { itemIndex ->
                    SyncItemView(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        syncUiItems = syncUiItems,
                        itemIndex = itemIndex,
                        cardExpanded = { syncUiItem, expanded ->
                            cardExpanded(CardExpanded(syncUiItem, expanded))
                        },
                        pauseRunClicked = pauseRunClicked,
                        removeFolderClicked = removeFolderClicked,
                        issuesInfoClicked = issuesInfoClicked,
                        isLowBatteryLevel = isLowBatteryLevel,
                        errorRes = syncUiItems[itemIndex].error
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncFoldersScreenEmptyState(
    addFolderClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(R.drawable.no_syncs_placeholder),
            contentDescription = "Sync folders empty state image",
        )
        MegaText(
            text = stringResource(id = sharedResR.string.device_center_sync_list_empty_state_title),
            textColor = TextColor.Primary,
            modifier = Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.h6Medium
        )
        MegaText(
            text = stringResource(id = sharedResR.string.device_center_sync_list_empty_state_message),
            textColor = TextColor.Secondary,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.subtitle2.copy(textAlign = TextAlign.Center),
        )
        RaisedDefaultMegaButton(
            textId = sharedResR.string.device_center_sync_add_new_syn_button_option,
            onClick = addFolderClicked,
            modifier = Modifier
                .padding(top = 162.dp)
                .defaultMinSize(minWidth = 232.dp),
        )
    }
}

@CombinedThemePreviews
@Composable
private fun SyncFoldersScreenEmptyStatePreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        SyncFoldersScreen(
            syncUiItems = emptyList(),
            cardExpanded = {},
            pauseRunClicked = {},
            removeFolderClicked = {},
            addFolderClicked = {},
            issuesInfoClicked = {},
            isLowBatteryLevel = false,
        )
    }
}

@CombinedThemePreviews
@Composable
private fun SyncFoldersScreenSyncingPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        SyncFoldersScreen(
            listOf(
                SyncUiItem(
                    id = 1,
                    folderPairName = "Folder pair name",
                    status = SyncStatus.SYNCING,
                    hasStalledIssues = false,
                    deviceStoragePath = "/path/to/local/folder",
                    megaStoragePath = "/path/to/mega/folder",
                    megaStorageNodeId = NodeId(1234L),
                    method = R.string.sync_two_way,
                    expanded = false,
                )
            ),
            cardExpanded = {},
            pauseRunClicked = {},
            removeFolderClicked = {},
            addFolderClicked = {},
            issuesInfoClicked = {},
            isLowBatteryLevel = false,
        )
    }
}

@CombinedThemePreviews
@Composable
private fun SyncFoldersScreenSyncingWithStalledIssuesPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        SyncFoldersScreen(
            listOf(
                SyncUiItem(
                    id = 1,
                    folderPairName = "Folder pair name",
                    status = SyncStatus.SYNCING,
                    hasStalledIssues = true,
                    deviceStoragePath = "/path/to/local/folder",
                    megaStoragePath = "/path/to/mega/folder",
                    megaStorageNodeId = NodeId(1234L),
                    method = R.string.sync_two_way,
                    expanded = false
                )
            ),
            cardExpanded = {},
            pauseRunClicked = {},
            removeFolderClicked = {},
            addFolderClicked = {},
            issuesInfoClicked = {},
            isLowBatteryLevel = false,
        )
    }
}
