/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package gp;

public class Add extends AbstractArithmeticNode {
    public String toString() {
        return "+";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return child1 + child2;
    }
}

