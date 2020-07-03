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

import CommunicationInterface.SensorI;
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

/**
 *
 * @author leandro
 */
public class LaserVrep implements SensorI {
    private  int time_graph;
    
    private remoteApi vrep;
    private CharWA signal_laser_value;
    private int clientID;
    private List<Float> laser_data;
    
    public LaserVrep(int clientid, CharWA slv, remoteApi rapi){
        this.time_graph = 0;
        laser_data = Collections.synchronizedList(new ArrayList<>(182));
        vrep = rapi;
        signal_laser_value = slv;
        clientID = clientid;
        
        for (int i = 0; i < 182; i++) {
            laser_data.add(10f);
        }
    }
    
    @Override
    public Object getData() {
        try {
            Thread.sleep(50);
//            // System.out.println("\u001B[31m"+"TRY CATCH");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        if(vrep.simxGetStringSignal(clientID, "LaserSignal", signal_laser_value, remoteApi.simx_opmode_buffer) == remoteApi.simx_error_noerror) {
            FloatWA laser_data_wa = new FloatWA(signal_laser_value.getLength()/4);
            laser_data_wa.initArrayFromCharArray(signal_laser_value.getArray());
            float data_arr[] = laser_data_wa.getArray();
//            // System.out.println("laserdata size = "+laser_data.size()+" data arr size = "+data_arr.length);
            for (int i = 0; i < 182 && 3*i+1 < data_arr.length; i++) {
                float temp;
                temp = (float) (Math.round(data_arr[3*i+1]*100.0)/100.0);
                if(temp > 10.0){
                    laser_data.set(i, 10.0f);
                }
                else{
                    laser_data.set(i, temp);
                }
            }


        }
        Collections.reverse(laser_data);
        
        // System.out.print("\u001B[31m"+"laser data"+laser_data);
        // SYNC
        if (vrep.simxSynchronous(clientID, true) == remoteApi.simx_return_ok)
  			vrep.simxSynchronousTrigger(clientID);
        printToFile(laser_data);
        

        
        return  laser_data;
    }
    
    private void printToFile(Object object){
        if(time_graph < 50){
            try(FileWriter fw = new FileWriter("laser.txt", true);
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

	@Override
	public void resetData() {
		// TODO Auto-generated method stub
		
	}
    
}
