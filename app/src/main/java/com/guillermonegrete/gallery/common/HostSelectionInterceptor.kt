package com.guillermonegrete.gallery.common

import com.guillermonegrete.gallery.data.source.SettingsRepository
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This interceptor reads and sets the url saved in the settings.
 * Using an interceptor to dynamically change urls is the recommended approach. See: https://github.com/square/retrofit/issues/1404#issuecomment-207408548
 *
 */
class HostSelectionInterceptor(private val settings: SettingsRepository): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val urlString = settings.getServerURL()
        val parsedUrl = "http://$urlString".toHttpUrlOrNull()

        val newUrl = if(parsedUrl != null) {
            request.url.newBuilder()
                .host(parsedUrl.host)
                .port(parsedUrl.port)
                .build()
        } else {
            HttpUrl.Builder().build()
        }
        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
