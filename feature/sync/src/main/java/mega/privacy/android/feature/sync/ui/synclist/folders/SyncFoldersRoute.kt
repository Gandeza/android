package mega.privacy.android.feature.sync.ui.synclist.folders

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.OnSyncsPausedErrorDialogDismissed
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.PauseRunClicked
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.RemoveFolderClicked
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.OnRemoveFolderDialogDismissed
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.OnRemoveFolderDialogConfirmed
import mega.privacy.android.feature.sync.ui.synclist.folders.SyncFoldersAction.SnackBarShown
import mega.privacy.android.shared.original.core.ui.controls.dialogs.ConfirmationDialog
import mega.privacy.android.shared.original.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.shared.original.core.ui.theme.OriginalTempTheme
import mega.privacy.android.shared.original.core.ui.utils.showAutoDurationSnackbar
import mega.privacy.android.shared.resources.R as sharedResR
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun SyncFoldersRoute(
    addFolderClicked: () -> Unit,
    upgradeAccountClicked: () -> Unit,
    issuesInfoClicked: () -> Unit,
    viewModel: SyncFoldersViewModel,
    state: SyncFoldersState,
    snackBarHostState: SnackbarHostState,
) {
    SyncFoldersScreen(
        syncUiItems = state.syncUiItems,
        cardExpanded = viewModel::handleAction,
        pauseRunClicked = {
            viewModel.handleAction(PauseRunClicked(it))
        },
        removeFolderClicked = {
            viewModel.handleAction(RemoveFolderClicked(it))
        },
        addFolderClicked = addFolderClicked,
        upgradeAccountClicked = upgradeAccountClicked,
        issuesInfoClicked = issuesInfoClicked,
        isLowBatteryLevel = state.isLowBatteryLevel,
        isFreeAccount = state.isFreeAccount,
        isLoading = state.isLoading,
        showSyncsPausedErrorDialog = state.showSyncsPausedErrorDialog,
        onShowSyncsPausedErrorDialogDismissed = {
            viewModel.handleAction(OnSyncsPausedErrorDialogDismissed)
        },
    )

    state.syncUiItemToRemove?.let {
        if (state.showConfirmRemoveSyncFolderDialog) {
            RemoveSyncFolderConfirmDialog(
                onConfirm = {
                    viewModel.handleAction(OnRemoveFolderDialogConfirmed)
                },
                onDismiss = {
                    viewModel.handleAction(OnRemoveFolderDialogDismissed)
                },
            )
        }
    }

    val message = state.snackbarMessage?.let { stringResource(id = it) }
    LaunchedEffect(key1 = state.snackbarMessage) {
        message?.let {
            snackBarHostState.showAutoDurationSnackbar(it)
            viewModel.handleAction(SnackBarShown)
        }
    }
}

@Composable
internal fun RemoveSyncFolderConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(id = sharedResR.string.sync_stop_sync_confirm_dialog_title),
        text = stringResource(id = sharedResR.string.sync_stop_sync_confirm_dialog_message),
        confirmButtonText = stringResource(id = sharedResR.string.sync_stop_sync_button),
        cancelButtonText = stringResource(id = sharedResR.string.general_dialog_cancel_button),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = Modifier.testTag(REMOVE_SYNC_FOLDER_CONFIRM_DIALOG_TEST_TAG),
    )
}

@CombinedThemePreviews
@Composable
private fun RemoveSyncFolderConfirmDialogPreview() {
    OriginalTempTheme(isDark = isSystemInDarkTheme()) {
        RemoveSyncFolderConfirmDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

internal const val REMOVE_SYNC_FOLDER_CONFIRM_DIALOG_TEST_TAG =
    "sync:remove_sync_folder:confirm_dialog"
