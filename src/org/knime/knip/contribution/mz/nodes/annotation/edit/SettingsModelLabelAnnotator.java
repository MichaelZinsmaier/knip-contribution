package org.knime.knip.contribution.mz.nodes.annotation.edit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.NativeImgLabeling;

import org.apache.xmlbeans.impl.util.Base64;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.core.io.externalization.BufferedDataInputStream;
import org.knime.knip.core.io.externalization.BufferedDataOutputStream;
import org.knime.knip.core.io.externalization.ExternalizerManager;
import org.knime.knip.core.io.externalization.externalizers.NativeImgLabelingExt0;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;
import org.knime.knip.io.nodes.annotation.SettingsModelAnnotatorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsModelLabelAnnotator extends SettingsModelAnnotatorView<Labeling<String>> {

	private final String m_configName;
	
	private HashMap<RowColKey, Labeling<String>> m_labelingMap = new HashMap<RowColKey, Labeling<String>>();
	
	/* Logger */
	private final Logger LOGGER = LoggerFactory
			.getLogger(SettingsModelLabelAnnotator.class);
	
	public SettingsModelLabelAnnotator(String configName) {
		m_configName = configName;
	}

	@Override
	public void setAnnotationMap(HashMap<RowColKey, Labeling<String>> map) {
		m_labelingMap = map;
	}

	@Override
	public Map<RowColKey, Labeling<String>> getAnnotationMap() {
		return m_labelingMap;
	}

	//helper
	
	@Override
	protected void saveSettings(NodeSettingsWO settings) {
		//save the labeling hashmap
		try {
			settings.addInt("numLabelingEntries", m_labelingMap.size());

			// save drawings
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedDataOutputStream out = new BufferedDataOutputStream(baos);
			
			for (Entry<RowColKey, Labeling<String>> entry : m_labelingMap.entrySet()) {

				// write key
				ExternalizerManager.write(out, entry.getKey());

				// write value
				NativeImgLabelingExt0 externalizer = new NativeImgLabelingExt0();
				externalizer.write(out, (NativeImgLabeling) entry.getValue());
			}
			out.flush();

			settings.addString("labeling", new String(Base64.encode(baos.toByteArray())));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	protected void loadSettings(NodeSettingsRO settings) {
		//load the labeling hashmap
		try {
			int numOverlays = settings.getInt("numLabelingEntries");

			// load drawings
			m_labelingMap = new HashMap<RowColKey, Labeling<String>>();

			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(settings.getString("labeling").getBytes()));
			BufferedDataInputStream in = new BufferedDataInputStream(bais);

			for (int i = 0; i < numOverlays; i++) {
				// reads the map key
				RowColKey key = ExternalizerManager.read(in);

				// reads the map value
				NativeImgLabelingExt0 externalizer = new NativeImgLabelingExt0();
				NativeImgLabeling<?, ?> value = externalizer.read(in);
				
				m_labelingMap.put(key, (Labeling<String>) value);
			}
			in.close();

		} catch (IOException e) {
			LOGGER.error("IOError while loading annotator", e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("ClassNotFound while loading annotator", e);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	//standard methods
	
	@Override
	protected <T extends SettingsModel> T createClone() {
		SettingsModelLabelAnnotator clone = new SettingsModelLabelAnnotator(m_configName);
		//is a shallow copy enough
		clone.setAnnotationMap((HashMap<RowColKey, Labeling<String>>)m_labelingMap.clone());
		return (T) clone;
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_labelannotation";
	}

	@Override
	protected String getConfigName() {
		return m_configName;
	}
	
	@Override
	public String toString() {
		return m_configName;
	}

}
