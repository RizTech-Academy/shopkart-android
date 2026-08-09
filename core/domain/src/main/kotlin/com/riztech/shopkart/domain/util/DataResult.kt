package com.riztech.shopkart.domain.util

/**
 * The outcome of an operation that can fail for a reason the UI must explain.
 *
 * Preferred over exceptions across layer boundaries because it makes failure
 * part of the signature: a caller cannot forget to handle it, and `when` over
 * a sealed hierarchy is checked by the compiler.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/** Failures the presentation layer is expected to distinguish between. */
enum class DataError {
    /** No usable connection. */
    Network,

    /** Reached the server, but it answered with an error. */
    Server,

    /** The requested thing does not exist. */
    NotFound,

    /** Anything we did not anticipate. */
    Unknown,
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Success) action(data)
}

inline fun <T> DataResult<T>.onFailure(action: (DataError) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Failure) action(error)
}

fun <T> DataResult<T>.getOrNull(): T? = (this as? DataResult.Success)?.data
