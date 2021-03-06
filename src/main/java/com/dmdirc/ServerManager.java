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

package com.dmdirc;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.WindowManager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engio.mbassy.listener.Handler;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * The ServerManager maintains a list of all servers, and provides methods to search or iterate over
 * them.
 */
@Singleton
public class ServerManager implements ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServerManager.class);
    /** All servers that currently exist. */
    private final Set<Connection> servers = new CopyOnWriteArraySet<>();
    /** The manager to use to find profiles. */
    private final ProfileManager profileManager;
    /** The identity factory to give to servers. */
    private final IdentityFactory identityFactory;
    /** Window manager to add new servers to. */
    private final WindowManager windowManager;
    /** Concrete server factory to use. */
    private final ServerFactoryImpl serverFactoryImpl;
    /** Event bus for servers. */
    private final EventBus eventBus;

    /**
     * Creates a new instance of ServerManager.
     *
     * @param profileManager     The manager to use to find profiles.
     * @param identityFactory    The factory to use to create new identities.
     * @param windowManager      Window manager to add new servers to.
     * @param serverFactory      The factory to use to create servers.
     * @param eventBus           The event bus to pass to servers.
     */
    @Inject
    public ServerManager(
            final ProfileManager profileManager,
            final IdentityFactory identityFactory,
            final WindowManager windowManager,
            final ServerFactoryImpl serverFactory,
            final EventBus eventBus) {
        this.profileManager = profileManager;
        this.identityFactory = identityFactory;
        this.windowManager = windowManager;
        this.serverFactoryImpl = serverFactory;
        this.eventBus = eventBus;
        this.eventBus.subscribe(this);
    }

    @Override
    public Connection createServer(final URI uri, final Profile profile) {
        final ConfigProviderMigrator configProvider = identityFactory.createMigratableConfig(uri.
                getScheme(), "", "", uri.getHost());

        final Connection server = serverFactoryImpl.getServer(
                configProvider,
                Executors.newScheduledThreadPool(1,
                        new ThreadFactoryBuilder().setNameFormat("server-timer-%d").build()),
                uri,
                profile);
        registerServer(server);
        windowManager.addWindow(server.getWindowModel());
        return server;
    }

    /**
     * Registers a new server with the manager.
     *
     * @param server The server to be registered
     */
    void registerServer(final Connection server) {
        servers.add(server);
    }

    /**
     * Unregisters a server from the manager. The request is ignored if the ServerManager is in the
     * process of closing all servers.
     *
     * @param server The server to be unregistered
     */
    void unregisterServer(final Server server) {
        servers.remove(server);
    }

    @Override
    public List<Connection> getConnections() {
        return new ArrayList<>(servers);
    }

    @Override
    public void disconnectAll(final String message) {
        for (Connection server : servers) {
            server.disconnect(message);
        }
    }

    @Override
    public void closeAll(final String message) {
        for (Connection server : servers) {
            server.disconnect(message);
            server.getWindowModel().close();
        }
    }

    @Override
    public int getConnectionCount() {
        return servers.size();
    }

    @Override
    public List<Connection> getConnectionsByNetwork(final String network) {
        return servers.stream()
                .filter(server -> server.isNetwork(network))
                .collect(Collectors.toList());
    }

    @Override
    public Connection connectToAddress(final URI uri) {
        return connectToAddress(uri, profileManager.getDefault());
    }

    @Override
    public Connection connectToAddress(final URI uri, final Profile profile) {
        final Connection server = servers.stream()
                .filter(s -> s.compareURI(uri)).findAny()
                .orElse(createServer(uri, profile));

        final Optional<Parser> parser = server.getParser();
        if (server.getState().isDisconnected() || !parser.isPresent()) {
            server.connect(uri, profile);
        } else {
            final Collection<? extends ChannelJoinRequest> joinRequests =
                    parser.get().extractChannels(uri);
            server.getGroupChatManager()
                    .join(joinRequests.toArray(new ChannelJoinRequest[joinRequests.size()]));
        }

        return server;
    }

    @Override
    public void joinDevChat() {
        final List<Connection> qnetServers = getConnectionsByNetwork("Quakenet");

        Connection connectedServer = null;

        for (Connection server : qnetServers) {
            if (server.getState() == ServerState.CONNECTED) {
                connectedServer = server;

                if (server.getGroupChatManager().getChannel("#DMDirc").isPresent()) {
                    server.getGroupChatManager().join(new ChannelJoinRequest("#DMDirc"));
                    return;
                }
            }
        }

        if (connectedServer == null) {
            try {
                connectToAddress(new URI("irc://irc.quakenet.org/DMDirc"));
            } catch (URISyntaxException ex) {
                LOG.warn(USER_ERROR, "Unable to construct new server", ex);
            }
        } else {
            connectedServer.getGroupChatManager().join(new ChannelJoinRequest("#DMDirc"));
        }
    }

    @Handler
    void handleWindowClosing(final FrameClosingEvent event) {
        if (event.getSource() instanceof Server) {
            unregisterServer((Server) event.getSource());
        }
    }

}
