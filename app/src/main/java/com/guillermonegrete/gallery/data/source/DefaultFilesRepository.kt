package com.guillermonegrete.gallery.data.source

import android.util.Patterns
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import io.reactivex.Single
import javax.inject.Inject

class DefaultFilesRepository @Inject constructor(private var fileAPI: FilesServerAPI): FilesRepository {

    private var baseUrl = ""
        set(newUrl){
            field = if(Patterns.WEB_URL.matcher(newUrl).matches()) newUrl else BASE_URL
        }

    override fun updateRepoURL(newURL: String) {
        baseUrl = newURL
    }

    override fun getFolders(): Single<GetFolderResponse> {
        return fileAPI.getFolders(baseUrl)
    }

    override fun getFiles(folder: String): Single<List<String>> {
        return fileAPI.getFiles(baseUrl, folder)
    }

    companion object{
        var BASE_URL = "http://localhost/"
    }

}