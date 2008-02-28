/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.config.prefs;

import com.dmdirc.config.prefs.validator.NotEmptyValidator;
import com.dmdirc.config.prefs.validator.PermissiveValidator;
import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesSettingTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMultichoice1() {
        new PreferencesSetting(PreferencesType.MULTICHOICE, "domain", 
                "option", "fallback", "title", "helptext");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMultichoice2() {
        new PreferencesSetting(PreferencesType.MULTICHOICE,
                new PermissiveValidator<String>(), "domain", 
                "option", "fallback", "title", "helptext");
    }
    
    @Test
    public void testNormalConstructor() {
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain", 
                "option", "fallback", "title", "helptext");
        
        assertEquals(PreferencesType.TEXT, ps.getType());
        assertEquals("fallback", ps.getValue());
        assertEquals("title", ps.getTitle());
        assertEquals("helptext", ps.getHelptext());
        assertFalse(ps.isRestartNeeded());
        assertTrue(ps.getValidator() instanceof PermissiveValidator);
    }
    
    @Test
    public void testValidatorConstructor() {
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT,
                new NotEmptyValidator(), "domain", 
                "option", "fallback", "title", "helptext");
        
        assertEquals(PreferencesType.TEXT, ps.getType());
        assertEquals("fallback", ps.getValue());
        assertEquals("title", ps.getTitle());
        assertEquals("helptext", ps.getHelptext());
        assertFalse(ps.isRestartNeeded());
        assertTrue(ps.getValidator() instanceof NotEmptyValidator);
    }    
    
    @Test
    public void testRestartNeeded() {
        final PreferencesSetting ps = new PreferencesSetting(PreferencesType.TEXT, "domain", 
                "option", "fallback", "title", "helptext");
        
        assertFalse(ps.isRestartNeeded());
        assertSame(ps, ps.setRestartNeeded());
        assertTrue(ps.isRestartNeeded());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(PreferencesSettingTest.class);
    }
}