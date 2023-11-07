package mega.privacy.android.app.di.imagepreview

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import mega.privacy.android.app.presentation.imagepreview.fetcher.AlbumContentImageNodeFetcher
import mega.privacy.android.app.presentation.imagepreview.fetcher.ImageNodeFetcher
import mega.privacy.android.app.presentation.imagepreview.fetcher.MediaDiscoveryImageNodeFetcher
import mega.privacy.android.app.presentation.imagepreview.fetcher.TimelineImageNodeFetcher
import mega.privacy.android.app.presentation.imagepreview.model.ImagePreviewFetcherSource

@Module
@InstallIn(ViewModelComponent::class)
interface ImagePreviewModule {
    @Binds
    @IntoMap
    @ImageNodeFetcherSourceKey(ImagePreviewFetcherSource.TIMELINE)
    fun TimelineImageNodeFetcher.bindTimelineImageNodeFetcher(): ImageNodeFetcher

    @Binds
    @IntoMap
    @ImageNodeFetcherSourceKey(ImagePreviewFetcherSource.ALBUM_CONTENT)
    fun AlbumContentImageNodeFetcher.bindAlbumContentImageNodeFetcher(): ImageNodeFetcher

    @Binds
    @IntoMap
    @ImageNodeFetcherSourceKey(ImagePreviewFetcherSource.MEDIA_DISCOVERY)
    fun MediaDiscoveryImageNodeFetcher.bindMediaDiscoveryImageNodeFetcher(): ImageNodeFetcher

}