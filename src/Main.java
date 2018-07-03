import static quantum.BitUtils.*;
import static quantum.Complex.*;
import static quantum.QuantumFunction.*;
import quantum.*;

import java.util.ArrayList;
import java.util.function.*;
import java.util.stream.Stream;

import static quantum.QuantumGate.*;

public class Main {
    public static void main(String[] args){

    }

    public static String toString(int[] given){
        StringBuilder total = new StringBuilder("[");
        for(int x = 0; x < given.length-1; x++)
            total.append(given[x]).append(", ");
        return total.append(given[given.length-1]).append("]").toString();
    }

    public static String toString(boolean[] given){
        StringBuilder total = new StringBuilder(given.length);
        for(int x = given.length-1; x >= 0; x--)
            total.append(given[x]?"1":"0");
        return total.toString();
    }
}