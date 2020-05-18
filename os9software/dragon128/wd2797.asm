         nam   wd2797
         ttl   os9 device driver


 use defsfile

tylg     set   Drivr+Objct
atrv     set   ReEnt+rev
rev      set   $01
         mod   DSKEND,DSKNAM,tylg,atrv,DSKENT,DSKSTA
u0000    rmb   1
u0001    rmb   1
u0002    rmb   1
u0003    rmb   1
u0004    rmb   1
u0005    rmb   1
u0006    rmb   2
u0008    rmb   1
u0009    rmb   1
u000A    rmb   2
u000C    rmb   1
u000D    rmb   1
u000E    rmb   1
u000F    rmb   1
u0010    rmb   1
u0011    rmb   1
u0012    rmb   2
u0014    rmb   2
u0016    rmb   1
u0017    rmb   1
u0018    rmb   1
u0019    rmb   1
u001A    rmb   2
u001C    rmb   1
u001D    rmb   24
u0035    rmb   2
u0037    rmb   2
u0039    rmb   23
u0050    rmb   48
u0080    rmb   38
u00A6    rmb   1
u00A7    rmb   2
u00A9    rmb   1
u00AA    rmb   1
u00AB    rmb   1
u00AC    rmb   1
u00AD    rmb   1
u00AE    rmb   1
u00AF    rmb   2
u00B1    rmb   2
u00B3    rmb   2
u00B5    rmb   2
u00B7    rmb   1
u00B8    rmb   1
u00B9    rmb   3
u00BC    rmb   1
u00BD    rmb   4
u00C1    rmb   1
u00C2    rmb   1
u00C3    rmb   2
u00C5    rmb   1
u00C6    rmb   1
u00C7    rmb   1
DSKSTA     equ   .

 fcb DIR.+SHARE.+PREAD.+PWRIT.+UPDAT.+EXEC.+PEXEC.

DSKNAM fcs /wd2797/
 fcb  1 Edition

DSKENT    equ   *
         lbra  INIDSK
         lbra  READSK
         lbra  WRTDSK
         lbra  L01B6
         lbra  L01B8
         lbra  L0283
         lbra  L02A8

L002A   fcb  $00,$80,$80

INIDSK    fcb $86
         lsr   <u00A7
         rora
         leax  u000F,u
         stx   >u00A7,u
         pshs  u
         leau  >u00AB,u
         lbsr  L0334
 ldd #256 "d" passes memory req size
         os9   F$SRqMem
         bcs   L0090
         tfr   u,d
         puls  u
         std   >u00A9,u
         ldd   #$FCC1
         leax  <L002A,pcr
         leay  >L05DE,pcr
         os9   F$IRQ
         bcs   L008F
         inc   >u00AC,u
         ldx   #$FCC0
         lda   ,x
         pshs  cc
         orcc  #$50
         lda   $01,x
         ora   #$03
         sta   $01,x
         puls  cc
         leax  >L05F7,pcr
         os9   F$Timer
         bcs   L008F
         leax  u000F,u
         ldb   #$04
         lda   #$FF
L0083    sta   $01,x
         sta   <$15,x
         leax  <$26,x
         decb
         bne   L0083
         clrb
L008F    rts
L0090    puls  pc,u

READSK    bsr   L00F1
         bcs   L008F
         ldx   >u00B5,u
         bne   L00BE
         bsr   L00BE
         bcs   L008F
         ldx   $08,y
         pshs  y,x
         tst   >u00C6,u
         bne   L00B8
         ldy   >u00A7,u
         ldb   #$14
READ01    lda   b,x
         sta   b,y
         decb
         bpl   READ01
L00B8    clr   >u00C6,u
         puls  pc,y,x

L00BE    leax  >L0364,pcr
L00C2    pshs  u,y
         ldy   u0001,u
         leau  >u00AB,u
         jsr   ,x
         puls  pc,u,y

WRTDSK    bsr   L00F1
         bcs   L0151
         leax  >L0408,pcr
         bsr   L00C2
         bcs   L0151
         lda   <$28,y
         bne   L0151
         ldd   $08,y
         pshs  b,a
         ldd   >u00A9,u
         std   $08,y
         bsr   READSK
         puls  x
         stx   $08,y
         rts

L00F1    tstb
         bne   L014E
         stx   >u00B5,u
         bsr   L0152
         bitb  #$40
         beq   L012F
         anda  #$F8
         pshs  a
         lda   <$27,y
         deca
         puls  a
         beq   L010C
         ora   #$01
L010C    ldb   <$24,y
         bitb  #$01
         beq   L0115
         ora   #$02
