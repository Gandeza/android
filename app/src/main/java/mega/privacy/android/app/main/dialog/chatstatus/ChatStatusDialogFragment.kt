package mega.privacy.android.app.main.dialog.chatstatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.R
import mega.privacy.android.app.arch.extensions.collectFlow
import mega.privacy.android.app.presentation.extensions.isDarkMode
import mega.privacy.android.core.ui.controls.dialogs.ConfirmationWithRadioButtonsDialog
import mega.privacy.android.core.ui.theme.AndroidTheme
import mega.privacy.android.domain.entity.ThemeMode
import mega.privacy.android.domain.entity.contacts.UserStatus
import mega.privacy.android.domain.usecase.GetThemeMode
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
internal class ChatStatusDialogFragment : DialogFragment() {
    @Inject
    lateinit var getThemeMode: GetThemeMode

    @Inject
    lateinit var userStatusToStringMapper: UserStatusToStringMapper

    private val viewModel by viewModels<ChatStatusViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Timber.d("showChatStatusDialog")
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val themeMode by getThemeMode()
                    .collectAsStateWithLifecycle(initialValue = ThemeMode.System)
                val uiState by viewModel.state.collectAsStateWithLifecycle()
                AndroidTheme(isDark = themeMode.isDarkMode()) {
                    ConfirmationWithRadioButtonsDialog(
                        radioOptions = listOf(
                            UserStatus.Online,
                            UserStatus.Away,
                            UserStatus.Busy,
                            UserStatus.Offline,
                        ),
                        initialSelectedOption = uiState.status,
                        titleText = stringResource(id = R.string.status_label),
                        onOptionSelected = viewModel::setUserStatus,
                        onDismissRequest = {
                            dismissAllowingStateLoss()
                        },
                        optionDescriptionMapper = { userStatusToStringMapper(it) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.collectFlow(viewModel.state) {
            if (it.shouldDismiss) {
                dismissAllowingStateLoss()
            }
        }
    }

    companion object {
        const val TAG = "ChatStatusDialogFragment"
    }
}