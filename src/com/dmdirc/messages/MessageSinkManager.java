/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.messages;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Manages message sinks and facilitates despatching of messages to sinks.
 */
public class MessageSinkManager {

    /** A singleton instance of the manager. */
    private static final MessageSinkManager INSTANCE = new MessageSinkManager();

    /** The configuration domain to use for looking up default sinks. */
    public static final String CONFIG_DOMAIN = "notifications";

    /** The default sink to use if none is specified or in case of error. */
    public static final String DEFAULT_SINK = "self";

    /** A list of known sinks. */
    private final List<MessageSink> sinks = new ArrayList<MessageSink>();

    /**
     * Adds a new sink to the list of known sinks.
     *
     * @param sink The sink to be added
     */
    public void addSink(final MessageSink sink) {
        sinks.add(sink);
    }

    /**
     * Removes an existing sink from the list of known sinks.
     *
     * @param sink The sink to be removed
     */
    public void removeSink(final MessageSink sink) {
        sinks.remove(sink);
    }

    /**
     * Despatches a message to the appropriate sink. This method will attempt
     * to select an appropriate target sink from the user's configuration.
     *
     * @param source The source of the message
     * @param date The date at which the message occurred
     * @param messageType The type (or 'format') of the message
     * @param args The message arguments
     */
    public void despatchMessage(final WritableFrameContainer source,
            final Date date, final String messageType, final Object... args) {
        final String target;
        if (source.getConfigManager().hasOptionString(CONFIG_DOMAIN, messageType)) {
            target = source.getConfigManager().getOption(CONFIG_DOMAIN, messageType);
        } else {
            target = DEFAULT_SINK;
        }

        despatchMessage(source, date, messageType, target, args);
    }

    /**
     * Despatches a message to the appropriate sink.
     *
     * @param source The source of the message
     * @param date The date at which the message occurred
     * @param messageType The type (or 'format') of the message
     * @param targetSink The textual representation of the destination sink
     * @param args The message arguments
     */
    public void despatchMessage(final WritableFrameContainer source,
            final Date date, final String messageType, final String targetSink,
            final Object... args) {
        for (MessageSink sink : sinks) {
            final Matcher matcher = sink.getPattern().matcher(targetSink);

            if (matcher.matches()) {
                final String[] matches = new String[matcher.groupCount()];

                for (int i = 0; i < matcher.groupCount(); i++) {
                    matches[i] = matcher.group(i + 1);
                }

                sink.handleMessage(this, source, matches, date, messageType, args);
                return;
            }
        }

        // None of the sinks matched :(
        Logger.userError(ErrorLevel.MEDIUM, "Invalid target message sink for type "
                + messageType + ": " + targetSink);

        if (!DEFAULT_SINK.equals(targetSink)) {
            despatchMessage(source, date, messageType, DEFAULT_SINK, args);
        }
    }

    /**
     * Loads the default message sinks into this manager.
     */
    public void loadDefaultSinks() {
        addSink(new AllMessageSink());
        addSink(new ChannelMessageSink());
        addSink(new CustomWindowMessageSink());
        addSink(new ForkMessageSink());
        addSink(new FormatMessageSink());
        addSink(new GroupMessageSink());
        addSink(new LastCommandMessageSink());
        addSink(new NullMessageSink());
        addSink(new SelfMessageSink());
        addSink(new ServerMessageSink());
        addSink(new StatusBarMessageSink());
    }

    /**
     * Retrieves a singleton instance of the sink manager.
     *
     * @return A singleton manager instance
     */
    public static MessageSinkManager getManager() {
        return INSTANCE;
    }

}