L0115    sta   >u00B7,u
         ldd   <$29,y
         std   >u00B1,u
         lda   <$27,y
         deca
         beq   L0127
         lslb
L0127    lda   <$26,y
         mul
         std   $01,x
         bra   L0136
L012F    clra
         ldb   $03,x
         std   >u00B1,u
L0136    ldd   >u00B5,u
         cmpd  $01,x
         bhi   L014E
         ldd   $08,y
         std   >u00BD,u
         ldd   <$2B,y
         std   >u00AF,u
         clrb
         rts
L014E    comb
         ldb   #$F1
L0151    rts

L0152    ldx   >u00A7,u
         beq   L015F
         lda   >u00BC,u
         sta   <$15,x
L015F    lda   <$21,y
         sta   >u00B3,u
         ldb   #$26
         mul
         leax  u000F,u
         leax  d,x
         stx   >u00A7,u
         lda   <$15,x
         sta   >u00BC,u
         lda   <$22,y
         anda  #$03
         sta   >u00C1,u
         ldb   <$24,y
         andb  #$02
         stb   >u00B8,u
         lda   <$10,x
         ldb   <$23,y
         andb  #$20
         stb   >u00C7,u
         ldb   <$23,y
         bitb  #$40
         bne   L01A5
         bita  #$04
         beq   L01A5
         clr   >u00B8,u
L01A5    bitb  #$01
         beq   L01AB
         ora   #$80
L01AB    bitb  #$08
         beq   L01B1
         ora   #$08
L01B1    sta   >u00B7,u
         rts
L01B6    bra   L01D0
L01B8    pshs  u,y
         ldx   $06,y
         ldb   $02,x
         cmpb  #$03
         beq   L01EB
         cmpb  #$04
         beq   L01FE
         cmpb  #$0A
         beq   L01D4
         cmpb  #$0B
         beq   L01DD
         puls  u,y
L01D0    comb
         ldb   #$D0
         rts
L01D4    ldb   #$01
         stb   >u00C6,u
         clrb
         puls  pc,u,y

L01DD    ldx   $04,x
         pshs  x
         lbsr  L0152
         puls  b,a
         std   $03,x
         clrb
         puls  pc,u,y
L01EB    lbsr  L0152
         ldx   >u00A7,u
         clr   <$15,x
         leax  >L04E8,pcr
         lbsr  L00C2
         puls  pc,u,y
L01FE    lda   $07,x
         ldb   $09,x
         pshs  b,a
         lbsr  L0152
         ldd   <$29,y
         std   >u00B1,u
         ldd   <$2B,y
         std   >u00AF,u
         ldd   #$0000
         std   >u00B5,u
         puls  b,a
         sta   <$10,x
         stb   >u00C2,u
         anda  #$01
         sta   >u00C5,u
         lbsr  L0152
         ldd   #$2900
         os9   F$SRqMem
         bcs   L0281
         ldx   <u0050
         lda   $06,x
         ldb   D.SysTsk
         ldy   ,s
         ldx   $06,y
         ldx   $04,x
         ldy   #$2900
         os9   F$Move
         leax  ,u
         ldu   $02,s
         stx   >u00BD,u
         leau  >u00AB,u
         ldy   >-u00AA,u
         lbsr  L0413
         bcs   L0281
         lbsr  L0492
         ldd   #$2900
         std   <u0014,u
         lda   #$F0
         sta   u000F,u
         lda   #$01
         sta   ,u
         lbsr  L0502
         pshs  b,cc
         ldu   <u0012,u
         ldd   #$2900
         os9   F$SRtMem
         puls  b,cc
L0281    puls  pc,u,y

L0283    pshs  u
         ldu   >u00A9,u
         ldd   #$0100
         os9   F$SRtMem
         puls  u
         ldx   #$FCC0
         lda   $01,x
         anda  #$FE
         sta   $01,x
         ldx   #$0000
         os9   F$IRQ
         ldx   #$0000
         os9   F$Timer
         clrb
         rts

L02A8    pshs  u,y,x,b,a
         leas  <-$1D,s
         ldd   #$0100
         os9   F$SRqMem
         bcs   L032F
         stu   <$12,s
         leau  ,s
         clr   u0001,u
L02BC    clra
         clrb
         std   u000A,u
         ldy   <$21,s
         bsr   L0334
         lda   >$FCC1
         ora   #$01
         sta   >$FCC1
         lbsr  L04E8
         bcs   L02BC
         lbsr  L0364
         bcs   L02BC
         ldx   <u0012,u
         lda   <$10,x
         ora   u000C,u
         sta   u000C,u
         bita  #$04
         beq   L02E8
         clr   u000D,u
