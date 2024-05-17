package mega.privacy.android.app.presentation.node.model.menuaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import mega.privacy.android.app.R
import mega.privacy.android.shared.original.core.ui.model.MenuAction
import javax.inject.Inject

/**
 * Remove link menu action
 *
 * @property orderInCategory
 */
class RemoveLinkDropdownMenuAction @Inject constructor() : MenuAction {

    @Composable
    override fun getDescription() = stringResource(id = R.string.context_remove_link_menu)

    override val testTag: String = "menu_action:remove_link"

    override val orderInCategory: Int
        get() = 170
}