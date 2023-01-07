package dev.vineeth.ktasyncremotecalls.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.function.Supplier
import org.apache.hc.core5.util.Timeout
import org.apache.hc.client5.http.config.RequestConfig
import java.util.concurrent.TimeUnit


@Configuration
class ApplicationConfiguration {

    @Bean
    fun objectMapper() : ObjectMapper {
        return ObjectMapper()
    }

    @Bean("ordersOkHttpClient")
    fun okHttpClient() : OkHttpClient {
        val client = OkHttpClient()
        return client
    }

    @Bean("ordersRestTemplate")
    fun restTemplate(
        clientRequestFactory: ClientHttpRequestFactory
    ) : RestTemplate {
        return RestTemplateBuilder()
            .rootUri("http://localhost:3005")
            .setConnectTimeout(Duration.ofMillis(1000))
            .requestFactory (Supplier { clientRequestFactory })
            .build()
    }

    @Bean
    fun customRequestFactory(poolingConnManager: PoolingHttpClientConnectionManager): ClientHttpRequestFactory {

        val rc = RequestConfig
            .custom()
            .setConnectionRequestTimeout(Timeout.of(300, TimeUnit.MILLISECONDS))
            .build()

        val httpClient = HttpClients.custom()
            .setConnectionManager(poolingConnManager)
            .setDefaultRequestConfig(rc)
            .build()

        return HttpComponentsClientHttpRequestFactory(httpClient)
    }

    @Bean
    fun poolingConnectionManager() : PoolingHttpClientConnectionManager {
        val poolingConnManager = PoolingHttpClientConnectionManager()

        poolingConnManager.maxTotal = 250
        poolingConnManager.defaultMaxPerRoute =250
        poolingConnManager.validateAfterInactivity = Timeout.ofMilliseconds(1000)

        return poolingConnManager
    }
}