package mega.privacy.android.app.presentation.node.model.toolbarmenuitems

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mega.privacy.android.app.presentation.mapper.RestoreNodeResultMapper
import mega.privacy.android.app.presentation.node.model.menuaction.RestoreMenuAction
import mega.privacy.android.app.presentation.snackbar.SnackBarHandler
import mega.privacy.android.core.ui.model.MenuAction
import mega.privacy.android.core.ui.model.MenuActionWithIcon
import mega.privacy.android.domain.entity.node.NodeNameCollisionType
import mega.privacy.android.domain.entity.node.TypedNode
import mega.privacy.android.domain.qualifier.ApplicationScope
import mega.privacy.android.domain.usecase.node.CheckNodesNameCollisionUseCase
import mega.privacy.android.domain.usecase.node.RestoreNodesUseCase
import timber.log.Timber
import javax.inject.Inject

/**
 * Restore menu item
 *
 * @property menuAction [RestoreMenuAction]
 * @property scope [CoroutineScope]
 * @property checkNodesNameCollisionUseCase [CheckNodesNameCollisionUseCase]
 * @property restoreNodesUseCase [RestoreNodesUseCase]
 * @property restoreNodeResultMapper [RestoreNodeResultMapper]
 * @property snackBarHandler [SnackBarHandler]
 */
class RestoreToolbarMenuItem @Inject constructor(
    override val menuAction: RestoreMenuAction,
    @ApplicationScope private val scope: CoroutineScope,
    private val checkNodesNameCollisionUseCase: CheckNodesNameCollisionUseCase,
    private val restoreNodesUseCase: RestoreNodesUseCase,
    private val restoreNodeResultMapper: RestoreNodeResultMapper,
    private val snackBarHandler: SnackBarHandler,
) : NodeToolbarMenuItem<MenuActionWithIcon> {


    override fun shouldDisplay(
        hasNodeAccessPermission: Boolean,
        selectedNodes: List<TypedNode>,
        canBeMovedToTarget: Boolean,
        noNodeInBackups: Boolean,
        noNodeTakenDown: Boolean,
        allFileNodes: Boolean,
        resultCount: Int,
    ) = selectedNodes.isNotEmpty() && noNodeTakenDown

    override fun getOnClick(
        selectedNodes: List<TypedNode>,
        onDismiss: () -> Unit,
        actionHandler: (menuAction: MenuAction, nodes: List<TypedNode>) -> Unit,
        navController: NavHostController,
    ): () -> Unit = {
        val restoreMap = mutableMapOf<Long, Long>()
        selectedNodes.forEach { node ->
            node.restoreId?.let {
                restoreMap[node.id.longValue] = it.longValue
            }
        }
        scope.launch {
            runCatching {
                checkNodesNameCollisionUseCase(restoreMap, NodeNameCollisionType.RESTORE)
            }.onSuccess { result ->
                if (result.conflictNodes.isNotEmpty()) {
                    actionHandler(menuAction, selectedNodes)
                }
                if (result.noConflictNodes.isNotEmpty()) {
                    val restoreResult = restoreNodesUseCase(result.noConflictNodes)
                    val message = restoreNodeResultMapper(restoreResult)
                    snackBarHandler.postSnackbarMessage(message)
                }
            }.onFailure {
                Timber.e(it)
            }
        }
        onDismiss()
    }
}