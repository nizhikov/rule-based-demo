package org.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rules.Action;
import org.rules.RestAction;
import org.rules.Rule;
import org.rules.Task;

public class ChangeCoordinatotsNotificationTask implements Task {
    List<String> clients = new ArrayList<>();
    List<String> coordinators = new ArrayList<>();
    List<String> toNotify = new ArrayList<>();
    List<String> toNotifyClients = new ArrayList<>();
    long lastUpdateClient = 0;
    long lastUpdate = 0;
    final long timeout;

    public ChangeCoordinatotsNotificationTask(long timeout) {
        this.timeout = timeout;
    }

    @Rule
    public Action updateClientRule() {
        if (lastUpdateClient + timeout > System.currentTimeMillis()) {
            return null;
        }

        return new RestAction("https://someserver/clientList").onSuccess(res -> {
            clients = Arrays.asList(res.get("list").split(","));
            lastUpdateClient = System.currentTimeMillis();
        });
    }

    @Rule
    public Action updateCoordinatorsRule() {
        if (lastUpdate + timeout > System.currentTimeMillis()) {
            return null;
        }

        return new RestAction("https://someserver/coordinatorList").onSuccess(res -> {
            List<String> currentCoordinators =
                    new ArrayList<>(Arrays.asList(res.get("list").split(",")));

            lastUpdate = System.currentTimeMillis();

            currentCoordinators.removeAll(coordinators);
            if (!currentCoordinators.isEmpty()) {
                toNotify = currentCoordinators;
                coordinators.addAll(toNotify);
                toNotifyClients = new ArrayList<>(clients);
            }
        });
    }

    @Rule
    public Action notifyRule() {
        if (toNotify == null || toNotifyClients == null || toNotifyClients.isEmpty()) {
            return null;
        }

        String client = toNotifyClients.get(0);

        return new RestAction("https://%s/updateCoordinatoList?list=%s".formatted(client, toNotify)).onSuccess(res -> {
            toNotifyClients.remove(0);
            if (toNotifyClients.isEmpty()) {
                toNotify = null;
                toNotifyClients = null;
            }
        });
    }

    @Override
    public String name() {
        return "change-coordinators-notification";
    }

    @Override
    public boolean goal() {
        return false;
    }
}
