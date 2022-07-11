package com.joinalongapp;

public class Constants {
    public static final int STATUS_HTTP_404 = 404;
    public static final int STATUS_HTTP_200 = 200;
    //TODO: maybe add 201 created for post requests?
    //TODO: maybe if puts return nothing, 204 no content might be more appropriate
    //TODO: i would argue that if token somehow become null (maybe google is down and the world is burning, or someone purposefully removed it)
    //      then we should have checks for no token on the backend, maybe return 403 or 400
    //      that way, we avoid the it did nothing i'm so confused when we were testing by having this error check on front and back

    public Constants() {
        // empty constructor
    }
}
