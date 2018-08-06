/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cst_attmod_app;

import outsideCommunication.OutsideCommunication;

/**
 *
 * @author leandro
 */
public class CST_AttMod_App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OutsideCommunication oc = new OutsideCommunication();
        oc.start();
        AgentMind am = new AgentMind(oc);
        
        
    }
    
}
