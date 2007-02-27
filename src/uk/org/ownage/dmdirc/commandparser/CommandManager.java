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

package uk.org.ownage.dmdirc.commandparser;

import java.util.Vector;
import uk.org.ownage.dmdirc.commandparser.commands.*;

/**
 * The command manager creates and manages a single instance of all commands,
 * and provides methods to load each group of commands into a parser instance
 * @author chris
 */
public class CommandManager {
    
    /**
     * The commands that have been instansiated
     */
    private static Vector<Command> serverCommands;
           
    /**
     * Loads all channel commands into the specified parser
     * @param parser The parser to load commands into
     */
    public static void loadChannelCommands(CommandParser parser) {
        throw new UnsupportedOperationException("Not implemented yet");        
    }
    
    /**
     * Loads all server commands into the specified parser
     * @param parser The parser to load commands into
     */
    public static void loadServerCommands(CommandParser parser) {
        if (serverCommands == null) {
            serverCommands = new Vector<Command>(0,1);

            serverCommands.add(new Test());
        }

        for (Command com : serverCommands) {
            parser.registerCommand(com);
        }
    }    
    
}
