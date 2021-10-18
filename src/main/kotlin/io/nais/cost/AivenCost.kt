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
import io.nais.cost.bigquery.BigQuery
import io.prometheus.client.CollectorRegistry
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer

private val LOGGER = LoggerFactory.getLogger("AivenCost")


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
    runAivenJob(configuration)
    scheduleJobEveryDay(configuration)
}

fun scheduleJobEveryDay(configuration: Configuration) {
    val osloTz = ZoneId.of("Europe/Oslo")
    val start = ZonedDateTime.now(osloTz).next(LocalTime.of(14, 3, 0, 0))
    fixedRateTimer(
        name = "AivenJobRunner",
        daemon = true,
        startAt = start,
        period = Duration.ofDays(1).toMillis()
    ) { runAivenJob(configuration) }
    LOGGER.info("scheduled Aiven job to run once each day, beginning on $start")
}

private fun runAivenJob(configuration: Configuration) {
    val invoiceData = Aiven(configuration.aivenToken).getInvoiceData()
    val costItems = invoiceData.flatMap { fromInvoiceLine(it) }
    LOGGER.info("fetched data from aiven: ${invoiceData.size} && ${costItems.size}")
    BigQuery().write(costItems)
}

fun ZonedDateTime.next(timeOfDay: LocalTime): Date =
    Date.from(
        this.plusDays(if (toLocalTime().isAfter(timeOfDay)) 1 else 0)
            .withHour(timeOfDay.hour)
            .withMinute(timeOfDay.minute)
            .withSecond(timeOfDay.second)
            .toInstant()
    )