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

package com.dmdirc.addons.mediasource_dcop;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all DCOP based media sources.
 */
public class DcopMediaSourcePlugin extends Plugin
        implements MediaSourceManager {
    
    /** Media sources. */
    private final List<MediaSource> sources;
    
    /**
     * Creates a new instance of DcopMediaSourcePlugin.
     */
    public DcopMediaSourcePlugin() {
        super();
        sources = new ArrayList<MediaSource>();
        sources.add(new AmarokSource());
        sources.add(new KaffeineSource());
        sources.add(new NoatunSource());
    }
    
    /** {@inheritDoc} */
    @Override
    public List<MediaSource> getSources() {
        return sources;
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean checkPrerequisites() {
        PluginManager.getPluginManager().addPlugin("dcop.jar");
        
        final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName("dcop");
        
        if (pi == null) {
            return false;
        }
        
        if (!pi.isLoaded()) {
            pi.loadPlugin();
        }

        return true;
    }
}
