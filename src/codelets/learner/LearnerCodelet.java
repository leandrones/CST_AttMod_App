package codelets.learner;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.RawMemory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.memory.WorkingStorage;
import br.unicamp.cst.learning.QLearning;
import br.unicamp.cst.learning.QLearning;

/**
 * @author carolina
 *
 */
public class LearnerCodelet extends Codelet 
{
	private QLearning ql;
	private ArrayList<String> actionsList;
	private ArrayList<String> statesList;
    private String salMapName;
    private String winnersListName;

    private List winnersList;
    private List saliencyMap;
    private int timeWindow;
    private int sensorDimension;
    

	
	public LearnerCodelet (String winnersLName, String salMName, int tWindow, int sensDim) {
		
		super();
		
		actionsList  = new ArrayList<>(Arrays.asList("Go to Winner", "Do nothing", "Turn to Winner"));
		statesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(5, 8)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		// QLearning initialization
		ql = new QLearning();
		ql.setActionsList(actionsList);
		ql.setStatesList(statesList);
		
		salMapName = salMName;
		winnersListName = winnersLName;
		timeWindow = tWindow;
        sensorDimension = sensDim;

	}

	// This method is used in every Codelet to capture input, broadcast 
	// and output MemoryObjects which shall be used in the proc() method. 
	// This abstract method must be implemented by the user. 
	// Here, the user must get the inputs and outputs it needs to perform proc.
	@Override
	public void accessMemoryObjects() {
		MemoryObject MO;
        MO = (MemoryObject) this.getInput(salMapName);
        saliencyMap = (List) MO.getI();
        MO = (MemoryObject) this.getInput(winnersListName);
        winnersList = (List) MO.getI();
        
		
	}

	// This abstract method must be implemented by the user. 
	// Here, the user must calculate the activation of the codelet
	// before it does what it is supposed to do in proc();

	@Override
	public void calculateActivation() {
		// TODO Auto-generated method stub
		
	}

	// Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
		
		
		// Converting salMap to discrete value
		
		//for (int t=0; t < saliencyMap.size(); t++) {
			if (saliencyMap.size() != 0) {
				// Transform each feature into a value between 0 and 4
				ArrayList<Float> lastLine;
				// Getting just the last entry
				lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
				
				ArrayList<Integer> salMapDiscrete = new ArrayList<Integer>() ;
				for (int i=0; i < sensorDimension; i++) {
					
					if (lastLine.get(i) < -2) {
						salMapDiscrete.add(0);
					}
					else if (lastLine.get(i) < -1) {
						salMapDiscrete.add(1);
					}
					else if (lastLine.get(i) < 0) {
						salMapDiscrete.add(2);
					}
					else if (lastLine.get(i) < 1) {
						salMapDiscrete.add(3);
					}
					else if (lastLine.get(i) < 2) {
						salMapDiscrete.add(4);
					}
					
				}
				System.out.println("LEARNER "+salMapDiscrete);
				
				// Getting state from discrete values
				int salMapState = 0;
				for (int i = 0; i < sensorDimension; i++) {
					salMapState += (int) Math.pow(5, i)*salMapDiscrete.get(i);
				}
				System.out.println("STATE "+salMapState);
				
			//}
		}
		
		
	}
	

	

}

