package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.Folder
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

    override fun updateFolderCover(id: Long, fileId: Long): Single<Folder> {
        TODO("Not yet implemented")
    }
}
