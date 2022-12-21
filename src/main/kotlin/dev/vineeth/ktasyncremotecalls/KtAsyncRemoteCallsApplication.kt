package dev.vineeth.ktasyncremotecalls

import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class KtAsyncRemoteCallsApplication

fun main(args: Array<String>) {
    System.setProperty(IO_PARALLELISM_PROPERTY_NAME, "250")
    runApplication<KtAsyncRemoteCallsApplication>(*args)
}


