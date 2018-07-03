# JQuantum
A quantum computing library for java

Quantum.java is a quantum computing emulation framework designed to be easy for java programmers to access and understand. It can run alongside classical computing java code. This library efficiantly runs all of the math required to produce an accurate quantum result. I have the intention of allowing as many programmers as possible to be able to utilize this field of computing for themselves and be able to comprehend the possibilities of such a technology.

Of course, this library cannot operate on an extreme scale, and it does not solve problems in a time comparable to theoretical quantum computers. The advantages of such an emulator are repeatable experiments, extreme reliability, scalability, and even the possibility of doing operations impossible on actual quantum hardware. Also, its more accessible, and written in a programmer's language, rather than a physicist's.

## It's not rocket science, just quantum physics

You may have heard that quantum computing allows for qubits to be in the 0 state and the 1 state at the same time. Its actually way cooler than it sounds. Not only can they be in both states at the same time, the entire quantum computer can be in all possible combinations of 1's and 0's simultaneously, and each of these states, called **basis states**, has a coefficient. This coefficient is a complex number, meaning it has a magnitude, and a **phase**. 

There's one catch. Although you can operate on these states to create all sorts of wild combinations of coefficients and phases, at the end of the day, they must be collapsed to a basis state to have meaning. 

With all of these rules in mind, though, the possibilities are great. Superpositions of states can sometimes act as passing all possible values through a function, providing unprecedented computing power, and a lot of quanum subroutines that just don't make practical sense in the real world. 

Oh and one more thing, you've probably seen the symbols ⟨Ψ| and |Ψ⟩. These are the "bra" and the "ket" respectively, and they are a nice segway into how quantum computing is really just glorified complex number matrix math. 

The ket is the most important and most basic, so we'll start with that. When you imagine a ket, think of a tall, skinny matrix, with each row corresponding to a possible state's coefficient. Imagine this matrix is completely full of 0's, except for one location, and that is the location indicated in the ket. The ket, in essence, lets us represent a single basis state very easily, without having to draw out all of the 0 coefficients. 

The bra, on the other hand, represents the _conjugate transpose_ operation on the equivalent ket matrix. This operation involves reflecting the matrix over the diagonal (so it's now horizontal instead of vertical), and taking the complex conjugate of all of the values (essentially flipping all of the phases)

Quantum gates are represented as large square matrices of complex numbers, such that a ket times this matrix yields a ket of the same length, with the applied transformation. These matrices are unitary, meaning that their inverse is their _conjugate transpose_. 

At the end of the day, quantum computing is just fancy matrix math. A tall, skinny matrix, just being passed through a bunch of square matrices. At the end of the chain, a basis state is randomly selected based on the magnitude of the complex numbers on the ket.

## getting to use my program

In my language, interfacing with the quantum is easy. You can initialize a qubit or a quantum register on the fly like so:
`Qubit q = new Qubit(true); //optional boolean to choose between basis states`
`QuantumRegister a = new QuantumRegister(5) //creates 5 qubit register`
`QuantumRegister b = new QuantumRegister(3, 5) //creates a 5 qubit register with the basis 3`

From there, you can call gates on these through the `QuantumGate` class
`H.accept(q) //applies a hadamard gate`
Or you can use preset functions and subroutines that are in the `QuantumFunction` class
`QFT(a) //applies the quantum fourier transform`

All of the classes given come with extensive documentation, and I have a javadoc included, so don't forget to take a look at it, and to see the inner workings of the quantum functions. I worked hard on them, after all.
