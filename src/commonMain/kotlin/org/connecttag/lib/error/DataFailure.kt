package org.connecttag.lib.error

/**
 * Common data access and storage failure abstraction.
 */
sealed class DataFailure(
    val failureCode: String? = null,
    override val cause: Throwable? = null,
    override val message: String? = failureCode,
) : Exception(message, cause) {

    class LocalStorage(
        failureCode: String? = null,
        cause: Throwable? = null,
    ) : DataFailure(failureCode = failureCode, cause = cause)

    class Network(
        failureCode: String? = null,
        cause: Throwable? = null,
    ) : DataFailure(failureCode = failureCode, cause = cause)

    class Validation(
        failureCode: String? = null,
        cause: Throwable? = null,
    ) : DataFailure(failureCode = failureCode, cause = cause)

    class NotFound(
        failureCode: String? = null,
        cause: Throwable? = null,
    ) : DataFailure(failureCode = failureCode, cause = cause)
}
