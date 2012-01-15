package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.


public class Util
{
    // Given a number of seconds, this class will return
    // a formatted hh:mm:ss string
    public static String timeString(int seconds)
    {
        Integer h = seconds / 3600;
        Integer m = (seconds - h*3600) / 60;
        Integer s = (seconds - h*3600 - m*60);
        
        String str = new String();
        if (h > 0) {
            str += h + ":";
            if (m < 10) {
                str += "0";
            }
        }
        
        if (h > 0 || m > 0) {
            str += m;
        }
        str += ":";
        if (s < 10) {
            str += "0";
        }
        str += s;
        
        return str;
    }
    
}