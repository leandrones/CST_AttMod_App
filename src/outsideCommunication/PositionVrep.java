package outsideCommunication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import CommunicationInterface.SensorI;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;

public class PositionVrep implements SensorI {
	
	private remoteApi vrep;
    private int clientID;
    private FloatWA position;
    private IntW handle;
	
	public PositionVrep (int clientID, IntW handle, remoteApi vrep) {
        this.handle = handle;

        this.vrep = vrep;
        this.clientID = clientID;
        this.position = new FloatWA(3);
	}

	@Override
	public Object getData() {
		FloatWA position = new FloatWA(3);
		vrep.simxGetObjectPosition(clientID, handle.getValue(), -1, position,
                vrep.simx_opmode_oneshot);
		
		printToFile(position, "positions.txt");
		
		return null;
	}
	
	public void resetData() {
		System.out.println("Resseting position");
		vrep.simxPauseCommunication(clientID, true);
		FloatWA position = initFloatWA(false);
		vrep.simxCallScriptFunction(clientID, "Pioneer_p3dx", vrep.sim_scripttype_childscript, "reset",  null , 
				null ,null , null , null, null , null, null, vrep.simx_opmode_blocking);
		vrep.simxSetObjectPosition(clientID, handle.getValue(), -1, position,
                vrep.simx_opmode_oneshot);
		FloatWA angles = initFloatWA(true);
		vrep.simxSetObjectOrientation(clientID, handle.getValue(), -1, angles, vrep.simx_opmode_oneshot);
		vrep.simxPauseCommunication(clientID, false);
		vrep.simxSynchronousTrigger(clientID);

	}
	
	private FloatWA initFloatWA(boolean orient) {
		FloatWA position = new FloatWA(3);
		float[] pos = position.getArray();
		
		if (orient) {
			pos[0] = 0.0f;
			pos[1] = 0.0f;
			pos[2] = (float) Math.random() * 360;
		}
		else {
			pos[0] = (float) Math.random() * 0.5f;
			pos[1] = (float) Math.random() * 0.5f;
			pos[2] = 0.138f;
		}
		return position;
	}
	
	private void printToFile(FloatWA position,String filename) {
		try(FileWriter fw = new FileWriter(filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(position.getArray()[0] + " " + position.getArray()[1]);
	            out.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

}
