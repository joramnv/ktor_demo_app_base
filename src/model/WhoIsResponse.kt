package com.joram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WhoIsResponse(
    @JsonProperty("requestFrom") val requestFrom: String,
    @JsonProperty("responseFrom") val responseFrom: String,
    @JsonProperty("communicationMethod") val communicationMethod: String,
    @JsonProperty("message") val message: String
)

fun WhoIsRequest.toResponse(thisApp: String, communicationMethod: String): WhoIsResponse =
    WhoIsResponse(
    requestFrom = this.requestFrom,
    responseFrom = thisApp,
    communicationMethod = communicationMethod,
    message = "Hello ${this.requestFrom}, this is a message from $thisApp. We are communicating over ${communicationMethod}."
)
