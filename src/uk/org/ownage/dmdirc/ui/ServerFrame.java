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

package uk.org.ownage.dmdirc.ui;

import java.awt.Dimension;
import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import uk.org.ownage.dmdirc.Server;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;

/**
 * The ServerFrame is the MDI window that shows server messages to the user
 * @author chris
 */
public class ServerFrame extends javax.swing.JInternalFrame {
    
    /**
     * The Server object that owns this frame
     */
    private Server parent;
    /**
     * The border used when the frame is not maximised
     */
    private Border myborder;
    /**
     * The dimensions of the titlebar of the frame
     **/
    private Dimension titlebarSize;
    /**
     * whether to auto scroll the textarea when adding text
     */
    private boolean autoScroll = true;
    /**
     * holds the scrollbar for the frame
     */
    private JScrollBar scrollBar;
    
    private ServerCommandParser commandParser;
    
    /**
     * Creates new form ServerFrame
     * @param parent The server instance that owns this frame
     */
    public ServerFrame(Server parent) {
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setVisible(true);
        setResizable(true);
        
        this.parent = parent;
        scrollBar = jScrollPane1.getVerticalScrollBar();
        
        commandParser = new ServerCommandParser(parent);
        
        jTextField1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ServerFrame.this.commandParser.parseCommand(jTextField1.getText());
                jTextField1.setText("");
            }
        });
        
        addPropertyChangeListener("maximum", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
                    ServerFrame.this.myborder = getBorder();
                    ServerFrame.this.titlebarSize =
                            ((BasicInternalFrameUI)getUI())
                            .getNorthPane().getPreferredSize();
                    
                    ((BasicInternalFrameUI)getUI()).getNorthPane()
                    .setPreferredSize(new Dimension(0,0));
                    setBorder(new EmptyBorder(0,0,0,0));
                } else {
                    setBorder(ServerFrame.this.myborder);
                    ((BasicInternalFrameUI)getUI()).getNorthPane()
                    .setPreferredSize(ServerFrame.this.titlebarSize);
                }
            }
        });
        
    }
    
    /**
     * Adds a line of text to the main text area, and scrolls the text pane
     * down so that it's visible if the scrollbar is already at the bottom
     * @param line text to add
     */
    public void addLine(String line) {
        StyledDocument doc = jTextPane1.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), line+"\n", null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        
        autoScroll = ((scrollBar.getValue() + scrollBar.getVisibleAmount())
        != scrollBar.getMaximum());
        System.out.println("Value: \t\t"+scrollBar.getValue()+
                "\r\nVisible \t"+scrollBar.getVisibleAmount()+
                "\r\nMax: \t\t"+scrollBar.getMaximum()+
                "\r\nAutoScrolling: \t"+autoScroll);
        if(autoScroll) { 
            jTextPane1.setCaretPosition(doc.getLength());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setTitle("Server Frame");

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
    
}
