package dev.vineeth.ktasyncremotecalls.api

import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponse

class Problem(
    private val statusCode: HttpStatusCode,
    private val detail: String
) : ErrorResponse {

    override fun getStatusCode(): HttpStatusCode {
        return this.statusCode
    }

    override fun getBody(): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            this.statusCode,
            this.detail
        )
    }
}