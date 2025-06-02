package ch.framedev.metarapp.database;



/*
 * ch.framedev.metarapp.database
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 12.12.2024 22:55
 */

/**
 * Represents a callback for asynchronous database operations.
 *
 * @param <T> The type of the result.
 */
public interface Callback<T> {

    /**
     * Called when the operation was successful.
     *
     * @param result The result of the operation.
     */
    void onResult(T result);

    /**
     * Called when the operation failed.
     *
     * @param throwable The throwable that caused the error.
     */
    void onError(Throwable throwable);
}
