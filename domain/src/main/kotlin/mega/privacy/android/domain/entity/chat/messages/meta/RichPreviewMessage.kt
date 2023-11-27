package mega.privacy.android.domain.entity.chat.messages.meta

/**
 * Rich preview message
 */
data class RichPreviewMessage(
    override val msgId: Long,
    override val time: Long,
    override val isMine: Boolean,
) : MetaMessage