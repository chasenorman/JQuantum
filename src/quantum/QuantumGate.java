package quantum;

import java.util.function.Consumer;

import static quantum.Complex.*;

/**
 * The {@code QuantumGate} class represents a logic gate in the form of a complex number matrix which operates on quantum states.
 */
public class QuantumGate implements Consumer<Qubit[]> {
    /**
     * the Hadamard gate
     *
     * <p>
     *     This gate transforms the |0⟩ basis state into an equal superposition of |0⟩ and |1⟩, while transforming the
     *     |1⟩ basis state into the same superposition, except with the opposite phase on the |1⟩.
     * </p>
     */
    public static final QuantumGate H = new QuantumGate(
            new Complex[][]{
                    {new Complex(1/Math.sqrt(2)), new Complex(1/Math.sqrt(2))},
                    {new Complex(1/Math.sqrt(2)), new Complex(-1/Math.sqrt(2))}
            }
    );

    /**
     * the Pauli-X gate
     *
     * <p>
     *     Acts as a NOT gate on a single Qubit, transforming |0⟩ to |1⟩ and vice versa.
     * </p>
     */
    public static final QuantumGate X = new QuantumGate(
            new Complex[][]{
                    {ZERO, ONE},
                    {ONE, ZERO}
            }
    );

    /**
     * the Pauli-Y gate
     *
     * <p>
     *     Represents a π radian rotation about the Y-axis
     * </p>
     */
    public static final QuantumGate Y = new QuantumGate(
            new Complex[][]{
                    {ZERO,new Complex(-1,Math.PI/2)},
                    {new Complex(1,Math.PI/2),ZERO}
            }
    );

    /**
     * the Pauli-Z gate
     *
     * <p>
     *     Represents a π radian phase shift in a single Qubit.
     * </p>
     */
    public static final QuantumGate Z = R(1);

    /**
     * the SWAP gate
     *
     * <p>
     *     Acts on two Qubits, swapping their respective states.
     * </p>
     */
    public static final QuantumGate S = new QuantumGate(
            new Complex[][]{
                    {ONE, ZERO, ZERO, ZERO},
                    {ZERO, ZERO, ONE, ZERO},
                    {ZERO, ONE, ZERO, ZERO},
                    {ZERO, ZERO, ZERO, ONE}
            }
    );

    /**
     * the √SWAP gae
     *
     * <p>
     *     When performed twice, this gate is equivalent to a swap, therefore this gate is the equivalent of the square root of a swap.
     * </p>
     */
    public static final QuantumGate SQRT_SWAP = new QuantumGate(
            new Complex[][]{
                    {ONE, ZERO, ZERO, ZERO},
                    {ZERO, new Complex(Math.sqrt(2) / 2, Math.PI / 4), new Complex(Math.sqrt(2) / 2, -Math.PI / 4), ZERO},
                    {ZERO, new Complex(Math.sqrt(2)/2,-Math.PI/4),new Complex(Math.sqrt(2)/2,Math.PI/4), ZERO},
                    {ZERO, ZERO, ZERO, ONE}
            }
    );

    /**
     * the Controlled-NOT gate
     *
     * <p>
     *    Operates on 2 Qubits, performing NOT on the first iff the second is |1⟩
     * </p>
     */
    public static final QuantumGate CNOT = C(X);

    /**
     * the √¬ gate
     *
     * <p>
     *     When performed twice, this gate is equivalent to a NOT (X) gate, so it considered the square root of the not gate.
     * </p>
     */
    public static final QuantumGate SQRT_NOT = new QuantumGate(
            new Complex[][]{
                    {new Complex(Math.sqrt(2)/2,Math.PI/4),new Complex(Math.sqrt(2)/2,-Math.PI/4)},
                    {new Complex(Math.sqrt(2)/2,-Math.PI/4),new Complex(Math.sqrt(2)/2,Math.PI/4)}
            }
    );

    private Complex[][] matrix;
    public int size;


