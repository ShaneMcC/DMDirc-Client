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

package com.dmdirc.ui.input;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.tabstyles.BashStyle;
import com.dmdirc.ui.input.tabstyles.MircStyle;
import com.dmdirc.ui.input.tabstyles.TabCompletionResult;
import com.dmdirc.ui.input.tabstyles.TabCompletionStyle;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.swing.components.ColourPickerDialog;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

/**
 * Handles events generated by a user typing into a textfield. Allows the user
 * to use shortcut keys for control characters (ctrl+b, etc), to tab complete
 * nicknames/channel names/etc, and to scroll through their previously issued
 * commands.
 *
 * @author chris
 */
public final class InputHandler implements KeyListener, ActionListener {
    
    /**
     * Indicates that the caret should be moved to the end of a selection when
     * a control code has been inserted.
     */
    private static final int POSITION_END = 1;
    
    /**
     * Indicates that the caret should be moved to the start of a selection when
     * a control code has been inserted.
     */
    private static final int POSITION_START = 2;
    
    /**
     * The current position in the buffer (where the user has scrolled back
     * to).
     */
    private int bufferPosition;
    
    /**
     * The maximum position we've got to in the buffer. This will be the
     * position that is inserted to next. Note that it will wrap around once
     * we hit the maximum size.
     */
    private int bufferMaximum;
    
    /** The maximum size of the buffer. */
    private final int bufferSize;
    
    /** The lowest entry we've used in the buffer. */
    private int bufferMinimum;
    
    /** The buffer itself. */
    private String[] buffer;
    
    /** The textfield that we're handling input for. */
    private final JTextField target;
    
    /** The TabCompleter to use for tab completion. */
    private TabCompleter tabCompleter;
    
    /** The CommandParser to use for our input. */
    private final CommandParser commandParser;
    
    /** The frame that we belong to. */
    private final InputWindow parentWindow;
    
    /** Colour picker dialog. */
    private ColourPickerDialog colourPicker;
    
    /** The tab completion style. */
    private TabCompletionStyle style;
    
    /**
     * Creates a new instance of InputHandler. Adds listeners to the target
     * that we need to operate.
     *
     * @param thisTarget The text field this input handler is dealing with.
     * @param thisCommandParser The command parser to use for this text field.
     * @param thisParentWindow The window that owns this input handler
     */
    public InputHandler(final JTextField thisTarget,
            final CommandParser thisCommandParser,
            final InputWindow thisParentWindow) {
        
        bufferSize = thisParentWindow.getConfigManager().getOptionInt("ui", "inputbuffersize", 50);
        
        this.commandParser = thisCommandParser;
        this.parentWindow = thisParentWindow;
        this.target = thisTarget;
        this.buffer = new String[bufferSize];
        bufferPosition = 0;
        bufferMinimum = 0;
        bufferMaximum = 0;
        
        if ("bash".equals(parentWindow.getConfigManager().getOption("tabcompletion", "style", "bash"))) {
            style = new BashStyle();
        } else {
            style = new MircStyle();
        }
        
        style.setContext(tabCompleter, parentWindow);
        
        target.addKeyListener(this);
        target.addActionListener(this);
        target.setFocusTraversalKeysEnabled(false);
    }
    
    /**
     * Sets this input handler's tab completer.
     *
     * @param newTabCompleter The new tab completer
     */
    public void setTabCompleter(final TabCompleter newTabCompleter) {
        tabCompleter = newTabCompleter;
        style.setContext(tabCompleter, parentWindow);
    }
    
    /**
     * Called when the user types a normal character.
     *
     * @param keyEvent The event that was fired
     */
    public void keyTyped(final KeyEvent keyEvent) {
        //Ignore.
    }
    
