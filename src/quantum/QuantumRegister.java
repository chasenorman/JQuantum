package quantum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static quantum.BitUtils.*;

/**
 * The {@code QuantumRegister} represents a set of Qubits that can be operated on as a group.
 */
public class QuantumRegister{

    /**the qubits contained in this register*/
    public final Qubit[] qubits;

    /**
     * Creates a new register with the |0⟩⊗n basis state
     * @param length size of the new register to be created, in Qubits
     */
    public QuantumRegister(int length){
        qubits = new Qubit[length];
        for(int x = 0; x < length; x++)
            qubits[x] = new Qubit();
    }

    /**
     * Creates a new register of a given basis state and length. If the given value has bits beyond which the length can include, they will be truncated.
     * @param value basis state value to be stored in the bits of the register
     * @param length amount of qubits to be in the register
     */
    public QuantumRegister(int value, int length){
        qubits = new Qubit[length];
        for(int x = 0; x < length; x++)
            qubits[x] = new Qubit(BitUtils.bit(value,x));
    }

    /**
     * Performs a measurement on all of the qubits in this register. This collapses their states into a basis.
     * @return the state that the register collapses into
     */
    public int measure(){
        Map<Qubit,Boolean> pairs = new HashMap<>(qubits.length);
        delegates().forEach(qs->pairs.putAll(qs.sample(qubits)));

        return buildInt(x->pairs.get(qubits[x]),qubits.length);
    }

    /**
     * Randomly chooses a state based on the their respective probabilities. This is the equivalent of performing a
     * measurement, but without the collapse to a basis state, allowing for the repeated sampling of values without destroying the state.
     * @return a random possible basis, weighted according to the state
     */
    public int sample(){
        Map<Qubit,Boolean> pairs = new HashMap<>(qubits.length);
        delegates().forEach(qs->pairs.putAll(qs.measure(qubits)));

        return buildInt(x->pairs.get(qubits[x]),qubits.length);
    }

    /**
     * Creates a {@code String} representation of this {@code QuantumRegister} including all possible states and their respective probabilities.
     * These probabilities will add to one, and will be separated by lines.
     * @return a {@code String} of the probabilities of each possible basis state.
     */
    public String toString(){
        StringBuilder total = new StringBuilder();
        for(int state = 0; state < (1<<qubits.length); state++){
            double probability = probabilityOf(state);
            if(probability > Complex.DELTA)
                total.append(String.format("%.3f",probability)).append("|").append(BitUtils.toString(toBooleanArray(state, qubits.length))).append("⟩\n");
        }

        return total.toString();
    }

    /**
     * Finds the probability that when measured or sampled, this {@code QuantumRegister} will represent a given basis state.
     * This is represented as a {@code double} value on [0,1]. The probabilities of all basis states added together will be 1.
     * This method will only test the bits that are within the {@code QuantumRegister}, and as such it will act as a periodic function
     * @param state the basis state to be tested for
     * @return the probability of the basis state being the measured value
     */
    public double probabilityOf(int state){
        Map<Qubit,Boolean> s = new HashMap<>();
        for(int x = 0; x < qubits.length; x++)
            s.put(qubits[x],bit(state,x));

        double probability = 1;
        for(QuantumState qs : delegates())
            probability *= qs.probabilityOf(s);
        return probability;
    }

    /**
     * Tests entanglement with a {@code Qubit}
     * @param other the {@code Qubit} to be tested for entanglement with this {@code QuantumRegister}
     * @return whether or not they are entangled, as a {@code boolean}
     */
    public boolean isEntangledWith(Qubit other){
        return delegates().contains(other.delegate);
    }

    /**
     * Creates a {@code Set} of all of the {@code QuantumStates} used by the qubits of this {@code QuantumRegister}
     * @return a set of all delegates in this {@code QuantumRegister}
     */
    Set<QuantumState> delegates(){
        Set<QuantumState> quantumStates = new HashSet<>();
        for(Qubit q : qubits)
            quantumStates.add(q.delegate);
        return quantumStates;
    }
}