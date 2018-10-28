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
 
package cst_attmod_app;

//import br.unicamp.cst.attention.DecisionMakingCodelet;
//import br.unicamp.cst.attention.SaliencyMapCodelet;
//import br.unicamp.cst.attention.Winner;
import attention.DecisionMakingCodelet;
import attention.SaliencyMapCodelet;
import attention.Winner;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.sensorial.SensorBufferCodelet;
import codelets.motor.MotorCodelet;
import codelets.sensors.AppCombFeatMapCodelet;
import codelets.sensors.DirectionFeatMapCodelet;
import codelets.sensors.DistanceFeatMapCodelet;
import codelets.sensors.LaserCodelet;
import codelets.sensors.SonarCodelet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import outsideCommunication.OutsideCommunication;
import outsideCommunication.SonarData;
import codelets.learner.LearnerCodelet;


/**
 *
 * @author leandro
 */
public class AgentMind extends Mind {
    
    public static final int buffersize = 50;
    public static final int laserdimension = 182;
    public static final int sonardimension = 8;
    public AgentMind(OutsideCommunication oc){
        
        super();
        
        //////////////////////////////////////////////
        //Declare Memory Objects
        //////////////////////////////////////////////
        
        //Motors
        MemoryObject motorActionMO;
        MemoryObject left_motor_speed;
        MemoryObject right_motor_speed;
        
        motorActionMO = createMemoryObject("MOTOR", "");
        left_motor_speed = createMemoryObject("L_M_SPEED", new Float(0));
        right_motor_speed  = createMemoryObject("R_M_SPEED", new Float(0));
        
        
        ArrayList<Memory> motorMOs = new ArrayList<>();
        motorMOs.add(motorActionMO);
        motorMOs.add(left_motor_speed);
        motorMOs.add(right_motor_speed);
        
        //Motors
        Codelet motors = new MotorCodelet(oc.right_motor, oc.left_motor);
        motors.addInputs(motorMOs);
        insertCodelet(motors);
        
        //Sensors
        //Sonars
        SonarData sonarData = new SonarData();
        MemoryObject sonar_read = createMemoryObject("SONARS", sonarData);
        
        //Laser
        List laser_data = Collections.synchronizedList(new ArrayList<Float>(laserdimension));
        MemoryObject laser_read = createMemoryObject("LASER",laser_data);
        
        //Sensor Buffers
        //Sonars
        List sonar_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(buffersize));
        MemoryObject sonar_bufferMO = createMemoryObject("SONAR_BUFFER",sonar_buffer_list);
        
        //Laser
        List laser_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(buffersize));
        MemoryObject laser_bufferMO = createMemoryObject("LASER_BUFFER",laser_buffer_list);
        
        // Feature Maps
        
        //Direction
        List directionFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        
        /*for (int i = 0; i < buffersize; i++) {
            directionFM.add(new ArrayList<Float>(sonardimension));
            for (int j = 0; j < sonardimension; j++) {
                ArrayList arrl;
                arrl = (ArrayList<Float>) directionFM.get(i);
                arrl.add(new Float(0));
            }
        }*/
        
        MemoryObject direction_fmMO = createMemoryObject("DIRECTION_FM", directionFM);
        
        //Distance
        List distFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        
        /*for (int i = 0; i < buffersize; i++) {
            distFM.add(new ArrayList<Float>(laserdimension));
            for (int j = 0; j < laserdimension; j++) {
                ArrayList arrl;
                arrl = (ArrayList<Float>) distFM.get(i);
                arrl.add(new Float(0));
            }
        }*/
        
        MemoryObject dist_fmMO = createMemoryObject("DISTANCE_FM", distFM);
        
        //
        //Combined Feat Map
        //
        
        List weights = Collections.synchronizedList(new ArrayList<Float>(2));
        weights.add(1.0f);
        weights.add(1.0f);
        MemoryObject weightsMO = createMemoryObject("FM_WEIGHTS",weights);
        
        List CombFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        /*for (int i = 0; i < buffersize; i++) {
            CombFM.add(new ArrayList<Float>(sonardimension));
            for (int j = 0; j < sonardimension; j++) {
                ArrayList arrl;
                arrl = (ArrayList<Float>) CombFM.get(i);
                arrl.add(new Float(0));
            }
        }*/
        
        MemoryObject combFMMO = createMemoryObject("COMB_FM",CombFM);
        
        //ATTENTIONAL MAP
        
        List att_mapList = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        
        /*for (int i = 0; i < buffersize; i++) {
            att_mapList.add(new ArrayList<Float>(sonardimension));
            for (int j = 0; j < sonardimension; j++) {
                ArrayList arrl;
                arrl = (ArrayList<Float>) att_mapList.get(i);
                arrl.add(new Float(0));
            }
        }*/
        
        MemoryObject attMapMO = createMemoryObject("ATTENTIONAL_MAP",att_mapList);
        
        //WINNERS
        
        List winnersList = Collections.synchronizedList(new ArrayList<Winner>());
        MemoryObject winnersMO = createMemoryObject("WINNERS",winnersList);
        
        //SALIENCY MAP
        
        List saliencyMap = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject salMapMO = createMemoryObject("SALIENCY_MAP", saliencyMap);
        
        //ACTIONS 
        
        List actionsList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject actionsMO = createMemoryObject("ACTIONS", actionsList);
        
        //STATES 
        
        List statesList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject statesMO = createMemoryObject("STATES", statesList);
        
        
        ////////////////////////////////////////////
        //Codelets
        ////////////////////////////////////////////
        
        
        //Sensors
        Codelet sonars = new SonarCodelet(oc.sonar);
        sonars.addOutput(sonar_read);
        insertCodelet(sonars);
        
        Codelet laser = new LaserCodelet(oc.laser);
        laser.addOutput(laser_read);
        insertCodelet(laser);
        
        //Sensor Buffers
        //Sonar
        Codelet sonar_buffer = new SensorBufferCodelet("SONARS", "SONAR_BUFFER", buffersize);
        sonar_buffer.addInput(sonar_read);
        sonar_buffer.addOutput(sonar_bufferMO);
        insertCodelet(sonar_buffer);
        
        //Laser
        Codelet laser_buffer = new SensorBufferCodelet("LASER", "LASER_BUFFER", buffersize);
        laser_buffer.addInput(laser_read);
        laser_buffer.addOutput(laser_bufferMO);
        insertCodelet(laser_buffer);
        
        //Feat Maps
        
        //Direction
        ArrayList<String> sensbuff_names = new ArrayList<>();
        sensbuff_names.add("SONAR_BUFFER");
        sensbuff_names.add("LASER_BUFFER");
        
