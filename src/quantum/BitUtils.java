package quantum;

import java.util.function.Predicate;

/**
 * The BitUtils class houses commonly used methods that involve the bits of an {@code int}
 */
public class BitUtils {
    /**
     * Calculates the log base 2 of a number rounded up. This is equivalent to the amount of bits in that number.
     *
     * @param n an {@code int}
     * @return the amount of bits required to represent {@code n} without padding
     */
    public static int log2(int n){
        return Integer.SIZE - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Creates a boolean array of a specified length that with the bits of a given {@code int}, starting with the least significant bit.
     * This is intended to make the nth index of the return value to be the nth bit of the state. This method will ignore any bits beyond the length that .
     *
     * @param state value to be put into the array
     * @param length length of the array to be created.
     * @return a boolean array representation of the bits of state.
     *
     */
    public static boolean[] toBooleanArray(int state, int length){
        boolean[] arr = new boolean[length];
        for(int x = 0; x < length; x++)
            arr[x] = bit(state,x);
        return arr;
    }

    /**
     * Tests for a specified bit of an {@code int}
     * @param state the value to be checked.
     * @param location the bit index of the value to be checked.
     * @return the bit of the value at the location, as a {@code boolean}
     */
    public static boolean bit(int state, int location){
        return ((state>>>location)&1)==1;
    }

    /**
     * Reads a {@code boolean[]} as bits and creates an {@code int}. The first index of the {@code boolean[]} is the least significant of the {@code int}.
     * This is intended to have the nth bit of the resultant to be equivalent to the nth index in the array.
     * @param given the bits of a number, starting with the least significant
     * @return an integer representation with the specified bits
     */
    public static int toInt(boolean[] given){
        return buildInt(x->given[x],given.length);
    }

    /**
     * Creates a binary string representation of a {@code boolean[]}. Ex: [true, true, false] -> 3 -> "011"
     * @param given the bits of the binary value, starting with the least significant bit
     * @return The binary {@code String}, starting with the most significant bit
     */
    public static String toString(boolean[] given){
        StringBuilder total = new StringBuilder(given.length);
        for(int x = given.length-1; x >= 0; x--)
            total.append(given[x]?"1":"0");
        return total.toString();
    }

    /**
     * Inserts a bit into an {@code int}, shifting the most significant bits in order to fit the new value.
     * This has the effect of keeping the least significant {@code location-1} bits the same, adding a new bit {@code value},
     * and shifting the remaining most significant bits over by 1.
     * @param state {@code int} value in which the bit should be inserted.
     * @param location bit index where the bit should be inserted.
     * @param value bit value to be inserted into the {@code int}
     * @return the {@code int} with the inserted bit.
     */
    public static int insertBit(int state, int location, boolean value){
        return (state&((1<<location)-1)) | (value?1<<location:0) | ((state>>>location)<<(location+1));
    }

    /**
     * Initializes an {@code int} with bits according to a specified function, and with a given amount of bits.
     * @param bitmap function for which the bits of the {@code int} will be determined. The {@code Integer} in this predicate is the bit index,
     *               and test should specify whether the bit at this index is 1.
     * @param length amount of bits to be included in the {@code int}. Any possible bits that the {@code bitmap} would respond true to beyond this point are <em>ignored</em>
     * @return the {@code int} with bits as specified by the function.
     */
    public static int buildInt(Predicate<Integer> bitmap, int length){
        int result = 0;
        for(int x = 0; x < length; x++)
            if(bitmap.test(x))
                result |= (1<<x);
        return result;
    }
}