    /**
     * Called when the user presses down any key. Handles the insertion of
     * control codes, tab completion, and scrolling the back buffer.
     *
     * @param keyEvent The event that was fired
     */
    public void keyPressed(final KeyEvent keyEvent) {
        if (colourPicker != null) {
            colourPicker.dispose();
            colourPicker = null;
        }
        
        // Formatting codes
        if ((keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            handleControlKey(keyEvent);
        }
        
        // Back buffer scrolling
        if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
            doBufferUp();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            doBufferDown();
        }
        
        // Tab completion
        if (keyEvent.getKeyCode() == KeyEvent.VK_TAB  && tabCompleter != null
                && (keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != KeyEvent.CTRL_MASK) {
            doTabCompletion();
        }
    }
    
    /**
     * Handles the pressing of a key while the control key is pressed.
     * Inserts control chars as appropriate.
     *
     * @param keyEvent The key event that triggered this event.
     */
    private void handleControlKey(final KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
        case KeyEvent.VK_B:
            addControlCode(Styliser.CODE_BOLD, POSITION_END);
            break;
            
        case KeyEvent.VK_U:
            addControlCode(Styliser.CODE_UNDERLINE, POSITION_END);
            break;
            
        case KeyEvent.VK_O:
            addControlCode(Styliser.CODE_STOP, POSITION_END);
            break;
            
        case KeyEvent.VK_I:
            addControlCode(Styliser.CODE_ITALIC, POSITION_END);
            break;
            
        case KeyEvent.VK_F:
            if ((keyEvent.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
                addControlCode(Styliser.CODE_FIXED, POSITION_END);
            }
            break;
            
        case KeyEvent.VK_K:
            if ((keyEvent.getModifiers() & KeyEvent.SHIFT_MASK) == 0) {
                addControlCode(Styliser.CODE_COLOUR, POSITION_START);
                showColourPicker(true, false);
            } else {
                addControlCode(Styliser.CODE_HEXCOLOUR, POSITION_START);
                showColourPicker(false, true);
            }
            break;
            
        case KeyEvent.VK_ENTER:
            commandParser.parseCommandCtrl(parentWindow, target.getText());
            addToBuffer(target.getText());
            break;
            
        default:
            /* Do nothing. */
            break;
        }
    }
    
    /**
     * Calls when the user presses the up key.
     * Handles cycling through the input buffer.
     */
    private void doBufferUp() {
        if (bufferPosition == bufferMinimum) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            bufferPosition = normalise(bufferPosition - 1);
            retrieveBuffer();
        }
    }
    
    /**
     * Called when the user presses the down key.
     * Handles cycling through the input buffer, and storing incomplete lines.
     */
    private void doBufferDown() {
        if (bufferPosition != bufferMaximum) {
            bufferPosition = normalise(bufferPosition + 1);
            retrieveBuffer();
        } else if (target.getText().isEmpty()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            addToBuffer(target.getText());
        }
    }
    
    /**
     * Handles tab completion of a string. Called when the user presses tab.
     */
    private void doTabCompletion() {
        final String text = target.getText();
        
        if (text.isEmpty()) {
            doNormalTabCompletion(text, 0, 0, null);
            return;
        }
        
        final int pos = target.getCaretPosition() - 1;
        int start = (pos < 0) ? 0 : pos;
        int end = (pos < 0) ? 0 : pos;
        
        // Traverse backwards
        while (start > 0 && text.charAt(start) != ' ') {
            start--;
        }
        if (text.charAt(start) == ' ') { start++; }
        
        // And forwards
        while (end < text.length() && text.charAt(end) != ' ') {
            end++;
        }
        
        if (start > end) {
            end = start;
        }
        
        if (start > 0 && text.charAt(0) == CommandManager.getCommandChar()) {
            doCommandTabCompletion(text, start, end);
        } else {
            doNormalTabCompletion(text, start, end, null);
        }
    }
    
    /**
     * Handles potentially intelligent tab completion.
     *
     * @param text The text that is being completed
     * @param start The start index of the word we're completing
     * @param end The end index of the word we're completing
     */
    private void doCommandTabCompletion(final String text, final int start,
            final int end) {
        final String signature = text.substring(1, text.indexOf(' '));
        final Command command = CommandManager.getCommand(signature);
        
        if (command instanceof IntelligentCommand) {
            int args = 0;
            int lastArg = signature.length() + 2;
            final List<String> previousArgs = new ArrayList<String>();
            
            for (int i = lastArg; i < start; i++) {
                if (text.charAt(i) == ' ') {
                    args++;
                    previousArgs.add(text.substring(lastArg, i));
                    lastArg = i + 1;
                }
            }
            
            final AdditionalTabTargets results =
                    ((IntelligentCommand) command).getSuggestions(args, previousArgs);
            
            doNormalTabCompletion(text, start, end, results);
        } else {
            doNormalTabCompletion(text, start, end, null);
        }
    }
    
