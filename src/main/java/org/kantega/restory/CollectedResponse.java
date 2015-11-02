package org.kantega.restory;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 */
public class CollectedResponse {
    private MultivaluedMap<String, String> headers;
    private String payload;
    private int responseCode;
    private String responseReason;


    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public String getResponseReason() {
        return responseReason;
    }
}
