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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.epos.pattern.eml.pattern;

import java.util.HashSet;
import java.util.Vector;

import org.n52.epos.pattern.eml.EMLParser;


/**
 * superclass of all patterns
 * 
 * @author Thomas Everding
 *
 */
public abstract class APattern {
	
	/**
	 * pattern id
	 */
	protected String patternID;
	
	/**
	 * description for this pattern
	 */
	protected String description = "";
	
	/**
	 * the collection of {@link SelFunction}s
	 */
	protected Vector<SelFunction> selectFunctions = new Vector<SelFunction>();
	
	/**
	 * the used property names
	 */
	protected HashSet<Object> propertyNames = new HashSet<Object>();

	/**
	 * @return the patternID
	 */
	public String getPatternID() {
		return this.patternID;
	}

	/**
	 * sets the patternID
	 * 
	 * @param patternID the patternID to set
	 */
	public void setPatternID(String patternID) {
		this.patternID = patternID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * sets the pattern description
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the select functions
	 */
	public Vector<SelFunction> getSelectFunctions() {
		return this.selectFunctions;
	}
	
	
	/**
	 * adds a select function
	 * 
	 * @param selectFunction the new select function
	 */
	public void addSelectFunction(SelFunction selectFunction) {
		this.selectFunctions.add(selectFunction);
	}
	
	
	/**
	 * creates an esper EPL statement
	 * 
	 * @return this pattern in esper EPL, one statement for every select clause
	 */
	public abstract Statement[] createEsperStatements();

	
	/**
	 * creates an esper EPL statement
	 * 
	 * @param parser the parser to build the statements
	 * 
	 * @return this pattern in esper EPL, one statement for every select clause
	 */
	public abstract Statement[] createEsperStatements(EMLParser parser);
	
	
	/**
	 * 
	 * @return all found property names of this pattern
	 */
	public HashSet<Object> getPropertyNames() {
		return this.propertyNames;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() +": "+this.getPatternID();
	}
	
}
