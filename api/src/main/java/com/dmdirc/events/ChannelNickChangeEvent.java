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

import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;

import java.time.LocalDateTime;

/**
 * Fired when a user changes nickname in a channel.
 */
public class ChannelNickChangeEvent extends ChannelDisplayableEvent {

    private final GroupChatUser client;
    private final String oldNick;

    public ChannelNickChangeEvent(final LocalDateTime timestamp, final GroupChat channel,
            final GroupChatUser client, final String oldNick) {
        super(timestamp, channel);
        this.client = client;
        this.oldNick = oldNick;
    }

    public ChannelNickChangeEvent(final GroupChat channel, final GroupChatUser client,
            final String oldNick) {
        super(channel);
        this.client = client;
        this.oldNick = oldNick;
    }

    public GroupChatUser getClient() {
        return client;
    }

    public String getOldNick() {
        return oldNick;
    }

}