//        // System.out.println("size sens buff name "+sensbuff_name.size());
        
        Codelet direction_fm_c = new DirectionFeatMapCodelet(sensbuff_names.size(), sensbuff_names, "DIRECTION_FM",buffersize,sonardimension);
        direction_fm_c.addInput(sonar_bufferMO);
        direction_fm_c.addInput(laser_bufferMO);
        direction_fm_c.addOutput(direction_fmMO);
        insertCodelet(direction_fm_c);

        //Distance
        ArrayList<String> sensbuff_names_dist = new ArrayList<>();
        sensbuff_names_dist.add("LASER_BUFFER");
        
//        // System.out.println("size sens buff name "+sensbuff_names.size());
        
        Codelet dist_fm_c = new DistanceFeatMapCodelet(sensbuff_names_dist.size(), sensbuff_names_dist, "DISTANCE_FM",buffersize,laserdimension);
        dist_fm_c.addInput(laser_bufferMO);
        dist_fm_c.addOutput(dist_fmMO);
        insertCodelet(dist_fm_c);
        
        ArrayList<String> FMnames = new ArrayList<>();
        FMnames.add("DIRECTION_FM");
        FMnames.add("DISTANCE_FM");        
        
        Codelet comb_fm_c = new AppCombFeatMapCodelet(FMnames.size(), FMnames,buffersize,sonardimension);
        comb_fm_c.addInput(direction_fmMO);
        comb_fm_c.addInput(dist_fmMO);
        comb_fm_c.addInput(weightsMO);
        comb_fm_c.addOutput(combFMMO);
        insertCodelet(comb_fm_c);
        
        //SALIENCY MAP CODELET
        Codelet sal_map_cod = new SaliencyMapCodelet("SALIENCY_MAP", "COMB_FM", "ATTENTIONAL_MAP", buffersize, sonardimension);
        sal_map_cod.addInput(combFMMO);
        sal_map_cod.addInput(attMapMO);
        sal_map_cod.addOutput(salMapMO);
        insertCodelet(sal_map_cod);
        
        //DECISION MAKING CODELET
        Codelet dec_mak_cod = new DecisionMakingCodelet("WINNERS", "ATTENTIONAL_MAP", "SALIENCY_MAP", buffersize, sonardimension);
        dec_mak_cod.addInput(salMapMO);
        dec_mak_cod.addOutput(winnersMO);
        dec_mak_cod.addOutput(attMapMO);
        insertCodelet(dec_mak_cod);
        
        Codelet learner_cod = new LearnerCodelet("WINNERS", "SALIENCY_MAP", "DISTANCE_FM", "L_M_SPEED", "R_M_SPEED", "ACTIONS", "STATES", buffersize, sonardimension);
        learner_cod.addInput(salMapMO);
        learner_cod.addInput(winnersMO);
        learner_cod.addInput(dist_fmMO);
        learner_cod.addOutputs(motorMOs);
        learner_cod.addOutput(actionsMO);
        learner_cod.addOutput(statesMO);
        insertCodelet(learner_cod);
        

        ///////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
        
        // NOTE Sets the time interval between the readings
        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);
	
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
	// Start Cognitive Cycle
	start(); 
    }
}
