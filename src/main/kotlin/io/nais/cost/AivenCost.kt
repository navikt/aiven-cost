package io.nais.cost

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.nais.cost.aiven.Aiven
import io.prometheus.client.CollectorRegistry
import org.slf4j.event.Level

fun Application.aivenApi() {
    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            !call.request.path().startsWith("/internal")
        }
    }
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json
        )
    }
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            CollectorRegistry.defaultRegistry,
            Clock.SYSTEM
        )
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics()
        )
    }
    routing {
        nais()
    }
    val configuration = Configuration()
    val invoiceData = Aiven(configuration.aivenToken).getInvoiceData()
    val costItems = invoiceData.map { fromInvoiceLine(it) }
    log.info("fetched data from aiven: ${invoiceData.size} && ${costItems.size}")


}

