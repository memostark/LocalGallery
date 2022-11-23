package com.guillermonegrete.gallery.folders.source

import io.reactivex.rxjava3.core.Single

class FakeFoldersApi: FoldersAPI {

    override fun getFolders(
        page: Int,
        query: String?,
        sort: String?,
        size: Int
    ): Single<PagedFolderResponse> {
        TODO("Not yet implemented")
    }
}
