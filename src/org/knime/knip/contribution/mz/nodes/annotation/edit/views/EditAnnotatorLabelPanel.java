package org.knime.knip.contribution.mz.nodes.annotation.edit.views;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.imglib2.labeling.Labeling;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsSelChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelingWithMetadataChgEvent;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class EditAnnotatorLabelPanel extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private JList<String> m_jLabelList;

        private Vector<String> m_labels;

        private EventService m_eventService;


        public EditAnnotatorLabelPanel() {
            super("Labels", false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            m_labels = new Vector<String>();
            m_jLabelList = new JList<String>();
            m_jLabelList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            m_jLabelList.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
					    String[] selectedAr = m_jLabelList.getSelectedValuesList().toArray(new String[] {});						
					    m_eventService.publish(new AnnotatorLabelsSelChgEvent(selectedAr));
					}
				}
			});
            
            
            JScrollPane scrollPane = new JScrollPane(m_jLabelList);
            scrollPane.setPreferredSize(new Dimension(150, 1));

            add(scrollPane);
        }



        @EventListener
        public void onLabelingUpdated(final LabelingWithMetadataChgEvent<String> e) {
        	Labeling<String> labeling = e.getData();
            
        	Vector<String> newLabels = new Vector<String>(); 
            for (final String label : labeling.firstElement().getMapping().getLabels()) {
                    newLabels.add(label);
            }
            Collections.sort(newLabels);
            
            boolean differ = false;
            if (newLabels.size() != m_labels.size()) {
            	differ = true;
            } else {
            	for (int i = 0; i < newLabels.size(); i++) {
            		if (!newLabels.get(i).equals(m_labels.get(i))) {
            			differ = true;
            			break;
            		}
            	}
            }
            
            if (differ) {
            	//only loose the selection if the labels changed
            	m_labels.clear();
            	m_labels = newLabels;
            	m_jLabelList.setListData(m_labels);
            }
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