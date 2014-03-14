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

package com.dmdirc;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.ChannelUserBackEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import com.dmdirc.parser.interfaces.callbacks.ChannelActionListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelKickListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelListModeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNonUserModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelPartListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelQuitListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelUserModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.OtherAwayStateListener;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Handles events for channel objects.
 */
public class ChannelEventHandler extends EventHandler implements
        ChannelMessageListener, ChannelNamesListener, ChannelTopicListener,
        ChannelJoinListener, ChannelPartListener, ChannelKickListener,
        ChannelQuitListener, ChannelActionListener, ChannelNickChangeListener,
        ChannelModeChangeListener, ChannelUserModeChangeListener,
        ChannelCtcpListener, OtherAwayStateListener, ChannelNoticeListener,
        ChannelNonUserModeChangeListener, ChannelModeNoticeListener,
        ChannelListModeListener {

    /** The channel that owns this event handler. */
    private final Channel owner;
    /** Event bus to send events on. */
    private final EventBus eventBus;

    public ChannelEventHandler(final Channel owner, final EventBus eventBus) {
        this.owner = owner;
        this.eventBus = eventBus;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends CallbackInterface> void addCallback(final CallbackManager cbm,
            final Class<T> type) {
        if (OtherAwayStateListener.class.equals(type)) {
            cbm.addCallback(type, (T) this);
        } else {
            cbm.addCallback(type, (T) this, owner.getChannelInfo().getName());
        }
    }

    @Override
    protected Connection getConnection() {
        return owner.getConnection();
    }

    /**
     * Determines if the specified client represents us.
     *
     * @param client The client to be tested
     *
     * @return True if the client is ourself, false otherwise.
     */
    protected boolean isMyself(final ChannelClientInfo client) {
        return client.getClient().equals(owner.getConnection().getParser().getLocalClient());
    }

    @Override
    public void onChannelMessage(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                isMyself(client) ? "channelSelfExternalMessage" : "channelMessage");
        triggerAction(messageType, CoreActionType.CHANNEL_MESSAGE, client, message);
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_MESSAGE,
                client, message);
    }

    @Override
    public void onChannelGotNames(final Parser parser, final Date date, final ChannelInfo channel) {
        checkParser(parser);

        owner.setClients(channel.getChannelClients());
        ActionManager.getActionManager().triggerEvent(CoreActionType.CHANNEL_GOTNAMES, null, owner);
    }

    @Override
    public void onChannelTopic(final Parser parser, final Date date,
            final ChannelInfo channel, final boolean isJoinTopic) {
        checkParser(parser);

        final Topic newTopic = new Topic(channel.getTopic(),
                channel.getTopicSetter(), channel.getTopicTime());

        if (isJoinTopic) {
            if (newTopic.getTopic().isEmpty()) {
                final StringBuffer messageType = new StringBuffer("channelNoTopic");
                triggerAction(messageType, CoreActionType.CHANNEL_NOTOPIC);
                owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_NOTOPIC);
            } else {
                final StringBuffer messageType = new StringBuffer("channelTopicDiscovered");
                triggerAction(messageType, CoreActionType.CHANNEL_GOTTOPIC, newTopic);
                owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_GOTTOPIC,
                        newTopic);
            }
        } else {
            final StringBuffer messageType = new StringBuffer(
                    channel.getTopic().isEmpty() ? "channelTopicRemoved" : "channelTopicChanged");
            triggerAction(messageType, CoreActionType.CHANNEL_TOPICCHANGE,
                    channel.getChannelClient(channel.getTopicSetter(), true), channel.getTopic());
            owner.doNotification(date, messageType.toString(),
                    CoreActionType.CHANNEL_TOPICCHANGE,
                    channel.getChannelClient(channel.getTopicSetter(), true), channel.getTopic());
        }

        if (!isJoinTopic
                || (owner.getCurrentTopic() == null && !newTopic.getTopic().isEmpty())
                || (owner.getCurrentTopic() != null
                && !newTopic.getTopic().equals(owner.getCurrentTopic().getTopic()))) {
            // Only add the topic if it's being changed when we're on the
            // channel (i.e., not a "joinTopic"), or if it's different to the
            // one we're expecting
            owner.addTopic(newTopic);
        }
    }

    @Override
    public void onChannelJoin(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer("channelJoin");
        triggerAction(messageType, CoreActionType.CHANNEL_JOIN, client);
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_JOIN, client);
        owner.addClient(client);
    }

    @Override
    public void onChannelPart(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                "channel"
                + (isMyself(client) ? "Self" : "") + "Part"
                + (reason.isEmpty() ? "" : "Reason"));

        triggerAction(messageType, CoreActionType.CHANNEL_PART, client, reason);
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_PART, client,
                reason);
        owner.removeClient(client);
    }

    @Override
    public void onChannelKick(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo kickedClient, final ChannelClientInfo client,
            final String reason, final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                "channelKick" + (reason.isEmpty() ? "" : "Reason"));
        triggerAction(messageType,
                CoreActionType.CHANNEL_KICK, client, kickedClient, reason);
        owner.doNotification(date, messageType.toString(),
                CoreActionType.CHANNEL_KICK, client, kickedClient, reason);
        owner.removeClient(kickedClient);
    }

    @Override
    public void onChannelQuit(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String reason) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                "channelQuit" + (reason.isEmpty() ? "" : "Reason"));
        triggerAction(messageType, CoreActionType.CHANNEL_QUIT, client, reason);
        owner.doNotification(date, messageType.toString(),
                CoreActionType.CHANNEL_QUIT, client, reason);
        owner.removeClient(client);
    }

    @Override
    public void onChannelAction(final Parser parser, final Date date, final ChannelInfo channel,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                isMyself(client) ? "channelSelfExternalAction" : "channelAction");
        triggerAction(messageType, CoreActionType.CHANNEL_ACTION, client, message);
        owner.doNotification(date, messageType.toString(),
                CoreActionType.CHANNEL_ACTION, client, message);
    }

    @Override
    public void onChannelNickChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String oldNick) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer(
                isMyself(client) ? "channelSelfNickChange" : "channelNickChange");

        triggerAction(messageType, CoreActionType.CHANNEL_NICKCHANGE, client, oldNick);
        owner.doNotification(date, messageType.toString(),
                CoreActionType.CHANNEL_NICKCHANGE, client, oldNick);
        owner.renameClient(oldNick, client.getClient().getNickname());
    }

    @Override
    public void onChannelModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client, final String host,
            final String modes) {
        checkParser(parser);

        if (!owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                || !owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                final StringBuffer messageType = new StringBuffer(
                        modes.length() <= 1 ? "channelNoModes" : "channelModeDiscovered");
                triggerAction(messageType, CoreActionType.CHANNEL_MODESDISCOVERED,
                        modes.length() <= 1 ? "" : modes);
                owner.doNotification(date, messageType.toString(),
                        CoreActionType.CHANNEL_MODESDISCOVERED, modes.length() <= 1 ? "" : modes);
            } else {
                final StringBuffer messageType = new StringBuffer(
                        isMyself(client) ? "channelSelfModeChanged" : "channelModeChanged");
                triggerAction(messageType, CoreActionType.CHANNEL_MODECHANGE, client, modes);
                owner.doNotification(date, messageType.toString(),
                        CoreActionType.CHANNEL_MODECHANGE, client, modes);
            }
        }

        owner.refreshClients();
    }

    @Override
    public void onChannelUserModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo targetClient,
            final ChannelClientInfo client, final String host, final String mode) {
        checkParser(parser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")) {
            String format = "channelSplitUserMode_" + mode;

            if (!owner.getConfigManager().hasOptionString("formatter", format)) {
                format = "channelSplitUserMode_default";
            }

            final StringBuffer messageType = new StringBuffer(format);
            triggerAction(messageType, CoreActionType.CHANNEL_USERMODECHANGE, client,
                    targetClient, mode);
            owner.doNotification(date, messageType.toString(),
                    CoreActionType.CHANNEL_USERMODECHANGE,
                    client, targetClient, mode);
        }
    }

    @Override
    public void onChannelCTCP(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String type, final String message, final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer("channelCTCP");
        if (triggerAction(messageType, CoreActionType.CHANNEL_CTCP, client, type, message)) {
            owner.getConnection().sendCTCPReply(client.getClient().getNickname(),
                    type, message);
        }
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_CTCP,
                client, type, message);
    }

    @Override
    public void onAwayStateOther(final Parser parser, final Date date,
            final ClientInfo client, final AwayState oldState, final AwayState state) {
        checkParser(parser);

        final ChannelClientInfo channelClient = owner.getChannelInfo().getChannelClient(client);

        if (channelClient != null) {
            final boolean away = state == AwayState.AWAY;
            final boolean discovered = oldState == AwayState.UNKNOWN;

            // TODO: The events should take a formatter.
            owner.doNotification(date, (away ? "channelUserAway" : "channelUserBack")
                    + (discovered ? "Discovered" : ""),
                    away ? CoreActionType.CHANNEL_USERAWAY : CoreActionType.CHANNEL_USERBACK,
                    channelClient);
            if (away) {
                eventBus.post(new ChannelUserAwayEvent(owner, channelClient));
            } else {
                eventBus.post(new ChannelUserBackEvent(owner, channelClient));
            }
        }
    }

    @Override
    public void onChannelNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String message, final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer("channelNotice");
        triggerAction(messageType, CoreActionType.CHANNEL_NOTICE, client, message);
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_NOTICE,
                client, message);
    }

    @Override
    public void onChannelNonUserModeChanged(final Parser parser, final Date date,
            final ChannelInfo channel, final ChannelClientInfo client,
            final String host, final String modes) {
        checkParser(parser);

        if (owner.getConfigManager().getOptionBool("channel", "splitusermodes")
                && owner.getConfigManager().getOptionBool("channel", "hideduplicatemodes")) {
            if (host.isEmpty()) {
                final StringBuffer messageType = new StringBuffer(
                        modes.length() <= 1 ? "channelNoModes" : "channelModeDiscovered");
                triggerAction(messageType, CoreActionType.CHANNEL_MODESDISCOVERED,
                        modes.length() <= 1 ? "" : modes);
                owner.doNotification(date, messageType.toString(),
                        CoreActionType.CHANNEL_MODESDISCOVERED,
                        modes.length() <= 1 ? "" : modes);
            } else {
                final StringBuffer messageType = new StringBuffer(
                        isMyself(client) ? "channelSelfModeChanged" : "channelModeChanged");
                triggerAction(messageType, CoreActionType.CHANNEL_MODECHANGE, client, modes);
                owner.doNotification(date, messageType.toString(),
                        CoreActionType.CHANNEL_MODECHANGE, client, modes);
            }
        }

        owner.refreshClients();
    }

    @Override
    public void onChannelModeNotice(final Parser parser, final Date date,
            final ChannelInfo channel, final char prefix,
            final ChannelClientInfo client, final String message,
            final String host) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer("channelModeNotice");
        triggerAction(messageType, CoreActionType.CHANNEL_MODE_NOTICE, client, String.
                valueOf(prefix), message);
        owner.doNotification(date, messageType.toString(), CoreActionType.CHANNEL_MODE_NOTICE,
                client, String.valueOf(prefix), message);
    }

    @Override
    public void onChannelGotListModes(final Parser parser, final Date date,
            final ChannelInfo channel, final char mode) {
        checkParser(parser);

        final StringBuffer messageType = new StringBuffer("channelListModeRetrieved");
        triggerAction(messageType, CoreActionType.CHANNEL_LISTMODERETRIEVED,
                Character.valueOf(mode));
        owner.doNotification(date, messageType.toString(),
                CoreActionType.CHANNEL_LISTMODERETRIEVED, Character.valueOf(mode));
    }

    private boolean triggerAction(final StringBuffer messageType, final ActionType actionType,
            final Object... args) {
        final List<Object> actionArgs = new ArrayList<>();
        actionArgs.add(owner);
        actionArgs.addAll(Arrays.asList(args));
        return ActionManager.getActionManager().triggerEvent(actionType, messageType,
                actionArgs.toArray());
    }

}
