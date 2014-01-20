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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xmlbeans.XmlObject;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.ActiveFilter;
import org.n52.epos.filter.FilterSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * XPath Filter for {@link EposEvent}.
 * 
 * @author matthes rieke
 * 
 */
public class XPathFilter implements ActiveFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(XPathFilter.class);
	private static XPathFactory factory = javax.xml.xpath.XPathFactory
			.newInstance();
	
	private XPathExpression expression;
	private XPath xpath;
	private String rawExpression;
	private Map<String, String> namespaces;

	public XPathFilter(String exp, Map<String, String> namespacePrefixes)
			throws XPathExpressionException {
		this.rawExpression = exp;
		this.namespaces = namespacePrefixes;
		setNamespacePrefixes(namespacePrefixes);
		setExpression(exp);
	}

	public void setExpression(String exp) throws XPathExpressionException {
		expression = xpath.compile(exp);		
	}

	public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
		xpath = factory.newXPath();
		xpath.setNamespaceContext(createNamespaceContext(
				xpath.getNamespaceContext(), namespacePrefixes));
	}
	
	

	public String getRawExpression() {
		return rawExpression;
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	/**
	 * This will only evaluate when the {@link EposEvent#getOriginalObject()} of
	 * {@link #matches(EposEvent)} is an instanceof {@link Document},
	 * {@link InputStream} (representing XML) or {@link XmlObject}. Otherwise
	 * false is returned.
	 * 
	 * @param event the event object
	 * @return true if the object matches the {@link #expression}.
	 */
	/* (non-Javadoc)
	 * @see org.n52.epos.filter.EposFilter#matches(org.n52.epos.event.EposEvent)
	 */
	/* (non-Javadoc)
	 * @see org.n52.epos.filter.EposFilter#matches(org.n52.epos.event.EposEvent)
	 */
	@Override
	public boolean matches(EposEvent event) {
		logger.debug("Evaluating XPath expression '{}' against object: {}", rawExpression, event.getOriginalObject());
		
		if (event.getOriginalObject() instanceof Document) {
			try {
				return (Boolean) expression.evaluate(event.getOriginalObject(),
						XPathConstants.BOOLEAN);
			} catch (XPathExpressionException e) {
				logger.warn("Error while evaluating XPath. Returning false!", e);
				return false;
			}
		}

		if (event.getOriginalObject() instanceof InputStream) {
			try {
				return (Boolean) expression.evaluate(new InputSource(
						(InputStream) event.getOriginalObject()),
						XPathConstants.BOOLEAN);
			} catch (XPathExpressionException e) {
				logger.warn("Error while evaluating XPath. Returning false!", e);
				return false;
			}
		}

		if (event.getOriginalObject() instanceof XmlObject) {
			try {
				return (Boolean) expression.evaluate(
						((XmlObject) event.getOriginalObject()).getDomNode(),
						XPathConstants.BOOLEAN);
			} catch (XPathExpressionException e) {
				logger.warn("Error while evaluating XPath. Returning false!", e);
				return false;
			}
		}

		return false;
	}

	private NamespaceContext createNamespaceContext(
			NamespaceContext namespaceContext,
			Map<String, String> namespacePrefixes) {
		return new EposNamespaceContext(namespaceContext, namespacePrefixes);
	}

	/**
	 * Provide {@link NamespaceContext} capabilities.
	 */
	private static class EposNamespaceContext implements NamespaceContext {

		private Map<String, String> prefixes;
		private NamespaceContext wrappedContext;

		public EposNamespaceContext(NamespaceContext namespaceContext,
				Map<String, String> namespacePrefixes) {
			this.prefixes = namespacePrefixes;
			this.wrappedContext = namespaceContext;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (this.prefixes.containsKey(prefix)) {
				return this.prefixes.get(prefix);
			}
			return this.wrappedContext.getPrefix(prefix);
		}

		@Override
		public String getPrefix(String namespaceURI) {
			for (String prefix : this.prefixes.keySet()) {
				if (this.prefixes.get(prefix).equals(namespaceURI)) {
					return prefix;
				}
			}
			return this.wrappedContext.getPrefix(namespaceURI);
		}

		@Override
		public Iterator<?> getPrefixes(String namespaceURI) {
			Collection<String> result = new ArrayList<String>();

			for (String prefix : this.prefixes.keySet()) {
				if (this.prefixes.get(prefix).equals(namespaceURI)) {
					result.add(prefix);
				}
			}

			Iterator<?> wrapped = this.wrappedContext.getPrefixes(namespaceURI);
			while (wrapped.hasNext()) {
				result.add((String) wrapped.next());
			}

			return result.iterator();
		}

	}

	@Override
	public CharSequence serialize(FilterSerialization serializer) {
		if (serializer != null) {
			return serializer.serializeFilter(this);
		}
		return this.expression.toString();
	}
	
	public CharSequence serialize() {
		return serialize(new WSNXPathSerialization());
	}

}
