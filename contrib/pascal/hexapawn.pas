{
HEXAPAWN:  INTERPRETATION OF HEXAPAWN GAME AS PRESENTED IN
MARTIN GARDNER'S "THE UNEXPECTED HANGING AND OTHER MATHEMATICAL
DIVERSIONS", CHAPTER EIGHT:  A MATCHBOX GAME-LEARNING MACHINE
ORIGINAL VERSION FOR H-P TIMESHARE SYSTEM BY R.A. KAAPKE 5/5/76
INSTRUCTIONS BY JEFF DALTON
CONVERSION TO MITS BASIC BY STEVE NORTH
CONVERSION TO PASCAL BY SOREN ROUG
}

PROGRAM hexapawn(input, output);

  CONST
    computer = -1;
    empty = 0;
    human = 1;
    maxpatterns = 19;
  TYPE
    str9 = ARRAY [1..9] OF char;
    str11 = ARRAY [1..11] OF char;
  VAR
    pattern : ARRAY [1..maxpatterns, 1..9] OF integer;
    moves : ARRAY [1..maxpatterns, 1..4] OF integer;
    s,t : ARRAY [1..9] OF integer;
    p : ARRAY [computer..human] OF char;
    m1,m2,i,j,k,won,lost,x,y : integer;
    a : char;
    seed : real;
    reversed,gameres : boolean;

FUNCTION inputerr(r: integer):integer;
  BEGIN
    writeln('** Input error - reenter **');
    inputerr := 1;
    readln(input) { skip beyond carriage return }
  END;

{ Random generator. Needs 32 bit integer or real as seed }
FUNCTION rnd(VAR seed : real):integer;
  CONST
    multiplier = 25173.0;
    increment = 13849.0;
    modulus = 32767.0;
  BEGIN
    rnd := round(seed);
    seed := afrac((multiplier * seed + increment) / modulus) * modulus
  END;

PROCEDURE randomize(VAR seed:real);
  CONST
    modulus = 32767.0;
  BEGIN
    seed := afrac(seed / modulus) * modulus
  END;

