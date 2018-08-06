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
import coppelia.remoteApi;
import CommunicationInterface.MotorI;
import CommunicationInterface.SensorI;
import coppelia.CharWA;

/**
 *
 * @author leandro
 */
public class OutsideCommunication {
    
    public remoteApi vrep;
    public MotorI right_motor, left_motor;
    public SensorI sonar;
    public SensorI laser;
    
    public OutsideCommunication(){
        vrep = new remoteApi();
    }
    
    public void start(){
        System.out.println("Program started");
        vrep = new remoteApi();
        vrep.simxFinish(-1); // just in case, close all opened connections
        int clientID = vrep.simxStart("127.0.0.1",25000,true,true,5000,5);
        
        if (clientID == -1){
            System.err.println("Connection failed");
            System.exit(1);
        }
        
        System.out.println("Connected to vrep");
        
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        //SYNC
        if(vrep.simxSynchronous(clientID, true) == remoteApi.simx_return_ok)
            vrep.simxSynchronousTrigger(clientID);
        
        //////////////////////////////////////////////////////////////////
        // Motors
        //////////////////////////////////////////////////////////////////
            
        IntW left_motor_h = new IntW(-1);
        vrep.simxGetObjectHandle(clientID,"Pioneer_p3dx_leftMotor", left_motor_h,remoteApi.simx_opmode_blocking);
        IntW right_motor_h = new IntW(-1);
        vrep.simxGetObjectHandle(clientID,"Pioneer_p3dx_rightMotor", right_motor_h,remoteApi.simx_opmode_blocking);
        
        right_motor = new MotorVrep(vrep, clientID, right_motor_h.getValue());
        left_motor = new MotorVrep(vrep, clientID, left_motor_h.getValue());

        //////////////////////////////////////////////////////////////////
        // Sonars
        //////////////////////////////////////////////////////////////////
        IntW[] sonar_handles = new IntW[16];

        for(int i = 1; i <= 16; i++){
            String proximity_sensors_name = "Pioneer_p3dx_ultrasonicSensor"+ i;
            sonar_handles[i-1] = new IntW(-1);

            //System.out.println(proximity_sensors_name);

            vrep.simxGetObjectHandle(clientID,proximity_sensors_name, sonar_handles[i-1],remoteApi.simx_opmode_blocking);
            if(sonar_handles[i-1].getValue() == -1)
                System.out.println("Error on connenting to sensor: " + i);
            else
                System.out.println("Connected to sensor "+i);            
        }
        
        sonar = new SonarVrep(vrep, clientID, sonar_handles);
        
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        // START 
        vrep.simxStartSimulation(clientID, remoteApi.simx_opmode_blocking);
        
        //Sonar initialization reading
        for(int i = 0; i< 16; i++){
            int ret = vrep.simxReadProximitySensor(clientID,sonar_handles[i].getValue(),null,null,null,null,remoteApi.simx_opmode_streaming);
            if(ret == 1){
                System.out.println("init ok i = "+i);
            }
            else {
                System.exit(1);
            }
        }
        
        //Laser scanner initialization reading
        CharWA signal_laser_value = new CharWA("");
            
        vrep.simxGetStringSignal(clientID, "LaserSignal", signal_laser_value, remoteApi.simx_opmode_streaming);
        
        laser = new LaserVrep(clientID, signal_laser_value, vrep);
        
    }
}