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

package org.n52.epos.engine.esper;


import org.n52.epos.pattern.CustomStatementEvent;
import org.n52.epos.engine.esper.concurrent.ThreadPool;
import org.n52.epos.event.MapEposEvent;
import org.n52.epos.filter.pattern.EventPattern;
import org.n52.epos.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EventBean;

/**
 * listener for a single esper pattern
 * 
 * @author Thomas Everding
 * 
 */
public class StatementListener implements UpdateListener {

	private EventPattern pattern;

	private EsperController controller;

	private Rule rule;

	private static int instanceCount = 1;

	private int instanceNumber;

	private static final Logger logger = LoggerFactory
			.getLogger(StatementListener.class);

	/**
	 * 
	 * Constructor
	 * 
	 * @param pattern
	 *            one {@link Statement}, used to configure this listener
	 * @param controller
	 *            the esper controller with the esper engine
	 */
	public StatementListener(EventPattern statement, EsperController controller) {
		this.pattern = statement;
		this.controller = controller;

		this.initialize();
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param pattern
	 *            pattern one {@link Statement}, used to configure this
	 *            listener
	 * @param controller
	 *            the esper controller with the esper engine
	 * @param rule
	 *            the subscription manager
	 */
	public StatementListener(EventPattern statement, EsperController controller,
			Rule rule) {
		this(statement, controller);
		this.rule = rule;
	}

	/**
	 * initializes the listener
	 */
	private void initialize() {
		// set instance number
		this.instanceNumber = instanceCount;
		instanceCount++;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		/*
		 * new matches for the pattern received
		 * 
		 * handle every match
		 */
		if (newEvents != null && newEvents.length > 0) {
			for (EventBean newEvent : newEvents) {
				this.handleMatch(newEvent);
			}
			
			if (this.pattern.hasCustomStatementEvents()) {
				for (CustomStatementEvent cse : this.pattern.getCustomStatementEvents()) {
					cse.eventFired(newEvents, this.rule);
				}
			}
		}
	}

	/**
	 * handles a single pattern match
	 * 
	 * @param newEvent
	 *            the EventBean representing the match
	 */
	protected synchronized void handleMatch(EventBean newEvent) {
		
		logger.debug("Statement {} matched for Event '{}'",
				this.pattern, newEvent.getUnderlying().toString());
		
		UpdateHandlerThread handler = new UpdateHandlerThread(this, newEvent);
		
		//handle match in its own thread using a ThreadPool
		ThreadPool tp = ThreadPool.getInstance();
		tp.execute(handler);
	}
	
	
//	/**
//	 * Sends the received result to the controller for output
//	 * 
//	 * @param resultEvent the result to send
//	 */
//	public synchronized void doOutput(MapEvent resultEvent) {
//		String outputName = this.statement.getSelectFunction().getOutputName();
//		
//		//load output description
//		if (this.outDescription == null) {
//			if (this.getOutDescriptionPerformed) {
//				//output description not found
//				return;
//			}
//			
//			//try to find output description
//			this.outDescription = this.controller.getOutputDescription(outputName);
//			this.getOutDescriptionPerformed = true;
//			
//			if (this.outDescription == null) {
//				//not found
//				return;
//			}
//		}
//		
//		//send output (the whole event or only the value)
//		if (this.outDescription.getDataType().equals(SupportedDataTypes.EVENT)) {
//			//send event
//			this.controller.doOutput(outputName, resultEvent);
//		}
//		else {
//			//send only value
//			this.controller.doOutput(outputName, resultEvent.get(MapEvent.VALUE_KEY));
//		}
//	}
	
	/**
	 * Sends the received result to the controller for output
	 * 
	 * @param resultEvent
	 *            the result to send
	 */
	public synchronized void doOutput(MapEposEvent resultEvent) {
		if (logger.isDebugEnabled())
			logger.debug("performing output for pattern:\n"
					+ this.pattern);

		// check if it is allowed to use the original message.
		// check also if it is used for GENESIS
		if (this.pattern.usesOriginalEventAsOutput()) {
			StatementListener.logger.info("trying to send original message as output");
			// get original message
			Object origMessage = resultEvent.getOriginalObject();
			if (origMessage != null) {
				// get message and forward to SESSubscriptionManager
				StatementListener.logger.info("sending original message");
				this.rule.filter(resultEvent);
			}
			else {
				logger.warn("Event did not contain the original message!");
			}
		}

		else {
			Object eventDoc = this.pattern.getOutputGenerator().generateOutput(resultEvent);
			resultEvent.setOriginalObject(eventDoc);

			this.rule.filter(resultEvent, eventDoc);
		}
	}

	/**
	 * @return the pattern
	 */
	public EventPattern getEventPattern() {
		return this.pattern;
	}

	/**
	 * @return the controller
	 */
	public EsperController getController() {
		return this.controller;
	}

	/**
	 * @return the instanceNumber
	 */
	public int getInstanceNumber() {
		return this.instanceNumber;
	}
}