    /**
     * Handles normal (non-intelligent-command) tab completion.
     *
     * @param text The text that is being completed
     * @param start The start index of the word we're completing
     * @param end The end index of the word we're completing
     * @param additional A list of additional strings to use
     */
    private void doNormalTabCompletion(final String text, final int start,
            final int end, final AdditionalTabTargets additional) {
        final TabCompletionResult res = style.getResult(text, start, end, additional);
        
        if (res != null) {
            target.setText(res.getText());
            target.setCaretPosition(res.getPosition());
        }
    }
    
    /**
     * Called when the user releases any key.
     * @param keyEvent The event that was fired
     */
    public void keyReleased(final KeyEvent keyEvent) {
        //Ignore.
    }
    
    /**
     * Called when the user presses return in the text area. The line they
     * typed is added to the buffer for future use.
     * @param actionEvent The event that was fired
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        final String line = actionEvent.getActionCommand();
        
        if (!line.isEmpty()) {
            final StringBuffer thisBuffer = new StringBuffer(line);
            
            ActionManager.processEvent(CoreActionType.CLIENT_USER_INPUT, null,
                    parentWindow.getContainer(), thisBuffer);
            
            addToBuffer(thisBuffer.toString());
            
            commandParser.parseCommand(parentWindow, thisBuffer.toString());
        }
    }
    
    /**
     * Adds the specified control code to the textarea. If the user has a range
     * of text selected, the characters are added before and after, and the
     * caret is positioned based on the position argument.
     * @param code The control code to add
     * @param position The position of the caret after a selection is altered
     */
    private void addControlCode(final int code, final int position) {
        final String insert = String.valueOf((char) code);
        final int selectionEnd = target.getSelectionEnd();
        final int selectionStart = target.getSelectionStart();
        if (selectionStart < selectionEnd) {
            final String source = target.getText();
            final String before = source.substring(0, selectionStart);
            final String selected = target.getSelectedText();
            final String after = source.substring(selectionEnd, source.length());
            target.setText(before + insert + selected + insert + after);
            if (position == POSITION_START) {
                target.setCaretPosition(selectionStart + 1);
            } else if (position == POSITION_END) {
                target.setCaretPosition(selectionEnd + 2);
            }
        } else {
            final int offset = target.getCaretPosition();
            final String source = target.getText();
            final String before = target.getText().substring(0, offset);
            final String after = target.getText().substring(offset, source.length());
            target.setText(before + insert + after);
            target.setCaretPosition(offset + 1);
        }
    }
    
    /**
     * Retrieves the buffered text stored at the position indicated by
     * bufferPos, and replaces the current textbox content with it.
     */
    private void retrieveBuffer() {
        target.setText(buffer[bufferPosition]);
    }
    
    /**
     * Normalises the input so that it is in the range 0 &lt;= x &lt; bufferSize.
     * @param input The number to normalise
     * @return The normalised number
     */
    private int normalise(final int input) {
        int res = input;
        while (res < 0) {
            res += bufferSize;
        }
        return res % bufferSize;
    }
    
    /**
     * Adds all items in the string array to the buffer.
     *
     * @param lines lines to add to the buffer
     */
    public void addToBuffer(final String[] lines) {
        for (String line : lines) {
            addToBuffer(line);
        }
    }
    
    
    /**
     * Adds the specified string to the buffer.
     * @param line The line to be added to the buffer
     */
    public void addToBuffer(final String line) {
        buffer[bufferMaximum] = line;
        bufferMaximum = normalise(bufferMaximum + 1);
        bufferPosition = bufferMaximum;
        
        if (buffer[bufferSize - 1] != null) {
            bufferMinimum = normalise(bufferMaximum + 1);
            buffer[bufferMaximum] = null;
        }
        
        target.setText("");
    }
    
    /**
     * Displays a colour picker for the target.
     *
     * @param irc show irc colours in the colour picker
     * @param hex show hex colours in the colour picker
     */
    private void showColourPicker(final boolean irc, final boolean hex) {
        if (IdentityManager.getGlobalConfig().getOptionBool("general", "showcolourdialog", false)) {
            colourPicker = new ColourPickerDialog(irc, hex);
            colourPicker.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent actionEvent) {
                    target.setText(target.getText() + actionEvent.getActionCommand());
                    colourPicker.dispose();
                    colourPicker = null;
                } });
                colourPicker.setLocation((int) target.getLocationOnScreen().getX(),
                        (int) target.getLocationOnScreen().getY() - colourPicker.getHeight());
                colourPicker.setVisible(true);
        }
    }
}
