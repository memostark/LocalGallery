package com.guillermonegrete.gallery.common

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
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

        val url = settings.getServerURL()

        val host = if (isIpValid(url)) url else "localhost"

        val newUrl = request.url.newBuilder()
            .host(host)
            .build()
        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }

    private fun isIpValid(ip: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InetAddresses.isNumericAddress(ip)
        } else {
            Patterns.IP_ADDRESS.matcher(ip).matches()
        }
    }
}
