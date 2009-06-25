package playground.mmoyo.TransitSimulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.transitSchedule.TransitStopFacility;
import org.matsim.core.api.network.Link;

import playground.marcel.pt.transitSchedule.TransitLine;
import playground.marcel.pt.transitSchedule.TransitRoute;
import playground.marcel.pt.transitSchedule.TransitRouteStop;
import playground.marcel.pt.transitSchedule.TransitSchedule;
import playground.marcel.pt.transitSchedule.Departure;
import playground.marcel.pt.transitSchedule.TransitScheduleWriterV1;
import playground.mmoyo.PTRouter.PTRouter2;
import playground.mmoyo.PTRouter.PTTimeTable2;

/**
 * Reads a TransitSchedule and creates:
 * -Plain network: A node represent a station. A link represents a simple connection between stations 
 * -logic layer network: with sequences of independent nodes for each TansitLine, include transfer links
 * -logic layer TransitSchedule: with a cloned stop facility for each transit stop of each Transit Line with new Id's mapped to the real transitFaciliteies
 * @param transitSchedule 
 */

public class LogicFactory{
	private TransitSchedule transitSchedule;
	private NetworkLayer logicNet= new NetworkLayer();
	private NetworkLayer plainNet= new NetworkLayer();
	private TransitSchedule logicTransitSchedule = new TransitSchedule(); 
	private LogicToPlainConverter logicToPlainConverter; 
	
	private Map<Id,List<Node>> facilityNodeMap = new TreeMap<Id,List<Node>>(); /** <key =PlainStop, value = List of logicStops to be joined by transfer links>*/
	private Map<Node,Node> logicToPlanStopMap = new TreeMap<Node,Node>(); 
	private Map<Id,Id> nodeLineMap = new TreeMap<Id,Id>();
	
	long newLinkId=0;
	long newPlainLinkId=0;
	long newStopId=0;
	
	final String STANDARDLINK = "Standard";
	
	public LogicFactory(final TransitSchedule transitSchedule){
		this.transitSchedule = transitSchedule;
		createLogicNet();
		this.logicToPlainConverter = new LogicToPlainConverter(plainNet,  logicToPlanStopMap);
	}
	
	/**Creates a logic network file and a logic TransitSchedule file with individualized id's for nodes and stops*/
	private void createLogicNet(){
	
		for (TransitLine transitLine : transitSchedule.getTransitLines().values()){
			TransitLine logicTransitLine = new TransitLine(transitLine.getId()); 
			
			for (TransitRoute transitRoute : transitLine.getRoutes().values()){
				List<TransitRouteStop> logicTransitStops = new ArrayList<TransitRouteStop>();
				Node lastLogicNode = null;
				Node lastPlainNode=null;
				boolean first= true;
				
				/**iterates in each transit stop to create nodes and links */
				for (TransitRouteStop transitRouteStop: transitRoute.getStops()) { 
					TransitStopFacility transitStopFacility = transitRouteStop.getStopFacility(); 
					
					/** Create nodes*/
					Coord coord = transitStopFacility.getCoord();
					Id idStopFacility = transitStopFacility.getId();  
					Id logicalId= createLogicalId(idStopFacility, transitRoute.getId()); 
					Node logicNode= logicNet.createNode(logicalId, coord);
					Node plainNode= createPlainNode(transitStopFacility);
					logicToPlanStopMap.put(logicNode, plainNode);
					
					/**fill the facilityNodeMap to create transfer links later on*/
					if (!facilityNodeMap.containsKey(idStopFacility)){
						List<Node> nodeStationArray = new ArrayList<Node>();
						facilityNodeMap.put(idStopFacility, nodeStationArray);
					}
					facilityNodeMap.get(idStopFacility).add(logicNode);

					/**Create links*/
					if (!first){
						createLogicLink(new IdImpl(newLinkId++), lastLogicNode, logicNode, "Standard");
						createPlainLink(lastPlainNode, plainNode);
					}else{
						first=false;
					}

					/**create logical stops and stopFacilities*/
					TransitStopFacility logicTransitStopFacility = new TransitStopFacility(logicalId, coord); 
					logicTransitSchedule.addStopFacility(logicTransitStopFacility);
					TransitRouteStop logicTransitRouteStop = new TransitRouteStop(logicTransitStopFacility, transitRouteStop.getArrivalDelay(), transitRouteStop.getDepartureDelay()); 
					logicTransitStops.add(logicTransitRouteStop);
					
					lastLogicNode= logicNode;
					lastPlainNode= plainNode;
				}
				TransitRoute logicTransitRoute = new TransitRoute(transitRoute.getId(), null, logicTransitStops, transitRoute.getTransportMode());
				for (Departure departure: transitRoute.getDepartures().values()) {
					logicTransitRoute.addDeparture(departure);
				}
				logicTransitRoute.setDescription(transitRoute.getDescription());
				logicTransitLine.addRoute(logicTransitRoute);
			}
			logicTransitSchedule.addTransitLine(logicTransitLine);
		}
		
		createTransferLinks();
		createDetachedTransferLinks(400);
	}
	