L02E8    clra
         ldb   $03,x
         std   u0006,u
         ldd   <$18,x
         std   <u001D,u
         ldd   #$0100
         leau  ,x
         ldx   <$16,x
         os9   F$SRtMem
         ldd   <$1D,s
         beq   L0328
         os9   F$BtMem
         bcs   L032F
         stu   <$1F,s
         stu   <$12,s
         leau  ,s
L0310    pshs  x,a
         stx   u000A,u
         bsr   L0364
         bcs   L032D
         puls  x,a
         leax  $01,x
         inc   <u0012,u
         deca
         bne   L0310
         leas  <$1D,s
         clrb
         puls  pc,u,y,x,b,a

L0328    comb
         ldb   #$F9
         bra   L032F

L032D    leas  $03,s
L032F    leas  <$1F,s
         puls  pc,u,y,x
L0334    leay  <$12,y
         lda   $01,y
         sta   u0008,u
         lda   $02,y
         anda  #$03
         sta   <u0016,u
         lda   $03,y
         clrb
         bita  #$01
         beq   L034B
         ldb   #$80
L034B    lda   $04,y
         anda  #$02
         sta   u000D,u
         lda   $03,y
         anda  #$20
         sta   <u001C,u
         stb   u000C,u
         ldd   $0B,y
         std   u0004,u
         ldy   -$03,y
         lda   ,y
         rts
L0364    clr   ,u
         lda   #$88
         sta   u000F,u
L036A    lda   #$DB
         pshs  a
         lda   u000C,u
         bita  #$08
         beq   L0398
         ldd   #$0080
         std   <u0014,u
         bsr   L039E
         bcs   L03F9
         ldd   <u0012,u
         leas  $01,s
         pshs  b,a
         addd  #$0080
         std   <u0012,u
         lda   #$DB
         pshs  a
         bsr   L039E
         puls  x
         stx   <u0012,u
         bra   L03F9
L0398    ldd   #$0100
         std   <u0014,u
L039E    ldd   u000A,u
         bne   L03A5
L03A2    lbsr  L04E8
L03A5    bsr   L0413
         bcs   L03F9
         clr   <u0017,u
         clr   <u001A,u
         ldd   u000A,u
         cmpd  u0004,u
         bcs   L03CD
         subd  u0004,u
L03B8    inc   <u0017,u
         subd  u0006,u
         bcc   L03B8
         addd  u0006,u
         lda   u000C,u
         bita  #$01
         beq   L03CD
         lsr   <u0017,u
         rol   <u001A,u
L03CD    lda   u000C,u
         bita  #$08
         beq   L03D7
         lslb
         decb
         bra   L03DD
L03D7    tst   <u001C,u
         beq   L03DD
         incb
L03DD    stb   <u0010,u
L03E0    lbsr  L0492
         lda   <u0010,u
         bsr   L03FC
         lbsr  L0502
         bcc   L03F9
         cmpb  #E$NotRdy Error: drive not ready
         orcc  #$01
         beq   L03F9
         lsr   ,s
         bcc   L03A2
         bne   L03E0
L03F9    leas  $01,s
         rts

L03FC    sta   $02,y
         ldb   #$0C
L0400    decb
         bne   L0400
         cmpa  $02,y
         bne   L03FC
         rts
L0408    lda   #$01
         sta   ,u
         lda   #$A8
         sta   u000F,u
         lbra  L036A
L0413    lda   u0008,u
         cmpa  #$04
         bcs   L041D
         comb
         ldb   #E$Unit
         rts

L041D    coma
         ldb   #$01
         stb   u0002,u
         anda  #$6F
         ldb   u0008,u
         cmpb  u0009,u
         beq   L0456
         stb   u0009,u
         ldb   <u0018,u
         orb   #$04
         stb   >$FC24
         ldx   #$0014
L0437    leax  -$01,x
         bne   L0437
         sta   >$FC24
         tst   u0001,u
         bne   L0450
         ldb   #$03
L0444    ldx   #$61A8
L0447    leax  -$01,x
         bne   L0447
         decb
         bne   L0444
         bra   L0456
L0450    ldx   #$000F
         os9   F$Sleep
L0456    anda  #$FB
         ldb   <u0018,u
         sta   <u0018,u
         sta   >$FC24
         lda   #$FA
         sta   u000E,u
         bitb  #$10
         beq   L0485
         ldb   u000C,u
         bmi   L0485
         tst   u0001,u
         bne   L047F
         ldb   #$0C
