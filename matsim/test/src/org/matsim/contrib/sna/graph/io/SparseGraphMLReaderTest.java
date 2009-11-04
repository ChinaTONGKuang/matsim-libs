/* *********************************************************************** *
 * project: org.matsim.*
 * SparseGraphMLReaderTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.contrib.sna.graph.io;

import org.matsim.contrib.sna.graph.SparseGraph;

import junit.framework.TestCase;

/**
 * @author illenberger
 *
 */
public class SparseGraphMLReaderTest extends TestCase {

	private static final String GRPAH_FILE = "test/input/org/matsim/contrib/sna/graph/io/test.graphml.gz";
	
	public void test() {
		SparseGraphMLReader reader = new SparseGraphMLReader();
		SparseGraph graph = reader.readGraph(GRPAH_FILE);
		
		assertEquals("The graph does not contain 1000 vertices.", 1000, graph.getVertices().size());
		assertEquals("the graph does not contain 49334 edges.", 49334, graph.getEdges().size());
	}
}
