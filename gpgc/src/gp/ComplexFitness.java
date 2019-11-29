package gp;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleDefaults;
import ec.util.Code;
import ec.util.Parameter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;


public class ComplexFitness extends Fitness {

    /**
     * base parameter for defaults
     */
    public static final String P_FITNESS = "complexFitness";

    /**
     * Basic preamble for printing Fitness values out
     */
    public static final String FITNESS_PREAMBLE = "Complex Fitness: ";
    //   private static double floatMultiplier = ((double) Float.MAX_VALUE) / Double.MAX_VALUE;
    //   private static double shortMultiplier = ((double) Short.MAX_VALUE) / (double) Integer.MAX_VALUE;
    protected boolean isIdeal;
    private int numPerfectlySeparate;
    private int numPerfectlyCompact;
    private double fitness;

    public Parameter defaultBase() {
        return SimpleDefaults.base().push(P_FITNESS);
    }

    /**
     * Deprecated -- now redefined to set the fitness but ALWAYS say that it's not ideal.
     * If you need to specify that it's ideal, you should use the new function
     * setFitness(final EvolutionState state, double _f, boolean _isIdeal).
     *
     * @deprecated
     */
//    public void setFitness(final EvolutionState state, int nPS, int nPC, double _f) {
//        setFitness(state, nPS, nPC, _f, false);
//    }
    public void setFitness(final EvolutionState state, int npS, int npC, double _f, boolean _isIdeal) {
        // we now allow f to be *any* value, positive or negative
        if (_f >= Double.POSITIVE_INFINITY || _f <= Double.NEGATIVE_INFINITY || Double.isNaN(_f)) {
            state.output.warning("Bad fitness: " + _f + ", setting to 0.");
            fitness = 0;
        } else fitness = _f;
        numPerfectlyCompact = npC;
        numPerfectlySeparate = npS;
        isIdeal = _isIdeal;
    }

    public double fitness() {
        //Dodgy
//        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
//        short nps = numPerfectlySeparate > Short.MAX_VALUE ? Short.MAX_VALUE : (short) numPerfectlySeparate;
//        byteBuffer.putShort(nps);
//        short npc = numPerfectlyCompact > Short.MAX_VALUE ? Short.MAX_VALUE : (short) numPerfectlyCompact;
//        byteBuffer.putShort(npc);
//        int fit = (int) (fitness * 10_000);
//        byteBuffer.putInt(fit);
//        long longFit = byteBuffer.getLong(0);
//        double doubleFit = (double) longFit;
        // System.out.print(Arrays.toString(byteBuffer.array()));
        //  System.out.printf("NPS: %d NPC: %d Fit: %d, LF: %d DF: %.4f\n", nps, npc, fit, longFit, doubleFit);
        //   return doubleFit;
        throw new UnsupportedOperationException();
        //return -1;
    }

    public void setup(final EvolutionState state, Parameter base) {
        super.setup(state, base);  // unnecessary but what the heck
    }

    public boolean isIdealFitness() {
        return isIdeal;
    }

    public boolean equivalentTo(final Fitness _fitness) {
        ComplexFitness other = (ComplexFitness) _fitness;
        if (numPerfectlySeparate == other.numPerfectlySeparate) {
            return true;
        }
        if (numPerfectlyCompact == other.numPerfectlyCompact) {
            return true;
        } else return fitness == other.fitness;
    }

    public boolean betterThan(final Fitness _fitness) {
        ComplexFitness other = (ComplexFitness) _fitness;
        if (numPerfectlySeparate > other.numPerfectlySeparate) {
            return true;
        }
        if (numPerfectlyCompact > other.numPerfectlyCompact) {
            return true;
        } else return fitness > other.fitness;
    }

    public String fitnessToString() {
        return FITNESS_PREAMBLE + Code.encode(fitness());
    }

    public String fitnessToStringForHumans() {
        return FITNESS_PREAMBLE + String.format("PS: %d, PC: %d, F:%.2f", numPerfectlySeparate, numPerfectlyCompact, fitness);

        //    return FITNESS_PREAMBLE + String.format("PS: %d, PC: %d, F:%.2f BF: %.2f", numPerfectlySeparate, numPerfectlyCompact, fitness, fitness());
    }

    /**
     * Presently does not decode the fact that the fitness is ideal or not
     */
    public void readFitness(final EvolutionState state,
                            final LineNumberReader reader)
            throws IOException {
        throw new UnsupportedOperationException();
        // Code.readStringWithPreamble(FITNESS_PREAMBLE,state,reader);
        //  setFitness(state, Code.readDoubleWithPreamble(FITNESS_PREAMBLE, state, reader));
    }

    public void writeFitness(final EvolutionState state,
                             final DataOutput dataOutput) throws IOException {
        throw new UnsupportedOperationException();

        // dataOutput.writeInt(numPerfectlySeparate);
        // dataOutput.writeInt(numPerfectlyCompact);
        //dataOutput.writeDouble(fitness);
        // dataOutput.writeBoolean(isIdeal);
        //   writeTrials(state, dataOutput);
    }

    public void readFitness(final EvolutionState state,
                            final DataInput dataInput) throws IOException {
        throw new UnsupportedOperationException();

        // fitness = dataInput.readDouble();
        // isIdeal = dataInput.readBoolean();
        // readTrials(state, dataInput);
    }

    public void setToMeanOf(EvolutionState state, Fitness[] fitnesses) {
        // this is not numerically stable.  Perhaps we should have a numerically stable algorithm for sums
        // we're presuming it's not a very large number of elements, so it's probably not a big deal,
        // since this function is meant to be used mostly for gathering trials together.
        double f = 0;
        double npC = 0;
        double npS = 0;
        boolean ideal = true;
        for (int i = 0; i < fitnesses.length; i++) {
            ComplexFitness fit = (ComplexFitness) (fitnesses[i]);
            f += fit.fitness;
            npC += fit.numPerfectlyCompact;
            npS += fit.numPerfectlySeparate;
            ideal = ideal && fit.isIdeal;
        }
        f /= fitnesses.length;
        int npCMean = (int) Math.round(npC / fitnesses.length);
        int npSMean = (int) Math.round(npS / fitnesses.length);
        fitness = f;
        numPerfectlyCompact = npCMean;
        numPerfectlySeparate = npSMean;
        isIdeal = ideal;
    }

    public Object clone() {
        ComplexFitness f = (ComplexFitness) (super.clone());
        f.fitness = fitness;
        f.numPerfectlySeparate = numPerfectlySeparate;
        f.numPerfectlyCompact = numPerfectlyCompact;
        return f;
    }

}
