/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2018.
 */

package ch.sbb.matsim.routing.pt.raptor;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.pt.PtConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author mrieser / SBB
 */
public class IntermodalAwareRouterModeIdentifierTest {

    @Test
    public void testSimplePt() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Collections.singletonList(
                PopulationUtils.createLeg(TransportMode.pt)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }

    @Test
    public void testPtWithAccessEgress() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }

    // Found this old test and renamed it. I'm wondering why it has no intermodal Leg (e.g. a "bike" leg) and why there is no
    // Stage activity between the pt and the non_network-walk leg
    @Test
    public void testPtWithIntermodalAccessEgressStageActivity() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.bike),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }
    
    @Test
    public void testPtWithIntermodalAccess() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.drt),
                PopulationUtils.createLeg(TransportMode.drt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.drt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }
    
    @Test
    public void testPtWithIntermodalEgress() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.pt),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, "drt2"),
                PopulationUtils.createLeg("drt2"),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, "drt2"),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }

    @Test
    public void testPtCustomModes() {
        Config config = ConfigUtils.createConfig();
        config.transit().setTransitModes(CollectionUtils.stringToSet("train,bus"));
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg("train"),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals("train", identifiedMode);
    }

    @Test
    public void testPtWalkOnly() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Collections.singletonList(
                PopulationUtils.createLeg(TransportMode.transit_walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.pt, identifiedMode);
    }

    @Test
    public void testNonPt() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Collections.singletonList(
                PopulationUtils.createLeg(TransportMode.bike)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.bike, identifiedMode);
    }

    @Test
    public void testNonPtWithAccessEgress() {
        Config config = ConfigUtils.createConfig();
        IntermodalAwareRouterModeIdentifier identifier = new IntermodalAwareRouterModeIdentifier(config);
        List<PlanElement> tripElements = Arrays.asList(
                PopulationUtils.createLeg(TransportMode.walk),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.bike),
                PopulationUtils.createStageActivityFromCoordLinkIdAndModePrefix(new Coord(0, 0), null, TransportMode.pt),
                PopulationUtils.createLeg(TransportMode.walk)
        );
        String identifiedMode = identifier.identifyMainMode(tripElements);
        Assert.assertEquals(TransportMode.bike, identifiedMode);
    }

}
