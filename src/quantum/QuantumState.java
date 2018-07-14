package quantum;

import java.util.*;
import static quantum.Complex.*;

import static quantum.BitUtils.*;

/**
 * Represents the state of one or many entangled qubits. Carries the coefficients of all states possible, and performs operations on them.
 * Altogether, this class performs all of the mathematics behind the quantum programming, and should be inaccessible through normal means.
 */
class QuantumState {
    /**contains the coefficients of all possible states from |0⟩⊗n to |1⟩⊗n*/
    private final Complex[] state;
    /**contains all of the qubits represented by this state*/
    private final Qubit[] qubits;

    /**
     * Creates an empty quantum state with arrays of null, but with the proper length.
     * @param q the amount of qubits to be in this {@code QuantumState}
     */
    private QuantumState(int q){
        state = new Complex[1<<q];
        qubits = new Qubit[q];
    }

    /**
     * Creates a {@code QuantumState} of a single {@code Qubit} of a specified basis
     * @param qubit the {@code Qubit} that this {@code QuantumState} will represent
     * @param initial the initial basis state of the {@code Qubit}
     */
    QuantumState(Qubit qubit, boolean initial){
        state = initial?new Complex[]{ZERO,ONE}
                       :new Complex[]{ONE,ZERO};
        qubits = new Qubit[]{qubit};
    }

    static synchronized void entangle(QuantumState first, QuantumState second){
        if(first == second)
            return;

        QuantumState result = new QuantumState(first.qubits.length+second.qubits.length);

        System.arraycopy(first.qubits, 0, result.qubits, 0, first.qubits.length);
        System.arraycopy(second.qubits, 0, result.qubits, first.qubits.length, second.qubits.length);

        for(int y = 0; y < second.state.length; y++)
            for(int x = 0; x < first.state.length; x++)
                result.state[y*first.state.length + x] = first.state[x].multiply(second.state[y]);

        for(int x = 0; x < result.qubits.length; x++) {
            result.qubits[x].delegate = result;
            result.qubits[x].index = x;
        }
    }

    /**
     * Collapses the state of all of the specified qubits that are in this system. This means <em>NOT ALL</em> qubits will be measured that are asked.
     * @param q an array of qubits to be measured
     * @return a mapping of the successfully measured qubits and their respective basis
     */
    synchronized Map<Qubit,Boolean> measure(Qubit... q) {
        Map<Qubit, Boolean> result = sample(q);

        QuantumState mainState = new QuantumState(qubits.length-result.size());
        double constant = Math.sqrt(1/probabilityOf(result));

        int currentState;
        for(int x = 0; x < (1<<(qubits.length-result.size())); x++){
            currentState = x;
            for(int y = 0; y < qubits.length; y++)
                if(result.containsKey(qubits[y]))
                    currentState = insertBit(currentState,y,result.get(qubits[y]));
            mainState.state[x] = state[currentState].multiply(constant);
        }

        int index = 0;
        for(int x = 0; x < qubits.length; x++)
            if(result.containsKey(qubits[x])) {
                qubits[x].delegate = new QuantumState(qubits[x], result.get(qubits[x]));
                qubits[x].index = 0;
            }
            else{
                qubits[x].delegate = mainState;
                qubits[x].index = index;
                mainState.qubits[index++] = qubits[x];
            }

        return result;
    }

    /**
     * Selects a random basis state mapping of the qubits specified. This method <em>WILL NOT</em> find a mapping for all of the
     * qubits specified, but only the ones that are included within this {@code QuantumState}. The random state will be chosen
     * according to the weighting in the {@code QuantumState}
     * @param qubits qubits to be sampled
     * @return a mapping of all qubits that are within this state, to a possible basis state.
     */
     Map<Qubit,Boolean> sample(Qubit... qubits){
        Map<Qubit, Boolean> result = new HashMap<>();

        double r = Math.random();
        double total = 0;
        int i = 0;
        while(total < r)
            total += state[i++].absoluteSquare();

        for(Qubit q : qubits)
            if(q.delegate == this)
                result.put(q, bit(i-1,q.index));

        return result;
    }

    /**
     * Applies a gate within this {@code QuantumState}, modifying the state
     * @param gate gate to be applied
     * @param operands an array of qubits that are within the {@code QuantumState}
     */
    synchronized void apply(QuantumGate gate, Qubit... operands) {
        //create the bitMap array, an array with the index of bits such that the operands are the least significant
        int[] map = new int[qubits.length];
        int operandBits = 0;
        int mapIndex;
        for (mapIndex = 0; mapIndex < operands.length; mapIndex++)
            operandBits ^= 1 << (map[mapIndex] = operands[mapIndex].index);
        for(int i = 0; i < qubits.length; i++)
            if(!bit(operandBits,i))
                map[mapIndex++] = i;

        for(int mappedState = 0; mappedState < (1<<qubits.length); mappedState += (1<<operands.length)){
            Complex[] gateInput = new Complex[1<<operands.length];
            int[] unmappedStates = new int[1<<operands.length];
            for(int i = 0; i < gateInput.length; i++){
                for(int x = 0; x < qubits.length; x++)
                    unmappedStates[i] |= (bit(mappedState+i,x)?1:0)<<map[x];
                gateInput[i] = state[unmappedStates[i]];
            }
            Complex[] gateOutput = gate.apply(gateInput);
            for(int i = 0; i < gateOutput.length; i++)
                state[unmappedStates[i]] = gateOutput[i];
        }

        simplify();
    }

