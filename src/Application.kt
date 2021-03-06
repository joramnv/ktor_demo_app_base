package com.joram

import arrow.core.Either
import arrow.core.right
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.baseModule(testing: Boolean = false) {
    val applicationConfig: ApplicationConfig = environment.config

    launchGrpcServer(applicationConfig)

    install(CallLogging)

    install(ContentNegotiation) {
        gson { }
    }

    routing {
        get("/health-check") {
            Either.resolve(
                f = {
                    val domainLogic: Either<DomainError, String> = "Feeling pretty healthy today.".right()
                    domainLogic
                },
                success = { a -> handleSuccessTextPlain(call, a) },
                error = { e -> handleDomainError(call, ::logError, e) },
                throwable = { throwable -> handleSystemFailure(call, ::logError, throwable) },
                unrecoverableState = { e -> logError(e) }
            )
        }
    }
}

data class DomainError(val errorMessage: String)

suspend fun handleSuccessTextPlain(
    call: ApplicationCall,
    text: String
): Either<Throwable, Unit> =
    Either.catch {
        call.respondText(text, contentType = ContentType.Text.Plain)
    }

suspend inline fun <reified A : Any> handleSuccess(
    call: ApplicationCall,
    a: A
): Either<Throwable, Unit> =
    Either.catch {
        call.respond(status = HttpStatusCode.OK, message = a)
    }

suspend fun <E> handleDomainError(
    call: ApplicationCall,
    log: suspend (e: E) -> Either<Throwable, Unit>,
    e: E
): Either<Throwable, Unit> =
    Either.catch {
        log(e)
        call.respond(status = HttpStatusCode.InternalServerError, message = "Something went wrong. Sorry!")
    }

suspend fun handleSystemFailure(
    call: ApplicationCall,
    log: suspend (throwable: Throwable) -> Either<Throwable, Unit>,
    throwable: Throwable
): Either<Throwable, Unit> =
    Either.catch {
        log(throwable)
        call.respond(status = HttpStatusCode.InternalServerError, message = "Something went wrong. Sorry!")
    }

fun <E> logError(e: E): Either<Throwable, Unit> =
    Either.catch {
        val logger: Logger = getLogger()
        logger.error("Something went wrong, $e")
    }

fun logInfo(message: String): Either<Throwable, Unit> =
    Either.catch {
        val logger: Logger = getLogger()
        logger.info(message)
    }

fun getLogger(): Logger {
    return LoggerFactory.getLogger("Top level logger")
}


val CONTENT_TYPE = "Content-Type"
val APPLICATION_JSON = "application/json"

fun launchGrpcServer(applicationConfig: ApplicationConfig) {
    GlobalScope.launch {
        val grpcPort: Int = applicationConfig.getGrpcPort()
        val appId: String = applicationConfig.getAppId()
        val appName: String = getAppName(appId)
        val grpcService = GrpcService(grpcPort, appName)
        grpcService.start()
        grpcService.awaitTermination()
    }
}

fun ApplicationConfig.getGrpcPort(): Int =
    this.property("grpc.deployment.port").getString().toInt()

fun ApplicationConfig.getAppId(): String =
    this.property("ktor.application.id").getString()

fun getAppName(appId: String): String =
    getSecret("app-name-$appId")
        .fold({ "default-app-name" }, { it })
