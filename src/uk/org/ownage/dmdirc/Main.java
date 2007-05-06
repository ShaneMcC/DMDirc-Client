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

import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.identities.IdentityManager;
import uk.org.ownage.dmdirc.plugins.PluginManager;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * Main class, handles initialisation.
 * @author chris
 */
public final class Main {
    
    /**
     * Stores the current program version.
     */
    public static final String VERSION = "SVN";
    
    /**
     * Prevents creation of main.
     */
    private Main() {
    }
    
    /**
     * Entry procedure.
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        MainFrame.initUISettings();
        
        IdentityManager.load();
        
        ActionManager.init();
        
        PluginManager.getPluginManager();
        
        ActionManager.loadActions();
        
        MainFrame.getMainFrame();
    }
    
    /**
     * Quits the client nicely.
     * @param reason The quit reason to send
     */
    public static void quit(final String reason) {
        ServerManager.getServerManager().disconnectAll(reason);
        Config.save();
        System.exit(0);
    }
    
    /**
     * Quits the client nicely, with the default closing message.
     */
    public static void quit() {
        quit(Config.getOption("general", "closemessage"));
    }
    
}
