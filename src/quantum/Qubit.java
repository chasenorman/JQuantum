package quantum;

import java.util.Collections;

/**
 * A quantum bit
 */
public class Qubit{
    QuantumState delegate;
    int index = 0;

    /**
     * Creates a qubit with the state |0⟩
     */
    public Qubit(){
        this(false);
    }

    /**
     * Creates a qubit with the basis state |0⟩ or |1⟩
     * @param b basis state of the {@code Qubit}
     */
    public Qubit(boolean b){
        delegate = new QuantumState(this,b);
    }

    /**
     * Collapses this Qubit to either |0⟩ or |1⟩
     * @return the result of this collapse
     */
    public boolean measure(){
        return delegate.measure(this).get(this);
    }

    /**
     * Samples a value of this {@code Qubit} without collapse.
     * @return the state, randomly chosen, from the possibilities of the state.
     */
    public boolean sample(){
        return delegate.sample(this).get(this);
    }

    /**
     * Creates a {@code String} representation of this {@code Qubit} with the probability of |0⟩ and |1⟩
     * @return A {@code String} representation of this {@code Qubit}
     */
    public String toString(){
        return String.format("%.3f",probabilityOf(false)) + "|0⟩ + " + String.format("%.3f",probabilityOf(true)) + "|1⟩";
    }

    /**
     * Finds the probability of a given basis state of this {@code Qubit} if it were to collapse. This probability
     * is distributed on [0,1]
     * @param state basis to test for the probability of
     * @return the probability of this basis state occuring.
     */
    public double probabilityOf(boolean state){
        return delegate.probabilityOf(Collections.singletonMap(this,state));
    }

    /**
     * Tests entanglement with a {@code Qubit}
     * @param other the {@code Qubit} to be tested for entanglement with this one
     * @return whether or not they are entangled, as a {@code boolean}
     */
    public boolean isEntangledWith(Qubit other){
        return other.delegate == this.delegate;
    }
}