L0473    ldx   #$61A8
L0476    leax  -$01,x
         bne   L0476
         decb
         bne   L0473
         bra   L0485
L047F    ldx   #$003C
         os9   F$Sleep
L0485    lda   ,y
         clrb
         bita  #$80
         beq   L0491
         clr   u0002,u
         comb
         ldb   #$F6
L0491    rts
L0492    lda   <u0011,u
         clr   <u0019,u
         tst   u000D,u
         beq   L049D
         lsla
L049D    sta   $01,y
         lda   <u0017,u
         ldb   u000C,u
         bmi   L04AA
         bitb  #$04
         beq   L04AE
L04AA    cmpa  #$28
         bra   L04B0
L04AE    cmpa  #$10
L04B0    bcs   L04BD
         ldb   <u0018,u
         andb  #$DF
         stb   <u0018,u
         stb   >$FC24
L04BD    cmpa  <u0011,u
         beq   L04E2
         sta   <u0011,u
         tst   u000D,u
         beq   L04CA
         lsla
L04CA    ldb   #$04
         stb   <u0019,u
         sta   $03,y
         lda   #$18
L04D3    ora   <u0016,u
         lbsr  L056E
         bsr   L04E2
         ldb   u0003,u
         andb  #$90
         lbra  L05BD
L04E2    lda   <u0011,u
         sta   $01,y
         rts
L04E8    lbsr  L0413
         clr   <u0011,u
         ldb   #$05
L04F0    lda   #$48
         ora   <u0016,u
         pshs  b
         bsr   L056E
         puls  b
         decb
         bne   L04F0
         lda   #$08
         bra   L04D3
L0502    clrb
         pshs  cc
         ldb   u000F,u
         tst   <u001A,u
         beq   L0510
         orb   #$02
         stb   u000F,u
L0510    orcc  #$50
         ldx   <u0035
         beq   L051E
         ldx   #$0001
         os9   F$Sleep
         bra   L0510
L051E    leax  $03,y
         stx   <u0035
         ldx   <u0012,u
         stx   <u0037
         lda   ,u
         sta   <u0039
         ldx   #$FCC0
         lda   #$00
         sta   ,x
         ora   #$80
         sta   ,x
         tst   <u001C,u
         bne   L054B
         ldb   u000C,u
         tst   <u0017,u
         bne   L0547
         tst   <u001A,u
         beq   L0556
L0547    bitb  #$02
         beq   L0556
L054B    lda   <u0018,u
         anda  #$BF
         sta   >$FC24
         sta   <u0018,u
L0556    lda   u000F,u
         ora   <u0019,u
         bsr   L056E
         bsr   L05BB
         ldx   #$0000
         stx   <u0035
         bcc   L056C
         lda   ,s
         ora   #$01
         sta   ,s
L056C    puls  pc,cc
L056E    pshs  cc
         orcc  #$50
         ldb   #$FA
         stb   u000E,u
         stb   u0002,u
         sta   ,y
         tst   u0001,u
         bne   L058C
L057E    sync
         lda   >$FCC1
         bita  #$80
         beq   L057E
         lda   ,y
         sta   u0003,u
         bra   L05B4
L058C    ldx   #$00C8
         lda   >-u00A7,u
         sta   >-u00A6,u
L0597    os9   F$Sleep
         orcc  #$50
         tst   >-u00A6,u
         beq   L05B7
         leax  ,x
         bne   L0597
         clr   >-u00A6,u
         lda   #$80
         sta   u0003,u
         lda   #$D0
         sta   ,y
         bsr   L05D7
L05B4    lda   >$FCC0
L05B7    clr   u0002,u
         puls  pc,cc
L05BB    ldb   u0003,u
L05BD    clra
         andb  #$FC
         beq   L05D7
L05C2    lslb
         inca
         bcc   L05C2
         deca
         leax  <L05D8,pcr
         ldb   a,x
         cmpb  #$F4
         bne   L05D6
         tst   ,u
         beq   L05D6
         ldb   #$F5
L05D6    coma
L05D7    rts

L05D8    ldb   >$F2F4
         stb   >$F4F4
L05DE    ldy   u0001,u
         ldb   ,y
         stb   >u00AE,u
         lda   >$FCC0
         lda   u0005,u
         beq   L05F5
         clr   u0005,u
         ldb   #$01
         os9   F$Send
L05F5    clrb
         rts

L05F7    tst   >u00B9,u
         beq   L0612
         tst   >u00AD,u
         bne   L0612
         dec   >u00B9,u
         bne   L0612
         lda   #$7F
         sta   >$FC24
         sta   >u00C3,u
L0612    rts

         emod
DSKEND equ *