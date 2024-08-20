package mega.privacy.android.app.presentation.documentscanner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import mega.privacy.android.app.presentation.documentscanner.navigation.SaveScannedDocumentsNavHostController
import mega.privacy.android.shared.original.core.ui.controls.layouts.MegaScaffold

/**
 * A Composable holding all Save Scanned Documents screens using the Navigation Controller
 */
@Composable
internal fun SaveScannedDocumentsScreen() {
    val navHostController = rememberNavController()

    MegaScaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = rememberScaffoldState(),
    ) { padding ->
        SaveScannedDocumentsNavHostController(
            modifier = Modifier.padding(padding),
            navHostController = navHostController,
        )
    }
}