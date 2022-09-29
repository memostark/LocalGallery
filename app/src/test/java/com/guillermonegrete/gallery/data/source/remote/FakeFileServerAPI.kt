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


    override fun getFolders(): Single<GetFolderResponse> = Single.just(GetFolderResponse("Name", folders.toList()))

    override fun getFiles(
        @Path(encoded = false, value = "path") folderName: String
    ): Single<List<FileResponse>> = Single.just(filesMap[folderName])

    override fun getPagedFiles(
        folderName: String,
        page: Int,
        sort: String?,
        size: Int
    ): Single<PagedFileResponse> {
        TODO("Not yet implemented")
    }

    override fun getPagedFilesByTag(
        folderId: Long,
        tagId: Long,
        page: Int,
        sort: String?,
        size: Int
    ): Single<PagedFileResponse> {
        TODO("Not yet implemented")
    }

    override fun getAllFiles(
        page: Int,
        sort: String?,
        size: Int
    ): Single<PagedFileResponse> {
        TODO("Not yet implemented")
    }

    override fun getAllFilesByTag(
        tagId: Long,
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