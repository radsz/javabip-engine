package org.bip.engine;

import java.util.ArrayList;
import java.util.Hashtable;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.bip.api.BIPComponent;
import org.bip.behaviour.Port;
import org.bip.exceptions.BIPEngineException;

/**
 * Receives the current state, glue and behaviour BDDs.
 * Computes the possible maximal interactions and picks one non-deterministically.
 * Notifies the OSGiBIPEngine about the outcome.
 * @author mavridou
 */
public interface BDDBIPEngine {

	/**
	 * @param component
	 * @param componentBDD BDD corresponding to the current state of the particular component
	 */
	void informCurrentState(BIPComponent component, BDD componentBDD);
	
	/**
	 * @param component
	 * @param componentBDD BDD corresponding to the behavior of the particular component
	 */
	void informBehaviour(BIPComponent component, BDD componentBDD);
	
	/**
	 * @param totalGlue BDD corresponding to the total glue of the components of the system
	 */
	void informGlue(BDD totalGlue);
	
	void totalBehaviourBDD();
	
	/**
	 * Computes possible maximal interactions and chooses one non-deterministically
	 * @throws BIPEngineException 
	 */
	void runOneIteration() throws BIPEngineException;
	
	/**
	 * Setter for the OSGiBIPEngine
	 */
	void setOSGiBIPEngine(BIPCoordinator wrapper);
	
	/**
	 * Getter for the BDD Manager
	 */
	BDDFactory getBDDManager(); 
	
	/**
	 * @return the position of port BDDs in the BDD Manager
	 */
	ArrayList<Integer> getPositionsOfPorts();
	
	Hashtable<Port, Integer> getPortToPosition();



}