    //TODO be a bit more restrictive with what can be a gate.
    public QuantumGate(Complex[][] matrix){
        if(((matrix.length & (matrix.length - 1)) != 0) || matrix.length == 0 || matrix.length != matrix[0].length)
            throw new IllegalArgumentException("Matrix not of proper dimensions");

        size = (int)Math.round(Math.log(matrix.length)/Math.log(2));
        this.matrix = matrix;
    }

    /**
     * Performs a complex number matrix multiplication in order to apply the gate on a set of complex numbers.
     * @param input an array of complex numbers, representing the coefficients of all of the quantum states |0⟩⊗n to |1⟩⊗n
     * @return the result of multiplying the given quantum state to the gate matrix. This will be of the same length as the input, and will represent the same coefficients as given.
     */
    Complex[] apply(Complex[] input){
        if(input.length != 1<<size)
            throw new IllegalArgumentException("input is of the wrong length.");

        Complex[] result = new Complex[1<<size];
        Complex[] sum = new Complex[result.length];
        
        for(int x = 0; x < result.length; x++) {
            for (int y = 0; y < result.length; y++)
                sum[y] = matrix[x][y].multiply(input[y]);
            result[x] = sum(sum);
        }

        return result;
    }

    /**
     * Creates a phase-shift gate, on one Qubit, of order k, such that the shift is determined by 2π/(2^k) radians. R(1) ≡ Z
     * @param k the order of the phase-shift gate to be created
     * @return a {@code QuantumGate} R_k
     */
    public static QuantumGate R(int k){
        double phi = (2*Math.PI)/(1<<k);
        return new QuantumGate(
            new Complex[][]{
                    {ONE, ZERO},
                    {ZERO, new Complex(1,phi)}
            }
        );
    }

    /**
     * Creates a gate that is controlled by the last parameter passed in. This means the gate will perform iff the last Qubit is |1⟩
     * @param gate The gate to be transformed into a controlled gate
     * @return The controlled gate.
     */
    public static QuantumGate C(QuantumGate gate){
        int gateStates = 1<<gate.size;
        Complex[][] matrix = new Complex[gateStates*2][gateStates*2];
        for(int x = 0; x < matrix.length; x++)
            for(int y = 0; y < matrix[x].length; y++)
                if(x >= gateStates && y >= gateStates)
                    matrix[x][y] = gate.matrix[x-gateStates][y-gateStates];
                else
                    matrix[x][y] = x==y ? ONE : ZERO;
        return new QuantumGate(matrix);
    }

    /**
     *
     * @return a {@code String} representation of the gate matrix
     */
    public String toString(){
        StringBuilder total = new StringBuilder("");
        for(Complex[] row : matrix){
            total.append("[");
            for(int x = 0; x < row.length-1; x++)
                total.append(row[x]).append(", ");
            total.append(row[row.length-1]).append("]\n");
        }
        return total.toString();
    }

    /**
     * Creates an inverse gate U^†, that when applied before or after the gate will result in the identity matrix. Because the matrices in
     * quantum gates are unitary, this is equivalent to the conjugate transpose.
     * @return The inverse of this gate.
     */
    public QuantumGate inverse(){
        Complex[][] m = new Complex[matrix.length][matrix[0].length];
        for(int x = 0; x < matrix.length; x++)
            for(int y = 0; y < matrix[x].length; y++)
                m[x][y] = matrix[y][x].conjugate();
        return new QuantumGate(m);
    }

    /**
     * Applies this quantum gate to the operand qubis
     * @param qubits the operand qubits
     */
    public void accept(Qubit... qubits){
        if(qubits.length != size)
            throw new IllegalArgumentException("Invalid number of operands");

        for(int x = 1; x < qubits.length; x++)
            QuantumState.combine(qubits[0].delegate,qubits[x].delegate);

        qubits[0].delegate.apply(this,qubits);
    }

    /**
     * Applies this quantum gate to the qubits of a quantum register
     * @param qr quantum register for which this gate will be applied
     */
    public void accept(QuantumRegister qr){
        if(qr.qubits.length != size)
            throw new IllegalArgumentException("Invalid number of operands");

        qr.delegates().forEach(qs->QuantumState.combine(qr.qubits[0].delegate,qs));

        qr.qubits[0].delegate.apply(this,qr.qubits);
    }
}