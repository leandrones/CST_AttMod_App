package codelets.learner;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import attention.Winner;
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
    private String salMapName;
    private String winnersListName;
    private String distListName;
    private String leftMotorName;
    private String rightMotorName;
    private String actionsListName;
    private String statesListName;


    private List winnersList;
    private List saliencyMap;
    private List distList;
    private MemoryObject leftMotorMO;
    private MemoryObject rightMotorMO;
    private List<String> actionsList;
    private List<String> statesList;
    private int timeWindow;
    private int sensorDimension;
    

	
	public LearnerCodelet (String winnersLName, String salMName, String distLName,
			String leftMName, String rightMName, 
				String actionsLName, String statesLName,
					int tWindow, int sensDim) {
		
		super();
		
		ArrayList<String> allActionsList  = new ArrayList<>(Arrays.asList("Go to Winner", "Do nothing", "Turn to Winner"));
		// States are 0 1 2 ... 5^8-1
		ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(5, 8)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		// QLearning initialization
		ql = new QLearning();
		ql.setActionsList(allActionsList);

		// Initialize QTable to 0
		for (int i=0; i < allStatesList.size(); i ++) {
			for (int j=0; j < allActionsList.size(); j++) {
				ql.setQ(0, allStatesList.get(i), allActionsList.get(j));
			
			}
		}
		//ql.printQ();
		
		salMapName = salMName;
		winnersListName = winnersLName;
		distListName = distLName;
		leftMotorName = leftMName;
		rightMotorName = rightMName;
		actionsListName = actionsLName;
		statesListName = statesLName;
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
        MO = (MemoryObject) this.getInput(distListName);
        distList = (List) MO.getI();
        
        
        leftMotorMO = (MemoryObject) this.getOutput(leftMotorName);
        rightMotorMO = (MemoryObject) this.getOutput(rightMotorName);
        
        MO = (MemoryObject) this.getOutput(actionsListName);
        actionsList = (List) MO.getI();
        
        MO = (MemoryObject) this.getOutput(statesListName);
        statesList = (List) MO.getI();
        
        
        
		
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
		int salMapState = 0;
		if (saliencyMap.size() != 0) {
			
			if (actionsList.size() != 0) {
				// Find reward of the current state
				//TODO: como descobrir a recompensa pelo mapa de saliencia? sera que precisa do mapa de features tbm?
				Double reward = find_reward();
				// Gets last action taken
				String lastAction = actionsList.get(actionsList.size() - 1);
				// Gets last state that was in
				String lastState = statesList.get(statesList.size() - 1);
				// Updates QLearning table
				ql.update(lastState, lastAction, reward);
			}
			
			
			// Transform each feature into a value between 0 and 4
			ArrayList<Float> lastLine;
			// Getting just the last entry
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
			
			int salMapDiscrete = 0;
			for (int i=0; i < sensorDimension; i++) {
				
				if (lastLine.get(i) < -2) {
					salMapDiscrete = 0;
				}
				else if (lastLine.get(i) < -1) {
					salMapDiscrete = 1;
				}
				else if (lastLine.get(i) < 0) {
					salMapDiscrete = 2;
				}
				else if (lastLine.get(i) < 1) {
					salMapDiscrete = 3;
				}
				else if (lastLine.get(i) < 2) {
					salMapDiscrete = 4;
				}
				
				// Getting state from discrete value
				salMapState += (int) Math.pow(5, i)*salMapDiscrete;
				
			}
			
			String state = Integer.toString(salMapState);
			//System.out.println("STATE "+salMapState);
			statesList.add(state);
			
			// Selects new best action to take
			String actionToTake = ql.getAction(state);
			actionsList.add(actionToTake);
			
			// Apply action
			//TODO: dada ação escolhida, como aplicar isso?
			leftMotorMO.setI(2f);
			rightMotorMO.setI(2f);
			
			
		}
		
		
	}
	
	public double find_reward() {
		Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
		List lastDistances = (List) distList.get(distList.size() - 1);
		return 0d;
	}
	

	

}

