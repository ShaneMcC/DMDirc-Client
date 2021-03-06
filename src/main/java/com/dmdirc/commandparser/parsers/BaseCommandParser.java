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
import com.dmdirc.commandparser.CommandInfoPair;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ExternalCommand;
import com.dmdirc.commandparser.commands.PreviousCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.provider.ReadOnlyConfigProvider;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.events.UnknownCommandEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.InputModel;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.util.collections.RollingList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a generic command parser. A command parser takes a line of input from the user,
 * determines if it is an attempt at executing a command (based on the character at the start of the
 * string), and handles it appropriately.
 */
public abstract class BaseCommandParser implements Serializable, CommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Commands that are associated with this parser. */
    private final Map<String, CommandInfoPair> commands;
    /** A history of commands that have been entered into this parser. */
    private final RollingList<PreviousCommand> history;
    /** Command manager to use. */
    protected final CommandController commandManager;
    /** Event bus to post events to. */
    private final EventBus eventBus;

    /**
     * Creates a new instance of CommandParser.
     *
     * @param configManager  Config manager to read settings
     * @param commandManager Command manager to load plugins from
     * @param eventBus       The event bus to post events to.
     */
    protected BaseCommandParser(
            final ReadOnlyConfigProvider configManager,
            final CommandController commandManager,
            final EventBus eventBus) {
        this.eventBus = eventBus;
        commands = new HashMap<>();
        history = new RollingList<>(configManager.getOptionInt("general", "commandhistory"));
        this.commandManager = commandManager;
        loadCommands();
    }

    /** Loads the relevant commands into the parser. */
    protected abstract void loadCommands();

    @Override
    public final void registerCommand(final Command command, final CommandInfo info) {
        commands.put(info.getName().toLowerCase(), new CommandInfoPair(info, command));
    }

    @Override
    public final void unregisterCommand(final CommandInfo info) {
        commands.remove(info.getName().toLowerCase());
    }

    @Override
    public Map<String, CommandInfoPair> getCommands() {
        return new HashMap<>(commands);
    }

    @Override
    public final void parseCommand(@Nonnull final WindowModel origin, final String line,
                                   final boolean parseChannel) {
        checkNotNull(origin);

        final CommandArguments args = new CommandArguments(commandManager, line);
        if (args.isCommand()) {
            if (handleChannelCommand(origin, args, parseChannel)) {
                return;
            }

            if (commands.containsKey(args.getCommandName().toLowerCase())) {
                final CommandInfoPair pair = commands.get(args.getCommandName().toLowerCase());
                addHistory(args.getStrippedLine());
                executeCommand(origin, pair.getCommandInfo(), pair.getCommand(), args,
                        getCommandContext(origin, pair.getCommandInfo(), pair.getCommand(), args));
            } else {
                handleInvalidCommand(origin, args);
            }
        } else {
            handleNonCommand(origin, line);
        }
    }

    /**
     * Checks to see whether the inputted command is a channel or external command, and if it is
     * whether one or more channels have been specified for its execution. If it is a channel or
     * external command, and channels are specified, this method invoke the appropriate command
     * parser methods to handle the command, and will return true. If the command is not handled,
     * the method returns false.
     *
     * @param origin       The container which received the command
     * @param args         The command and its arguments
     * @param parseChannel Whether or not to try parsing channel names
     *
     * @return True iff the command was handled, false otherwise
     */
    protected boolean handleChannelCommand(@Nonnull final WindowModel origin,
            final CommandArguments args, final boolean parseChannel) {
        final boolean silent = args.isSilent();
        final String command = args.getCommandName();
        final String[] cargs = args.getArguments();
        final Optional<Connection> connection = origin.getConnection();

        if (cargs.length == 0
                || !parseChannel
                || !connection.isPresent()
                || !commandManager.isChannelCommand(command)) {
            return false;
        }

        final Connection server = connection.get();
        final String[] parts = cargs[0].split(",");
        boolean someValid = false;
        for (String part : parts) {
            someValid |= server.getGroupChatManager().isValidChannelName(part);
        }

        if (someValid) {
            for (String channelName : parts) {
                if (!server.getGroupChatManager().isValidChannelName(channelName)) {
                    origin.getEventBus().publishAsync(new CommandErrorEvent(origin,
                            "Invalid channel name: " + channelName));
                    continue;
                }

                final String newCommandString = commandManager.getCommandChar()
                        + (silent ? String.valueOf(commandManager.getSilenceChar()) : "")
                        + args.getCommandName()
                        + (cargs.length > 1 ? ' ' + args.getArgumentsAsString(1) : "");

                final Optional<GroupChat> channel = server.getGroupChatManager()
                        .getChannel(channelName);
                if (channel.isPresent()) {
                    channel.get().getWindowModel().getInputModel().map(InputModel::getCommandParser)
                            .ifPresent(cp -> cp.parseCommand(origin, newCommandString, false));
                } else {
                    final Map.Entry<CommandInfo, Command> actCommand = commandManager.getCommand(
                            CommandType.TYPE_CHANNEL, command);

                    if (actCommand != null && actCommand.getValue() instanceof ExternalCommand) {
                        ((ExternalCommand) actCommand.getValue()).execute(
                                origin, server, channelName, silent,
                                new CommandArguments(commandManager, newCommandString));
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Adds a command to this parser's history.
     *
     * @param command The command name and arguments that were used
     */
    private void addHistory(final String command) {
        synchronized (history) {
            final PreviousCommand pc = new PreviousCommand(command);
            history.remove(pc);
            history.add(pc);
        }
    }

    /**
     * Retrieves the most recent time that the specified command was used. Commands should not
     * include command or silence chars.
     *
     * @param command The command to search for
     *
     * @return The timestamp that the command was used, or 0 if it wasn't
     */
    public long getCommandTime(final String command) {
        long res = 0;

        synchronized (history) {
            for (PreviousCommand pc : history.getList()) {
                if (pc.getLine().matches("(?i)" + command)) {
                    res = Math.max(res, pc.getTime());
                }
            }
        }

        return res;
    }

    @Override
    public void parseCommand(@Nonnull final WindowModel origin, final String line) {
        parseCommand(origin, line, true);
    }

    @Override
    public void parseCommandCtrl(final WindowModel origin, final String line) {
        handleNonCommand(origin, line);
    }

    @Override
    @Nullable
    public CommandInfoPair getCommand(final String commandName) {
        return commands.get(commandName);
    }

    /**
     * Gets the context that the command will execute with.
     *
     * @param origin      The container which received the command
     * @param commandInfo The command information object matched by the command
     * @param command     The command to be executed
     * @param args        The arguments to the command
     *
     * @return The context for the command.
     */
    protected abstract CommandContext getCommandContext(
            final WindowModel origin,
            final CommandInfo commandInfo,
            final Command command,
            final CommandArguments args);

    /**
     * Executes the specified command with the given arguments.
     *
     * @param origin      The container which received the command
     * @param commandInfo The command information object matched by the command
     * @param command     The command to be executed
     * @param args        The arguments to the command
     * @param context     The context to use when executing the command
     */
    protected abstract void executeCommand(
            @Nonnull final WindowModel origin,
            final CommandInfo commandInfo, final Command command,
            final CommandArguments args, final CommandContext context);

    /**
     * Called when the user attempted to issue a command (i.e., used the command character) that
     * wasn't found. It could be that the command has a different arity, or that it plain doesn't
     * exist.
     *
     * @param origin The window in which the command was typed
     * @param args   The arguments passed to the command
     *
     * @since 0.6.3m1
     */
    protected void handleInvalidCommand(final WindowModel origin,
            final CommandArguments args) {
        eventBus.publishAsync(new UnknownCommandEvent(origin, args.getCommandName(),
                args.getArguments()));
    }

    /**
     * Called when the input was a line of text that was not a command. This normally means it is
     * sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line   The line input by the user
     */
    protected abstract void handleNonCommand(final WindowModel origin,
            final String line);

    /**
     * Determines if the specified command has defined any command options.
     *
     * @param command The command to investigate
     *
     * @return True if the command defines options, false otherwise
     */
    protected boolean hasCommandOptions(final Command command) {
        return command.getClass().isAnnotationPresent(CommandOptions.class);
    }

    /**
     * Retrieves the command options for the specified command. If the command does not define
     * options, this method will return null.
     *
     * @param command The command whose options should be retrieved
     *
     * @return The command's options, or null if not available
     */
    protected CommandOptions getCommandOptions(final Command command) {
        return command.getClass().getAnnotation(CommandOptions.class);
    }

}
