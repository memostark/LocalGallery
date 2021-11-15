package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.rxjava3.core.Single

class FakeFoldersApi: FoldersAPI {

    override fun getFolders(baseUrl: String): Single<GetFolderResponse> {
        TODO("Not yet implemented")
    }

    override fun getFolders(
        baseUrl: String,
        page: Int,
        query: String?,
        sort: String?,
        size: Int
    ): Single<PagedFolderResponse> {
        TODO("Not yet implemented")
    }
}
