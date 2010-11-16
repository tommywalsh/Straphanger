// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.content.Context;

import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

public class ProfileProvider
{
    static final private String FILE_NAME = "profiles";

    Vector<Profile> m_allProfiles = null;
    private Context m_context;

    ProfileProvider(Context context) {
	m_context = context;
    }


    public Vector<Profile> getAllProfiles() {
	if (m_allProfiles == null) {
	    m_allProfiles = new Vector<Profile>();

	    m_allProfiles = loadSavedProfiles(m_context);
	    if (m_allProfiles.isEmpty()) {
		loadDefaultProfiles();
	    }
	}
	return m_allProfiles;
    }

    private void loadDefaultProfiles() {
	m_allProfiles.add(getHomeToWorkProfile());
	m_allProfiles.add(getWorkToHomeProfile());
	saveProfiles(m_context, m_allProfiles);
    }

    private synchronized static Vector<Profile> loadSavedProfiles(Context context) {	
	Vector<Profile> emptyVec = new Vector<Profile>();
	try {
	    FileInputStream fis = context.openFileInput(FILE_NAME);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    Object obj = ois.readObject();
	    
	    // Will warn about unchecked operations:
	    if (obj.getClass() == emptyVec.getClass()) {
		return (Vector<Profile>)obj;
	    }
	} catch (java.io.FileNotFoundException e) {
	} catch (java.io.IOException e) {
	} catch (java.lang.ClassNotFoundException e) {
	}

	return emptyVec;
    }


    private synchronized static void saveProfiles(Context context, Vector<Profile> profiles) {
	try {
	    FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(profiles);
	    fos.close();
	} catch (java.io.FileNotFoundException e) {
	} catch (java.io.IOException e) {
	}
    }

    static private Profile getHomeToWorkProfile() {
	Profile p = new Profile();	
	p.stops.add(new Stop("748","2541"));
	p.stops.add(new Stop("748","2612"));
	p.stops.add(new Stop("91","2531"));
	p.stops.add(new Stop("91","2612"));
	p.stops.add(new Stop("86","25712"));
	p.stops.add(new Stop("86","2615"));
	p.stops.add(new Stop("85","2612"));
	p.stops.add(new Stop("87","2510"));
	p.name = "Home to Work";
	return p;
    }

    static private Profile getWorkToHomeProfile() {
	Profile p = new Profile();	
	p.stops.add(new Stop("747","17863"));
	p.stops.add(new Stop("47","17863"));
	p.stops.add(new Stop("1","64"));
	p.name = "Work To Home";
	return p;
    }

}
