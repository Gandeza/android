package mega.privacy.android.app.presentation.meeting.chat.view.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import mega.privacy.android.app.presentation.meeting.chat.model.messages.actions.MessageBottomSheetAction
import mega.privacy.android.shared.original.core.ui.controls.chat.MegaEmojiPickerView
import mega.privacy.android.shared.original.core.ui.controls.chat.messages.reaction.AddReactionsSheetItem
import mega.privacy.android.shared.original.core.ui.controls.dividers.DividerType
import mega.privacy.android.shared.original.core.ui.controls.dividers.MegaDivider
import mega.privacy.android.shared.original.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.shared.theme.MegaAppTheme

/**
 * Bottom sheet for chat message options.
 */
@Composable
fun MessageOptionsBottomSheet(
    showReactionPicker: Boolean,
    onReactionClicked: (String) -> Unit,
    onMoreReactionsClicked: () -> Unit,
    actions: List<MessageBottomSheetAction>,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = !showReactionPicker) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .testTag(TEST_TAG_MESSAGE_OPTIONS_PANEL)
        ) {
            AddReactionsSheetItem(
                onReactionClicked = {
                    onReactionClicked(it)
                },
                onMoreReactionsClicked = onMoreReactionsClicked,
                modifier = Modifier.padding(8.dp),
            )

            var group = if (actions.isNotEmpty()) actions.first().group else null
            actions.forEach {
                if (group != it.group) {
                    MegaDivider(dividerType = DividerType.BigStartPadding)
                    group = it.group
                }
                it.view()
            }
        }
    }
    AnimatedVisibility(visible = showReactionPicker) {
        MegaEmojiPickerView(
            onEmojiPicked = {
                //Add reaction
                onReactionClicked(it.emoji)
            },
            showEmojiPicker = showReactionPicker,
        )
    }
}

@CombinedThemePreviews
@Composable
private fun MessageOptionsBottomSheetPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        MessageOptionsBottomSheet(
            showReactionPicker = false,
            onReactionClicked = {},
            onMoreReactionsClicked = {},
            actions = listOf(),
        )
    }
}

internal const val TEST_TAG_MESSAGE_OPTIONS_PANEL = "chat_view:message_options_panel"