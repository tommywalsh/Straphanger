package com.github.tommywalsh.mbta;

import android.view.Menu;
import android.view.SubMenu;

import java.util.HashMap;

// Provides a submenu consisting of "profiles"
// from which the user can pick

public class ProfilePicker {

    private HashMap<Integer,Profile> m_idToProfile = new HashMap<Integer, Profile>();
    

    ProfilePicker (Menu parentMenu, ProfileProvider provider) { 
	SubMenu sm = parentMenu.addSubMenu(R.string.load_profile);

	int id = 1000;
	
	for(Profile p : provider.getAllProfiles()) {
	    sm.add(Menu.NONE, id, Menu.NONE, p.name);
	    m_idToProfile.put(id, p);	    
	    id++;
	}
    }



    Profile processSelection(int itemId) {
	return m_idToProfile.get(itemId);
    }
}