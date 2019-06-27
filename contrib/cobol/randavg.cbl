       IDENTIFICATION DIVISION.
       PROGRAM-ID. AVERAGES-TEST.
       AUTHOR. S. ROUG.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. LINUX.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY "rand-ws.cbl".
      * Variables for the testing
       77 AVERAGE     PIC 999V9(9) VALUE 0.

       PROCEDURE DIVISION.
       TESTIT.
           DISPLAY "TEST RANDOM NUMBER GENERATOR".
           MOVE -2992 TO RANDSEED.
           PERFORM TESTRUN 1000 TIMES.
           DIVIDE 1000 INTO AVERAGE.
           DISPLAY "AVERAGE: ", AVERAGE.
           STOP RUN.
       TESTRUN.
           PERFORM RAND.
           ADD RANDVAL TO AVERAGE
              ON SIZE ERROR DISPLAY "OVERFLOW".
      *    DISPLAY RANDVAL.

       COPY "rand-proc.cbl".
