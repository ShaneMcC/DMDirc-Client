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

package com.dmdirc.ui.core.aliases;

import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.interfaces.ui.AliasDialogModelListener;

import java.util.Optional;

/**
 * An abstract adapter class for receiving alias dialog model events. The methods in this class are
 * empty. This class exists as convenience for creating listener objects.
 */
public class AliasDialogModelAdapter implements AliasDialogModelListener {

    @Override
    public void aliasAdded(final Alias alias) {
    }

    @Override
    public void aliasRemoved(final Alias alias) {
    }

    @Override
    public void aliasEdited(final Alias oldAlias, final Alias newAlias) {
    }

    @Override
    public void aliasRenamed(final Alias oldAlias, final Alias newAlias) {
    }

    @Override
    public void aliasSelectionChanged(final Optional<Alias> alias) {
    }

    @Override
    public void selectedAliasEdited(final String name, final int minArgs, final String sub) {
    }

}
