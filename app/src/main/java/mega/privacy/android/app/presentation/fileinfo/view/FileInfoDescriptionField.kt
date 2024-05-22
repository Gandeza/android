package mega.privacy.android.app.presentation.fileinfo.view

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.shared.original.core.ui.controls.text.MegaText
import mega.privacy.android.shared.original.core.ui.controls.textfields.GenericDescriptionTextField
import mega.privacy.android.shared.original.core.ui.preview.CombinedTextAndThemePreviews
import mega.privacy.android.shared.original.core.ui.theme.tokens.TextColor
import mega.privacy.android.shared.resources.R
import mega.privacy.android.shared.theme.MegaAppTheme

/**
 * TextField Generic Description
 *
 * @param descriptionText       Description text value
 * @param labelId               Label string resource Id
 * @param placeholderId         Placeholder string resource Id
 * @param descriptionLimit      Description text character limit
 * @param onConfirmDescription  Description is confirmed by keyboard interaction
 */
@Composable
fun FileInfoDescriptionField(
    modifier: Modifier = Modifier,
    descriptionText: String,
    @StringRes labelId: Int? = null,
    @StringRes placeholderId: Int? = null,
    descriptionLimit: Int = Constants.MAX_DESCRIPTION_SIZE,
    isEditable: Boolean = true,
    onConfirmDescription: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(descriptionText) }
    var descriptionCount by rememberSaveable { mutableIntStateOf(description.length) }

    Column(modifier = modifier) {
        labelId?.let {
            MegaText(
                text = stringResource(id = labelId),
                textColor = if (!isFocused) TextColor.Secondary else TextColor.Accent,
                style = MaterialTheme.typography.caption,
            )
        }

        GenericDescriptionTextField(
            modifier = Modifier
                .onFocusChanged {
                    if (isFocused != it.isFocused) {
                        isFocused = it.isFocused
                    }
                },
            value = description,
            charLimit = descriptionLimit,
            initiallyFocused = false,
            maxLines = 5,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    onConfirmDescription(description)
                    focusManager.clearFocus()
                }
            ),
            placeholderId = placeholderId,
            onValueChange = {
                description = it.take(descriptionLimit)
                descriptionCount = description.length
            },
        )

        if (isFocused) {
            MegaText(
                modifier = Modifier.align(Alignment.End),
                text = "$descriptionCount/$descriptionLimit",
                textColor = TextColor.Primary,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@CombinedTextAndThemePreviews
@Composable
private fun FileInfoDescriptionFieldPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        FileInfoDescriptionField(
            descriptionText = "This is a description",
            labelId = R.string.file_info_information_description_label,
            placeholderId = R.string.file_info_information_description_placeholder,
        )
    }
}
