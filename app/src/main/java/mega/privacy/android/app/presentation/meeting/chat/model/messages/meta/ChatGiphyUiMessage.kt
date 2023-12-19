package mega.privacy.android.app.presentation.meeting.chat.model.messages.meta

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mega.privacy.android.app.presentation.meeting.chat.model.messages.UiChatMessage
import mega.privacy.android.app.presentation.meeting.chat.view.ChatAvatar
import mega.privacy.android.app.presentation.meeting.chat.view.message.meta.GiphyMessageView
import mega.privacy.android.app.utils.GiphyUtil
import mega.privacy.android.domain.entity.chat.messages.meta.GiphyMessage

class ChatGiphyUiMessage(
    override val message: GiphyMessage,
    override val showDate: Boolean,
    override val showAvatar: Boolean,
    override val showTime: Boolean,
) : UiChatMessage {
    override val contentComposable: @Composable (RowScope.() -> Unit) = {
        message.giphy?.let { giphy ->
            GiphyMessageView(
                url = giphy.webpSrc?.let { GiphyUtil.getOriginalGiphySrc(it) }?.toString() ?: "",
                width = giphy.width,
                height = giphy.height,
                title = giphy.title
            )
        }
    }

    override val avatarComposable: @Composable (RowScope.() -> Unit) = {
        if (showAvatar) {
            ChatAvatar(modifier = Modifier.align(Alignment.Bottom), handle = message.userHandle)
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}