/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package outsideCommunication;

import coppelia.remoteApi;
import CommunicationInterface.MotorI;

/**
 *
 * @author leandro
 */
public class MotorVrep implements MotorI {
    
    private float speed = 0;
    private remoteApi vrep;
    private int clientID;
    private int motor_handle;
    
    public MotorVrep(remoteApi rApi_, int clientid, int mot_han){
        vrep = rApi_;
        clientID = clientid;
        motor_handle = mot_han;        
    }
    
    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public int setSpeed(float speed) {
        this.speed = speed;
                
        return vrep.simxSetJointTargetVelocity(clientID, motor_handle, speed, remoteApi.simx_opmode_streaming);
    }
    
}
