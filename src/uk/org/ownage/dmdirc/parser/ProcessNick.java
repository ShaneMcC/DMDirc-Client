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

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelNickChanged;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnNickChanged;
import java.util.Enumeration;

/**
 * Process a Nick change.
 */
public class ProcessNick extends IRCProcessor {
	/**
	 * Process a Nick change.
	 *
	 * @param type Type of line to process ("005", "PRIVMSG" etc)
	 * @param tokens IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		ClientInfo iClient;
		ChannelClientInfo iChannelClient;
		ChannelInfo iChannel;
		
		iClient = getClientInfo(token[0]);
		if (iClient != null) {
			// Remove the client from the known clients list
			myParser.hClientList.remove(iClient.getNickname().toLowerCase());
			// Change the nickame
			iClient.setUserBits(token[2],true);
			// Readd the client
			myParser.hClientList.put(iClient.getNickname().toLowerCase(),iClient);
			
			for (Enumeration e = myParser.hChannelList.keys(); e.hasMoreElements();) {
				iChannel = myParser.hChannelList.get(e.nextElement());
				iChannelClient = iChannel.getUser(iClient);
				if (iChannelClient != null) {
					callChannelNickChanged(iChannel,iChannelClient,ClientInfo.parseHost(token[0]));
				}
			}
			
			callNickChanged(iClient, ClientInfo.parseHost(token[0]));
		}
		
	}
	
	/**
	 * Callback to all objects implementing the ChannelNickChanged Callback.
	 *
	 * @see IChannelNickChanged
	 * @param cChannel One of the channels that the user is on
	 * @param cChannelClient Client changing nickname
	 * @param sOldNick Nickname before change
	 */
	protected boolean callChannelNickChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sOldNick) {
		CallbackOnChannelNickChanged cb = (CallbackOnChannelNickChanged)getCallbackManager().getCallbackType("OnChannelNickChanged");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sOldNick); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the NickChanged Callback.
	 *
	 * @see INickChanged
	 * @param cClient Client changing nickname
	 * @param sOldNick Nickname before change
	 */
	protected boolean callNickChanged(ClientInfo cClient, String sOldNick) {
		CallbackOnNickChanged cb = (CallbackOnNickChanged)getCallbackManager().getCallbackType("OnNickChanged");
		if (cb != null) { return cb.call(cClient, sOldNick); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[1];
		iHandle[0] = "NICK";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessNick (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
}