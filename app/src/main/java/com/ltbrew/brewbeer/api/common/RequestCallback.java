package com.ltbrew.brewbeer.api.common;

public interface RequestCallback<T> {

    void onSuccess(T result);

    void onError(String errorMsg);
}
