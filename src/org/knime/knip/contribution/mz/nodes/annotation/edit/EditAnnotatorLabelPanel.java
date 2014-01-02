/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 25.09.2013 by zinsmaie
 */
package org.knime.knip.contribution.mz.nodes.annotation.edit;

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
 * TODO Auto-generated
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
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
