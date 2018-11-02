package codelets.learner;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import outsideCommunication.OutsideCommunication;
import outsideCommunication.SonarData;


/**
 * @author carolina
 *
 */
public class LearnerCodelet extends Codelet 

{

	private int time_graph;
	private static float CRASH_TRESHOLD = 0.5f;
	
	private QLearning ql;
    

    private List winnersList;
    private List saliencyMap;
    private SonarData sonarReadings;
    private MemoryObject motorActionMO;
    private MemoryObject leftMotorMO;
    private MemoryObject rightMotorMO;
    
    private List<String> actionsList;
    private List<String> statesList;
    
    private OutsideCommunication oc;
    private int timeWindow;
    private int sensorDimension;
    

	
	public LearnerCodelet (OutsideCommunication outc, int tWindow, int sensDim) {
		
		super();
		time_graph = 0;
		
		ArrayList<String> allActionsList  = new ArrayList<>(Arrays.asList("Move Foward", "Do nothing", "Turn to Winner"));
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
		
		
		oc = outc;
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
        MO = (MemoryObject) this.getInput("SALIENCY_MAP");
        saliencyMap = (List) MO.getI();
        MO = (MemoryObject) this.getInput("WINNERS");
        winnersList = (List) MO.getI();
//        MO = (MemoryObject) this.getInput("SONARS");
//        sonarReadings = (SonarData) MO.getI();
        
        motorActionMO = (MemoryObject) this.getOutput("MOTOR");
        leftMotorMO = (MemoryObject) this.getOutput("L_M_SPEED");
        rightMotorMO = (MemoryObject) this.getOutput("R_M_SPEED");
        
        MO = (MemoryObject) this.getOutput("ACTIONS");
        actionsList = (List) MO.getI();
        
        MO = (MemoryObject) this.getOutput("STATES");
        statesList = (List) MO.getI();
        
        
        
		
	}

	// This abstract method must be implemented by the user. 
	// Here, the user must calculate the activation of the codelet
	// before it does what it is supposed to do in proc();

	@Override
	public void calculateActivation() {
		// TODO Auto-generated method stub
		
	}
	
	public static Object getLast(List list) {
		if (list.size() != 0) {
			return list.get(list.size()-1);
		}
		return null;
	}

	
	// Normalize and transform a salience map into one state
	// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
	// Them this values are computed into one respective state
	public String getStateFromSalMap() {
		
		ArrayList<Float> lastLine;
		// Getting just the last entry (current sal map)
		lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
		
		// For normalizing readings between 0 and 1 before transforming to state 
		Float max = Collections.max(lastLine);
		Float min = Collections.min(lastLine);		
		
		Integer discreteVal = 0;
		Integer stateVal = 0;
		for (int i=0; i < sensorDimension; i++) {
			// Normalizing value
			Float normVal = (lastLine.get(i)-min)/(max-min);

			// Getting discrete value
			if (normVal < 0.2) {
				discreteVal = 0;
			}
			else if (normVal < 0.4) {
				discreteVal = 1;
			}
			else if (normVal < 0.6) {
				discreteVal = 2;
			}
			else if (normVal < 0.8) {
				discreteVal = 3;
			}
			else if (normVal < 1) {
				discreteVal = 4;
			}
			
			// Getting state from discrete value
			stateVal += (int) Math.pow(5, i)*discreteVal;
			
		}
		
		return stateVal.toString();
	}
	
	//@Override
	//public synchronized boolean shouldLoop() 
	//{
	//	return false;
	//}

	// Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
		
		String state = "-1";
		if (!saliencyMap.isEmpty() && !winnersList.isEmpty()) {
			
			Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
			Integer winnerIndex = lastWinner.featureJ;
			
			if (!actionsList.isEmpty()) {
				// Find reward of the current state, given current winner (TODO maybe previous winner)
				
				Double reward = find_reward(winnerIndex);
				// Gets last action taken
				String lastAction = actionsList.get(actionsList.size() - 1);
				// Gets last state that was in
				String lastState = statesList.get(statesList.size() - 1);
				// Updates QLearning table
				ql.update(lastState, lastAction, reward);
			}
			
			
			state = getStateFromSalMap();
			statesList.add(state);
			
			
			// Selects new best action to take
			String actionToTake = ql.getAction(state);
			actionsList.add(actionToTake);
			
			
			motorActionMO.setI(actionToTake);
			
			// Apply action
			if (actionToTake == "Move Foward") {
				// Sets leftMototMO and right to velocity proportional to winner feature
				leftMotorMO.setI(2f);
				rightMotorMO.setI(2f);
				
			}
			else if (actionToTake == "Turn to Winner") {
				// get robot orientation
				Float pioneer_orientation = (Float) oc.pioneer_orientation.getData();
				// get winner orientation
				Float winner_orientation = (Float) oc.sonar_orientations.get(winnerIndex).getData();
				//Fazer a diferença dos angulos e setar velocidades nas rotas corretamente
				//ex: andar por 2 s (tempo definido no Motor Codelete) x angulos -- qual a velocidade em cada roda? (uma zero e a outra v)
				
				
			}
		}

		printToFile(state, "states.txt");
		
		
	}
	
	
	
	public double find_reward(Integer winnerIndex) {
		
		// crashed winner
//		if (sonarReadings.sonar_readings.get(winnerIndex) < CRASH_TRESHOLD) {
//			return 10d;
//		}
//		// check if crashed other thing
//		for (int i=0; i < sensorDimension; i++) {
//			if (i != winnerIndex && sonarReadings.sonar_readings.get(i) < CRASH_TRESHOLD) {
//				return -10d;
//			} 
//		}
		// nothing
		return 0d;
			
	}
	
	private void printToFile(Object object,String filename){
        
        if (time_graph < 50) {
	        try(FileWriter fw = new FileWriter(filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(time_graph+" "+ object);
	            time_graph++;
	            out.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
        
        
    }

	

}