	/**Created a new id for a new node. Besides important values are stores in logicStopMap. */ 
	private Id createLogicalId(final Id idStopFacility, final Id lineId){
		Id newId = new IdImpl(newStopId++);
		nodeLineMap.put(newId, lineId);
		return newId;
	}
	
	private void createTransferLinks(){
		for (List<Node> chList : facilityNodeMap.values()) 
			for (Node fromNode : chList) 
				for (Node toNode : chList) 
					if (!fromNode.equals(toNode) && willJoin2StandardLinks(fromNode, toNode)){
						Id idNewLink = new IdImpl("T" + ++newLinkId);
						createLogicLink(idNewLink, fromNode, toNode, "Transfer");
					}
		facilityNodeMap = null;
	}
	
	public void createDetachedTransferLinks (final double distance){
		for (Node centerNode: logicNet.getNodes().values()){
			Collection<Node> nearNodes= logicNet.getNearestNodes(centerNode.getCoord(), distance);
			nearNodes.remove(centerNode);
			for (Node nearNode : nearNodes){
				boolean areConected = centerNode.getOutNodes().containsValue(nearNode); 
				boolean belongToSameLine = nodeLineMap.get(centerNode.getId()) == nodeLineMap.get(nearNode.getId()); 
				if (!belongToSameLine && !areConected && willJoin2StandardLinks(centerNode, nearNode)){  /**avoid joining nodes that are already joined by standard links AND joining nodes of the same Line*/
					Id idNewLink = new IdImpl("DT" + ++newLinkId);
					createLogicLink(idNewLink, centerNode, nearNode, "DetTransfer");
				}
			}
		}
		//NodeLineMap= null;??
	}
	
	/**Asks if the fromNode has at least a standard inLink and also if the toNode has at least a standard outLink
	 * Otherwise it is senseless to create a transfer link between them */
	private boolean willJoin2StandardLinks(Node fromNode, Node toNode){
		int numInGoingStandards =0;
		for (Link inLink : fromNode.getInLinks().values()){
			if (inLink.getType().equals(STANDARDLINK)) 	numInGoingStandards++;
		}	
		
		int numOutgoingStandards =0;
		for (Link outLink : toNode.getOutLinks().values()){
			if (outLink.getType().equals(STANDARDLINK)) numOutgoingStandards++;
		}
		
		return (numInGoingStandards>0) && (numOutgoingStandards>0); 
	}
	
	
	
	/**links for the logical network, one for transitRoute*/
	private void createLogicLink(Id id, Node fromNode, Node toNode, String type){
		double length= CoordUtils.calcDistance(fromNode.getCoord(), toNode.getCoord());
		logicNet.createLink(id, fromNode, toNode, length , 1.0 , 1.0, 1.0, "0", type);	
	}
	
	/**links for the plain network, only one between two stations*/
	private void createPlainLink(Node fromNode, Node toNode){
		if (!fromNode.getOutNodes().containsValue(toNode)){
			double length= CoordUtils.calcDistance(fromNode.getCoord(), toNode.getCoord());
			plainNet.createLink(new IdImpl(newPlainLinkId++), fromNode, toNode, length, 1.0, 1.0 , 1);
		}
	}
	
	private Node createPlainNode(TransitStopFacility transitStopFacility){
		Node plainNode = null;
		Id id = transitStopFacility.getId();
		if (this.plainNet.getNodes().containsKey(id)){
			plainNode = plainNet.getNode(id);
		}else{
			plainNode = plainNet.createNode(id, transitStopFacility.getCoord());
		}
		return plainNode;
	}
	
	public void writeLogicElements(final String outPlainNetFile, final String outTransitScheduleFile, final String outLogicNetFile ){
		/**Writes logicTransitSchedule*/
		TransitScheduleWriterV1 transitScheduleWriterV1 = new TransitScheduleWriterV1 (this.logicTransitSchedule);
		try{
			transitScheduleWriterV1.write(outTransitScheduleFile);
		} catch (IOException ex) {
			System.out.println(this + ex.getMessage());
		}
		new NetworkWriter(logicNet, outLogicNetFile).write();
		//new NetworkWriter(plainNet, outPlainNetFile).write();   //->for the time being the plainNet is itself the input
		System.out.println("done.");
	}

	/****************get methods************/  // -->
	public NetworkLayer getLogicNet(){
		return this.logicNet;
	}

	public NetworkLayer getPlainNet(){
		return this.plainNet;
	}
	
	public LogicToPlainConverter getLogicToPlainConverter(){
		return this.logicToPlainConverter ;
	}

	public PTRouter2 getPTRouter(){
		PTTimeTable2 logicPTTimeTable = new PTTimeTable2();
		TransitTravelTimeCalculator transitTravelTimeCalculator = new TransitTravelTimeCalculator(logicTransitSchedule,logicNet);
		transitTravelTimeCalculator.fillTimeTable(logicPTTimeTable);
		PTRouter2 ptRouter = new PTRouter2(logicNet, logicPTTimeTable, logicToPlainConverter);
		return ptRouter; 
	}

}
