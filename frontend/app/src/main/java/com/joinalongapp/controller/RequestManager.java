package com.joinalongapp.controller;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestManager {
    private final String SCHEME = "http";
    private final String BASE_URL;

    public RequestManager(String baseUrl) {
        BASE_URL = baseUrl;
    }

    /**
     * Read from the given path
     * @param path path of URL to read from
     * @return result/response of reading at url
     * @throws IOException
     */
    public Response get(String path) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = buildUrl(path, null);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return client.newCall(request).execute();
    }

    /**
     * Read from the given path with search parameters
     * @param path path of URL to read from
     * @param parameters parameters to search with
     * @return result/response of reading at url
     * @throws IOException
     */
    public Response get(String path, Map<String, String> parameters) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = buildUrl(path, parameters);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return client.newCall(request).execute();
    }

    /**
     * Creates object in the database at the given path with the given data in jsonBody
     * @param path path to create object
     * @param jsonBody object data
     * @return response/result of creating
     * @throws IOException
     */
    public Response post(String path, String jsonBody) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = buildUrl(path, null);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return client.newCall(request).execute();
    }

    /**
     * Deletes an object at the given path
     * @param path path at which to delete object
     * @return response of delete
     * @throws IOException
     */
    public Response delete(String path) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = buildUrl(path, null);

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        return client.newCall(request).execute();
    }

    /**
     * Updates an object at the given path with the new data in jsonBody
     * @param path path at which to update the object
     * @param jsonBody new data to update the object to
     * @return response from update
     * @throws IOException
     */
    public Response put(String path, String jsonBody) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = buildUrl(path, null);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        return client.newCall(request).execute();
    }

    private HttpUrl buildUrl(String path, @Nullable Map<String, String> parameters) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(BASE_URL)
                .addPathSegments(path);

        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                urlBuilder.addQueryParameter(parameter.getKey(), parameter.getValue());
            }
        }

        return urlBuilder.build();
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
}
