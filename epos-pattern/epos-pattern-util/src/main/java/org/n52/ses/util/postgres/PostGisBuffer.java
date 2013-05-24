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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.ses.util.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.n52.epos.pattern.spatial.ICreateBuffer;
import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.oxf.conversion.unit.ucum.UCUMTools;
import org.n52.ses.util.postgres.PostgresConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Matthes Rieke
 *
 */
public class PostGisBuffer implements ICreateBuffer {
	
	private static final Logger logger = LoggerFactory
			.getLogger(PostGisBuffer.class);
	
	private Connection connection;

	/**
	 * 
	 * Constructor
	 *
	 */
	public PostGisBuffer() {
		try {
			initDBConnection();
		} catch (SQLException e) {
			PostGisBuffer.logger.warn(e.getMessage(), e);
		}
	}


	public Geometry buffer(Geometry geom, double distance, String ucumUom,
			String crs) {

		NumberWithUOM values = UCUMTools.convert(ucumUom, distance);

		if (!values.getUom().equals("m")) {
			throw new IllegalStateException("Could not convert uom: "+ucumUom);
		}

		List<String> resultSet = null;

		try {
			resultSet = invokeQuery(createBufferStatement(geom, values.getValue() ));
		} catch (NumberFormatException e1) {
			logger.warn(e1.getMessage(), e1);
		} catch (SQLException e1) {
			logger.warn( e1.getMessage(), e1);
		}

		String wktString;
		if (resultSet != null) {
			wktString = resultSet.get(0);
		} else {
			throw new IllegalStateException("Could not receive result from PostGIS.");
		}

		try {
			return new WKTReader().read(wktString);
		} catch (ParseException e) {
			throw new IllegalStateException(e);
		}
	}


	private void initDBConnection() throws SQLException {
		this.connection = PostgresConnection.getInstance().getConnection();
		PostGisBuffer.logger.info("... connection initalized");
	}

	private List<String> invokeQuery(String query) throws SQLException {
		PostGisBuffer.logger.info("invoking postgis query:\n\t" + query);
		if (this.connection != null) {

			ArrayList<String> result = new ArrayList<String>();
			Statement st = this.connection.createStatement();
			ResultSet rs = st.executeQuery(query);

			StringBuilder log = new StringBuilder();
			log.append("query result:");
			while (rs.next()) {
				result.add(rs.getString(1));
				log.append("\n\t" + rs.getString(1));
			}
			PostGisBuffer.logger.info(log.toString());

			rs.close();
			st.close();
			return result;
		}
		
		PostGisBuffer.logger.warn("connection is null");
		return null;
	}

	private static String createBufferStatement(Geometry geom, double distMeter) {
		List<Geometry> geomList = resolveSegmentizedGeometry(geom);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Select st_astext(");
		if (geomList.size() == 1) {
			sb.append("geometry(st_buffer(geography(st_geomfromtext('");
			sb.append(geomList.get(0).toText());
			sb.append("', 4326)), ");
			sb.append(distMeter);
			sb.append("))");
		} else if (geomList.size() > 1){
			createBufferUnionStatement(geomList, sb, distMeter);
		} else {
			throw new IllegalStateException("No geometry available!");
		}
		sb.append(")"); // end st_astext

		return sb.toString();
	}
	
	private static void createBufferUnionStatement(List<Geometry> geomList,
			StringBuilder sb, double distMeter) {
		sb.append("st_union(");
		sb.append("ARRAY[");
		for (Geometry geometry : geomList) {
			sb.append("geometry(st_buffer(geography(st_geomfromtext('");
			sb.append(geometry.toText());
			sb.append("', 4326)), ");
			sb.append(distMeter);
			sb.append("))");
			sb.append(",");
		}
		sb.delete(sb.length()-1, sb.length());
		sb.append("]"); // end ARRAY
		sb.append(")"); // end st_union
	}


	private static List<Geometry> resolveSegmentizedGeometry(Geometry geom) {
		List<Geometry> result = new ArrayList<Geometry>();
		
		if (geom instanceof Point) {
			result.add(geom);
		} else if (geom instanceof LineString) {
			resolveLineStringSegments((LineString) geom, result);
		} else {
			throw new UnsupportedOperationException("Only Point and LineString are currently supported for Buffering.");
		}
		
		return result;
	}


	private static void resolveLineStringSegments(LineString geom, List<Geometry> result) {
		for (int i = 0; i < geom.getCoordinateSequence().size() - 1; i++) {
			result.add(geom.getFactory().createLineString(new Coordinate[] {
					geom.getCoordinateSequence().getCoordinate(i),
					geom.getCoordinateSequence().getCoordinate(i+1)
			}));
		}
	}
	
	
}