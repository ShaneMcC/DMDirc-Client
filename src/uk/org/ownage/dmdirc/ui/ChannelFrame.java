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

package uk.org.ownage.dmdirc.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 * @author  chris
 */
public class ChannelFrame extends JInternalFrame implements CommandWindow {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /**
     * The InputHandler for our input field.
     */
    private InputHandler inputHandler;
    /**
     * The channel object that owns this frame.
     */
    private Channel parent;
    /**
     * The nick list model used for this channel's nickname list.
     */
    private NicklistListModel nicklistModel;
    /**
     * The border used when the frame is not maximised.
     */
    private Border myborder;
    /**
     * The dimensions of the titlebar of the frame.
     **/
    private Dimension titlebarSize;
    /**
     * whether to auto scroll the textarea when adding text.
     */
    private boolean autoScroll = true;
    /**
     * holds the scrollbar for the frame.
     */
    private JScrollBar scrollBar;
    /**
     * This channel's command parser.
     */
    private ChannelCommandParser commandParser;
    
    /** Nick list. */
    private JList jList1;
    
    /** scrollpane. */
    private JScrollPane jScrollPane1;
    
    /** scrollpane. */
    private JScrollPane jScrollPane2;
    
    /** input text field. */
    private JTextField jTextField1;
    
    /** text pane. */
    private JTextPane jTextPane1;
    
    /** split pane. */
    private JSplitPane splitPane;
    
    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     * @param owner The Channel object that owns this frame
     */
    public ChannelFrame(final Channel owner) {
        parent = owner;
        
        setFrameIcon(MainFrame.getMainFrame().getIcon());
        
        nicklistModel = new NicklistListModel();
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        
        jTextPane1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        jTextPane1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        jTextField1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        jTextField1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        jTextField1.setCaretColor(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        jList1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        jList1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        
        commandParser = new ChannelCommandParser(parent.getServer(), parent);
        
        inputHandler = new InputHandler(jTextField1, commandParser, this);
        
        scrollBar = jScrollPane1.getVerticalScrollBar();
        
        addPropertyChangeListener("maximum", new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
                    ChannelFrame.this.myborder = getBorder();
                    ChannelFrame.this.titlebarSize =
                            ((BasicInternalFrameUI) getUI())
                            .getNorthPane().getPreferredSize();
                    
                    ((BasicInternalFrameUI) getUI()).getNorthPane()
                    .setPreferredSize(new Dimension(0, 0));
                    setBorder(new EmptyBorder(0, 0, 0, 0));
                    
                    MainFrame.getMainFrame().setMaximised(true);
                } else {
                    autoScroll = (scrollBar.getValue() + scrollBar.getVisibleAmount())
                    != scrollBar.getMaximum();
                    if (autoScroll) {
                        jTextPane1.setCaretPosition(jTextPane1.getStyledDocument().getLength());
                    }
                    
                    setBorder(ChannelFrame.this.myborder);
                    ((BasicInternalFrameUI) getUI()).getNorthPane()
                    .setPreferredSize(ChannelFrame.this.titlebarSize);
                    
                    MainFrame.getMainFrame().setMaximised(false);
                    MainFrame.getMainFrame().setActiveFrame(ChannelFrame.this);
                }
            }
        });
        
        addInternalFrameListener(new InternalFrameListener() {
            public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
                jTextField1.requestFocus();
            }
            public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
            }
        });
    }
    
    /**
     * Makes this frame visible. We don't call this from the constructor
     * so that we can register an actionlistener for the open event before
     * the frame is opened.
     */
    public final void open() {
        setVisible(true);
    }
    
    /**
     * Clears the main text area of the command window.
     */
    public final void clear() {
        jTextPane1.setText("");
    }
    
    /**
     * Sets the tab completer for this frame's input handler.
     * @param tabCompleter The tab completer to use
     */
    public final void setTabCompleter(final TabCompleter tabCompleter) {
        inputHandler.setTabCompleter(tabCompleter);
    }
    
    /**
     * Adds a line of text to the main text area.
     * @param line text to add
     */
    public final void addLine(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : line.split("\n")) {
                    String ts = Formatter.formatMessage("timestamp", new Date());
                    if (!jTextPane1.getText().equals("")) { ts = "\n" + ts; }
                    Styliser.addStyledString(jTextPane1.getStyledDocument(), ts);
                    Styliser.addStyledString(jTextPane1.getStyledDocument(), myLine);
                }
                
                if (scrollBar.getValue() + Math.round(scrollBar.getVisibleAmount() * 1.5) < scrollBar.getMaximum()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        private Rectangle prevRect = jTextPane1.getVisibleRect();
                        public void run() {
                            jTextPane1.scrollRectToVisible(prevRect);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jTextPane1.setCaretPosition(jTextPane1.getDocument().getLength());
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    public final void addLine(final String messageType, final Object... args) {
        addLine(Formatter.formatMessage(messageType, args));
    }
    
    /**
     * Updates the list of clients on this channel.
     * @param newNames The new list of clients
     */
    public final void updateNames(final ArrayList<ChannelClientInfo> newNames) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.replace(newNames);
            }
        });
    }
    
    /**
     * Has the nick list update, to take into account mode changes.
     */
    public final void updateNames() {
        nicklistModel.sort();
    }
    
    /**
     * Adds a client to this channels' nicklist.
     * @param newName the new client to be added
     */
    public final void addName(final ChannelClientInfo newName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.add(newName);
            }
        });
    }
    
    /**
     * Removes a client from this channels' nicklist.
     * @param name the client to be deleted
     */
    public final void removeName(final ChannelClientInfo name) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.remove(name);
            }
        });
    }
    
    /**
     * Initialises the compoents in this frame.
     */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        jScrollPane1 = new JScrollPane();
        jTextPane1 = new JTextPane();
        jTextField1 = new JTextField();
        jScrollPane2 = new JScrollPane();
        jList1 = new JList();
        
        splitPane.setBorder(null);
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
        if (divider != null){
            divider.setBorder(null);
        }
        
        jScrollPane1.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);
        
        jList1.setFont(new Font("Dialog", 0, 12));
        jList1.setModel(nicklistModel);
        jScrollPane2.setViewportView(jList1);
        
        getContentPane().setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(splitPane, constraints);
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        getContentPane().add(jTextField1, constraints);
        
        splitPane.setLeftComponent(jScrollPane1);
        splitPane.setRightComponent(jScrollPane2);
        
        splitPane.setDividerLocation(465);
        splitPane.setResizeWeight(1);
        splitPane.setDividerSize(5);
        
        pack();
    }
    
}
