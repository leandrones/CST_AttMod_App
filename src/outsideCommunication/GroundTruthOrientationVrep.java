package outsideCommunication;

import CommunicationInterface.SensorI;
import coppelia.CharW;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GroundTruthOrientationVrep implements SensorI {
	private int time_graph;
	
	private remoteApi vrep;
    private int clientID;
    private FloatWA orientation;
    private int my_handle;
	
	public  GroundTruthOrientationVrep( int clientid, int handle, remoteApi rapi) {
		time_graph = 0;
        my_handle = handle;
        vrep = rapi;
        clientID = clientid;
	}

	@Override
	public Object getData() {
		vrep.simxGetObjectOrientation(clientID, my_handle, -1, orientation, remoteApi.simx_opmode_streaming);
        return orientation;
		
	}

}
