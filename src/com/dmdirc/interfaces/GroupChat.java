/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.interfaces;

import com.dmdirc.Topic;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A chat containing multiple participants.
 */
public interface GroupChat {

    /**
     * Adds a nicklist listener to this channel.
     *
     * @param listener The listener to notify about nicklist changes.
     */
    void addNicklistListener(final NicklistListener listener);

    /**
     * Adds a topic change listener to this channel.
     *
     * @param listener The listener to notify about topic changes.
     */
    void addTopicChangeListener(final TopicChangeListener listener);

    /**
     * Gets the connection that this chat is happening on.
     *
     * @return This chat's connection.
     */
    @Nonnull
    Connection getConnection();

    /**
     * Returns the current topic for this channel.
     *
     * @return Current channel topic
     */
    Optional<Topic> getCurrentTopic();

    /**
     * Gets an event bus which will only contain events generated in relation to this channel.
     *
     * @return An event bus scoped to this channel.
     */
    EventBus getEventBus();

    /**
     * Gets the maximum length of a line that may be sent to this chat.
     *
     * @return The maximum line length that may be sent.
     */
    int getMaxLineLength();

    /**
     * Retrieves the maximum length that a topic on this channel can be.
     *
     * @return The maximum length that this channel's topic may be
     */
    int getMaxTopicLength();

    /**
     * Retrieve the topics that have been seen on this channel.
     *
     * @return A list of topics that have been seen on this channel, including the current one.
     */
    List<Topic> getTopics();

    /**
     * Determines if we are currently joined to the chat.
     *
     * @return True if joined, false otherwise.
     */
    boolean isOnChannel();

    /**
     * Joins the specified channel. This only makes sense if used after a call to part().
     */
    void join();

    /**
     * Parts this channel with the specified message. Parting does NOT close the channel window.
     *
     * @param reason The reason for parting the channel
     */
    void part(final String reason);

    /**
     * Removes a nicklist listener from this channel.
     *
     * @param listener The listener to be removed.
     */
    void removeNicklistListener(final NicklistListener listener);

    /**
     * Removes a topic change listener from this channel.
     *
     * @param listener The listener to be removed.
     */
    void removeTopicChangeListener(final TopicChangeListener listener);

    /**
     * Requests all available list modes for this channel.
     */
    void retrieveListModes();

    /**
     * Sends an action to the chat. If an action is too long to be sent, an error will be displayed.
     *
     * @param action The action to be sent.
     */
    void sendAction(final String action);

    /**
     * Sends a line of text to the chat. If a line is too long to be sent, it will be split and sent
     * as multiple lines.
     *
     * @param line The line to be sent.
     */
    void sendLine(final String line);

    /**
     * Attempts to set the topic of this channel.
     *
     * @param topic The new topic to be used. An empty string will clear the current topic
     */
    void setTopic(final String topic);

}