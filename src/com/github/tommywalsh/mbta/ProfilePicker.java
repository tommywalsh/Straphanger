// Copyright 2010-11 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.view.Menu;
import android.view.SubMenu;

//import java.util.HashMap;

// Provides a submenu consisting of "profiles"
// from which the user can pick

public class ProfilePicker {

    ProfileProvider m_provider;
    final static private int s_firstIndex = 1000;
    

    ProfilePicker (Menu parentMenu, ProfileProvider provider) { 
        m_provider = provider;
	SubMenu sm = parentMenu.addSubMenu(R.string.load_profile);

        int id = s_firstIndex;
        for(String name : provider.getProfileNames()) {
	    sm.add(Menu.NONE, id, Menu.NONE, name);
	    id++;
        }
    }



    Profile processSelection(int itemId) {
        return m_provider.getProfile(itemId - s_firstIndex);
    }
}
