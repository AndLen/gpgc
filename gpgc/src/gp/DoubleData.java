/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package gp;

import ec.gp.GPData;

public class DoubleData extends GPData
    {
    public double val;    // return value

    public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
        { ((DoubleData)gpd).val = val; }
    }


