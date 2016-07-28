package com.notedok.notedok;

import android.os.AsyncTask;

/**
 * Async task that executes a worker and calls onSuccess with the result or onError with the exception.
 * @param <T> The type of the result returned by worker.
 */
public class AsyncWorkerTask<T> extends AsyncTask<Void, Void, T> {
    public interface Worker<T> {
        T getResult();
    }

    private final Worker<T> _worker;
    private final OnSuccess<T> _onSuccess;
    private final OnError _onError;
    private Exception _exception;

    public AsyncWorkerTask(Worker<T> worker, OnSuccess<T> onSuccess, OnError onError) {
        if (worker == null) {
            throw new IllegalArgumentException("worker");
        }
        if (onSuccess == null) {
            throw new IllegalArgumentException("onSuccess");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError");
        }

        _worker = worker;
        _onSuccess = onSuccess;
        _onError = onError;
    }

    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);
        if (_exception != null) {
            _onError.call(_exception);
        } else {
            _onSuccess.call(result);
        }
    }

    @Override
    protected T doInBackground(Void... params) {
        try {
            return _worker.getResult();
        } catch (Exception e) {
            _exception = e;
        }
        return null;
    }
}
