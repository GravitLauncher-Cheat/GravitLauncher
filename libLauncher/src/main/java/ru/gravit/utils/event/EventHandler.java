package ru.gravit.utils.event;

import java.util.UUID;

@FunctionalInterface
public interface EventHandler<T extends EventInterface>
{
    void run(final UUID p0, final T p1);
}
