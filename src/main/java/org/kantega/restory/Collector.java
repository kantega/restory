package org.kantega.restory;

/**
 *
 */
public class Collector {

    private static ThreadLocal<CollectedExchange> current = new ThreadLocal<>();

    public static void newExchange(CollectedRequest msg) {
        current.set(new CollectedExchange(msg));
    }

    public static void endExchange(CollectedResponse msg) {
        current.get().setResponse(msg);
    }


    public static CollectedExchange lastExchange() {
        return current.get();
    }
}
