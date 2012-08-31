/*
 * Copyright 2012 Lolmewn <info@lolmewn.nl>
 */

package nl.lolmewn.skillz;

import java.util.HashMap;

/**
 *
 * @author Lolmewn
 */
public class UserManager {

    private Main plugin;
    private HashMap<String, User> users = new HashMap<String, User>();
    
    public UserManager(Main main){
        this.plugin = main;
    }
    
    private Main getPlugin(){
        return this.plugin;
    }

    protected void saveAllUsers() {
        
    }
}
