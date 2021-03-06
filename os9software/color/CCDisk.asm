 nam   CCDisk
* Original edition from OS-9 for the TRS-80 Color Computer

 use   defsfile

 ttl   Driver Module
 pag
*******************************************************************
*
*                Microware Systems Corporation
*
*******************************************************************

NMIVec equ $109

V.SEL equ    $FF40 Drive select reg addr.
V.CMDR equ   $FF48 Status/command register
V.TRKR equ $FF49 Track register
V.SECR equ   $FF4A Sector register
V.DATR equ   $FF4B Data register

***************************************************************
*
* Disk Driver Module Header
*
*
 mod DSKEND,DSKNAM,DRIVR+OBJCT,REENT+1,DSKENT,DSKSTA
 fcb DIR.+SHARE.+PREAD.+PWRIT.+UPDAT.+EXEC.+PEXEC.
DSKNAM fcs "CCDisk"

 fcb 2 Edition telltale byte
*******************
* Revision History
* edition 2: prehistoric

 pag
*********************************************************************
*
* Static Storage
*
*
 org Drvbeg
 rmb Drvmem*DriveCnt
CURTBL rmb 2 Ptr to current drive tbl
CURDRV rmb 1
V.DOSK rmb 1
V.BUF rmb 2 Local buffer addr
DSKSTA equ . Total static requirement



******************************************************************
*
* Branch Table
*
DSKENT lbra INIDSK Initialize i/o
 lbra READSK Read sector
 lbra WRTDSK Write sector
 lbra GETSTA Get status call
 lbra PUTSTA
 lbra TERMNT Terminate device use

*
* WD1793 Commands
*
F.REST equ $00
F.SEEK equ $10
F.STEP equ $28
F.STPI equ $40
F.READ equ $88 Read sector command
F.WRIT equ $A8 Write sector command
F.TYP1 equ $D0 Force type 1 status
F.WRTR equ $F0 Write track command
STEPRATE equ 3

DRIVE1   equ %00000001
DRIVE2   equ %00000010
DRIVE3   equ %00000100
MOTORON  equ %00001000
PRECOMP  equ %00010000
DDENS    equ %00100000 Double density enable
DRIVE4   equ %01000000
WAITENBL equ %10000000 Wait enable

S.DRQ    equ %00000010
 pag
****************************************************************
*
* Initialize The I/O Port
*
*  Input: (U)= Pointer To Global Storage
*
*  On Exit: (A) Modified
*           (X) Modified
*           (Y) Unchanged
*           (U) Unchanged
*
INIDSK clra
 sta >D.DskTmr
 ldx #V.SEL
 leax V.CMDR-V.SEL,x
 lda #F.TYP1
 sta 0,x Send to command register
 lbsr DELAY1
 lda 0,x
 lda #$FF
 ldb #DriveCnt
 leax DRVBEG,U Point to first drive table
INILUP sta 0,x
 sta V.TRAK,X Inz to high track count
 leax DRVMEM,X Point to next drive table
 decb DONE
 bne INILUP ...no; inz more.
 leax NMISVC,pcr
 stx NMIVec+1
 lda #$7E Store JMP command
 sta NMIVec
 ldd #256 "d" passes memory req size
 pshs U Save "u" we need it later
 OS9 F$SRqMem Request 1 pag of mem
 tfr U,X
 puls U
 bcs RETRN1 ..oh ..oh; no mem available
 stx V.BUF,U Save for future use
 clrb
RETRN1 rts

TERMNT
GETSTA clrb
 rts
*************************************************************
*
* Read Sector Command
*
* Input: B = Msb Of Logical Sector Number
*        X = Lsb'S Of Logical Sector Number
*        Y = Ptr To Path Descriptor
*        U = Ptr To Global Storage
*
* Output: 256 Bytes Of Data Returned In Buffer
*
* Error: Cc=Set, B=Error Code
*
READSK lda #$91 Error retry code
 cmpx #0 Is this sector zero?
 bne RDDSK3 Branch if not
 bsr RDDSK3
 bcs WRERR9
 ldx PD.BUF,Y Point to buffer
 pshs X,Y
 ldy CURTBL,u
 ldb #DD.SIZ-1
READ01 lda B,X
 sta B,Y
 decb
 bpl READ01
 clrb
 puls pc,y,x

WRERR9 rts

RDDSK1 bcc RDDSK3 Retry without restore
 pshs D,X
 lbsr RESTOR Drive to tr00
 puls D,X
RDDSK3 pshs D,X
 bsr READSC Read sector
 puls D,X
 bcc WRERR9 Return if no error
 lsra DONE?
 bne RDDSK1 ...no; retry.
*
* Fall Through To Try One Last Time
*
READSC lbsr SEEK Move head to track
 bcs WRERR9
 ldx PD.BUF,Y Point to buffer
 pshs y,cc
 orcc #IntMasks
 ldy #$FFFF
 ldb #WAITENBL
 stb >V.CMDR
 ldb #F.STEP
 orb >CURDRV,u
 stb >V.SEL
 ldb   #$A8
 orb >CURDRV,u
 lbsr DELAY1
 lda #S.DRQ
