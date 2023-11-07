package mega.privacy.android.app.di.imagepreview

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import mega.privacy.android.app.presentation.imagepreview.menu.AlbumContentImagePreviewMenuOptions
import mega.privacy.android.app.presentation.imagepreview.menu.ImagePreviewMenuOptions
import mega.privacy.android.app.presentation.imagepreview.menu.MediaDiscoveryImagePreviewMenuOptions
import mega.privacy.android.app.presentation.imagepreview.menu.TimelineImagePreviewMenuOptions
import mega.privacy.android.app.presentation.imagepreview.model.ImagePreviewMenuSource

@Module
@InstallIn(ViewModelComponent::class)
interface ImagePreviewMenuModule {

    @Binds
    @IntoMap
    @ImagePreviewMenuFeatureKey(ImagePreviewMenuSource.TIMELINE)
    fun TimelineImagePreviewMenuOptions.bindTimelineImagePreviewMenuOptions(): ImagePreviewMenuOptions

    @Binds
    @IntoMap
    @ImagePreviewMenuFeatureKey(ImagePreviewMenuSource.ALBUM_CONTENT)
    fun AlbumContentImagePreviewMenuOptions.bindAlbumContentImagePreviewMenuOptions(): ImagePreviewMenuOptions

    @Binds
    @IntoMap
    @ImagePreviewMenuFeatureKey(ImagePreviewMenuSource.MEDIA_DISCOVERY)
    fun MediaDiscoveryImagePreviewMenuOptions.bindMediaDiscoveryMenuOptions(): ImagePreviewMenuOptions
}
