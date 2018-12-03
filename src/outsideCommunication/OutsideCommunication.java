/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/

package outsideCommunication;

import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

import java.util.ArrayList;

import CommunicationInterface.MotorI;
import CommunicationInterface.SensorI;
import coppelia.CharWA;
import coppelia.FloatWA;
import outsideCommunication.OrientationVrep;

/**
 *
 * @author leandro
 */
public class OutsideCommunication {

	public remoteApi vrep;
	public int clientID;
	public IntW pioneer_handle;
	public MotorI right_motor, left_motor;
	public SensorI sonar;
	public SensorI laser;
	public SensorI pioneer_orientation;
	public ArrayList<SensorI> sonar_orientations;

	public OutsideCommunication() {
		vrep = new remoteApi();
		sonar_orientations = new ArrayList<SensorI>();
	}

	public void start() {
		// System.out.println("Program started");
		vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		clientID = vrep.simxStart("127.0.0.1", 25000, true, true, 5000, 5);

		if (clientID == -1) {
			System.err.println("Connection failed");
			System.exit(1);
		}

		// System.out.println("Connected to vrep");

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}

		// SYNC
		if (vrep.simxSynchronous(clientID, true) == remoteApi.simx_return_ok)
			vrep.simxSynchronousTrigger(clientID);

		//////////////////////////////////////////////////////////////////
		// Motors
		//////////////////////////////////////////////////////////////////

		IntW left_motor_h = new IntW(-1);
		vrep.simxGetObjectHandle(clientID, "Pioneer_p3dx_leftMotor", left_motor_h, remoteApi.simx_opmode_blocking);
		IntW right_motor_h = new IntW(-1);
		vrep.simxGetObjectHandle(clientID, "Pioneer_p3dx_rightMotor", right_motor_h, remoteApi.simx_opmode_blocking);

		right_motor = new MotorVrep(vrep, clientID, right_motor_h.getValue());
		left_motor = new MotorVrep(vrep, clientID, left_motor_h.getValue());

		//////////////////////////////////////////////////////////////////
		// Sonars
		//////////////////////////////////////////////////////////////////
		IntW[] sonar_handles = new IntW[16];

		for (int i = 1; i <= 16; i++) {
			String proximity_sensors_name = "Pioneer_p3dx_ultrasonicSensor" + i;
			sonar_handles[i - 1] = new IntW(-1);

			//// System.out.println(proximity_sensors_name);

			vrep.simxGetObjectHandle(clientID, proximity_sensors_name, sonar_handles[i - 1],
					remoteApi.simx_opmode_blocking);
			if (sonar_handles[i - 1].getValue() == -1)
				System.out.println("Error on connenting to sensor: " + i);
			else
				System.out.println("Connected to sensor " + i);
		}

		sonar = new SonarVrep(vrep, clientID, sonar_handles);

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}

		// START
		vrep.simxStartSimulation(clientID, remoteApi.simx_opmode_blocking);

		// Sonar initialization reading
		for (int i = 0; i < 16; i++) {
			int ret = vrep.simxReadProximitySensor(clientID, sonar_handles[i].getValue(), null, null, null, null,
					remoteApi.simx_opmode_streaming);
			if (ret == 1) {
				// System.out.println("init ok i = "+i);
			} else {
				System.exit(1);
			}
		}

		// Laser scanner initialization reading
		CharWA signal_laser_value = new CharWA("");

		vrep.simxGetStringSignal(clientID, "LaserSignal", signal_laser_value, remoteApi.simx_opmode_streaming);

		laser = new LaserVrep(clientID, signal_laser_value, vrep);

		// Orientation initialization
		pioneer_handle = new IntW(-1);
		vrep.simxGetObjectHandle(clientID, "Pioneer_p3dx", pioneer_handle, remoteApi.simx_opmode_oneshot_wait);
		if (pioneer_handle.getValue() == -1)
			System.out.println("Error on initialing orientation ground truth: ");

		FloatWA angles = new FloatWA(3);
		vrep.simxGetObjectOrientation(clientID, pioneer_handle.getValue(), -1, angles, remoteApi.simx_opmode_streaming);
		for (int i = 0; i < 8; i++) {
			vrep.simxGetObjectOrientation(clientID, sonar_handles[i].getValue(), -1, angles,
					remoteApi.simx_opmode_streaming);
			sonar_orientations.add(new OrientationVrep(clientID, sonar_handles[i], vrep));
		}
		pioneer_orientation = new OrientationVrep(clientID, pioneer_handle, vrep);

	}
	
	public void reset_robot_position() {
		System.out.println("Resseting position");
		vrep.simxPauseCommunication(clientID, true);
		FloatWA position = initFloatWA(false);
		vrep.simxCallScriptFunction(clientID, "Pioneer_p3dx", vrep.sim_scripttype_childscript, "reset",  null , 
				null ,null , null , null, null , null, null, vrep.simx_opmode_blocking);
		vrep.simxSetObjectPosition(clientID, pioneer_handle.getValue(), -1, position,
                vrep.simx_opmode_oneshot);
		FloatWA angles = initFloatWA(true);
		vrep.simxSetObjectOrientation(clientID, pioneer_handle.getValue(), -1, angles, vrep.simx_opmode_oneshot);
		vrep.simxPauseCommunication(clientID, false);
		vrep.simxSynchronousTrigger(clientID);

	}
	
	public FloatWA initFloatWA(boolean orient) {
		FloatWA position = new FloatWA(3);
		float[] pos = position.getArray();
		
		if (orient) {
			pos[0] = 0.0f;
			pos[1] = 0.0f;
			pos[2] = (float) Math.random() * 360;
		}
		else {
			pos[0] = (float) Math.random() * 1.5f;
			pos[1] = (float) Math.random() * 1.5f;
			pos[2] = 0.138f;
		}
		return position;
	}

}