READS1 bita  >V.CMDR
 bne   READS2
 leay  -1,y
         bne   READS1
 lda   >CURDRV,u
 ora   #MOTORON
 sta >V.SEL
 puls y,cc
 lbra RDERR

* The CPU is halted until the data register contains the data
READS2 lda >V.DATR
 sta ,X+ Store it
 stb >V.SEL
 bra READS2


***************************************************************
*
* Write Sector Command
*
* Input:
*   B = Msb Of Logical Sector Number
*   X = Lsb'S Of Logical Sector Number
*   Y = Ptr To Path Descriptor
*   U = Ptr To Global Storage
*
*
* Error:
*   Carry Set
*   B = Error Code
*
WRTDSK lda #$91 Error retry code
WRTDS1 pshs D,X Save regs
 bsr WRITSC Write sector
 puls D,X Restore regs
 bcs WRTDS3 Write error; try again.
 tst PD.VFY,Y Verify desired?
 bne WRTDS2 ...no; all is well.
 lbsr Wrtvfy Go verify sector
 bcs WRTDS3 ...verify failed
WRTDS2 clrb
 rts

WRTDS3 lsra
 lbeq Wrerr Retries done; ...exit
 bcc WRTDS1 Retry without restore
 pshs D,X
 lbsr RESTOR Restore drive
 puls D,X
 bra WRTDS1 Retry after restore


WRITSC lbsr SEEK
 lbcs WRERR9
 ldx PD.BUF,Y Buffer addr
 ldb   #WAITENBL+DDENS
WRITS1 pshs  y,cc
 orcc #IntMasks
 ldy #$FFFF
 stb >V.CMDR
 ldb #DDENS+MOTORON
 orb >CURDRV,u
 stb >V.SEL
 ldb #F.WRIT
 orb >CURDRV,u
 lbsrDELAY1
 lda #S.DRQ
WRITS2 bita >V.CMDR
 bne WRITS3
 leay -1,y
 bne WRITS2
 lda >CURDRV,u
 ora #MOTORON
 sta >V.SEL
 puls y,cc
 lbra WRERR

WRITS3 lda ,x+
 sta   >V.DATR
 stb   >V.SEL
 bra WRITS3

NMISVC leas R$Size,s
 puls y,cc
 ldb >V.CMDR
 bitb #$04
 lbne RDERR
 lbra STCK

WRTVFY pshs D,X
 ldx PD.BUF,Y Save present buffer addr
 pshs X On stack
 ldx V.BUF,U Point to local buffer
 stx PD.BUF,Y
 ldx 4,S Restore (x)
 lbsr READSC
 puls X
 stx PD.BUF,Y Restore buffer pointer
 bcs WRTVF6 Error; ...try again
 lda #32 Test 32 places in buffer
 pshs a,y,u
 ldy V.BUF,u Point "y" to local buffer
 tfr x,u
WRTCHK ldx 0,u Get two bytes
 cmpx 0,y Check with 'read data'
 bne WRTVF2 Error; ...return carry set
 leau 8,u
 leay 8,y Bump both pointers
 dec 0,s Done yet?
 bne WRTCHK No; ....keep checking
 bra WRTVF4
WRTVF2 orcc #%00000001 Set carry
WRTVF4 puls a,y,u
WRTVF6 puls d,x,pc
 pag
***************************************************************
*
* Seek A Track
*
* Input:
*   B = Msb Of Logical Sector Number
*   X = Lsb'S Of Logical Sector Number
*
* Output:
*   X = Physical Sector Number
*   A,B = Undefined
*
* Error:
*   Carry Set
*   B = Error Code
*
SEEK clr >V.DOSK,u
 bsr SELECT Select drive
 tstb CHECK Sector bounds
 bne PHYERR msb must be zero
 tfr X,D Logical sector (os-9)
 ldx CURTBL,u
 cmpd #0 Logical sector zero?
 beq PHYSC7 ..yes; skip conversion.
 cmpd DD.TOT+1,X Too high sector number?
 bcs PHYSC1 ..no
PHYERR comb
 ldb #E$SECT Error: bad sector number
 rts

PHYSC1 clr ,-s
 bra PHYSC5
PHYSC4 inc 0,S
PHYSC5 subd #18 Subtract one track worth of sectors
 bcc PHYSC4 Repeat until less than 1 track size
 addb #18 Add back for sector number
 puls A Desired track.
 cmpa #21 Precompensation needed?
 bls PHYSC7 ..no
 pshs a
 lda >CURDRV,u
 ora #PRECOMP Write Precompensation enable
 sta >CURDRV,u
 puls a
PHYSC7 incb
 stb V.SECR Put sector (b) in sector reg
