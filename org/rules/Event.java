package org.rules;

public interface Event {
    public int type();

    public Object payload();
}
