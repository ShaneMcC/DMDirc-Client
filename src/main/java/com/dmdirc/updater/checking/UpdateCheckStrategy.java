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

package com.dmdirc.updater.checking;

import com.dmdirc.updater.UpdateComponent;

import java.util.Collection;
import java.util.Map;

/**
 * Defines a strategy for checking for updates for one or more {@link UpdateComponent}s.
 */
public interface UpdateCheckStrategy {

    /**
     * Checks the given collection of components to determine whether or not any updates are
     * available.
     *
     * @param components The components to be checked for updates
     *
     * @return A map of each of the provided component to an {@link UpdateCheckResult} indicating
     *         the status of the component and any additional information concerning how it should
     *         be updated.
     */
    Map<UpdateComponent, UpdateCheckResult> checkForUpdates(Collection<UpdateComponent> components);

}
