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
package org.n52.epos.pattern.eml;

import java.io.IOException;

import net.opengis.fes.x20.FilterDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FESFilterInstantiationTest {

	private static final Logger logger = LoggerFactory.getLogger(FESFilterInstantiationTest.class);
	
	@Test
	public void shouldInstantiationEMLPatternFilter() throws XmlException, IOException, FilterInstantiationException {
		FilterDocument eml = FilterDocument.Factory.parse(getClass().getResourceAsStream("fes.xml"));
		
		EposFilter filter = FilterInstantiationRepository.Instance.instantiate(eml);
		
		Assert.assertTrue("filter is null!", filter != null);
		Assert.assertTrue("Not a PatternFilter!", filter instanceof EMLPatternFilter);
		
		EMLPatternFilter pattern = (EMLPatternFilter) filter;
		Assert.assertTrue("Filter should create a final output!", pattern.getPatterns().get(0).createsFinalOutput());
		Assert.assertTrue("Filter should use the original event as output!", pattern.getPatterns().get(0).usesOriginalEventAsOutput());
		
		logger.info(pattern.getPatterns().get(0).createStringRepresentation());
	}
	
}
