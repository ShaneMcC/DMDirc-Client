/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.events;

import com.dmdirc.events.eventbus.BaseEvent;
import com.dmdirc.interfaces.WindowModel;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Fired when an unknown command is used.
 */
public class UnknownCommandEvent extends BaseEvent implements DisplayableEvent {

    /** The properties associated with this event. */
    private final DisplayPropertyMap properties = new DisplayPropertyMap();
    @Nullable private final WindowModel source;
    private final String command;
    private final String[] arguments;

    public UnknownCommandEvent(final LocalDateTime timestamp, @Nullable final WindowModel source,
            final String command, final String[] arguments) {
        super(timestamp);
        this.source = source;
        this.command = command;
        this.arguments = arguments;
    }

    public UnknownCommandEvent(@Nullable final WindowModel source, final String command,
            final String[] arguments) {
        this.source = source;
        this.command = command;
        this.arguments = arguments;
    }

    @Override
    @Nullable
    public WindowModel getSource() {
        return source;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArguments() {
        return arguments;
    }

    @Override
    public <T> void setDisplayProperty(final DisplayProperty<T> property, final T value) {
        properties.put(property, value);
    }

    @Override
    public <T> Optional<T> getDisplayProperty(final DisplayProperty<T> property) {
        return properties.get(property);
    }

    @Override
    public DisplayPropertyMap getDisplayProperties() {
        return properties;
    }

}
