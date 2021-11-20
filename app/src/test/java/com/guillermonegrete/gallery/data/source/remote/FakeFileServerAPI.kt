package com.guillermonegrete.gallery.data.source.remote

import androidx.annotation.VisibleForTesting
import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.PagedFileResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Path

class FakeFileServerAPI(folderServiceData: LinkedHashMap<Folder, List<FileResponse>>): FilesServerAPI {

    var folders = folderServiceData.keys.toMutableList()
    var filesMap = folderServiceData.mapKeys { it.key.name } as LinkedHashMap<String, List<FileResponse>>


    override fun getFolders(
        @Path(encoded = false, value = "baseUrl") baseUrl: String
    ): Single<GetFolderResponse> = Single.just(GetFolderResponse("Name", folders.toList()))

    override fun getFiles(
        @Path(encoded = true, value = "baseUrl") baseUrl: String,
        @Path(encoded = false, value = "path") path: String
    ): Single<List<FileResponse>> = Single.just(filesMap[path])

    override fun getPagedFiles(
        baseUrl: String,
        path: String,
        page: Int,
        sort: String?,
        size: Int
    ): Single<PagedFileResponse> {
        TODO("Not yet implemented")
    }

    override fun getPagedFiles(
        baseUrl: String,
        page: Int,
        sort: String?,
        size: Int
    ): Single<PagedFileResponse> {
        TODO("Not yet implemented")
    }

    @VisibleForTesting
    fun addFolder(folder: Folder, files: List<FileResponse>){
        folders.add(folder)
        filesMap[folder.name] = files
    }
}