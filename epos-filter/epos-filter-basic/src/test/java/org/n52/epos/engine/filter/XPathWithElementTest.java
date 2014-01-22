/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.epos.engine.filter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.ActiveFilter;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XPathWithElementTest {

private static final Logger logger = LoggerFactory.getLogger(XPathFilterTest.class);
	
	@Mock
	private EposEvent eventObject;
	private Element xo;
	
	@Before
	public void init() throws IOException, FilterInstantiationException, XPathFactoryConfigurationException, SAXException, ParserConfigurationException {
		MockitoAnnotations.initMocks(this);
		xo = readElement(getClass().getResourceAsStream("xpathTestDocument.xml"));
		Mockito.when(eventObject.getOriginalObject()).thenReturn(xo);
		
		XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		logger.info("Using {} for XPath matching", factory.getClass().getCanonicalName());
	}
	
	private Element readElement(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		return fac.newDocumentBuilder().parse(inputStream).getDocumentElement();
	}

	@Test
	public void shouldInstantiateXPathFilter() throws FilterInstantiationException {
		Map<String,String> map = new HashMap<String, String>();
		map.put("fes20", "http://www.opengis.net/fes/2.0");
		XPathConfiguration conf = new XPathConfiguration("//fes20:Literal", map);
		EposFilter filter = FilterInstantiationRepository.Instance.instantiate(conf);
		
		Assert.assertThat(filter, is(instanceOf(XPathFilter.class)));
	}
	
	@Test
	public void shouldPassXPathFilter() throws IOException, XPathExpressionException,
				FilterInstantiationException {
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("fes20", "http://www.opengis.net/fes/2.0");
		ActiveFilter filter = new XPathFilter("//fes20:Literal", map);
		
		for (int i = 0; i < 100; i++) {
			Assert.assertTrue("Filter did not match!", filter.matches(eventObject));	
		}
	}
	
	@Test
	public void shouldNotPassXPathFilter() throws IOException, XPathExpressionException {
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("fes20", "http://www.opengis.net/fes/2.0");
		ActiveFilter filter = new XPathFilter("//fes20:Literals", map);
		
		for (int i = 0; i < 100; i++) {
			Assert.assertTrue("Filter did match but should not have!", !filter.matches(eventObject));
		}
	}
}
