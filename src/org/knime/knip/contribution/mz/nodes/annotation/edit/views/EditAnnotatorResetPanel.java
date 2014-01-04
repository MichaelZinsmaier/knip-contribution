package org.knime.knip.contribution.mz.nodes.annotation.edit.views;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorResetEvent;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class EditAnnotatorResetPanel extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private EventService m_eventService;


        public EditAnnotatorResetPanel() {
            super("", false);           

            JButton resetConfig = new JButton("Reset All Labels");
            resetConfig.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_eventService.publish(new EditAnnotatorResetConfigEvent());
				}
			});
            
            JPanel p = new JPanel();
            p.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            p.add(resetConfig, gbc);           
                       
            add(p);
            setMaximumSize(new Dimension(1000000, 60));
        }



        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
            return Position.EAST;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEventService(final EventService eventService) {
            m_eventService = eventService;
            eventService.subscribe(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
            //nothing to do
        }



        /**
         * {@inheritDoc}
         */
        @Override
        public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
            //nothing to do
        }

}
