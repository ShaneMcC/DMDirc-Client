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

package com.dmdirc.ui.swing.dialogs.prefs;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.components.PackingTable;
import com.dmdirc.ui.swing.components.URLProtocolPanel;

import java.net.URI;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;

/**
 * URL Config panel. List all known url protocols and allows them to be 
 * configured.
 */
public class URLConfigPanel extends JPanel implements ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Protocol list. */
    private PackingTable table;
    /** Table mode. */
    private URLHandlerTableModel model;
    /** Table scrollpane. */
    private JScrollPane tableScrollPane;
    /** Protocol config panel. */
    private Map<URI, URLProtocolPanel> details;
    /** Empty info panel. */
    private URLProtocolPanel empty;
    /** Current component. */
    private URLProtocolPanel activeComponent;

    /**
     * Instantiates a new URL config panel.
     */
    public URLConfigPanel() {
        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        tableScrollPane = new JScrollPane();
        model = new URLHandlerTableModel();
        table = new PackingTable(model, false, tableScrollPane) {
            private static final long serialVersionUID = 1;
            @Override
            public TableCellRenderer getCellRenderer(final int row, final int column) {
                switch (column) {
                    case 0:
                        return new URISchemeCellRenderer();
                    default:
                        return super.getCellRenderer(row, column);
                }
            }
        };
        
        details = new HashMap<URI, URLProtocolPanel>();
        empty = new URLProtocolPanel(null, true);
        activeComponent = empty;

        tableScrollPane.setViewportView(table);

        final List<String> options = IdentityManager.getGlobalConfig().
                getOptions("protocol");

        for (String option : options) {
            try {
                final URI uri = new URI(option + "://example.test.com");
                model.addURI(uri);
                details.put(uri, new URLProtocolPanel(uri, true));
            } catch (URISyntaxException ex) {
            //Ignore wont happen
            }
        }
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
        table.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        removeAll();
        setLayout(new MigLayout("ins 0, wrap 1"));

        add(tableScrollPane, "grow, pushy");
        add(activeComponent, "growx, pushx");
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            setVisible(false);
            if (table.getSelectedRow() == -1) {
                activeComponent = empty;
                layoutComponents();
            } else {
                activeComponent =
                        details.get(model.getValueAt(table.getSelectedRow(), 0));
                activeComponent.updateSelection();
                layoutComponents();
            }
            setVisible(true);
        }
    }

}
