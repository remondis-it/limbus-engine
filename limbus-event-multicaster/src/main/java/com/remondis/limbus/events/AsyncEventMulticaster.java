package com.remondis.limbus.events;

/**
 * This async event multicaster manages a list of subscribers that are called on an income of a specific event. The
 * event
 * multicaster is assumed to be thread safe. This version of event multicaster manages a thread to broadcast an event
 * asynchronously.
 *
 * @param <I>
 *        The subscriber interface type.
 * @author schuettec
 *
 */
public interface AsyncEventMulticaster<I> extends EventMulticaster<I>, AutoCloseable {

}
