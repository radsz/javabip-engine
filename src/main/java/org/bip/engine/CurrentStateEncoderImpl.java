package org.bip.engine;

import java.util.ArrayList;
import java.util.Hashtable;

import net.sf.javabdd.BDD;

import org.bip.api.BIPComponent;
import org.bip.behaviour.Port;
import org.bip.exceptions.BIPEngineException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives information about the current state and the list of disabled ports of each registered component and computes the current state BDDs.
 * @author mavridou
 */

/** Computes the BDD of the Current State of all components */
public class CurrentStateEncoderImpl implements CurrentStateEncoder {

	private BehaviourEncoder behaviourEncoder; 
	private BDDBIPEngine engine;
	private BIPCoordinator wrapper;

	private Logger logger = LoggerFactory.getLogger(CurrentStateEncoderImpl.class);


	public BDD inform(BIPComponent component, String currentState, ArrayList<Port> disabledPorts) {
		Integer CompID = wrapper.getBIPComponentIdentity(component);

		ArrayList<String> componentStates = (ArrayList<String>) wrapper.getBehaviourById(CompID).getStates();
		
		if (currentState == null) {
	        try {
				throw new BIPEngineException("Current State of component is null");
			} catch (BIPEngineException e) {
				e.printStackTrace();
				logger.error("Component did not inform about its current state correctly");
			}
	      }


		int StateID = 0;
		for (int i = 1; i <= componentStates.size(); i++) {
			if (componentStates.get(i - 1).equals(currentState)) {
				StateID = i;
				break;
			}
		}

		int[] noDisabledPorts = new int[disabledPorts.size()];
		Hashtable<String, ArrayList<Port>> statePorts = new Hashtable<String, ArrayList<Port>>();
		statePorts = (Hashtable<String, ArrayList<Port>>) wrapper.getBehaviourById(CompID).getStateToPorts();
		ArrayList<Port> componentPorts = new ArrayList<Port>();
		componentPorts = statePorts.get(currentState);

		for (int l = 0; l < disabledPorts.size(); l++) {
			int k = 0;
			while (disabledPorts.get(l) != componentPorts.get(k)) {
				if (k == statePorts.get(StateID).size() - 1) {
					try {
						throw new BIPEngineException("Disabled Port cannot be found.");
					} catch (BIPEngineException e) {
						e.printStackTrace();
						logger.error(e.getMessage());	
					} 
				}
				k++;
			}
			if (disabledPorts.get(l) == componentPorts.get(k))
				noDisabledPorts[noDisabledPorts.length - 1] = k;
		}

		return componentCurrentStateBDD(CompID, StateID, noDisabledPorts);
	}

	private synchronized BDD componentCurrentStateBDD(Integer ComponentID, int stateID, int[] disabledPorts) {

		int noStates =  ((ArrayList<String>) wrapper.getBehaviourById(ComponentID).getStates()).size();
		int noPorts = ((ArrayList<Port>)wrapper.getBehaviourById(ComponentID).getEnforceablePorts()).size();
		BDD[] portsBDDs = new BDD[noPorts];
		BDD[] statesBDDs = new BDD[noStates];
		BIPComponent component = wrapper.getBIPComponent(ComponentID);

		for (int i = 0; i < noPorts; i++) {
			portsBDDs[i] = behaviourEncoder.getPortBDDs().get(component)[i];
		}
		for (int j = 0; j < noStates; j++) {
			statesBDDs[j] = behaviourEncoder.getStateBDDs().get(component)[j];
		}

		BDD partialBDD[] = new BDD[2];
		BDD aux1 = null;
		try {
			aux1 = engine.getBDDManager().one();
			for (int k = 1; k <= noStates; k++) {
				if (stateID == k) {
					partialBDD[0] = aux1.and(statesBDDs[k - 1]);
				} else {
					partialBDD[0] = aux1.and(statesBDDs[k - 1].not());
				}
				if (k != noStates) {
					aux1.free();
					aux1 = partialBDD[0];
				}
			}
		} finally {
			if (aux1 != null)
				aux1.free();
		}

		BDD aux2 = null;
		try {
			aux2 = engine.getBDDManager().one();
			for (int k = 0; k < disabledPorts.length; k++) {
				partialBDD[1] = aux2.and(portsBDDs[disabledPorts[k]].not());
				if (k != disabledPorts.length - 1) {
					aux2.free();
					aux2 = partialBDD[1];
				}
			}
		} finally {
			if (aux2 != null)
				aux2.free();
		}

		BDD componentCurrentStateBDD;
		if (disabledPorts.length != 0) {
			componentCurrentStateBDD = partialBDD[0].and(partialBDD[1]);
			partialBDD[0].free();
			partialBDD[1].free();
		} else {
			componentCurrentStateBDD = partialBDD[0];
		}
		
//		if (componentCurrentStateBDD == null) {
//	        try {
//				throw new BIPEngineException("Current State BDD of component is null");
//			} catch (BIPEngineException e) {
//				e.printStackTrace();
//				logger.error(" Current State BDD was not computed correctly");
//			}
//	      }


		return componentCurrentStateBDD;
	}
	
	public void setBIPCoordinator(BIPCoordinator wrapper) {
		this.wrapper = wrapper;

	}

	public void setBehaviourEncoder(BehaviourEncoder behaviourEncoder) {
		this.behaviourEncoder = behaviourEncoder;
	}

	public void setEngine(BDDBIPEngine engine) {
		this.engine = engine;
	}


}
