package com.guillermonegrete.gallery.data.source.remote

import androidx.annotation.VisibleForTesting
import com.guillermonegrete.gallery.data.Folder
import io.reactivex.Single

class FakeFileServerAPI(folderServiceData: LinkedHashMap<Folder, List<String>>): FilesServerAPI {

    var folders = folderServiceData.keys.toMutableList()
    var filesMap = folderServiceData.mapKeys { it.key.name } as LinkedHashMap<String, List<String>>


    override fun getFolders(): Single<List<Folder>> = Single.just(folders.toList())

    override fun getFiles(path: String): Single<List<String>> = Single.just(filesMap[path])

    @VisibleForTesting
    fun addFolder(folder: Folder, files: List<String>){
        folders.add(folder)
        filesMap[folder.name] = files
    }
}