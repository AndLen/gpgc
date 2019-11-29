/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package gp.locum;

import ec.gp.GPData;

public class DoubleDataPair extends GPData {
    public double val1, val2;    // return value

    public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
    {
        DoubleDataPair gpd1 = (DoubleDataPair) gpd;
        gpd1.val1 = val1;
        gpd1.val2 = val2;
    }
}


