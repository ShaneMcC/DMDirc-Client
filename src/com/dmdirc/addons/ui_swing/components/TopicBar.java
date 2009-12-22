/*
 * 
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.Channel;
import com.dmdirc.Topic;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.ui.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import net.miginfocom.swing.MigLayout;

/**
 * Component to show and edit topics for a channel.
 */
public class TopicBar extends JComponent implements PropertyChangeListener,
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    private final TextFieldInputField topicText;
    private final JButton topicEdit;
    private final JButton topicCancel;
    private Channel channel;

    /**
     * Instantiates a new topic bar.
     *
     * @param channelFrame Parent channel frame
     */
    public TopicBar(final ChannelFrame channelFrame) {
        topicText = new TextFieldInputField();
        topicEdit = new ImageButton("edit", IconManager.getIconManager().
                getIcon("edit-inactive"),  IconManager.getIconManager().
                getIcon("edit"));
        topicCancel = new ImageButton("cancel", IconManager.getIconManager().
                getIcon("close"),  IconManager.getIconManager().
                getIcon("close-active"));
         this .channel = channelFrame.getChannel();

        new SwingInputHandler(topicText, channelFrame.getCommandParser(),
                channelFrame).setTypes(false,
                false, true, false);

        topicText.setEnabled(false);
        topicCancel.setVisible(false);

        setLayout(new MigLayout("fill, ins 0, hidemode 3"));
        add(topicText, "growx, pushx");
        add(topicCancel, "");
        add(topicEdit, "");


        channelFrame.addPropertyChangeListener("title", this);
        topicText.addActionListener(this);
        topicEdit.addActionListener(this);
        topicCancel.addActionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        List<Topic> topics = channel.getTopics();
        String topic;
        if (topics.size() == 0) {
            topic = "";
        } else {
            topic = topics.get(topics.size() - 1).getTopic();
        }
        topicText.setText(topic);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == topicEdit || e.getSource() == topicText) {
            if (topicText.isEnabled()) {
                channel.setTopic(topicText.getText());
                topicText.setEnabled(false);
                topicCancel.setVisible(false);
            } else {
                topicText.setEnabled(true);
                topicCancel.setVisible(true);
            }
        } else if (e.getSource() == topicCancel) {
            topicText.setEnabled(false);
            topicCancel.setVisible(false);
            propertyChange(null);
        }
    }
}