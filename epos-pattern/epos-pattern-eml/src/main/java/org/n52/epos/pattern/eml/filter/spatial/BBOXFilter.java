/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.epos.pattern.eml.filter.spatial;

import org.apache.xmlbeans.XmlException;
import org.n52.epos.event.MapEposEvent;
import org.n52.epos.pattern.functions.MethodNames;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.opengis.fes.x20.BBOXType;
import net.opengis.gml.x32.EnvelopeType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import net.opengis.gml.x32.EnvelopeDocument;
import org.apache.xmlbeans.XmlObject;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class BBOXFilter extends ASpatialFilter {

	private BBOXType bboxOperator;
	
	private static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";

	private static final String	ENVELOPE_NAME = "Envelope";
	
	private static final Logger logger = LoggerFactory
			.getLogger(BBOXFilter.class);

	/**
	 * 
	 * Constructor
	 *
	 * @param bboxOp FES bounding box
	 */
	public BBOXFilter(BBOXType bboxOp) {
		this.bboxOperator = bboxOp;
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		Geometry geom = null;
		try {
                        //get Envelope
                        EnvelopeType envelope;
                        XmlObject[] envelopes = this.bboxOperator.selectChildren(EnvelopeDocument.type.getDocumentElementName());
                        if (envelopes != null && envelopes.length > 0) {
                            envelope = (EnvelopeType) envelopes[0];
                        }
                        else {
                            logger.warn("could not find envelope from elem: "+this.bboxOperator);
                            return "";
                        }
			
			//parse Envelope
			geom = GMLGeometryFactory.parseGeometry(envelope);
		} catch (ParseException e) {
			logger.warn(e.getMessage(), e);
		}
		
		if (geom == null) {
			//error while creating geometry
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		sb.append("bbox(");
		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		//create WKT from corners
		sb.append("fromWKT(\""+ geom.toText() +"\")");
		sb.append(", ");
		sb.append(MapEposEvent.GEOMETRY_KEY +")");
		
		return sb.toString();
	}

	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
