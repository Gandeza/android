package mega.privacy.android.app.presentation.search.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import mega.privacy.android.app.presentation.node.NodeBottomSheetActionHandler
import mega.privacy.android.app.presentation.node.NodeOptionsBottomSheetViewModel
import mega.privacy.android.app.presentation.node.view.NodeOptionsBottomSheetContent

internal const val nodeBottomSheetRoute = "search/node_bottom_sheet"
internal const val nodeBottomSheetRouteNodeIdArg = "node_id"

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.nodeBottomSheetNavigation(
    nodeBottomSheetActionHandler: NodeBottomSheetActionHandler,
    navHostController: NavHostController,
    nodeOptionsBottomSheetViewModel: NodeOptionsBottomSheetViewModel,
) {
    bottomSheet(
        route = nodeBottomSheetRoute.plus("/{${nodeBottomSheetRouteNodeIdArg}}"),
        arguments = listOf(
            navArgument(nodeBottomSheetRouteNodeIdArg) {
                type = NavType.LongType
            },
        ),
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        it.arguments?.getLong(nodeBottomSheetRouteNodeIdArg)?.let { nodeId ->
            LaunchedEffect(Unit) {
                keyboardController?.hide()
                nodeOptionsBottomSheetViewModel.getBottomSheetOptions(nodeId)
            }
            NodeOptionsBottomSheetContent(
                handler = nodeBottomSheetActionHandler,
                navHostController = navHostController,
                viewModel = nodeOptionsBottomSheetViewModel,
                onDismiss = {
                    navHostController.navigateUp()
                },
            )
        }
    }
}