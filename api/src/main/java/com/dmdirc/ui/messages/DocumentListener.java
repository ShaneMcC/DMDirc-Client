/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.ui.messages;

import java.util.EventListener;

/**
 * Interface for document listeners.
 */
public interface DocumentListener extends EventListener {

    /**
     * A line has been added to the textpane.
     *
     * @param line   Index of the added line
     * @param length Number of lines added
     * @param size   New number of lines
     */
    void linesAdded(final int line, final int length, final int size);

    /**
     * The textpane has been trimmed to a new size.
     *
     * @param newSize    New number of lines
     * @param numTrimmed Number of lines trimmed
     */
    void trimmed(int newSize, int numTrimmed);

    /**
     * The textpane has been cleared.
     */
    void cleared();

    /**
     * The textpane requires repainting.
     */
    void repaintNeeded();

}
