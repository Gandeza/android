package mega.privacy.android.app.presentation.contact

import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import mega.privacy.android.app.namecollision.data.LegacyNameCollision
import mega.privacy.android.app.presentation.transfers.starttransfer.model.TransferTriggerEvent
import mega.privacy.android.domain.entity.node.MoveRequestResult

/**
 * Contact file list ui state
 *
 * @property moveRequestResult
 * @property nodeNameCollisionResult
 * @property messageText
 * @property copyMoveAlertTextId
 * @property snackBarMessage
 * @property uploadEvent Event to trigger upload actions
 */
data class ContactFileListUiState(
    val moveRequestResult: Result<MoveRequestResult>? = null,
    val nodeNameCollisionResult: List<LegacyNameCollision> = emptyList(),
    val messageText: String? = null,
    val copyMoveAlertTextId: Int? = null,
    val snackBarMessage: Int? = null,
    val uploadEvent: StateEventWithContent<TransferTriggerEvent.StartUpload> = consumed(),
)