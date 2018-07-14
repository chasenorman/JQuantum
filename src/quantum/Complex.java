package quantum;

/**
 * The {@code Complex} class represents a complex number with a magnitude and a phase.
 */
class Complex {
    /** a constant with the {@code Complex} equivalent of the number 0*/
    static final Complex ZERO = new Complex(0);
    /** a constant with the {@code Complex} equivalent of the number 1*/
    static final Complex ONE = new Complex(1);
    /** a constant for which calculations should be done within the precision of*/
    static final double DELTA = 1e-16;
    /** magnitude (radius) of the {@code Complex}*/
    final double r;
    /** phase value of the {@code Complex}, in radians*/
    final double theta;

    /**
     * Creates a complex number with a given real part, and no imaginary part. This number will have a phase of 0.
     *
     * @param r a {@code double} for the real part of a complex number
     */
    Complex(double r){
        this(r, 0);
    }

    /**
     * Creates a complex number with a given magnitude and phase. The {@code theta} value will be changed such that it is within {@code [0,2*Math.PI)}
     * and the {@code r} value is positive.
     * @param r magnitude of the complex number to be created
     * @param theta phase of the complex number to be created
     */
    Complex(double r, double theta){
        if(r < 0) {
            theta += Math.PI;
            r = -r;
        }
        theta = theta - Math.PI*2*Math.floor(theta/(Math.PI*2));

        this.r = r;
        this.theta = theta;
    }

    /**
     * Multiplies two complex numbers together, and returns the result. This does not modify either of the operands
     * @param o the second operand
     * @return the product of the two complex numbers
     */
    Complex multiply(Complex o){
        return new Complex(r*o.r,theta+o.theta);
    }

    /**
     * Calculates a summation of an array of complex numbers. This is done for efficiency and accuracy in calculation.
     * @param given an array of complex numbers to be summed
     * @return the sum total as a complex number
     */
    static Complex sum(Complex[] given){
        double a = 0;
        double b = 0;
        for(Complex c : given){
            a += c.r*Math.cos(c.theta);
            b += c.r*Math.sin(c.theta);
        }
        return new Complex(Math.sqrt(a*a+b*b),Math.atan2(b,a));
    }

    /**
     * Creates a new vector scaled by a factor of {@code scalar}. If the scalar is negative, this will change theta by {@code Math.PI}
     * @param scalar value for which the vector is to be multiplied
     * @return a scaled vector by a factor of {@code scalar}
     */
    Complex multiply(double scalar){
        return new Complex(scalar*r,theta);
    }

    /**
     * Performs the absolute square operaion, which is equivalent to the square of the magnitude, and is a real number.
     * @return the absolute square of the complex number, as a {@code double}
     */
    double absoluteSquare(){
        return r*r;
    }

    /**
     * Creates a new complex number that is the complex conjugate. This has the same real part but a negated imaginary part.
     * @return the complex conjugate of {@code this}
     */
    Complex conjugate(){
        return new Complex(r,-theta);
    }

    /**
     *
     * @return a {@code String} representation of this complex number, in the form (r, theta)
     */
    public String toString(){
        return "(" + String.format("%.3f",r) + ", " + String.format("%.3f",theta) + ")";
    }

    /**
     * returns true if both the {@code Object} given (either a {@code Complex} or {@code Number} is within {@code DELTA} of this complex number.
     * @param o {@code Object} to be tested for equality
     * @return {@code true} if {@code this} and {@code o} are within {@code DELTA}, {@code false} if otherwise.
     */
    public boolean equals(Object o){
        if(o instanceof Complex) {
            Complex other = ((Complex) o);
            double x = r*Math.cos(theta) - other.r*Math.cos(other.theta);
            double y = r*Math.sin(theta) - other.r*Math.sin(other.theta);
            return x*x+y*y<DELTA*DELTA;
        }
        else if(o instanceof Number){
            Number other = ((Number) o);
            double x = r*Math.cos(theta) - other.doubleValue();
            double y = r*Math.sin(theta);
            return x*x+y*y<DELTA*DELTA;
        }
        return false;
    }
}
