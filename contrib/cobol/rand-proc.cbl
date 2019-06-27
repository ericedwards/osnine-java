      * AUTHOR. S. ROUG.
      * Knuth's random generator from pages 221-222 in
      * Numerical Recipes in Pascal, 1st edition.
      * Returns a uniform random deviate between 0.0 and 1.0.
      * (Uses long integers)
      * Set randseed to any negative value to (re)initialize the sequence.
      * Returns the value in global RANDVAL.
       RAND.
           IF RANDSEED < 0
               PERFORM RAN3-INIT.
           ADD 1 TO RAN3INEXT.
           IF RAN3INEXT > 55
               MOVE 1 TO RAN3INEXT.
           ADD 1 TO RAN3EXTP.
           IF RAN3EXTP > 55
               MOVE 1 TO RAN3EXTP.
           SUBTRACT RAN3MA(RAN3EXTP) FROM RAN3MA(RAN3INEXT)
               GIVING RAN3-MJ.
           IF RAN3-MJ < 0
               ADD RAN3-MBIG TO RAN3-MJ.
           MOVE RAN3-MJ TO RAN3MA(RAN3INEXT).
           MULTIPLY RAN3-MJ BY RAN3-FAC GIVING RANDVAL.

      *
      * Initialize the storage for the random generator
      *
       RAN3-INIT.
           ADD RAN3-MSEED TO RANDSEED GIVING RAN3-MJ.
           IF RAN3-MJ < 0
               SUBTRACT RAN3-MJ FROM 0 GIVING RAN3-MJ.
           PERFORM RAN3-BELOW-MBIG UNTIL RAN3-MJ NOT > RAN3-MBIG.
           MOVE RAN3-MJ TO RAN3MA(55).
           MOVE 1 TO RAN3-MK.
           PERFORM RAN3-INITTBL VARYING RAN3-I
               FROM 1 BY 1 UNTIL RAN3-I = 55.
           PERFORM RAN3-SCRM1 4 TIMES.
           MOVE 0 TO RAN3INEXT.
           MOVE 31 TO RAN3EXTP.
           MOVE 1 TO RANDSEED.

       RAN3-INITTBL.
           MULTIPLY RAN3-I BY 21 GIVING RAN3-J.
           DIVIDE RAN3-J BY 55 GIVING RAN3-K.
           MULTIPLY 55 BY RAN3-K.
           SUBTRACT RAN3-K FROM RAN3-J.
           MOVE RAN3-MK TO RAN3MA(RAN3-J).
           SUBTRACT RAN3-MK FROM RAN3-MJ GIVING RAN3-MK.
           IF RAN3-MK < 0
               ADD RAN3-MBIG TO RAN3-MK.
           MOVE RAN3MA(RAN3-J) TO RAN3-MJ.

       RAN3-BELOW-MBIG.
           SUBTRACT RAN3-MBIG FROM RAN3-MJ.

       RAN3-SCRM1.
           PERFORM RAN3-SCRM2 VARYING RAN3-I
               FROM 1 BY 1 UNTIL RAN3-I > 55.
       RAN3-SCRM2.
           ADD 30 TO RAN3-I GIVING RAN3-J.
           DIVIDE RAN3-J BY 55 GIVING RAN3-K.
           MULTIPLY 55 BY RAN3-K.
           SUBTRACT RAN3-K FROM RAN3-J.
           ADD 1 TO RAN3-J.
           SUBTRACT RAN3MA(RAN3-J) FROM RAN3MA(RAN3-I).
           IF RAN3MA(RAN3-I) < 0
               ADD RAN3-MBIG TO RAN3MA(RAN3-I).
