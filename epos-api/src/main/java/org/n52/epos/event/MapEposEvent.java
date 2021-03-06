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
package org.n52.epos.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * EPOS-internal representation of an Event. 
 * 
 * @author matthes rieke
 *
 */
public class MapEposEvent implements EposEvent, Map<String, Object> {


	/**
	 * reserved key for the start timestamp
	 */
	public static final String START_KEY = "startTime";
	
	/**
	 * reserved key for the end timestamp
	 */
	public static final String END_KEY = "endTime";
	
	/**
	 * reserved key for the valid time of the event (from start to end)
	 */
	public static final String VALID_TIME_KEY = "validTime";
	
	/**
	 * reserved key for the causal vector
	 */
	public static final String CAUSALITY_KEY = "causality";
	
	/**
	 * reserved key for the value of an event
	 */
	public static final String VALUE_KEY = "value";
	
	
	/**
	 * key for the value of an event as string
	 */
	public static final String STRING_VALUE_KEY = "stringValue";
	
	
	/**
	 * key for the value of an event as double
	 */
	public static final String DOUBLE_VALUE_KEY = "doubleValue";
	
	/**
	 * reserved key for the geometry of an event
	 */
	public static final String GEOMETRY_KEY = "geometry";
	
	/**
	 * reserved key for the sensor ID of an event
	 */
	public static final String SENSORID_KEY = "sensorID";
	
	
	/**
	 * key for the reflection property back to this {@link MapEvent}
	 */
	public static final String THIS_KEY = "this";

	
	/**
	 * key for the original message
	 */
	public static final String ORIGNIAL_OBJECT_KEY = "originalObject";

	/**
	 * key for the publication
	 */
	public static final String PUBLICATION_KEY = "publicationId";
	
	/**
	 * key for the feature of interest ID
	 */
	public static final String FOI_ID_KEY = "foiID";
	
	
	/**
	 * key for the result value
	 */
	public static final String RESULT_KEY = "result";
	
	
	/**
	 * Key for the causal ancestor (only used in select functions and thus in EventBeans from esper)
	 */
	public static final String CAUSAL_ANCESTOR_1_KEY = "ancestor1";
	
	
	
	/**
	 * Key for the causal ancestor (only used in select functions and thus in EventBeans from esper)
	 */
	public static final String CAUSAL_ANCESTOR_2_KEY = "ancestor2";
	
	
	/**
	 * Key for the observed property
	 */
	public static final String OBSERVED_PROPERTY_KEY = "observedProperty";

	/**
	 * Key for the generic content of an XML node
	 */
	public static final String CONTENT_KEY	= "content";
	
	/**
	 * Key for the code space of an gml:identifier element
	 */
	public static final String IDENTIFIER_CODESPACE_KEY = "identifierCodeSpace";
	
	/**
	 * Key for the value of an gml:identifier element
	 */
	public static final String IDENTIFIER_VALUE_KEY = "gml:identifier";
	
	/**
	 * Key for the aixm:designator value
	 */
	public static final String AIXM_DESIGNATOR_KEY = "designator";
	
	/**
	 * Key for feature type
	 */
	public static final String FEATURE_TYPE_KEY = "featureType";
	
	/**
	 * Key for the status
	 */
	public static final String STAUS_KEY = "status";
	
	/**
	 * Key for the lower limit of a volume
	 */
	public static final String LOWER_LIMIT_KEY = "lowerLimit";
	
	/**
	 * Key for the upper limit of a volume
	 */
	public static final String UPPER_LIMIT_KEY = "upperLimit";
	
	/**
	 * Key for the reservation phase
	 */
	public static final String RESERVATION_PHASE_KEY = "reservationPhase";
	
	
	private HashMap<String, Object> map;
	
	private long start;
	
	private long end;
	
	private Object originalObject;
        
        //default to XML for backwards compatibility
        private String contentType = "application/xml";
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param startTime start time in ms
	 * @param endTime end time in ms (can be <= startTime for end = start)
	 *
	 */
	public MapEposEvent(long startTime, long endTime) {
		
		this.map = new HashMap<String, Object>();
		
		this.start = startTime;
		if (endTime > startTime) {
			this.end = endTime;
		}
		else {
			this.end = this.start;
		}
		
		this.initialize();
	}
	
	/**
	 * Constructor creating a duplication of the
	 * passed MapEvent.
	 * 
	 * @param duplicate the MapEvent to be duplicated
	 */
	public MapEposEvent(MapEposEvent duplicate) {
		this.map = new HashMap<String, Object>(duplicate.map);
		
		this.start = duplicate.start;
		this.end = duplicate.end;
	}




