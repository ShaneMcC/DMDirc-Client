/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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
 *
 * SVN: $Id: CallbackOnChannelNotice.java 257 2007-03-02 23:08:30Z ShaneMcC $
 */

package uk.org.ownage.dmdirc.parser;

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelQuit;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnQuit;
import java.util.Enumeration;

/**
 * Process a Quit message.
 */
public class ProcessQuit extends IRCProcessor {
	/**
	 * Process a Quit message.
	 *
	 * @param type Type of line to process ("005", "PRIVMSG" etc)
	 * @param tokens IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		// :nick!ident@host QUIT
		// :nick!ident@host QUIT :reason
		if (token.length < 2) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		
		if (iClient == null) { return; }
		if (myParser.alwaysUpdateClient) {
			// This may seem pointless - updating before they leave - but the formatter needs it!
			if (iClient.getHost().equals("")) {iClient.setUserBits(token[0],false); }
		}
		String sReason = "";
		if (token.length > 2) { sReason = token[token.length-1]; }
		
		for (Enumeration e = myParser.hChannelList.keys(); e.hasMoreElements();) {
			iChannel = myParser.hChannelList.get(e.nextElement());
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient != null) {
				callChannelQuit(iChannel,iChannelClient,sReason);
				if (iClient == myParser.cMyself) {
					iChannel.emptyChannel();
					myParser.hChannelList.remove(iChannel.getName().toLowerCase());
				} else {
					iChannel.delClient(iClient);
				}
			}
		}

		callQuit(iClient,sReason);
		if (iClient == myParser.cMyself) {
			myParser.hClientList.clear();
		} else {
			myParser.hClientList.remove(iClient.getNickname().toLowerCase());
		}
	}	
	
	/**
	 * Callback to all objects implementing the ChannelQuit Callback.
	 *
	 * @see IChannelQuit
	 * @param cChannel Channel that user was on
	 * @param cChannelClient User thats quitting
	 * @param sReason Quit reason
	 */
	protected boolean callChannelQuit(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		CallbackOnChannelQuit cb = (CallbackOnChannelQuit)getCallbackManager().getCallbackType("OnChannelQuit");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sReason); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the Quit Callback.
	 *
	 * @see IQuit
	 * @param cClient Client Quitting
	 * @param sReason Reason for quitting (may be "")
	 */
	protected boolean callQuit(ClientInfo cClient, String sReason) {
		CallbackOnQuit cb = (CallbackOnQuit)getCallbackManager().getCallbackType("OnQuit");
		if (cb != null) { return cb.call(cClient, sReason); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "QUIT";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessQuit (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
}