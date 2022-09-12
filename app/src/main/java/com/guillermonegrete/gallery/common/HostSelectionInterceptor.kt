package com.guillermonegrete.gallery.common

import com.guillermonegrete.gallery.data.source.SettingsRepository
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

        val host: String = settings.getServerURL()

        val newUrl = request.url().newBuilder()
            .host(host)
            .build()

        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
