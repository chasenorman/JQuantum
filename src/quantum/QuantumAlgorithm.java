package quantum;

import java.math.BigInteger;
import java.util.function.*;

import static quantum.BitUtils.*;
import static quantum.Complex.*;
import static quantum.QuantumGate.*;

/**
 * Houses example static quantum subroutine methods
 */
public class QuantumAlgorithm{
    /**
     * Changes the state of a {@code QubitRegister} from QFT(a) to QFT(a + b). The first parameter of this <em>MUST</em> have gone
     * through the QFT function beforehand, and the result will only be found by performing the inverse QFT function.
     * @param qft a {@code QubitRegister} for the function to be applied on, already passed through the QFT function
     * @param b value to be added to the register
     */
    public static void QFTADD(QubitRegister qft, int b){
        for(int q = qft.qubits.length-1; q >= 0; q--)
            for(int i = 0; i <= q; i++)
                if (BitUtils.bit(b,q-i))
                    R(i+1).accept(qft.qubits[(qft.qubits.length-1) - q]);
    }

    /**
     * Changes the state of a {@code QubitRegister} from QFT(a) to QFT(a + b) if the control Qubit is set.
     * The first parameter of this <em>MUST</em> have gone through the QFT function beforehand,
     * and the result will only be found by performing the inverse QFT function.
     * @param qft a {@code QubitRegister} for the function to be applied on, already passed through the QFT function
     * @param b value to be added to the register
     * @param c the control {@code Qubit}
     */
    public static void CQFTADD(QubitRegister qft, int b, Qubit c){
        for(int q = qft.qubits.length-1; q >= 0; q--)
            for(int i = 0; i <= q; i++)
                if (BitUtils.bit(b,q-i))
                    C(R(i+1)).accept(qft.qubits[(qft.qubits.length-1) - q], c);
    }

    /**
     * the Multiplier Accumulator. Changes the state of a {@code QubitRegister} from QFT(a) to QFT(a + x*y)
     * @param qft a {@code QubitRegister} for the function to be applied on, already passed through the QFT function
     * @param x operand to be multiplied
     * @param y multiplier constant
     */
    public static void QFTMAC(QubitRegister qft, QubitRegister x, int y){
        for(int i = 0; i < x.qubits.length; i++)
            CQFTADD(qft,y*(1<<i),x.qubits[i]);
    }

    /**
     * Changes the state of a {@code QubitRegister} from QFT(a) to QFT(a + b). The first parameter of this <em>MUST</em> have gone
     * through the QFT function beforehand, and the result will only be found by performing the inverse QFT function.
     * @param qft a {@code QubitRegister} for the function to be applied on, already passed through the QFT function
     * @param b quantum value to be added to the register
     */
    public static void genericQFTADD(QubitRegister qft, QubitRegister b){
        for(int q = qft.qubits.length-1; q >= 0; q--)
            for(int i = 0; i <= q; i++)
                if (q-i < b.qubits.length)
                    C(R(i + 1)).accept(qft.qubits[(qft.qubits.length - 1) - q], b.qubits[q - i]);
    }

    /**
     * Applies the Quantum Fourier Transform on a register
     * @param qr operand
     */
    public static void QFT(QubitRegister qr){
        for(int x = 0; x < qr.qubits.length; x++){ //0..<length
            H.accept(qr.qubits[qr.qubits.length-1-x]);
            for(int y = 0; y < qr.qubits.length-1-x; y++)
                C(R(y+2)).accept(qr.qubits[qr.qubits.length-1-x],qr.qubits[qr.qubits.length-x-y-2]);
        }
        reverse(qr);
    }

    /**
     * Applies the inverse Quantum Fourier Transform on a register
     * @param qr operand
     */
    public static void inverseQFT(QubitRegister qr) {
        for(int x = 0; x < qr.qubits.length; x++){ //0..<length
            for(int y = 0; y < x; y++)
                C(R(1+x-y)).inverse().accept(qr.qubits[qr.qubits.length-1-x],qr.qubits[qr.qubits.length-1-y]);
            H.accept(qr.qubits[qr.qubits.length-1-x]);
        }
        reverse(qr);
    }

    /**
     * Applies the hadamard gate on the entirety of a {@code QubitRegister}.
     * @param qr register to apply the Hadamard gate to.
     */
    public static void H(QubitRegister qr){
        for(Qubit q : qr.qubits)
            H.accept(q);
    }

