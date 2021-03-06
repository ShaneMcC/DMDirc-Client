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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.config.provider.AggregateConfigProvider;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * The command parser used for global commands.
 */
@Singleton
public class GlobalCommandParser extends BaseCommandParser {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalCommandParser.class);
    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new command parser for global commands.
     *
     * @param configManager  Config manager to read settings from
     * @param commandManager Command manager to load commands from
     * @param eventBus       eventBus
     */
    @Inject
    public GlobalCommandParser(
            @GlobalConfig final AggregateConfigProvider configManager,
            final CommandController commandManager,
            final EventBus eventBus) {
        super(configManager, commandManager, eventBus);
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        commandManager.loadCommands(this, CommandType.TYPE_GLOBAL);
    }

    @Override
    protected CommandContext getCommandContext(
            final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args) {
        return new CommandContext(origin, commandInfo);
    }

    @Override
    protected void executeCommand(
            @Nonnull final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args,
            final CommandContext context) {
        command.execute(origin, args, context);
    }

    /**
     * Called when the input was a line of text that was not a command. This normally means it is
     * sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line   The line input by the user
     */
    @Override
    protected void handleNonCommand(final WindowModel origin, final String line) {
        if (origin == null) {
            LOG.warn(USER_ERROR, "Invalid Global Command: {}", line,
                    new IllegalArgumentException("Invalid Global Command: " + line));
        } else {
            origin.getEventBus().publishAsync(
                    new CommandErrorEvent(origin, "Invalid global command: " + line));
        }
    }

}
