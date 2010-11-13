package com.github.tommywalsh.mbta;

import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

// Provides a submenu consisting of "profiles"
// from which the user can pick

public class ProfilePicker {

    private HashMap<Integer,Profile> m_idToProfile = new HashMap<Integer, Profile>();
    

    ProfilePicker (Menu parentMenu) { 
	SubMenu sm = parentMenu.addSubMenu(R.string.load_profile);

	int id = 1000;
	MenuItem mi = sm.add(Menu.NONE, id, Menu.NONE, "Home To Work");
	m_idToProfile.put(id, Profile.getHomeToWorkProfile());
    }



    Profile processSelection(int itemId) {
	return m_idToProfile.get(itemId);
    }
}