	/**
	 * initializes the event map
	 */
	private void initialize() {
		//create causal vector
		Vector<EposEvent> causality = new Vector<EposEvent>();
		this.put(CAUSALITY_KEY,causality);
		
		//set start and end time
		this.put(START_KEY, this.start);
		this.put(END_KEY, this.end);
		
		//set this
		this.put(THIS_KEY, this);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void addCausalAncestor(EposEvent event) {
		((Vector<EposEvent>)this.get(CAUSALITY_KEY)).add(event);
	}
	

	@Override
	public void clear() {
		this.map.clear();
		
		//initialize
		this.initialize();
	}
	

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}
	

	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}
	

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return this.map.entrySet();
	}
	

	@Override
	public Object get(Object key) {
		return this.map.get(key);
	}

	@Override
	public Object getValue(CharSequence key) {
		return this.map.get(key);
	}
	
	@Override
	public void setValue(CharSequence key, Object value) {
		this.put(key.toString(), value);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	

	@Override
	public Set<String> keySet() {
		return this.map.keySet();
	}
	

	@Override
	public Object put(String key, Object value) {
		if (key.equals(ORIGNIAL_OBJECT_KEY)) {
			this.originalObject = value;
		}
		else if (key.equals(VALUE_KEY)) {
			//check if we must add id also as doubleValue and/or stringValue
			try {
				Double doubleTest = Double.parseDouble(value.toString());
				//parsing worked: double value
				this.map.put(DOUBLE_VALUE_KEY, doubleTest);
				this.map.put(STRING_VALUE_KEY, ""+ doubleTest);
			} catch (NumberFormatException e) {
				//no double value, just string
				this.map.put(STRING_VALUE_KEY, value.toString());
			}
		}
		else if (key.equals(START_KEY)) {
			//set value
			this.start = Long.parseLong(value.toString());
		}
		else if (key.equals(END_KEY)) {
			//set value
			this.end = Long.parseLong(value.toString());
		}
		return this.map.put(key, value);
	}
	

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		//iterate through all entries to use local put method
		Set<? extends String> keys = m.keySet();
		for (String key : keys) {
			this.put(key, m.get(key));
		}
	}
	

	@Override
	public Object remove(Object key) {
		//causality and start/end time are not to be removed
		if (key.equals(START_KEY)
			|| key.equals(END_KEY)
			|| key.equals(CAUSALITY_KEY)
			|| key.equals(THIS_KEY)) {
			return null;
		}
		
		return this.map.remove(key);
	}
	

	@Override
	public int size() {
		return this.map.size();
	}
	

	@Override
	public Collection<Object> values() {
		return this.map.values();
	}


	@Override
	public Object getOriginalObject() {
		return this.originalObject;
	}

	@Override
	public long getStartTime() {
		return (Long) this.map.get(START_KEY);
	}
	
	@Override
	public long getEndTime() {
		return (Long) this.map.get(END_KEY);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MapEvent={");
		for (String key : this.map.keySet()) {
			if (!key.equals(MapEposEvent.THIS_KEY) && !key.equals(MapEposEvent.ORIGNIAL_OBJECT_KEY)) {
				if (this.map.get(key) instanceof Map<?, ?>) {
					//recursion
					sb.append(key);
					sb.append("={");
					sb.append(this.writeMapContent((Map<String, Object>) this.map.get(key), ", "));
					sb.append("}, ");
				}
				else if (this.map.get(key) instanceof Vector<?>) {
					//vector recursion
					sb.append(key + "={");
					sb.append(this.writeVectorContent((Vector<Object>) this.map.get(key), ", "));
					sb.append("}, ");
				}
				else {
					sb.append(key +"="+ this.map.get(key) +", ");
				}
			}
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * recursively makes nice strings from maps
	 * @param map the map to print
	 * @param indention add one " |\t" per recursion
	 * @return pretty string of the map
	 */
	@SuppressWarnings("unchecked")
	private String writeMapContent(Map<String, Object> m, String indention) {
		StringBuilder sb = new StringBuilder();
		
		for (String s : m.keySet()) {
			if (s.equals(MapEposEvent.THIS_KEY)) {
				//ignore this reference
			}
			else if (m.get(s) instanceof Map<?, ?>) {
				//map recursion
				sb.append(indention);
				sb.append(s);
				sb.append("={");
				sb.append(this.writeMapContent((Map<String, Object>) m.get(s), indention + ", "));
				sb.append("}");
			}
			else if (m.get(s) instanceof Vector<?>) {
				//vector recursion
				sb.append(indention);
				sb.append(s);
				sb.append("={");
				sb.append(this.writeVectorContent((Vector<Object>) m.get(s), indention + ", "));
				sb.append("}");
			}
			else {
				sb.append(indention);
				sb.append(s);
				sb.append("=");
				sb.append(m.get(s));
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}

	/**
	 * recursively makes nice strings from vectors
	 * @param vector the vector to print
	 * @param indention indention add one " |\t" per recursion
	 * @return pretty string of the vector
	 */
	@SuppressWarnings("unchecked")
	private String writeVectorContent(Vector<Object> vector, String indention) {
		StringBuilder sb = new StringBuilder();
		
		Object item;
		for (int i = 0; i < vector.size(); i++) {
			item = vector.get(i);
			
			if (item instanceof Map<?, ?>) {
				//map recursion
				sb.append(indention);
				sb.append(i);
				sb.append("={");
				sb.append(this.writeMapContent((Map<String, Object>) item, indention + ", "));
				sb.append("}");
			}
			else if (item instanceof Vector<?>) {
				//vector recursion
				sb.append(indention);
				sb.append(i);
				sb.append("={");
				sb.append(this.writeVectorContent((Vector<Object>) item, indention + ", "));
				sb.append("}");
			}
			else {
				//append content
				sb.append(indention);
				sb.append(i);
				sb.append("=");
				sb.append(item.toString());
			}
		}
		
		return sb.toString();
	}

	@Override
	public void setOriginalObject(Object input) {
		setValue(MapEposEvent.ORIGNIAL_OBJECT_KEY, input);		
	}

    @Override
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}