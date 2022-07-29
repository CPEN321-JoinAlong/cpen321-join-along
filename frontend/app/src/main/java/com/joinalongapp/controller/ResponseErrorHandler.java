package com.joinalongapp.controller;

import static com.joinalongapp.FeedbackMessageBuilder.createDefaultNeutralConflictErrorOnHttp409;
import static com.joinalongapp.FeedbackMessageBuilder.createDefaultNeutralInvalidErrorOnHttp422;
import static com.joinalongapp.FeedbackMessageBuilder.createDefaultNeutralNotFoundErrorOnHttp404;
import static com.joinalongapp.FeedbackMessageBuilder.createServerInternalError;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_404;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_409;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_422;
import static com.joinalongapp.HttpStatusConstants.STATUS_HTTP_500;

import android.app.Activity;

import okhttp3.Response;

public class ResponseErrorHandler {

    public static void createErrorMessage(Response response, String operation, String object, Activity activity){
        switch(response.code()) {

            case STATUS_HTTP_404:
                createDefaultNeutralNotFoundErrorOnHttp404(operation, object, activity);
                break;

            case STATUS_HTTP_409:
                createDefaultNeutralConflictErrorOnHttp409(operation, activity);
                break;

            case STATUS_HTTP_422:
                createDefaultNeutralInvalidErrorOnHttp422(operation, activity);
                break;

            case STATUS_HTTP_500:
            default:
                createServerInternalError(operation, activity);
                break;
        }
    }
}
