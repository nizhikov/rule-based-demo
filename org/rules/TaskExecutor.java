package org.rules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rules.impl.ChangeCoordinatotsNotificationTask;

public class TaskExecutor implements Runnable {
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();

    private final List<Task> toStart = new ArrayList<>();
    private final List<String> toStop = new ArrayList<>();

    private AtomicBoolean stoped = new AtomicBoolean(false);


    @Override
    public void run() {
        while (!stoped.get()) {
            for (String task : toStop) {
                if (tasks.remove(task) != null) {
                    System.out.println(String.format("Task stoped [name=%s]", task));
                }
            }

            for (Task task : toStart) {
                if (tasks.putIfAbsent(task.name(), task) != null) {
                    System.out.println(String.format("Task already running [name=%s]", task.name()));
                }
            }

            toStart.clear();
            toStop.clear();

            tasks.entrySet().removeIf(e -> e.getValue().goal());

            Optional<Action> action = tasks.entrySet().stream().map(this::taskAction).filter(Objects::nonNull).findFirst();

            action.ifPresent(Action::execute);
        }
    }

    private Action taskAction(Map.Entry<String, Task> e) {
        Task t = e.getValue();
        Method[] methods = t.getClass().getMethods();

        for (Method m : methods) {
            if (m.isAnnotationPresent(Rule.class)) {
                try {
                    Action maybeAction = (Action) m.invoke(t);

                    if (maybeAction != null)
                        return maybeAction;
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return null;
    }

    public void start(Task task) {
        toStart.add(task);
    }

    public void stop(String name) {
        toStop.add(name);
    }

    public void stop() {
        stoped.set(true);
    }

    public static void main(String[] args) {
        TaskExecutor t = new TaskExecutor();

        t.start(new ChangeCoordinatotsNotificationTask(5000));

        t.run();
    }
}
