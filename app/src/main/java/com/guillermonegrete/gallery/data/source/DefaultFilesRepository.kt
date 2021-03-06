package com.guillermonegrete.gallery.data.source

import android.util.Patterns
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.remote.FilesPageSource
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import io.reactivex.Flowable
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

    override fun getFiles(folder: String): Single<List<File>> {
        return fileAPI.getFiles(baseUrl, folder).map { list ->
            // This map should not be necessary later because a Moshi adapter should handle the file object creation
            list.map {
                val type = it.url.split(".").last()
                File(it.url, type, it.width, it.height)
            }
        }
    }

    override fun getPagedFiles(folder: String): Flowable<PagingData<File>> {
        return Pager(PagingConfig(pageSize = 20)) {
            FilesPageSource(fileAPI, baseUrl, folder)
        }.flowable
    }

    companion object{
        var BASE_URL = "http://localhost/"
    }

}