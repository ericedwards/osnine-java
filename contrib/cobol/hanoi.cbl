       IDENTIFICATION DIVISION.
       PROGRAM-ID. TOWERS-OF-HANOI.
       DATE-WRITTEN. 2019-06-24.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       77  NUM-DISKS                   PIC 9 VALUE 4.
       77  STACK-POINTER               PIC 9 USAGE COMP.
       77  OLD-POINTER                 PIC 9 USAGE COMP.
       01  GAMESET.
           02  STACK OCCURS 10 TIMES.
             03 N                        PIC 9 USAGE COMP.
             03 FROM-POLE                PIC 9 USAGE COMP.
             03 TO-POLE                  PIC 9 USAGE COMP.
             03 VIA-POLE                 PIC 9 USAGE COMP.

      * Towers of Hanoi. Assumes paragraphs can be called recursively
       PROCEDURE DIVISION.
       HANOI.
           DISPLAY "TOWERS OF HANOI PUZZLE WITH ", NUM-DISKS, " DISKS.".
           MOVE 1 TO STACK-POINTER.
           MOVE NUM-DISKS TO N (STACK-POINTER).
           MOVE 1 TO FROM-POLE (STACK-POINTER).
           MOVE 2 TO TO-POLE (STACK-POINTER).
           MOVE 3 TO VIA-POLE (STACK-POINTER).
           PERFORM CHECK-MOVE.
           DISPLAY "TOWERS OF HANOI PUZZLE COMPLETED!".
           STOP RUN.

       CHECK-MOVE.
           IF N (STACK-POINTER) > 0 PERFORM MOVE-DISK.
       MOVE-DISK.
           MOVE STACK-POINTER TO OLD-POINTER.
           ADD 1 TO STACK-POINTER.
           SUBTRACT 1 FROM N (OLD-POINTER) GIVING N (STACK-POINTER).
           MOVE FROM-POLE (OLD-POINTER) TO FROM-POLE (STACK-POINTER).
           MOVE VIA-POLE (OLD-POINTER) TO TO-POLE (STACK-POINTER).
           MOVE TO-POLE (OLD-POINTER) TO VIA-POLE (STACK-POINTER).
           PERFORM CHECK-MOVE.
           SUBTRACT 1 FROM STACK-POINTER GIVING OLD-POINTER.
           DISPLAY "MOVE DISK FROM ", FROM-POLE (OLD-POINTER),
               " TO ", TO-POLE (OLD-POINTER).
           SUBTRACT 1 FROM N (OLD-POINTER) GIVING N (STACK-POINTER).
           MOVE VIA-POLE (OLD-POINTER) TO FROM-POLE (STACK-POINTER).
           MOVE TO-POLE (OLD-POINTER) TO TO-POLE (STACK-POINTER).
           MOVE FROM-POLE (OLD-POINTER) TO VIA-POLE (STACK-POINTER).
           PERFORM CHECK-MOVE.
           SUBTRACT 1 FROM STACK-POINTER.