    /**
     * reverses the qubits of a {@code QubitRegister} by directly reordering them.
     * @param qr {@code QubitRegister} to be reversed
     */
    public static void reverse(QubitRegister qr){
        Qubit[] q = new Qubit[qr.qubits.length];
        System.arraycopy(qr.qubits,0,q,0,qr.qubits.length);

        for(int i = 0; i < qr.qubits.length; i++)
            qr.qubits[i] = q[qr.qubits.length-i-1];
    }

    /**
     * Performs Grovers search algorithm. Finds a particular value of an oracle function in O(sqrt(N)) evaluations
     * @param oracle function to be searched. This oracle function should flip the polarity of whichever state is to be searched for.
     * @param length the amount of bits in the result to be searched
     * @return the value whose phase is flipped through the oracle
     */
    public static int grovers(Consumer<QubitRegister> oracle, int length){
        QubitRegister qr = new QubitRegister(length);

        Consumer<QubitRegister> nand = oracle(x->x!=0);

        H(qr);
        for(int invocations = 0; invocations < Math.PI*0.25*Math.sqrt(1<<length)+1; invocations++){
            oracle.accept(qr);
            H(qr);
            nand.accept(qr);
            H(qr);
        }

        return qr.measure();
    }

    /**
     * Determines whether a predicate function is constant or balanced. The function should flip the phase of any "true" values and leave
     * others alone.
     * @param oracle predicate function
     * @param length length of {@code QubitRegister} this function should operate on
     * @return evaluates to {@code true} if constant, and {@code false} if balanced
     */
    public static boolean deutschJozsa(Consumer<QubitRegister> oracle, int length){
        QubitRegister qr = new QubitRegister(length);

        H(qr);
        oracle.accept(qr);
        H(qr);

        return qr.measure()==0;
    }

    /**
     * Creates a quantum function that flips the phase of any basis state coefficients as determined by the predicate specified.
     * @param function predicate to determine which basis state coefficients are phase-flipped
     * @return a quantum oracle function representation of the predicate given
     */
    public static Consumer<QubitRegister> oracle(Predicate<Integer> function){
        return qr->{
            Complex[][] matrix = new Complex[1 << qr.qubits.length][1 << qr.qubits.length];

            for (int x = 0; x < 1 << qr.qubits.length; x++) {
                boolean test = function.test(x);
                for (int y = 0; y < 1 << qr.qubits.length; y++) {
                    if (x == y && test)
                        matrix[x][y] = new Complex(-1);
                    else if (x == y)
                        matrix[x][y] = ONE;
                    else
                        matrix[x][y] = ZERO;
                }
            }

            new QuantumGate(matrix).accept(qr);
        };
    }

    /**
     * Creates a quantum function version of a given integer operator. The resulting function will take two registers as input, one to
     * be used as input for the function, and the other to be used as a regiser for the output. Any data in the output register will
     * be ignored. The resulant function will instantiate a gate of arbitrary size, that will execute the function given.
     * @param function The function to be mapped into a quantum function
     * @return the quantum function created
     */
    public static BiConsumer<QubitRegister, QubitRegister> quantumFunction(UnaryOperator<Integer> function){
        //this assumes that the first qubits in the gate are least significant. This assumption may need to be reversed

        return (input, output)->{
            Qubit[] qubits = new Qubit[input.qubits.length+output.qubits.length];
            System.arraycopy(input.qubits,0,qubits,0,input.qubits.length);
            System.arraycopy(output.qubits,0,qubits,input.qubits.length,output.qubits.length);

            Complex[][] matrix = new Complex[1 << qubits.length][1 << qubits.length];

            int mask = (1<<input.qubits.length)-1;

            for (int x = 0; x < 1 << qubits.length; x++) {
                for (int y = 0; y < 1 << qubits.length; y++) {
                    int value = function.apply(y&mask)%(1<<output.qubits.length);
                    matrix[x][y] = ZERO;
                    //if x is the output of y
                    if((x&mask)==(y&mask) && (x>>>input.qubits.length)==value)
                        matrix[x][y] = ONE;
                }
            }

            new QuantumGate(matrix).accept(qubits);
        };
    }

