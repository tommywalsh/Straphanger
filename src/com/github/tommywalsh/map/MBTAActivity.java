// Copyright 2010 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.

package com.github.tommywalsh.mbta;

import android.app.ListActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.os.Bundle;

import java.util.SortedSet;

public class MBTAActivity extends ListActivity
{
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	MBTAParser parser = new MBTAParser();

String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> "+
"<body copyright=\"All data copyright MBTA 2010.\">"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"CT2-North\" routeTag=\"748\" stopTitle=\"30 Prospect St\" stopTag=\"2531\" dirTitleBecauseNoPredictions=\"Sullivan Station via Kendall/MIT (Limited Stops)\">"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"CT2-North\" routeTag=\"748\" stopTitle=\"Somerville Ave @ Stone Ave\" stopTag=\"2612\" dirTitleBecauseNoPredictions=\"Ruggles via Kendall/MIT (Limited Stops)\">"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"91\" routeTag=\"91\" stopTitle=\"30 Prospect St\" stopTag=\"2531\">"+
"  <direction title=\"Sullivan Station via Union Square\">"+
"    <prediction seconds=\"205\" minutes=\"3\" epochTime=\"1287846530783\" isDeparture=\"false\" dirTag=\"91_910004v0_0\" vehicle=\"0046\" block=\"G93_10\" />"+
"    <prediction seconds=\"1620\" minutes=\"27\" epochTime=\"1287847945770\" isDeparture=\"false\" dirTag=\"91_910004v0_0\" vehicle=\"0396\" block=\"G93_9\" />"+
"    <prediction seconds=\"2820\" minutes=\"47\" epochTime=\"1287849145770\" isDeparture=\"false\" dirTag=\"91_910004v0_0\" vehicle=\"0752\" block=\"G93_8\" />"+
"    <prediction seconds=\"4020\" minutes=\"67\" epochTime=\"1287850345770\" isDeparture=\"false\" dirTag=\"91_910004v0_0\" vehicle=\"0366\" block=\"G93_11\" />"+
"    <prediction seconds=\"5220\" minutes=\"87\" epochTime=\"1287851545770\" isDeparture=\"false\" dirTag=\"91_910004v0_0\" vehicle=\"0720\" block=\"G91_4\" />"+
"  </direction>"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"91\" routeTag=\"91\" stopTitle=\"Somerville Ave @ Stone Ave\" stopTag=\"2612\">"+
"  <direction title=\"Central Square via Union Square\">"+
"    <prediction seconds=\"211\" minutes=\"3\" epochTime=\"1287846536833\" isDeparture=\"false\" dirTag=\"91_910003v0_1\" vehicle=\"0396\" block=\"G93_9\" />"+
"    <prediction seconds=\"1302\" minutes=\"21\" epochTime=\"1287847627242\" isDeparture=\"false\" dirTag=\"91_910003v0_1\" vehicle=\"0752\" block=\"G93_8\" />"+
"    <prediction seconds=\"2502\" minutes=\"41\" epochTime=\"1287848827242\" isDeparture=\"false\" dirTag=\"91_910003v0_1\" vehicle=\"0366\" block=\"G93_11\" />"+
"    <prediction seconds=\"3702\" minutes=\"61\" epochTime=\"1287850027242\" isDeparture=\"false\" dirTag=\"91_910003v0_1\" vehicle=\"0720\" block=\"G91_4\" />"+
"    <prediction seconds=\"4902\" minutes=\"81\" epochTime=\"1287851227242\" isDeparture=\"false\" dirTag=\"91_910003v0_1\" vehicle=\"0046\" block=\"G93_10\" />"+
"  </direction>"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"86\" routeTag=\"86\" stopTitle=\"Washington St @ Parker St\" stopTag=\"25712\">"+
"  <direction title=\"Sullivan Station via Harvard\">"+
"    <prediction seconds=\"724\" minutes=\"12\" epochTime=\"1287847049899\" isDeparture=\"false\" dirTag=\"86_860006v0_0\" vehicle=\"0363\" block=\"T86_70\" />"+
"    <prediction seconds=\"2603\" minutes=\"43\" epochTime=\"1287848929006\" isDeparture=\"false\" dirTag=\"86_860006v0_0\" vehicle=\"0287\" block=\"T86_71\" />"+
"    <prediction seconds=\"4218\" minutes=\"70\" epochTime=\"1287850543273\" isDeparture=\"false\" dirTag=\"86_860006v0_0\" vehicle=\"0705\" block=\"T86_78\" />"+
"  </direction>"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"86\" routeTag=\"86\" stopTitle=\"Washington St opp Parker St\" stopTag=\"2615\">"+
"  <direction title=\"Cleveland Circle via Harvard\">"+
"    <prediction seconds=\"33\" minutes=\"0\" epochTime=\"1287846358779\" isDeparture=\"false\" dirTag=\"86_860005v0_1\" vehicle=\"0705\" block=\"T86_78\" />"+
"    <prediction seconds=\"4136\" minutes=\"68\" epochTime=\"1287850461685\" isDeparture=\"false\" dirTag=\"86_860005v0_1\" vehicle=\"0287\" block=\"T86_71\" />"+
"  </direction>"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"85\" routeTag=\"85\" stopTitle=\"Somerville Ave @ Stone Ave\" stopTag=\"2612\" dirTitleBecauseNoPredictions=\"Spring Hill via Union Square\">"+
"</predictions>"+
"<predictions agencyTitle=\"MBTA\" routeTitle=\"87\" routeTag=\"87\" stopTitle=\"Somerville Ave @ Union Square\" stopTag=\"2510\">"+
"  <direction title=\"Lechmere Station via Davis Square\">"+
"    <prediction seconds=\"198\" minutes=\"3\" epochTime=\"1287846523368\" isDeparture=\"false\" dirTag=\"87_870004v0_1\" vehicle=\"0293\" block=\"T87_90\" />"+
"    <prediction seconds=\"1700\" minutes=\"28\" epochTime=\"1287848025405\" isDeparture=\"false\" dirTag=\"87_870004v0_1\" vehicle=\"0309\" block=\"T87_89\" />"+
"    <prediction seconds=\"3140\" minutes=\"52\" epochTime=\"1287849465405\" isDeparture=\"false\" dirTag=\"87_870004v0_1\" vehicle=\"0328\" block=\"T87_88\" />"+
"    <prediction seconds=\"4580\" minutes=\"76\" epochTime=\"1287850905405\" isDeparture=\"false\" dirTag=\"87_870004v0_1\" vehicle=\"0293\" block=\"T87_90\" />"+
"  </direction>"+
"</predictions>"+
"</body>";

	ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.listitem);
        SortedSet<Departure> ds = parser.parse(xml);
	for (Departure d : ds) {
	    String mess = d.route + " to " + d.direction + " stops at " + d.where + " in " + new Integer(d.when).toString() + " minutes";
	    Log.d("mbta", mess);
	    aa.add(mess);
	}
	setListAdapter(aa);
    }

}


