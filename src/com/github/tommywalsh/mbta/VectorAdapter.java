package com.github.tommywalsh.mbta;

// Copyright 2012 Tom Walsh
//
// This program is free software released under version 3
// of the GPL.  See file gpl.txt for more information.



import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import java.util.Vector;

// This class adapts a Vector for use with widgets like the ListView
// This class takes care of converting between Vector APIs, Adapter APIs,
// and LayoutInflater APIs
// You provide a base class which provides a Vector, and fills in a view
// for a Vector element
public abstract class VectorAdapter<V> extends BaseAdapter
{
    ///////////////////////////////////////////////////
    // Subclasses must override these methods

    // Simply return the vector we're wrapping
    public abstract Vector<V> getVector();

    // Fill in the UI of the given view with information from the
    // given Vector item
    public abstract View processView(int position, V item, View view);


    ////////////////////////////////////////////////////

    
    // Pass in the application context, and the layout that you wish to use
    public VectorAdapter(Context ctx, int layoutId) {
        m_layoutId = layoutId;
        m_context = ctx;
    }


    private int m_layoutId;
    private Context m_context;

    public int getCount() {
        return getVector().size();
    }

    public V getItem(int position) {
        return getVector().elementAt(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(m_layoutId, null);
        }
        return processView(position, getItem(position), convertView);
    }
}
