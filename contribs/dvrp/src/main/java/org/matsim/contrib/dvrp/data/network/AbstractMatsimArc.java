/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.data.network;

import org.matsim.api.core.v01.network.Link;

/**
 * TODO The current implementation is simplistic; the class will be re-implemented in the future.
 * 
 * @author michalm
 */
public abstract class AbstractMatsimArc
    implements MatsimArc
{
    protected final Link fromLink;
    protected final Link toLink;


    public AbstractMatsimArc(Link fromLink, Link toLink)
    {
        this.fromLink = fromLink;
        this.toLink = toLink;
    }


    @Override
    public Link getFromLink()
    {
        return fromLink;
    }


    @Override
    public Link getToLink()
    {
        return toLink;
    }


    @Override
    public int getTimeOnDeparture(int departureTime)
    {
        // no interpolation between consecutive timeSlices!
        return getShortestPath(departureTime).travelTime;
    }


    @Override
    public int getTimeOnArrival(int arrivalTime)
    {
        // TODO: very rough!!!
        return getShortestPath(arrivalTime).travelTime;

        // probably a bit more accurate but still rough and more time consuming
        // return shortestPath.getSPEntry(arrivalTime -
        // shortestPath.getSPEntry(arrivalTime).travelTime);
    }


    @Override
    public double getCostOnDeparture(int departureTime)
    {
        // no interpolation between consecutive timeSlices!
        return getShortestPath(departureTime).travelCost;
    }
}
