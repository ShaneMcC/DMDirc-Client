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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;

/**
 * Implements a raw server command (i.e., a command that is sent to the server as-is.
 */
@CommandOptions(allowOffline = false)
public class RawServerCommand extends BaseCommand implements CommandInfo {

    /** The name of this raw command. */
    private final String myName;

    /**
     * Creates a new instance of RawServerCommand.
     *
     * @param controller The controller to use for command info.
     * @param command    The command name for this raw command
     */
    public RawServerCommand(final CommandController controller, final String command) {
        super(controller);
        myName = command;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final Connection connection = ((ServerCommandContext) context).getConnection();
        connection.getParser().get().sendRawMessage(myName.toUpperCase() + ' '
                + args.getArgumentsAsString());
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public CommandType getType() {
        return CommandType.TYPE_SERVER;
    }

    @Override
    public String getHelp() {
        return myName + " [arguments] - sends a " + myName + " command to the server";
    }

}
