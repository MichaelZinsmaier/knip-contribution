/*Copyright (C) 2014 Michael Zinsmaier
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
*/
package org.knime.knip.contribution.mz.nodes.annotation.edit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.img.unary.ImgCopyOperation;
import net.imglib2.type.numeric.integer.IntType;

import org.apache.xmlbeans.impl.util.Base64;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.core.io.externalization.BufferedDataInputStream;
import org.knime.knip.core.io.externalization.BufferedDataOutputStream;
import org.knime.knip.core.io.externalization.ExternalizerManager;
import org.knime.knip.core.io.externalization.externalizers.NativeImgLabelingExt0;
import org.knime.knip.core.types.ImgFactoryTypes;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;
import org.knime.knip.io.nodes.annotation.SettingsModelAnnotatorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
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
				externalizer.write(out, asNTreeBased((NativeImgLabeling<String, IntType>) entry.getValue()));
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
				
				m_labelingMap.put(key, asArrayBased((NativeImgLabeling<String, IntType>) value));
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

	@SuppressWarnings("unchecked")
	private NativeImgLabeling<String, IntType> asNTreeBased(NativeImgLabeling<String, IntType> arrayBased) throws IncompatibleTypeException {
		//TODO use an op for factory conversion if possible
		long[] dims = new long[arrayBased.numDimensions()];
		arrayBased.dimensions(dims);
			
		NativeImgFactory<?> imgFac = (NativeImgFactory<?>) ImgFactoryTypes.getImgFactory(ImgFactoryTypes.NTREE_IMG_FACTORY);
		NativeImgLabeling<String, IntType> copiedLabeling = new NativeImgLabeling<String, IntType>(imgFac.imgFactory(new IntType()).create(dims, new IntType()));
					
		ImgCopyOperation<LabelingType<String>> copy = new ImgCopyOperation<LabelingType<String>>();
		return (NativeImgLabeling<String, IntType>)copy.compute(arrayBased, copiedLabeling);		
	}
	
	@SuppressWarnings("unchecked")
	private NativeImgLabeling<String, IntType> asArrayBased(NativeImgLabeling<String, IntType> ntreeBased) throws IncompatibleTypeException {
		//TODO use an op for factory conversion if possible
		long[] dims = new long[ntreeBased.numDimensions()];
		ntreeBased.dimensions(dims);
			
		NativeImgFactory<?> imgFac = (NativeImgFactory<?>) ImgFactoryTypes.getImgFactory(ImgFactoryTypes.ARRAY_IMG_FACTORY);
		NativeImgLabeling<String, IntType> copiedLabeling = new NativeImgLabeling<String, IntType>(imgFac.imgFactory(new IntType()).create(dims, new IntType()));
					
		ImgCopyOperation<LabelingType<String>> copy = new ImgCopyOperation<LabelingType<String>>();
		return (NativeImgLabeling<String, IntType>)copy.compute(ntreeBased, copiedLabeling);	
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
