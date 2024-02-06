package sengine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;

import java.util.Map;

import sengine.Sys;

/**
 * Created by Azmi on 3/14/2017.
 */
public class NetRequest extends Net.HttpRequest implements Net.HttpResponseListener {
    private static final String TAG = "NetRequest";

    private final String requestName;

    // Current
    private String response;
    private Throwable error;

    public boolean isFinished() {
        return error != null || response != null;
    }

    public Throwable getError() {
        return error;
    }

    public String getResponse() {
        return response;
    }

    public String getRequestName() {
        return requestName;
    }



    public NetRequest() {
        this(null);
    }

    public NetRequest(String requestName) {
        this.requestName = requestName;
    }

    public NetRequest instantiate() {
        return instantiate(requestName);
    }

    /**
     * Requests with content stream are not supported
     */
    public NetRequest instantiate(String requestName) {
        NetRequest request = new NetRequest(requestName);
        request.setUrl(getUrl());
        request.setMethod(getMethod());
        // Body
        for (Map.Entry<String, String> header : getHeaders().entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }
        request.setContent(getContent());
        return request;
    }

    @Override
    public synchronized void handleHttpResponse(Net.HttpResponse httpResponse) {
        try {
            // Get response
            response = httpResponse.getResultAsString();

            // Validate status code
            int statusCode = httpResponse.getStatus().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Sys.error(TAG, "Unexpected " + statusCode + " for " + requestName + "\n" + response);
                error = new RuntimeException("Unexpected status code " + statusCode);
            } else
                Sys.info(TAG, "Received 200 OK for " + requestName);
        } catch (Throwable e) {
            error = e;
        } finally {
            notify();       // release
        }
    }

    @Override
    public synchronized void failed(Throwable e) {
        try {
            error = e;

            Sys.error(TAG, "Request failed for " + requestName, e);
        } finally {
            notify();       // release
        }
    }

    @Override
    public synchronized void cancelled() {
        try {
            error = new RuntimeException("Request cancelled");

            Sys.error(TAG, "Request cancelled for " + requestName);
        } finally {
            notify();       // release
        }
    }

    public void send() {
        Sys.info(TAG, "Downloading " + requestName);
        Gdx.net.sendHttpRequest(this, this);
    }

    public synchronized String read() {
        try {
            send();
            wait();
        } catch (Throwable e) {
            error = e;
        }

        if (error != null)
            throw new RuntimeException("Read failed for " + requestName, error);
        return response;
    }
}
