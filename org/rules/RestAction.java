package org.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RestAction implements Action {
    private final String url;

    private Consumer<Map<String, String>> callback;
    private Consumer<Throwable> onError;

    public RestAction(String url) {
        this.url = url;
    }

    public RestAction onSuccess(Consumer<Map<String, String>> callback) {
        this.callback = callback;
        return this;
    }

    public RestAction onError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    @Override
    public void execute() {
        System.out.println("Query rest endpoint: " + url);

        Map<String, String> res = new HashMap<>();

        if (url.contains("coordinatorList")) {
            res.put("list", "coord1,coord2,coord3");
        } else if (url.contains("clientList")){
            res.put("list", "client1,client2,client3");
        } else {
            System.out.println("UPDATING COORDINATOR LIST FOR: " + url);
        }

        callback.accept(res);
    }
}
