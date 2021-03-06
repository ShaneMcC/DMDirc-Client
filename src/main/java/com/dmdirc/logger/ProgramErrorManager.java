/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.logger;

import com.dmdirc.events.ErrorEvent;
import com.dmdirc.events.FatalProgramErrorEvent;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.util.EventUtils;
import com.dmdirc.util.LogUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import net.engio.mbassy.listener.Handler;

/**
 * Listens for {@link ErrorEvent}s, creates {@link ProgramError}s and raises {@link
 * ProgramErrorEvent}s.
 */
@Singleton
public class ProgramErrorManager {

    /** The event bus to listen for errors on. */
    private final EventBus eventBus;
    /** The current list of errors. */
    private final Set<ProgramError> errors;
    /** Factory to create {@link ProgramError}s. */
    private final ProgramErrorFactory programErrorFactory;

    @Inject
    public ProgramErrorManager(final EventBus eventBus,
            final ProgramErrorFactory programErrorFactory) {
        this.eventBus = eventBus;
        this.programErrorFactory = programErrorFactory;
        errors = new CopyOnWriteArraySet<>();
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise() {
        eventBus.subscribe(this);
    }

    void handle(final ILoggingEvent event) {
        final ProgramError error = addError(LogUtils.getErrorLevel(event.getLevel()),
                event.getFormattedMessage(), LogUtils.getThrowable(event),
                isAppError(event.getMarker()));
        handleErrorEvent(error);
    }

    private boolean isAppError(@Nullable final Marker marker) {
        return marker != null
                && (marker == LogUtils.APP_ERROR || marker == LogUtils.FATAL_APP_ERROR);
    }

    void handleErrorEvent(final ProgramError error) {
        if (error.getLevel() == ErrorLevel.FATAL) {
            eventBus.publish(new FatalProgramErrorEvent(error));
        } else {
            eventBus.publish(new NonFatalProgramErrorEvent(error));
        }
    }

    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "ThrowableResultOfMethodCallIgnored"})
    void handleProgramError(final NonFatalProgramErrorEvent event) {
        if (!event.isHandled()) {
            System.err.println(event.getError().getMessage());
            event.getError().getThrowableAsString().ifPresent(System.err::println);
        }
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param throwable The exception that caused the error, if any.
     * @param appError  Whether or not this is an application error
     *
     * @since 0.6.3m1
     */
    private ProgramError addError(final ErrorLevel level, final String message,
            final Throwable throwable, final boolean appError) {

        final ProgramError error = programErrorFactory.create(level, message, throwable,
                LocalDateTime.now(), appError);
        errors.add(error);
        return error;
    }

    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        errors.remove(error);
        eventBus.publishAsync(new ProgramErrorDeletedEvent(error));
    }

    /**
     * Deletes all errors from the manager.
     *
     * @since 0.6.3m1
     */
    public void deleteAll() {
        final Collection<ProgramError> errorsCopy = new HashSet<>(errors);
        errors.clear();
        errorsCopy.stream().map(ProgramErrorDeletedEvent::new).forEach(eventBus::publish);
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public Set<ProgramError> getErrors() {
        return Collections.unmodifiableSet(errors);
    }
}
