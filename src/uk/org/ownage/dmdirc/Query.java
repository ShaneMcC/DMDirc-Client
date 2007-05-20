/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc;

import java.awt.Color;
import java.net.URL;

import javax.swing.ImageIcon;

import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.actions.CoreActionType;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ClientInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.INickChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.QueryFrame;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 * The Query class represents the client's view of a query with another user.
 * It handles callbacks for query events from the parser, maintains the
 * corresponding ServerFrame, and handles user input to a ServerFrame.
 * @author chris
 */
public final class Query extends FrameContainer implements IPrivateAction,
        IPrivateMessage, INickChanged {
    
    /**
     * The Server this Query is on.
     */
    private Server server;
    
    /**
     * The ServerFrame used for this Query.
     */
    private QueryFrame frame;
    
    /**
     * The full host of the client associated with this Query.
     */
    private String host;
    
    /**
     * The icon being used for this query.
     */
    private final ImageIcon imageIcon;
    
    /**
     * The tab completer for this frame.
     */
    private final TabCompleter tabCompleter;
    
    /**
     * Creates a new instance of Query.
     * @param newHost host of the remove client
     * @param newServer The server object that this Query belongs to
     */
    public Query(final Server newServer, final String newHost) {
        this.server = newServer;
        this.host = newHost;
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/query.png");
        imageIcon = new ImageIcon(imageURL);
        
        frame = new QueryFrame(this);
        
        ActionManager.processEvent(CoreActionType.QUERY_OPENED, null, this);
        
        MainFrame.getMainFrame().addChild(frame);
        frame.addInternalFrameListener(this);
        frame.setFrameIcon(imageIcon);
        
        if (!Config.getOptionBool("general", "hidequeries")) {
            frame.open();
        }
        
        tabCompleter = new TabCompleter(server.getTabCompleter());
        tabCompleter.addEntries(CommandManager.getQueryCommandNames());
        frame.setTabCompleter(tabCompleter);
        
        try {
            server.getParser().getCallbackManager().addCallback("onPrivateAction", this, ClientInfo.parseHost(host));
            server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, ClientInfo.parseHost(host));
            server.getParser().getCallbackManager().addCallback("onNickChanged", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to get query events", ex);
        }
        
        updateTitle();
    }
    
    /**
     * Shows this query's frame.
     */
    public void show() {
        frame.open();
    }
    
    /**
     * Returns the internal frame belonging to this object.
     * @return This object's internal frame
     */
    public CommandWindow getFrame() {
        return frame;
    }
    
    /**
     * Returns the tab completer for this query.
     * @return This query's tab completer
     */
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }
    
    /**
     * Sends a private message to the remote user.
     * @param line message text to send
     */
    public void sendLine(final String line) {
        final ClientInfo client = server.getParser().getMyself();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", host);
        
        if (maxLineLength >= line.length()) {
            server.getParser().sendMessage(ClientInfo.parseHost(host), line);
            
            final StringBuffer buff = new StringBuffer("querySelfMessage");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_MESSAGE, buff, this, line);
            
            frame.addLine(buff, client.getNickname(), client.getIdent(), client.getHost(), line);
        } else {
            sendLine(line.substring(0, maxLineLength));
            sendLine(line.substring(maxLineLength));
        }
    }
    
    /**
     * Sends a private action to the remote user.
     * @param action action text to send
     */
    public void sendAction(final String action) {
        final ClientInfo client = server.getParser().getMyself();
        final int maxLineLength = server.getParser().getMaxLength("PRIVMSG", host);
        
        if (maxLineLength >= action.length() + 2) {
            server.getParser().sendAction(ClientInfo.parseHost(host), action);
            
            final StringBuffer buff = new StringBuffer("querySelfAction");
            
            ActionManager.processEvent(CoreActionType.QUERY_SELF_ACTION, buff, this, action);
            
            frame.addLine(buff, client.getNickname(), client.getIdent(), client.getHost(), action);
        } else {
            frame.addLine("actionTooLong", action.length());
        }
    }
    
    /**
     * Handles a private message event from the parser.
     * @param parser Parser receiving the event
     * @param message message received
     * @param remoteHost remote user host
     */
    public void onPrivateMessage(final IRCParser parser, final String message,
            final String remoteHost) {
        final String[] parts = ClientInfo.parseHostFull(remoteHost);
        
        final StringBuffer buff = new StringBuffer("queryMessage");
        
        ActionManager.processEvent(CoreActionType.QUERY_MESSAGE, buff, this, message);
        
        frame.addLine(buff, parts[0], parts[1], parts[2], message);
        
        sendNotification();
    }
    
    /**
     * Handles a private action event from the parser.
     * @param parser Parser receiving the event
     * @param message message received
     * @param remoteHost remote host
     */
    public void onPrivateAction(final IRCParser parser, final String message,
            final String remoteHost) {
        final String[] parts = ClientInfo.parseHostFull(host);
        
        final StringBuffer buff = new StringBuffer("queryAction");
        
        ActionManager.processEvent(CoreActionType.QUERY_ACTION, buff, this, message);
        
        frame.addLine(buff, parts[0], parts[1], parts[2], message);
        
        sendNotification();
    }
    
    /**
     * Updates the QueryFrame title.
     */
    private void updateTitle() {
        final String title = ClientInfo.parseHost(host);
        
        frame.setTitle(title);
        
        if (frame.isMaximum() && frame.equals(MainFrame.getMainFrame().getActiveFrame())) {
            MainFrame.getMainFrame().setTitle(MainFrame.getMainFrame().getTitlePrefix() + " - " + title);
        }
    }
    
    /**
     * Handles nick change events from the parser.
     * @param parser Parser receiving the event
     * @param client remote client changing nick
     * @param oldNick clients old nickname
     */
    public void onNickChanged(final IRCParser parser, final ClientInfo client,
            final String oldNick) {
        if (oldNick.equals(ClientInfo.parseHost(host))) {
            server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
            server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
            try {
                server.getParser().getCallbackManager().addCallback("onPrivateAction", this, client.getNickname());
                server.getParser().getCallbackManager().addCallback("onPrivateMessage", this, client.getNickname());
            } catch (CallbackNotFound ex) {
                Logger.error(ErrorLevel.FATAL, "Unable to get query events", ex);
            }
            frame.addLine("queryNickChanged", oldNick, client.getIdent(), client.getHost(), client.getNickname());
            host = client.getNickname() + "!" + client.getIdent() + "@" + client.getHost();
            sendNotification();
            updateTitle();
        }
    }
    
    /**
     * Returns the Server assocaited with this query.
     * @return asscoaited Server
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Closes the query and associated frame.
     */
    public void close() {
        server.getParser().getCallbackManager().delCallback("onPrivateAction", this);
        server.getParser().getCallbackManager().delCallback("onPrivateMessage", this);
        server.getParser().getCallbackManager().delCallback("onNickChanged", this);
        
        ActionManager.processEvent(CoreActionType.QUERY_CLOSED, null, this);
        
        frame.setVisible(false);
        server.delQuery(host);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    /**
     * Returns this query's name.
     * @return A string representation of this query (i.e., the user's name)
     */
    public String toString() {
        return ClientInfo.parseHost(host);
    }
    
    /**
     * Returns the host that this query is with.
     * @return The full host that this query is with
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        if (!frame.isVisible()) {
            show();
        }
        
        MainFrame.getMainFrame().setActiveFrame(frame);
    }
    
    /**
     * Formats the specified arguments using the supplied message type, and
     * outputs to the main text area.
     * @param messageType the message type to use
     * @param args the arguments to pass
     */
    public void addLine(final String messageType, final Object... args) {
        frame.addLine(messageType, args);
    }
    
    /**
     * Retrieves the icon used by the query frame.
     * @return The query frame's icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Sends a notification to the frame manager if this frame isn't active.
     */
    public void sendNotification() {
        if (!MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
            final Color colour = ColourManager.getColour(4);
            MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
        }
    }
    
    /**
     * Returns this query's config manager.
     * @return This query's config manager
     */
    public ConfigManager getConfigManager() {
        return server.getConfigManager();
    }
}
