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
import codelets.motor.Lock;
import coppelia.FloatWA;
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
	private static float CRASH_TRESHOLD = 0.07f;
	
	private static int MAX_ACTION_NUMBER = 10;
	
	private static int MAX_EXPERIMENTS_NUMBER = 1;
	
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
    
    private float vel = 2f;
    
    private int global_reward;
    private int action_number;
    private int experiment_number;
    
    private String mode;
	
	public LearnerCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode) {
		
		super();
		time_graph = 0;
		
		global_reward = 0;
		
		action_number = 0;
		
		experiment_number = 1;
		
		ArrayList<String> allActionsList  = new ArrayList<>(Arrays.asList("Move Foward", "Do nothing", "Turn to Winner"));
		// States are 0 1 2 ... 5^8-1
		ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(5, 8)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		// QLearning initialization
		ql = new QLearning();
		ql.setActionsList(allActionsList);
		// learning mode ---> build q table from scratch
		if (mode.equals("learning")) {
		// Initialize QTable to 0
			for (int i=0; i < allStatesList.size(); i ++) {
				for (int j=0; j < allActionsList.size(); j++) {
					ql.setQ(0, allStatesList.get(i), allActionsList.get(j));
				
				}
			}
		}
		// exploring mode ---> reloads q table 
		else {
			ql.recoverQ();
		}
		
		
		oc = outc;
		timeWindow = tWindow;
        sensorDimension = sensDim;
        this.mode = mode;

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
        MO = (MemoryObject) this.getInput("SONARS");
        sonarReadings = (SonarData) MO.getI();
        
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

	
	
	//@Override
	//public synchronized boolean shouldLoop() 
	//{
	//	return false;
	//}

	// Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
		
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
		System.out.println("Reward: "+global_reward);
		String state = "-1";
		
		if (!saliencyMap.isEmpty() && !winnersList.isEmpty()) {
			
			Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
			Integer winnerIndex = lastWinner.featureJ;
			
			if (!actionsList.isEmpty() && mode.equals("learning")) {
				// Find reward of the current state, given previous  winner 
				check_stop_experiment();
				Double reward = 1d;
				global_reward += reward;
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
			
			action_number++;
			
			motorActionMO.setI(actionToTake);
			
			System.out.println("Action "+actionToTake);
			
			// Apply action
			if (actionToTake.equals("Move Foward")) {
				// Sets leftMototMO and right to velocity proportional to winner feature
				leftMotorMO.setI(4.0f);
				rightMotorMO.setI(4.0f);
				
			}
			else if (actionToTake.equals("Turn to Winner")) {
				// get robot orientation
				Float pioneer_orientation = (Float) oc.pioneer_orientation.getData();
				// get winner orientation
				Float winner_orientation = (Float) oc.sonar_orientations.get(winnerIndex).getData();

				Float diff = pioneer_orientation - winner_orientation;
				
				if (diff.floatValue() < 0) {
//					System.out.println("Winner on the left! (diff NEG). Seeting left right speed");
					// Target is on the left of robot: rotate right motor
					leftMotorMO.setI(0f);
					rightMotorMO.setI(3f);
					
				}
				else if (diff.floatValue() > 0) {
//					System.out.println("Winner on the right! (diff POS). Seeting left left speed");
					// Target is on the right of robot: rotate left motor
					leftMotorMO.setI(3f);
					rightMotorMO.setI(0f);
					
				}
				
				
			}
			// Do nothing
			else {
				leftMotorMO.setI(0f);
				rightMotorMO.setI(0f);
			}
		}

		time_graph = printToFile(state, "states.txt", time_graph, true);
		if (mode.equals("exploring")) {
			oc.pioneer_position.getData();
		}
		
	}
	
	
	
	public void check_stop_experiment() {
		
		boolean crashed = false;
		if (!sonarReadings.sonar_readings.isEmpty()){
			// check if crashed anything
			for (int i=0; i < sensorDimension; i++) {
				float reading = sonarReadings.sonar_readings.get(i);
				if ( reading != 0.0f & reading < CRASH_TRESHOLD) {
					System.out.println("Crashed something!");
					crashed = true;
					break;
				} 
			}
		}
		if (action_number >= MAX_ACTION_NUMBER | crashed) {
			System.out.println("Max number of actions or crashed");
			action_number = 0;
			experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false);
			if (experiment_number > MAX_EXPERIMENTS_NUMBER) {
				ql.storeQ();
				System.exit(0);
			}
			oc.pioneer_position.resetData();
			try {
	            Thread.sleep(500);
	        } catch (Exception e) {
	            Thread.currentThread().interrupt();
	        }
		}
		
		
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
				if (normVal <= 0.2) {
					discreteVal = 0;
				}
				else if (normVal <= 0.4) {
					discreteVal = 1;
				}
				else if (normVal <= 0.6) {
					discreteVal = 2;
				}
				else if (normVal <= 0.8) {
					discreteVal = 3;
				}
				else if (normVal <= 1) {
					discreteVal = 4;
				}
				
				// Getting state from discrete value
				stateVal += (int) Math.pow(5, i)*discreteVal;
				
			}
			
			return stateVal.toString();
		}
		
	
	private int printToFile(Object object,String filename, int counter, boolean check){
        
        if (!check || counter < MAX_EXPERIMENTS_NUMBER) {
	        try(FileWriter fw = new FileWriter(filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(counter+" "+ object);
	            
	            out.close();
	            return ++counter;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
      
		return counter;

	}
		

}

