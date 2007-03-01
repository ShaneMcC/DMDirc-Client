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
 * SVN: $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */

package uk.org.ownage.dmdirc.parser;

/** 
 * Called when a users channel mode is changed.
 */
public interface IChannelUserModeChanged extends ICallbackInterface {
	/**
	 * Called when a users channel mode is changed.
	 * cChannelClient is null if the modes were found from raw 324 (/MODE #Chan reply) or if a server set the mode.<br>
	 * If a Server set the mode, sHost is the servers name, else it is the full host of the user who set it
	 * 
	 * @param tParser Reference to the parser object that made the callback.
	 * @param cChannel Channel where modes were changed
	 * @param cChangedClient Client being changed
	 * @param cSetByClient Client chaning the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @param sMode String representing mode change (ie +o)
	 * @see IRCParser#addChannelUserModeChanged
	 * @see IRCParser#delChannelUserModeChanged
	 * @see IRCParser#callChannelUserModeChanged
	 */
	public void onChannelUserModeChanged(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChangedClient, ChannelClientInfo cSetByClient, String sHost, String sMode);
}