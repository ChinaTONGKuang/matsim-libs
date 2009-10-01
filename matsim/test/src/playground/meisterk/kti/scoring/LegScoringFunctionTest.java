/* *********************************************************************** *
 * project: org.matsim.*
 * LegScoringFunctionTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.meisterk.kti.scoring;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.RouteFactory;
import org.matsim.core.population.routes.RouteWRefs;
import org.matsim.core.scoring.CharyparNagelScoringParameters;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

import playground.meisterk.kti.config.KtiConfigGroup;
import playground.meisterk.kti.router.KtiPtRoute;
import playground.meisterk.kti.router.KtiPtRouteFactory;
import playground.meisterk.kti.router.PlansCalcRouteKtiInfo;

public class LegScoringFunctionTest extends MatsimTestCase {

	private Config config = null;
	private KtiConfigGroup ktiConfigGroup = null;
	private NetworkLayer network = null;
	private PersonImpl testPerson = null;
	private PlanImpl testPlan = null;
	private PlansCalcRouteKtiInfo plansCalcRouteKtiInfo = null;

	protected void setUp() throws Exception {
		super.setUp();
		this.config = super.loadConfig(null);

		ktiConfigGroup = new KtiConfigGroup();
		ktiConfigGroup.setUsePlansCalcRouteKti(false);
		ktiConfigGroup.setPtHaltestellenFilename(this.getClassInputDirectory() + "haltestellen.txt");
		ktiConfigGroup.setPtTraveltimeMatrixFilename(this.getClassInputDirectory() + "pt_Matrix.mtx");
		ktiConfigGroup.setWorldInputFilename(this.getClassInputDirectory() + "world.xml");
		ktiConfigGroup.setDistanceCostCar(1.6);
		ktiConfigGroup.setDistanceCostPtNoTravelCard(1.0);
		ktiConfigGroup.setDistanceCostPtUnknownTravelCard(0.5);
		ktiConfigGroup.setTravelingBike(-2.0);
		ktiConfigGroup.setConstBike(-0.5);
		ktiConfigGroup.setConstCar(-0.6);
		this.config.addModule(KtiConfigGroup.GROUP_NAME, ktiConfigGroup);

		CharyparNagelScoringConfigGroup charyparNagelConfigGroup = this.config.charyparNagelScoring();
		charyparNagelConfigGroup.setMarginalUtlOfDistancePt(-0.5);
		charyparNagelConfigGroup.setMarginalUtlOfDistanceCar(-0.1);
		charyparNagelConfigGroup.setTravelingPt(-10.0);
		charyparNagelConfigGroup.setTraveling(0.0);
		charyparNagelConfigGroup.setTravelingWalk(-100.0);

		network = new NetworkLayer();

		network.createAndAddNode(new IdImpl(1), new CoordImpl(1000.0, 1000.0));
		network.createAndAddNode(new IdImpl(2), new CoordImpl(1100.0, 1100.0));
		network.createAndAddNode(new IdImpl(3), new CoordImpl(1200.0, 1200.0));

		network.createAndAddLink(new IdImpl(1), network.getNode("1"), network.getNode("2"), 1.0, 1.0, 1.0, 1.0);
		network.createAndAddLink(new IdImpl(2), network.getNode("2"), network.getNode("3"), 1.0, 1.0, 1.0, 1.0);

		testPerson = new PersonImpl(new IdImpl("123"));
		testPlan = new PlanImpl();
		testPerson.addPlan(testPlan);

		ActivityImpl home = new ActivityImpl("home", network.getLink("1"));
		home.setCoord(new CoordImpl(1050.0, 1050.0));
		ActivityImpl work = new ActivityImpl("work", network.getLink("2"));
		work.setCoord(new CoordImpl(1150.0, 1150.0));

		LegImpl testLeg = new LegImpl(TransportMode.undefined);

		testPlan.addActivity(home);
		testPlan.addLeg(testLeg);
		testPlan.addActivity(work);

	}

	@Override
	protected void tearDown() throws Exception {
		this.plansCalcRouteKtiInfo = null;
		this.testPlan = null;
		this.testPerson = null;
		this.network = null;
		this.ktiConfigGroup = null;
		this.config = null;
		super.tearDown();
	}

	public void testCalcLegScorePt() {
		this.runATest(TransportMode.pt, null, "\n\tabcd", -10.0);
		this.ktiConfigGroup.setUsePlansCalcRouteKti(true);
//		this.runATest(TransportMode.pt, null, "kti=8503006=26101=300.0=26102=8503015", -8.116533);
		this.runATest(TransportMode.pt, null, "kti=8503006=26101=300.0=26102=8503015", -10.0);
	}

	public void testCalcLegScorePtUnknown() {
		this.runATest(TransportMode.pt, "unknown", "", -7.5);
		this.ktiConfigGroup.setUsePlansCalcRouteKti(true);
//		this.runATest(TransportMode.pt, "unknown", "kti=8503006=26101=300.0=26102=8503015", -8.010467);
		this.runATest(TransportMode.pt, "unknown", "kti=8503006=26101=300.0=26102=8503015", -7.5);
	}	

	public void testCalcLegScoreCar() {
		this.runATest(TransportMode.car, null, null, -2.2);
	}

	public void testCalcLegScoreBike() {
		this.runATest(TransportMode.bike, null, null, -1.5);
	}

	public void testCalcLegScoreWalk() {
		this.runATest(TransportMode.walk, null, null, -50.0);
	}

	private void runATest(final TransportMode mode, final String travelCard, final String routeDescription, final double expectedScore) {

		if (travelCard != null) {
			this.testPerson.addTravelcard(travelCard);
		}

		LegImpl testLeg = (LegImpl) this.testPlan.getPlanElements().get(1);
		testLeg.setMode(mode);

		Link startLink = testPlan.getPreviousActivity(testLeg).getLink();
		Link endLink = testPlan.getNextActivity(testLeg).getLink();

		if (this.ktiConfigGroup.isUsePlansCalcRouteKti()) {
			this.plansCalcRouteKtiInfo = new PlansCalcRouteKtiInfo();
			this.plansCalcRouteKtiInfo.prepare(ktiConfigGroup, this.network);
		}
		RouteFactory ptRouteFactory = new KtiPtRouteFactory(this.plansCalcRouteKtiInfo);
		this.network.getFactory().setRouteFactory(TransportMode.pt, ptRouteFactory);

		RouteWRefs route = network.getFactory().createRoute(mode, startLink, endLink);
		testLeg.setRoute(route);
		route.setDistance(10000.0);
		if (route instanceof KtiPtRoute) {
			((KtiPtRoute) route).setRouteDescription(startLink, routeDescription, endLink);
		}

		CharyparNagelScoringParameters charyparNagelParams = new CharyparNagelScoringParameters(config.charyparNagelScoring());
		LegScoringFunction testee = new LegScoringFunction(
				testPlan, 
				charyparNagelParams,
				config,
				this.ktiConfigGroup);
		double actualLegScore = testee.calcLegScore(Time.parseTime("06:00:00"), Time.parseTime("06:30:00"), testLeg);

		assertEquals(expectedScore, actualLegScore, 1e-6);

	}

}