{ Read integer coordinate separated by a comma }
PROCEDURE readmove(VAR x,y:integer);
  VAR
    r : integer;
    comma: char;
  BEGIN
    prompt;  { flush output }
    REPEAT
      ioabort(input, false);
      readln(x, comma, y);
      r := ioresult(input);
      ioabort(input, true);
      IF r <> 0 THEN
        r := inputerr(r);
      IF comma <> ',' THEN
        r := inputerr(20);
    UNTIL r = 0;
  END;

  FUNCTION fnr(x:integer):integer;
  BEGIN
     CASE x OF
     1: fnr := 3;
     2: fnr := 2;
     3: fnr := 1;
     4: fnr := 6;
     5: fnr := 5;
     6: fnr := 4;
     7: fnr := 9;
     8: fnr := 8;
     9: fnr := 7
     END
  END;

  FUNCTION fnm(y:integer):integer;
  BEGIN
    fnm := y MOD 10
  END;

  { Read a y/n answer. Returns uppercase }
  PROCEDURE readyesno(var yesno:char);
    BEGIN
      prompt;  { flush output }
      readln(yesno);
      if (yesno >= 'a') and (yesno <= 'z') then
        yesno := chr(ord(yesno) - ord('a') + ord('A'));
    END;

  { Initialise the patterns. Only the patterns that have moves are listed. }
  PROCEDURE init;
  VAR
    s9 : str9;
    n2 : ARRAY [0..1] OF char;
    c : ARRAY [1..maxpatterns] OF str9;
    n11 : ARRAY [1..maxpatterns] OF str11;
    s11 : str11;
    i,j : integer;
    tmpr : real;

  BEGIN
    c[ 1] := 'XXXO...OO';
    c[ 2] := 'XXX.O.O.O';
    c[ 3] := 'X.XXO...O';
    c[ 4] := '.XXOX...O';
    c[ 5] := 'X.XOO..O.';
    c[ 6] := 'XX.O.O..O';
    c[ 7] := '.XX.XOO..';
    c[ 8] := '.XXXOOO..';
    c[ 9] := 'X.XX.O.O.';
    c[10] := '.XX.O...O';
    c[11] := '.XX.O.O..';
    c[12] := 'X.XO....O';
    c[13] := '..XXXO...';
    c[14] := 'X..OOO...';
    c[15] := '.X.XOO...';
    c[16] := 'X..XXO...';
    c[17] := '..XXO....';
    c[18] := '.X.OX....';
    c[19] := 'X..XO....';

    FOR i := 1 TO maxpatterns DO
      FOR j := 1 TO 9 DO
        BEGIN
          s9 := c[i];
          CASE s9[j] OF
            'X': pattern[i,j] := computer;
            '.': pattern[i,j] := empty;
            'O': pattern[i,j] := human;
          END
        END;

    { Possible moves for each pattern }
    n11[ 1] := '24,25,36,00';
    n11[ 2] := '14,15,36,00';
    n11[ 3] := '15,35,36,47';
    n11[ 4] := '36,58,59,00';
    n11[ 5] := '15,35,36,00';
    n11[ 6] := '24,25,26,00';
    n11[ 7] := '26,57,58,00';
    n11[ 8] := '26,35,00,00';
    n11[ 9] := '47,48,00,00';
    n11[10] := '35,36,00,00';
    n11[11] := '35,36,00,00';
    n11[12] := '36,00,00,00';
    n11[13] := '47,58,00,00';
    n11[14] := '15,00,00,00';
    n11[15] := '26,47,00,00';
    n11[16] := '47,58,00,00';
    n11[17] := '35,36,47,00';
    n11[18] := '28,58,00,00';
    n11[19] := '15,47,00,00';

    FOR i := 1 TO maxpatterns DO
      FOR j := 1 TO 4 DO
        BEGIN
          s11 := n11[i];
          n2[0] := s11[j * 3 - 2];
          n2[1] := s11[j * 3 - 1];
          tmpr := cnvtreal(n2);
          moves[i,j] := round(tmpr)
        END
  END;

  PROCEDURE printboard;
  VAR
    i,j : integer;
  BEGIN
    FOR i := 1 TO 3 DO
      BEGIN
        write(' ':10);
        FOR j := 1 TO 3 DO
          write(p[s[(i-1)*3+j]]);
        writeln
      END;
    writeln
  END;

  PROCEDURE instructions;
  BEGIN
    writeln;
    writeln('THIS PROGRAM PLAYS THE GAME OF HEXAPAWN.');
    writeln('HEXAPAWN IS PLAYED WITH CHESS PAWNS ON A 3 BY 3 BOARD.');
    writeln('THE PAWNS ARE MOVED AS IN CHESS - ONE SPACE FORWARD TO');
    writeln('AN EMPTY SPACE OR ONE SPACE FORWARD AND DIAGONALLY TO');
    writeln('CAPTURE AN OPPOSING MAN.  ON THE BOARD, YOUR PAWNS');
    writeln('ARE ''O'', THE COMPUTER''S PAWNS ARE ''X'', AND EMPTY ');
    writeln('SQUARES ARE ''.''.  TO ENTER A MOVE, TYPE THE NUMBER OF');
    writeln('THE SQUARE YOU ARE MOVING FROM, FOLLOWED BY THE NUMBER');
    writeln('OF THE SQUARE YOU WILL MOVE TO.  THE NUMBERS MUST BE');
    writeln('SEPERATED BY A COMMA.');
    writeln;
    writeln('THE COMPUTER STARTS A SERIES OF GAMES KNOWING ONLY WHEN');
    writeln('THE GAME IS WON (A DRAW IS IMPOSSIBLE) AND HOW TO MOVE.');
    writeln('IT HAS NO STRATEGY AT FIRST AND JUST MOVES RANDOMLY.');
    writeln('HOWEVER, IT LEARNS FROM EACH GAME.  THUS, WINNING BECOMES');
    writeln('MORE AND MORE DIFFICULT.  ALSO, TO HELP OFFSET YOUR');
    writeln('INITIAL ADVANTAGE, YOU WILL NOT BE TOLD HOW TO WIN THE');
    writeln('GAME BUT MUST LEARN THIS BY PLAYING.');
    writeln;
    writeln('THE NUMBERING OF THE BOARD IS AS FOLLOWS:');
    writeln(' ':10, '123');
    writeln(' ':10, '456');
    writeln(' ':10, '789');
    writeln;
    writeln('FOR EXAMPLE, TO MOVE YOUR RIGHTMOST PAWN FORWARD,');
    writeln('YOU WOULD TYPE 9,6 IN RESPONSE TO THE QUESTION');
    writeln('''YOUR MOVE ?''.  SINCE I''M A GOOD SPORT, YOU''LL ALWAYS');
    writeln('GO FIRST.');
    writeln
  END;

  { Get move from player and check boundaries }
  PROCEDURE getmove;
  VAR
    valid : boolean;
  BEGIN
    valid := false;
    REPEAT
      write('YOUR MOVE? ');
      readmove(m1,m2);
      if (m1 > 0) AND (m1 < 10) AND (m2 > 0) AND (m2 < 10) then
        valid := true
      else
        writeln('ILLEGAL CO-ORDINATES.');
    UNTIL valid = true;
  END;

  PROCEDURE usermove;
  VAR
    valid : boolean;
  BEGIN
    REPEAT
      valid := true;
      getmove;
      IF s[m1] <> human THEN valid := false;
      IF valid AND (s[m2] = human) THEN valid := false;
      IF valid AND (m2-m1 <> -3) AND (s[m2] <> computer) THEN valid := false;
      IF valid AND (m2 > m1) THEN valid := false;
      IF valid AND (m2-m1 = -3) AND (s[m2]<> empty) THEN valid := false;
      IF valid AND (m2-m1 < -4) THEN valid := false;
      IF valid AND (m1 = 7) AND (m2 = 3) THEN valid := false;

      IF NOT valid THEN
        writeln('ILLEGAL MOVE');
      UNTIL valid = true;
  END;

PROCEDURE initboard;
  BEGIN
    s[4] := empty;
    s[5] := empty;
    s[6] := empty;
    s[1] := computer;
    s[2] := computer;
    s[3] := computer;
    s[7] := human;
    s[8] := human;
    s[9] := human;
  END;

FUNCTION hasnopawns(player:integer):boolean;
  VAR
    i : integer;
  BEGIN
    hasnopawns := true;
    FOR i := 1 TO 9 DO
      IF s[i] = player THEN hasnopawns := false;
  END;

{ Play a game of hexapawn. Returns true if computer won,
  false if player won.
}
FUNCTION playgame:boolean;
  LABEL 120,330,350,460,510,540,600,790,800;

  VAR
    i,j,k : integer;

  BEGIN
    x := 0;
    y := 0;
    initboard;
    printboard;

120:
    usermove;
    s[m1] := empty;
    s[m2] := human;
    printboard;
    IF (s[1] = human) OR (s[2] = human) OR (s[3] = human) THEN
      BEGIN
        playgame := false;
        GOTO 800
      END;
    IF hasnopawns(computer) THEN
      BEGIN
        playgame := false;
        GOTO 800
      END;
    FOR i := 1 TO 9 DO
      BEGIN
        IF s[i] <> computer THEN GOTO 330; { continue }
        IF s[i+3] = empty THEN GOTO 350; { break }
        IF (fnr(i) = i) THEN
          IF((s[i+2] = human) OR (s[i+4] = human)) THEN GOTO 350  { break }
          ELSE GOTO 330;
        IF i > 3 THEN
          IF s[8] = human THEN GOTO 350; { break }
330:  END;
350:
    FOR i := 1 TO maxpatterns DO
       BEGIN
         FOR j := 1 TO 3 DO
           FOR k := 3 DOWNTO 1 DO
             t[(j-1)*3+k] := pattern[i,(j-1)*3+4-k];
         FOR j := 1 TO 9 DO
           IF s[j] <> pattern[i,j] THEN GOTO 460;
         reversed := false;
         GOTO 540;
460:     FOR j := 1 TO 9 DO
           IF s[j] <> t[j] THEN GOTO 510; { continue }
         reversed := true;
         GOTO 540;
510:   END;
    { No pattern with a possible move was found }
    writeln('I RESIGN');
    playgame := false;
    GOTO 800;

540:
    x := i;
    FOR i := 1 TO 4 DO
      IF moves[x,i] <> 0 THEN GOTO 600;
    write('I CAN''T MOVE, SO ');
    playgame := false;
    GOTO 800;

600:
    REPEAT
      y := rnd(seed) MOD 4 + 1;
    UNTIL moves[x,y] <> 0;
    IF reversed THEN
       BEGIN
         writeln('I MOVE FROM ', fnr(moves[x,y] DIV 10):1, ' TO ',
             fnr(fnm(moves[x,y])):1);
         s[fnr(moves[x,y] DIV 10)] := empty;
         s[fnr(fnm(moves[x,y]))] := computer
       END
     ELSE
       BEGIN
         writeln('I MOVE FROM ', (moves[x,y] DIV 10):1, ' TO ',
             fnm(moves[x,y]):1);
         s[moves[x,y] DIV 10] := empty;
         s[fnm(moves[x,y])] := computer
       END;
  printboard;
  IF (s[7] = computer) OR (s[8] = computer) OR (s[9] = computer) THEN
    BEGIN
      playgame := true;
      GOTO 800
    END;
  IF hasnopawns(human) THEN
    BEGIN
      playgame := true;
      GOTO 800
    END;

  FOR i := 1 TO 9 DO
    BEGIN
      IF s[i] <> human THEN GOTO 790; { continue }
      IF s[i-3] = empty THEN GOTO 120;
      IF (fnr(i) = i) THEN
        IF ((s[i-2] = computer) OR (s[i-4] = computer)) THEN GOTO 120
        ELSE GOTO 790;
      IF (i < 7) THEN
        IF (s[2] = computer) THEN GOTO 120
        ELSE GOTO 790;
      IF s[5] = computer THEN GOTO 120;
790: END;
    write('YOU CAN''T MOVE, SO ');
    playgame := true;  
800:
    END;

BEGIN { MAIN }
  randomize(seed);
  writeln(' ':32, 'HEXAPAWN');
  writeln(' ':15, 'CREATIVE COMPUTING  MORRISTOWN, NEW JERSEY');
  writeln;
  writeln;
  p := 'X.O';
  won := 0;
  lost := 0;
  init;
  REPEAT
    write('INSTRUCTIONS (Y-N)? ');
    readyesno(a);
  UNTIL (a = 'Y') OR (a = 'N');
  IF a = 'Y' THEN instructions;

  WHILE true DO
    BEGIN
      gameres := playgame;
      IF gameres THEN
        BEGIN
          writeln('I WIN.');
          won := won + 1;
        END
      ELSE
        BEGIN
          writeln('YOU WIN.');
          moves[x,y] := 0;
          lost := lost + 1;
        END;
      writeln('I HAVE WON ',won:1,' AND YOU ',lost:1,' OUT OF ',won+lost:1,' GAMES.');
      writeln;
    END
END.
