package com.joram

import com.google.protobuf.Any
import com.google.protobuf.Message
import com.joram.examples.ProtoModels.SayRequest
import com.joram.examples.ProtoModels.SayResponse
import com.joram.examples.ProtoModels.WhoIsRequest
import com.joram.examples.ProtoModels.WhoIsResponse
import com.joram.model.CommunicationMethod
import io.dapr.v1.AppCallbackGrpc.AppCallbackImplBase
import io.dapr.v1.CommonProtos.InvokeRequest
import io.dapr.v1.CommonProtos.InvokeResponse
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

/**
 * Server mode: class that encapsulates all server-side logic for gRPC.
 * Implementation strongly inspired by:
 * https://github.com/dapr/java-sdk/blob/master/examples/src/main/java/io/dapr/examples/invoke/grpc/HelloWorldService.java
 */
class GrpcService(private val port: Int, private val appName: String) : AppCallbackImplBase() {
    /**
     * Server mode: Grpc server.
     */
    private var server: Server? = null

    /**
     * Server mode: starts listening on given port.
     *
     * @throws IOException Errors while trying to start service.
     */
    fun start() {
        server = ServerBuilder
            .forPort(port)
            .addService(this)
            .build()
            .start()
        System.out.printf("Server: started listening on port %d\n", port)
        // Now we handle ctrl+c (or any other JVM shutdown)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Server: shutting down gracefully ...")
                server?.shutdown()
                println("Server: Bye.")
            }
        })
    }

    /**
     * Server mode: waits for shutdown trigger.
     *
     * @throws InterruptedException Propagated interrupted exception.
     */
    fun awaitTermination() {
        if (server != null) {
            server!!.awaitTermination()
        }
    }

    /**
     * Server mode: this is the Dapr method to receive Invoke operations via Grpc.
     *
     * @param request          Dapr envelope request,
     * @param responseObserver Dapr envelope response.
     */
    override fun onInvoke(
        request: InvokeRequest,
        responseObserver: StreamObserver<InvokeResponse>
    ) {
        try {
            if ("say" == request.method) {
                val sayRequest: SayRequest = SayRequest.newBuilder().setMessage(request.data.value.toStringUtf8()).build()
                val sayResponse: SayResponse = say(sayRequest)
                val responseBuilder = InvokeResponse.newBuilder()
                responseBuilder.data = Any.pack<Message>(sayResponse)
                responseObserver.onNext(responseBuilder.build())
            } else if ("whoIs" == request.method) {
                val whoIsRequest: WhoIsRequest = WhoIsRequest.parseFrom(request.data.value)
                val whoIsResponse: WhoIsResponse = whoIs(whoIsRequest)
                val responseBuilder = InvokeResponse.newBuilder()
                responseBuilder.data = Any.pack<Message>(whoIsResponse)
                responseObserver.onNext(responseBuilder.build())
            }
        } finally {
            responseObserver.onCompleted()
        }
    }

    /**
     * Handling of the 'say' method.
     *
     * @param request Request to say something.
     * @return Response with when it was said.
     */
    fun say(request: SayRequest): SayResponse {
        val utcNow = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        val utcNowAsString = DATE_FORMAT.format(utcNow.time)
        // Handles the request by printing message.
        println("Server: " + request.message)
        println("@ $utcNowAsString")
        // Now respond with current timestamp.
        val responseBuilder: SayResponse.Builder = SayResponse.newBuilder()
        return responseBuilder.setTimestamp(utcNowAsString).build()
    }

    fun whoIs(request: WhoIsRequest): WhoIsResponse {
        val requestFrom: String = request.requestFrom
        val responseFrom = appName
        val communicationMethod = CommunicationMethod.GRPC.value
        val message = "Hello $requestFrom, this is a message from $responseFrom. We are communicating over ${communicationMethod}."
        return WhoIsResponse.newBuilder()
            .setRequestFrom(requestFrom)
            .setResponseFrom(responseFrom)
            .setCommunicationMethod(communicationMethod)
            .setMessage(message)
            .build()
    }

    companion object {
        /**
         * Format to output date and time.
         */
        private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }
}
