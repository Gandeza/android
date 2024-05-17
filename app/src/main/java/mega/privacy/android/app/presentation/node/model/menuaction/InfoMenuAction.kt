package mega.privacy.android.app.presentation.node.model.menuaction

import mega.privacy.android.icon.pack.R as iconPackR
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mega.privacy.android.app.R
import mega.privacy.android.shared.original.core.ui.model.MenuActionWithIcon
import javax.inject.Inject

/**
 * Info menu action
 */
class InfoMenuAction @Inject constructor() : MenuActionWithIcon {
    @Composable
    override fun getIconPainter() = painterResource(id = iconPackR.drawable.ic_info_medium_regular_outline)

    @Composable
    override fun getDescription() = stringResource(id = R.string.general_info)

    override val orderInCategory = 50
    override val testTag: String
        get() = "menu_action:info"
}