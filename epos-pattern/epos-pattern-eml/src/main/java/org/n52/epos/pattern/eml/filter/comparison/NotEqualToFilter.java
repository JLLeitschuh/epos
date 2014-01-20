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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.epos.pattern.eml.filter.comparison;

import java.util.HashSet;

import net.opengis.fes.x20.BinaryComparisonOpType;


/**
 * filters via !=
 * 
 * @author Thomas Everding
 * 
 */
public class NotEqualToFilter extends ABinaryComparisonFilter {

	/**
	 * 
	 * Constructor
	 * 
	 * @param binaryOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public NotEqualToFilter(BinaryComparisonOpType binaryOp, HashSet<Object> propertyNames) {
		this.initialize(binaryOp, propertyNames);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "";

		if (this.first.getUsedProperty() != null || this.second.getUsedProperty() != null) {
			
			result += createUsedPropertyString();

		}
		
		String firstString = this.first.createExpressionString(complexPatternGuard);
		String secondString = this.second.createExpressionString(complexPatternGuard);
		
		/*
		 * check if the format is parsable with esper. otherwise
		 * change it (add quotes etc.)
		 */
		String[] a = checkDataTypes(complexPatternGuard, firstString, secondString);
		firstString = a[0];
		secondString = a[1];
		
		result += "("
						+ firstString
						+ " != "
						+ secondString
						+")";
		return result;
	}

}