SETRK3 ldb <V.Trak,x
 stb >V.TRKR
 tst V.DOSK,U Force seek?
 bne SETRK4 ..yes; do it.
 cmpa V.TRAK,X Same track?
 beq SETRK9 ..yes; skip seek.
SETRK4 sta V.Trak,x
 sta >V.DATR Put new trk in data reg
 ldb #F.SEEK+STEPRATE
 bsr WCR0 Issue command
 pshs x
 ldx #$222E delay loop
SETRK8 leax -1,x
 bne SETRK8
 puls x
SETRK9 clrb
 rts

DRVBITS fcb DRIVE1,DRIVE2,DRIVE3,DRIVE4

SELECT lbsr STARTMOT
 lda PD.DRV,Y Get drive number
 cmpa #DriveCnt Drive num ok?
 bcs SELCT3 ..yes
 comb
 ldb #E$UNIT Error: illegal unit (drive)
 rts

SELCT3 pshs X,D
 leax >DRVBITS,pcr
 ldb   a,x
 stb CURDRV,u
 leax DRVBEG,U Table beginning
 ldb #DRVMEM
 mul OFFSET For this drive
 leax D,X
 cmpx CURTBL,U New device call?
 beq SELCT5 ...no; don't force seek
 stx CURTBL,U Current table ptr
 com V.DOSK,U Set force seek flag
SELCT5 puls pc,x,b,a

STCK bitb #%11111000 Any error codes set?
 beq NOERR
 bitb #%10000000 Drive ready?
 bne ERNRDY ..no; error
 bitb #%01000000 Write protected?
 bne WPERR ..yes; return error
 bitb #%00100000 Head loaded?
 bne WRERR ..yes; return error.
 bitb #%00010000 Seek error?
 bne ERSEEK ..yes; return error
 bitb #%00001000 Check sum ok?
 bne ERRCRC ..no; return error
NOERR clrb
 rts


ERNRDY comb
 ldb #E$NotRdy Error: drive not ready
 rts

WPERR comb
 ldb #E$WP
 rts
WRERR comb
 ldb #E$Write
 rts
ERSEEK comb
 ldb #E$SEEK Error: seek error
 rts

ERRCRC comb
 ldb #E$CRC Error: bad check sum
 rts

RDERR comb
 ldb #E$Read
 rts

WCR0 bsr DELAY
WCR02 ldb >V.CMDR
 bitb #%00000001 Busy?
 beq DELAY4 ..yes; wait for it.
 lda #$F0
 sta >D.DskTmr
 bra WCR02
WCR lda #MOTORON
 ora >CURDRV,u
 sta >V.SEL
 stb >V.CMDR
 rts
DELAY bsr WCR
DELAY1 lbsr DELAY2
DELAY2 lbsr DELAY4
DELAY4 rts

 pag
************************************************************
*
* Put Status Call
*
*
*
PUTSTA ldx PD.RGS,Y Point to parameters
 ldb R$B,X Get stat call
 cmpb #SS.Reset Restore call?
 beq RESTOR ..yes; do it.
 cmpb #SS.WTrk Write track call?
 beq WRTTRK ..yes; do it.
 comb ...NO; Error
 ldb #E$UnkSvc Error code
RETRN2 rts


*****************************************************************
*
* Write Full Track
*  Input: (A)=Track
*         (Y)=Path Descriptor
*         (U)=Global Storage
*
WRTTRK    lbsr  SELECT
 lda R$U+1,X Track number
 cmpa  #21 Precompensation needed?
 bls WRTRK2 ..no
 ldb CURDRV,u
 orb #PRECOMP Write Precompensation enable
 stb CURDRV,u
WRTRK2 ldx Curtbl,u Point to drive table
 lbsr SETRK3
 bcs RETRN2
 ldx PD.RGS,Y
 ldx R$X,X Get buffer addr
 ldb #F.WRTR
 lbra WRITS1
 pag
*********************************************************
*
* Restore Drive To Track Zero
*
*  Input: (Y)= Pointer To Path Descriptor
*         (U)= Pointer To Global Storage
*
*  If Error: (B)= Error Code & Carry Is Set
*
* Note:  We Are Stepping In Several Tracks Before
*        Issuing The Restore.  As Suggested In The
*        Application Notes.
*
RESTOR lbsr SELECT Select drive
 ldx CURTBL,U
 clr V.TRAK,X Old track = 0
 lda #5 Repeat five times
RESTR2 ldb #F.STPI+STEPRATE
 pshs A
 lbsr WCR0 Issue command, delay & wait for done.
 puls A
 deca DONE Stepping?
 bne RESTR2 ...no; step again.
 ldb #F.REST+STEPRATE
 bra WCR0

STARTMOT pshs x,d
 lda >D.DskTmr
 bne START10
 lda #MOTORON
 sta >V.SEL
 ldx #$A000
WAITLOOP nop
 nop
 leax -1,x
 bne WAITLOOP
START10 lda #$F0
 sta >D.DskTmr
 puls pc,X,D

 emod

DSKEND   equ   *
