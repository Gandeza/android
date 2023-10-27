package mega.privacy.android.app.presentation.node.model.menuaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mega.privacy.android.app.R
import mega.privacy.android.core.ui.model.MenuActionWithIcon
import javax.inject.Inject

/**
 * Favourite menu action
 */
class FavouriteMenuAction @Inject constructor() : MenuActionWithIcon {
    @Composable
    override fun getIconPainter() = painterResource(id = R.drawable.ic_add_favourite)

    @Composable
    override fun getDescription() = stringResource(id = R.string.file_properties_favourite)

    override val orderInCategory: Int
        get() = 80
    override val testTag: String
        get() = "menu_action:favourite"
}