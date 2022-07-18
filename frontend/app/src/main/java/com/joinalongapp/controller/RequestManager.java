package com.joinalongapp.controller;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestManager implements Callback {
    private final String SCHEME = "http";
    private final String BASE_URL = "54.200.52.211";
    private final int PORT = 3000;
    private OnRequestCompleteListener onRequestCompleteListener;

    public RequestManager() {
        super();
    }

    /**
     * Read from the given path
     * @param path path of URL to read from
     * @param tokenHeader header for user token
     * @return result/response of reading at url
     * @throws IOException
     */
    public void get(String path, String tokenHeader, OnRequestCompleteListener callback) throws IOException {
        onRequestCompleteListener = callback;
        OkHttpClient client = new OkHttpClient();

        String url = buildUrl(path, null);

        Request request = new Request.Builder()
                .url(url)
                .header("token", tokenHeader)
                .get()
                .build();

        client.newCall(request).enqueue(this);
    }

    /**
     * Read from the given path with search parameters
     * @param path path of URL to read from
     * @param parameters parameters to search with
     * @param tokenHeader header for user token
     * @return result/response of reading at url
     * @throws IOException
     */
    public void get(String path, Map<String, String> parameters, String tokenHeader, OnRequestCompleteListener callback) throws IOException {
        onRequestCompleteListener = callback;
        OkHttpClient client = new OkHttpClient();

        String url = buildUrl(path, parameters);

        Request request = new Request.Builder()
                .url(url)
                .header("token", tokenHeader)
                .get()
                .build();

        client.newCall(request).enqueue(this);
    }

    /**
     * Creates object in the database at the given path with the given data in jsonBody
     * @param path path to create object
     * @param jsonBody object data
     * @return response/result of creating
     * @throws IOException
     */
    public void post(String path, String jsonBody, OnRequestCompleteListener callback) throws IOException {
        onRequestCompleteListener = callback;
        OkHttpClient client = new OkHttpClient();

        String url = buildUrl(path, null);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(this);
    }

    /**
     * Deletes an object at the given path
     * @param path path at which to delete object
     * @return response of delete
     * @throws IOException
     */
    public void delete(String path, OnRequestCompleteListener callback) throws IOException {
        onRequestCompleteListener = callback;
        OkHttpClient client = new OkHttpClient();

        String url = buildUrl(path, null);

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(this);
    }

    /**
     * Updates an object at the given path with the new data in jsonBody
     * @param path path at which to update the object
     * @param jsonBody new data to update the object to
     * @return response from update
     * @throws IOException
     */
    public void put(String path, String jsonBody, OnRequestCompleteListener callback) throws IOException {
        onRequestCompleteListener = callback;
        OkHttpClient client = new OkHttpClient();

        String url = buildUrl(path, null);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        client.newCall(request).enqueue(this);
    }

    private String buildUrl(String path, @Nullable Map<String, String> parameters) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(BASE_URL)
                .port(PORT)
                .addPathSegments(path);

        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                urlBuilder.addQueryParameter(parameter.getKey(), parameter.getValue());
            }
        }

        return urlBuilder.build().toString();
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        onRequestCompleteListener.onError(call, e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        onRequestCompleteListener.onSuccess(call, response);
    }

    public enum Range {
        GREATER_THAN {
            @NonNull
            @Override
            public String toString() {
                return "gt";
            }
        },
        LESS_THAN {
            @NonNull
            @Override
            public String toString() {
                return "lt";
            }
        }
    }

    public interface OnRequestCompleteListener {
        void onSuccess(Call call, Response response);
        void onError(Call call, IOException e);
    }
}