    /**
     * Splits this {@code QuantumState}, if possible, into separate groups of entanglement.
     * This method removes qubits from this {@code QuantumState} and creates new delegates for them based on their entanglement.
     * After execution, the qubits and coefficients of this {@code QuantumState} may have changed, but there will be no unentangled qubits.
     */
    private synchronized void simplify(){
        List<List<Integer>> dependencies = getDependencies();

        if(dependencies.size() <= 1)
            return;

        QuantumState[] quantumStates = new QuantumState[dependencies.size()];
        for(int x = 0; x < quantumStates.length; x++)
            quantumStates[x] = new QuantumState(dependencies.get(x).size());

        for(int x = 0; x < quantumStates.length; x++){
            double constant = 0;
            int start = 0;
            Map<Qubit,Boolean> s = new HashMap<>();
            while(constant < Complex.DELTA) {
                int z = 0;
                for(int y = 0; y < qubits.length; y++)
                    if(!dependencies.get(x).contains(y))
                        s.put(qubits[y],bit(start,z++));
                constant = probabilityOf(s);
                start++;
            }

            constant = Math.sqrt(constant);

            for(int y = 0; y < (1<<dependencies.get(x).size()); y++){
                int s0 = start-1;
                for(int z = 0; z < dependencies.get(x).size(); z++)
                    s0 = insertBit(s0, dependencies.get(x).get(z), bit(y, z));
                quantumStates[x].state[y] = new Complex((state[s0].r)/constant,state[s0].theta - ((x==quantumStates.length-1)?0:state[0].theta));
            }
        }

        for(int x = 0; x < quantumStates.length; x++)
            for(int y = 0; y < dependencies.get(x).size(); y++) {
                int i = dependencies.get(x).get(y);
                qubits[i].delegate = quantumStates[x];
                qubits[i].index = y;
                quantumStates[x].qubits[y] = qubits[i];
            }
    }

    /**
     * Creates a {@code String} representation of this {@code QuantumState}, and the complex coefficients of all possible basis states.
     * @return A {@code String} representation of this {@code QuantumState}
     */
    public String toString(){
        StringBuilder total = new StringBuilder();
        for(int x = 0; x < state.length; x++)
            if(!(state[x].r < Complex.DELTA))
                total.append(state[x]).append("|").append(BitUtils.toString(toBooleanArray(x,qubits.length))).append("⟩\n");
        return total.toString();
    }

    /**
     * @param i1 index of the first Qubit, must be former
     * @param i2 index of the second Qubit, must be latter
     * @return whether the two Qubits are entangled within the {@code QuantumState}
     */
    private synchronized boolean areEntangled(int i1, int i2){
        for(int x = 0; x < 1<<(qubits.length-2); x++){
            final int s = insertBit(insertBit(x,i1,false),i2,false);
            if(!state[s].multiply(state[s|(1<<i1)|(1<<i2)]).equals(
                    state[s|(1<<i1)].multiply(state[s|(1<<i2)])))
                return true;
        }
        return false;
    }

    /**
     * Determines a list of all groupings of entangled Qubits within this state. Under normal conditions
     * all qubits would be entangled in a single group, but after a gate, this could change before simplification.
     * @return A list, with the groupings inside of them, using integers as indices to represent the qubits.
     */
    private synchronized List<List<Integer>> getDependencies(){
        List<List<Integer>> result = new ArrayList<>();
        boolean[] placed = new boolean[qubits.length];

        for(int x = 0; x < qubits.length; x++)
            if(!placed[x]){
                placed[x] = true;
                List<Integer> segment = new ArrayList<>();
                segment.add(x);
                for(int y = x+1; y < qubits.length; y++)
                    if(!placed[y] && areEntangled(x,y)){
                        segment.add(y);
                        placed[y] = true;
                    }
                result.add(segment);
            }

        return result;
    }

    /**
     * Determines the probability of finding a particular state of Qubits within this {@code QuantumState}. All qubits outside the state
     * will be ignored in the calculation. This probability will be on the interval [0,1]
     * @param s state mapping of qubits to basis to be tested for
     * @return the probability of finding the state {@code s} within the {@code QuantumState}
     */
    synchronized double probabilityOf(Map<Qubit,Boolean> s){
        long inner = s.keySet().stream().filter(q->q.delegate==this).count();
        if(inner == 0)
            return 1;

        double probability = 0;
        int currentState;
        for(int x = 0; x < (1<<(qubits.length-inner)); x++){
            currentState = x;
            for(int y = 0; y < qubits.length; y++)
                if(s.containsKey(qubits[y]))
                    currentState = insertBit(currentState,y,s.get(qubits[y]));
            probability += state[currentState].absoluteSquare();
        }

        return probability;
    }
}