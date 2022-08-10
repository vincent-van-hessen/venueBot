package venuebot


import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpHelper {

    fun getPage(url: String, params: List<String> = emptyList(), cookies: String? = null, rawData: String? = null, timeout: Int = 30000, referer:String? = null): String {
        println("Getting -> $url")
        val returnString: String
        val buildString = StringBuilder()
        var connection: URLConnection
        var responseCookies: String = ""
        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, null)
            connection = URL(url).openConnection()
            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = sc.socketFactory
            }
            if (connection is HttpURLConnection) {
                connection.instanceFollowRedirects = false
                HttpURLConnection.setFollowRedirects(false)
            }
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0")
            connection.setRequestProperty("Accept-Charset", charset)
            connection.setRequestProperty("Referer", referer ?: "https://www.dresden.de/apps_ext/StrassenmusikApp_en/login?2")
            connection.setRequestProperty("Origin", "https://www.dresden.de")
            if (rawData != null) {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            connection.setRequestProperty(
                "Accept",
                "text/html,application/xhtml+xml,application/xml,application/json;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
            )
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            if (cookies != null) {
                connection.setRequestProperty("Cookie", cookies)
            }

            if (rawData != null) {
                connection.doOutput = true
                val outputStream = connection.getOutputStream()
                val writer = outputStream.writer()
                writer.write(rawData)
                writer.flush()
                writer.close()
            }

            var redirect = false


            if (connection is HttpURLConnection) {
                val status = connection.responseCode
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                    )
                        redirect = true;
                }
                println("Response Code ... $status")
            }

            if (redirect) {

                val cookiesRetrieved = connection.headerFields["Set-Cookie"] ?: emptyList()
                val (connection, cookies) = redirectToURLFromHeaders(connection, cookiesRetrieved, sc)

                if (isRedirected(connection)) {
                    val (connection, cookies) = redirectToURLFromHeaders(connection, cookies, sc)

                    if (isRedirected(connection)) {
                        val (connection, cookies) = redirectToURLFromHeaders(connection, cookies, sc)
                        if (isRedirected(connection)) {
                            val (connection, cookies) = redirectToURLFromHeaders(connection, cookies, sc)
                            if (isRedirected(connection)) {
                                val (connection, cookies) = redirectToURLFromHeaders(connection, cookies, sc)
                            }
                        }
                    }
                    responseCookies = (connection.headerFields["Set-Cookie"] ?: cookies).joinToString(separator = ";")
                }
            }

            val response = connection.getInputStream()

//            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
//                System.out.println(header.getKey() + "=" + header.getValue());
//            }
            val contentType = connection.getHeaderField("Content-Type")
            var charset: String? = null
            if (contentType != null) {
                for (param in contentType.replace(" ", "").split(";".toRegex()).toTypedArray()) {
                    if (param.startsWith("charset=")) {
                        charset = param.split("=".toRegex(), 2).toTypedArray()[1]
                        break
                    }
                }
            }
            if (charset == null) {
                charset = "UTF-8"
            }
            BufferedReader(InputStreamReader(response, charset)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    buildString.append(line).append(System.getProperty("line.separator"))
                }
            }
        } catch (e: Exception) {
            throw (e)
        }
        returnString = buildString.toString()
        if (buildString.isBlank()) return responseCookies
        return returnString
    }

    private fun isRedirected(connection: URLConnection): Boolean {
        var isRedirected = false
        if (connection is HttpURLConnection) {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER
                )
                    isRedirected = true;
            }
            println("Response Code ... $status")
        }
        return isRedirected
    }

    private fun redirectToURLFromHeaders(
        connection: URLConnection,
        cookies: List<String>,
        sc: SSLContext
    ): Pair<URLConnection, List<String>> {
        var connection1 = connection
        var redirect = false
        // get redirect url from "location" header field
        var newUrl: String = connection1.getHeaderField("Location")
        println("Redirected to URL : $newUrl")

        // open the new connnection again
        connection1 = URL(newUrl).openConnection()
        if (connection1 is HttpsURLConnection) {
            connection1.sslSocketFactory = sc.socketFactory
        }
        connection1.setRequestProperty("Cookie", cookies.joinToString(separator = ";"))
        connection1.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
        connection1.addRequestProperty("User-Agent", "Mozilla")
        connection1.addRequestProperty("Referer", "https://www.dresden.de/apps_ext/StrassenmusikApp_en/login")

        val newCookies: List<String> = connection1.headerFields["Set-Cookie"] ?: cookies
        return connection1 to newCookies
    }

    fun getRawPage(url: String?, params: List<String?>?, cookies: String?, timeout: Int): ByteArray? {
        val connection: URLConnection
        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, null)
            connection = URL(url).openConnection()
            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = sc.socketFactory
            }
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0"
            )
            connection.setRequestProperty("Accept-Charset", charset)
            connection.setRequestProperty(
                "Accept",
                "text/html,application/xhtml+xml,application/xml,application/json;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
            )
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            if (cookies != null) {
                connection.setRequestProperty("Cookie", cookies)
            }
            val response = connection.getInputStream()

//            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
//                System.out.println(header.getKey() + "=" + header.getValue());
//            }
            val contentType = connection.getHeaderField("Content-Type")
            var charset: String? = null
            if (contentType != null) {
                for (param in contentType.replace(" ", "").split(";".toRegex()).toTypedArray()) {
                    if (param.startsWith("charset=")) {
                        charset = param.split("=".toRegex(), 2).toTypedArray()[1]
                        break
                    }
                }
            }
            if (charset == null) {
                charset = "UTF-8"
            }
            return IOUtils.toByteArray(response)
        } catch (ignored: IOException) {
        } catch (ignored: NoSuchAlgorithmException) {
        } catch (ignored: KeyManagementException) {
        }
        return null
    }

    private val charset = UTF_8.name() // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }

            override fun checkClientTrusted(
                certs: Array<X509Certificate>,
                authType: String
            ) {
            }

            override fun checkServerTrusted(
                certs: Array<X509Certificate>,
                authType: String
            ) {
            }
        }
    )

    @Throws(IOException::class)
    fun downloadFileToPath(fileURLFromTorrent: String, localPath: String) {
        val remoteUrl = URL(fileURLFromTorrent)
        val rbc = Channels.newChannel(remoteUrl.openStream())
        val fos = FileOutputStream(localPath)
        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
    }
}

