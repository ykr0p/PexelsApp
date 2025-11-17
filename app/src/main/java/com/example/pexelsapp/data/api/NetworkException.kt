package com.example.pexelsapp.data.api

sealed class NetworkException(message: String) : Exception(message) {

    class NoConnectionException(message: String = "No internet connection") : NetworkException(message)

    class TimeoutException(message: String = "Network request timed out") : NetworkException(message)

    class NetworkErrorException(message: String = "Network error occurred") : NetworkException(message)
}

sealed class ApiException(message: String) : Exception(message) {

    class ServerException(val code: Int, message: String) : ApiException(message)

    class ParseException(message: String = "Failed to parse response") : ApiException(message)
}
