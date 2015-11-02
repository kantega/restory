package org.kantega.restory;

/**
 *
 */
public class CollectedExchange {
    private final CollectedRequest request;
    private CollectedResponse response;

    public CollectedExchange(CollectedRequest msg) {

        this.request = msg;
    }



    public CollectedRequest getRequest() {
        return request;
    }

    public CollectedResponse getResponse() {
        return response;
    }

    public void setResponse(CollectedResponse response) {
        this.response = response;
    }
}
