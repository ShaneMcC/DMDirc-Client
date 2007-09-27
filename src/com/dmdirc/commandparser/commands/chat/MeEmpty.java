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

package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.ChatCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * The me/0 command simply tells the user that the correct usage of /me requires
 * they pass an action to perform.
 * @author chris
 */
public final class MeEmpty extends ChatCommand {
    
    /** Creates a new instance of MeEmpty. */
    public MeEmpty() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final Server server,
            final MessageTarget target, final boolean isSilent, final String... args) {
        sendLine(origin, isSilent, "commandUsage", 
                IdentityManager.getGlobalConfig().getOption("general", "commandchar"),
                "me", "<action>");
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "me";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return null;
    }
    
}
