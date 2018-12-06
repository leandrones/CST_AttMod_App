package outsideCommunication;

import CommunicationInterface.SensorI;
import coppelia.CharW;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class OrientationVrep implements SensorI {
	private int time_graph;
	
	private remoteApi vrep;
    private int clientID;
    private FloatWA angles;
    private IntW handle;

	
	public  OrientationVrep( int clientID, IntW handle, remoteApi vrep) {
		this.time_graph = 0;
        this.handle = handle;

        this.vrep = vrep;
        this.clientID = clientID;
        this.angles = new FloatWA(3);
	}


	@Override
	public Object getData() {
		if (vrep.simxGetObjectOrientation(clientID, handle.getValue(), -1, angles, remoteApi.simx_opmode_buffer) == remoteApi.simx_return_ok) {		    
		    // returns gamma angle
			return angles.getArray()[2];
		}
		return null;
	}


	@Override
	public void resetData() {
		// TODO Auto-generated method stub
		
	}

}
