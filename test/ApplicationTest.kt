package com.joram

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() {
        withTestApplication({ baseModule(testing = true) }) {
            handleRequest(HttpMethod.Get, "/health-check").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Feeling pretty healthy today.", response.content)
            }
        }
    }
}