    /**
     * More likely to evaluate to a possible period of a quantum function.
     * @param oracle function that takes in two registers. This function changes the value of the second to the output of a function with the first.
     * @param inputLength length of the input register of the function
     * @param outputLength length of the output register of the function
     * @return a {@code QubitRegister} with higher probability to evaluate to the period of the function
     */
    public static QubitRegister periodFinder(BiConsumer<QubitRegister,QubitRegister> oracle, int inputLength, int outputLength){
        QubitRegister input = new QubitRegister(inputLength);
        QubitRegister output = new QubitRegister(outputLength);
        H(input);
        oracle.accept(input,output);
        inverseQFT(input);
        return input;
    }


    /**
     * Performs a rudimentary, inefficient version of Shor's factorization algorithm. This will print out debug information, and will occasionally reproduce
     * the value to be factored. Should really only be tested on the values 15 or 21, as this is unusable with square numbers.
     * @param N value to be factored, probably should not exceed 21
     * @return a factor of N
     */
    public static int shorsAlgorithm(int N){
        int r;
        int a;
        while(true){
            a = (int)(Math.random()*N);
            if(BigInteger.valueOf(a).gcd(BigInteger.valueOf(N)).intValueExact()!=1) {
                System.out.println("found solution trivially :(");
                return BigInteger.valueOf(a).gcd(BigInteger.valueOf(N)).intValueExact();
            }
            r = shorsQuantumSubroutine(N,a);
            if(r%2==0 && ((int)Math.pow(a,r/2))%N != N-1)
                return BigInteger.valueOf((int)Math.pow(a,r/2) + 1).gcd(BigInteger.valueOf(N)).intValueExact();
        }
    }

    private static int shorsQuantumSubroutine(int N, int a){
        int evaluations = 0;
        int n = log2(N);
        int q = log2(N*N);
        int Q = 1<<q;
        QubitRegister qr = periodFinder(quantumFunction(x -> ((int) Math.pow(a, x)) % N), q, n);
        while(true) {
            evaluations++;
            int y = qr.sample();
            int[][] cfe = fractionOf(y / (double) Q, 30);
            for (int x = 0; x < cfe[0].length; x++) {
                //if (Math.abs((q / (double) Q) - (cfe[0][x] / (double) cfe[1][x])) < 1 / (double) (2 * Q)) { //according to wikipedia, this is a requirement.
                    int k = 1;
                    while(cfe[1][x]*k < N){
                        if(Math.pow(a,cfe[1][x]*k)%N==1){
                            System.out.println("candidate found in " + evaluations + " evaluations.");
                            return cfe[1][x]*k;
                        }
                        k++;
                    }
                //}
            }
        }
    }

    private static int[] continuedFractionExpansion(double value, int length){
        if(length == 0)
            return new int[]{};
        else{
            int[] result = new int[length+1];
            int[] x = continuedFractionExpansion(1/(value - (int)value),length-1);
            System.arraycopy(x,0,result,1,x.length);
            result[0] = (int)value;
            return result;
        }
    }

    private static int[][] fractionOf(double value, int length){
        int[] v = continuedFractionExpansion(value,length);
        int pp=1;
        int qq=0;
        int p = v[0];
        int q=1;    // pp is p_{n-1} and p is p_n.
        int[][] answer = new int[2][v.length]; // put answer in this vector
        answer[0][0] = p;
        answer[1][0] = q;
        for(int n = 1; n < v.length; n++) {
            int tp = p;
            int tq = q;
            p = v[n] * p + pp;
            q = v[n] * q + qq;
            pp = tp;
            qq = tq;
            answer[0][n] = p;
            answer[1][n] = q;
        }
        return answer;
    }

    /**
     * uses a Hadamard gate to create a superposition, and collapses that superposition for a random boolean value.
     * @return a random boolean
     */
    public static boolean randomBit(){
        Qubit q = new Qubit();
        H.accept(q);
        return q.measure();
    }

    /**
     * A simple test to prove the system is in working order. Prints out the millis taken to perform the operation, should be around 100.
     */
    public static void main(String[] args){
        long t = System.currentTimeMillis();
        QubitRegister a = new QubitRegister(5,3);
        QubitRegister b = new QubitRegister(5,3);

        QFT(a);
        QFTMAC(a,b,3);
        inverseQFT(a);

        int a0 = a.measure();
        int b0 = b.measure();

        if(a0 == 12 && b0 == 3)
            System.out.println("All ran according to plan in " + (System.currentTimeMillis()-t) + " millis");
        else
            System.out.println("a = " + a0 + ", b = " + b0 + " :(");
    }
}