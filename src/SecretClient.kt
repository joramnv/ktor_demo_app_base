package com.joram

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.dapr.client.DaprClientBuilder

/**
 * Identifier in Dapr for the secret store.
 */
private const val SECRET_STORE_NAME = "vault"

fun getSecret(secretKey: String): Either<DomainError, String> =
    Either.catch {
        DaprClientBuilder().build().use { client ->
            client.getSecret(SECRET_STORE_NAME, secretKey).block()
        }
    }
        .fold(
            {
                DomainError("Something went wrong while retrieving secret with key $secretKey. The exception is: $it").left()
            },
            { secret: MutableMap<String, String>? ->
                secret?.get(secretKey)?.right() ?: DomainError("Something went wrong, there is no secret for key $secretKey").left()
            }
        )
