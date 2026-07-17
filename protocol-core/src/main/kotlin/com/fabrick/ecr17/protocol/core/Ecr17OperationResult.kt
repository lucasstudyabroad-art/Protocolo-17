package com.fabrick.ecr17.protocol.core

/**
 * Generic outcome of an ECR17 operation.
 *
 * [Uncertain] is a distinct, first-class case (not just a [Failure]): it means the request
 * was physically ACKed by the terminal but the connection was lost before a final result
 * arrived. Callers must never treat [Uncertain] as [Success] or auto-retry the underlying
 * financial operation.
 */
sealed interface Ecr17OperationResult<out T> {
    data class Success<T>(val response: T) : Ecr17OperationResult<T>
    data class Rejected<T>(val response: T) : Ecr17OperationResult<T>
    data class Failure(val error: Ecr17Error) : Ecr17OperationResult<Nothing>
    data class Uncertain(val operationId: String, val reason: String) : Ecr17OperationResult<Nothing>
}

inline fun <T, R> Ecr17OperationResult<T>.map(transform: (T) -> R): Ecr17OperationResult<R> = when (this) {
    is Ecr17OperationResult.Success -> Ecr17OperationResult.Success(transform(response))
    is Ecr17OperationResult.Rejected -> Ecr17OperationResult.Rejected(transform(response))
    is Ecr17OperationResult.Failure -> this
    is Ecr17OperationResult.Uncertain -> this
}
