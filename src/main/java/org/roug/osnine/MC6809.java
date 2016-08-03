package org.roug.osnine;

public class MC6809 extends USimMotorola {

    private int debug;

    public static final int immediate = 0;
    public static final int relative = 0;
    public static final int inherent = 1;
    public static final int extended = 2;
    public static final int direct = 3;
    public static final int indexed = 4;

    protected int mode;

/*
    public enum Modes {
                immediate(0),
                relative(0),
                inherent(1),
                extended(2),
                direct(3),
                indexed(4)
    }
    protected Modes mode;
*/


    /** Stack pointer U. */
    public final Word u = new Word("U");
    /** Stack pointer S. */
    public final Word s = new Word("S");
    /** Index register X. */
    public final Word x = new Word("X");
    /** Index register Y. */
    public final Word y = new Word("Y");

    /** Direct Page register. */
    public final UByte dp = new UByte("DP");
    public final UByte a = new UByte("A");
    public final UByte b = new UByte("B");
    public final RegisterD d = new RegisterD(a, b);

    /** Condiction codes. */
    public final RegisterCC cc = new RegisterCC();

    /**
     * Constructor: Allocate 65.536 bytes of memory and reset the CPU.
     */
    public MC6809() //: a(acc.byte.a), b(acc.byte.b), d(acc.d)
    {
        allocate_memory(0x10000);
        reset();
    }

    /**
     * Reset the simulator. Program counter is set to the content for the top
     * two bytes in memory. Direct page register is set to 0.
     */
    public void reset() {
        pc = read_word(0xfffe);
        dp.set(0x00);      /* Direct page register = 0x00 */
        cc.clear();      /* Clear all flags */
        cc.setI(1);       /* IRQ disabled */
        cc.setF(1);       /* FIRQ disabled */
    }

    public void status() {
    }

    /**
     * Execute one instruction.
     */
    public void execute() {
    //                   disasmPC();
        ir = fetch();

        /* Select addressing mode */
        switch (ir & 0xf0) {
            case 0x00: case 0x90: case 0xd0:
                mode = direct; break;
            case 0x20:
                mode = relative; break;
            case 0x30: case 0x40: case 0x50:
                if (ir < 0x34) {
                    mode = indexed;
                } else if (ir < 0x38) {
                    mode = immediate;
                } else {
                    mode = inherent;
                }
                break;
            case 0x60: case 0xa0: case 0xe0:
                mode = indexed; break;
            case 0x70: case 0xb0: case 0xf0:
                mode = extended; break;
            case 0x80: case 0xc0:
                if (ir == 0x8d) {
                    mode = relative;
                } else {
                    mode = immediate;
                }
                break;
            case 0x10:
                switch (ir & 0x0f) {
                    case 0x02: case 0x03: case 0x09:
                    case 0x0d: case 0x0e: case 0x0f:
                        mode = inherent; break;
                    case 0x06: case 0x07:
                        mode = relative; break;
                    case 0x0a: case 0x0c:
                        mode = immediate; break;
                    case 0x00: case 0x01:
                        ir <<= 8;
                        ir |= fetch();
                        switch (ir & 0xf0) {
                            case 0x20:
                                mode = relative; break;
                            case 0x30:
                                mode = inherent; break;
                            case 0x80: case 0xc0:
                                mode = immediate; break;
                            case 0x90: case 0xd0:
                                mode = direct; break;
                            case 0xa0: case 0xe0:
                                mode = indexed; break;
                            case 0xb0: case 0xf0:
                                mode = extended; break;
                        }
                        break;
                }
                break;
        }

        // Select instruction
        switch (ir) {
            case 0x3a:
                abx(); break;
            case 0x89: case 0x99: case 0xa9: case 0xb9:
                adca(); break;
            case 0xc9: case 0xd9: case 0xe9: case 0xf9:
                adcb(); break;
            case 0x8b: case 0x9b: case 0xab: case 0xbb:
                adda(); break;
            case 0xcb: case 0xdb: case 0xeb: case 0xfb:
                addb(); break;
            case 0xc3: case 0xd3: case 0xe3: case 0xf3:
                addd(); break;
            case 0x84: case 0x94: case 0xa4: case 0xb4:
                anda(); break;
            case 0xc4: case 0xd4: case 0xe4: case 0xf4:
                andb(); break;
            case 0x1c:
                andcc(); break;
            case 0x47:
                asra(); break;
            case 0x57:
                asrb(); break;
            case 0x07: case 0x67: case 0x77:
                asr(); break;
/*
            case 0x24:
                bcc(); break;
            case 0x25:
                bcs(); break;
            case 0x27:
                beq(); break;
            case 0x2c:
                bge(); break;
            case 0x2e:
                bgt(); break;
            case 0x22:
                bhi(); break;
            case 0x85: case 0x95: case 0xa5: case 0xb5:
                bita(); break;
            case 0xc5: case 0xd5: case 0xe5: case 0xf5:
                bitb(); break;
            case 0x2f:
                ble(); break;
            case 0x23:
                bls(); break;
            case 0x2d:
                blt(); break;
            case 0x2b:
                bmi(); break;
            case 0x26:
                bne(); break;
            case 0x2a:
                bpl(); break;
            case 0x20:
                bra(); break;
            case 0x16:
                lbra(); break;
            case 0x21:
                brn(); break;
            case 0x8d:
                bsr(); break;
            case 0x17:
                lbsr(); break;
            case 0x28:
                bvc(); break;
            case 0x29:
                bvs(); break;
*/
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x4f:
            case 0x4f: case 0x4e:
                clra(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x5f:
            case 0x5f: case 0x5e:
                clrb(); break;
            case 0x0f: case 0x6f: case 0x7f:
                clr(); break;
/*
            case 0x81: case 0x91: case 0xa1: case 0xb1:
                cmpa(); break;
            case 0xc1: case 0xd1: case 0xe1: case 0xf1:
                cmpb(); break;
            case 0x1083: case 0x1093: case 0x10a3: case 0x10b3:
                cmpd(); break;
            case 0x118c: case 0x119c: case 0x11ac: case 0x11bc:
                cmps(); break;
            case 0x8c: case 0x9c: case 0xac: case 0xbc:
                cmpx(); break;
            case 0x1183: case 0x1193: case 0x11a3: case 0x11b3:
                cmpu(); break;
            case 0x108c: case 0x109c: case 0x10ac: case 0x10bc:
                cmpy(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x43:
            case 0x43: case 0x42: case 0x1042:
                coma(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x53:
            case 0x53: case 0x52:
                comb(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x03: case 0x63: case 0x73:
            case 0x03: case 0x62: case 0x63: case 0x73:
                com(); break;
            case 0x19:
                daa(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x4a:
            case 0x4a: case 0x4b:
                deca(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x5a:
            case 0x5a: case 0x5b:
                decb(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x0a: case 0x6a: case 0x7a:
            case 0x0a: case 0x0b: case 0x6a: case 0x6b:
            case 0x7a: case 0x7b:
                dec(); break;
            case 0x88: case 0x98: case 0xa8: case 0xb8:
                eora(); break;
            case 0xc8: case 0xd8: case 0xe8: case 0xf8:
                eorb(); break;
            case 0x1e:
                exg(); break;
            case 0x4c:
                inca(); break;
            case 0x5c:
                incb(); break;
            case 0x0c: case 0x6c: case 0x7c:
                inc(); break;
            case 0x0e: case 0x6e: case 0x7e:
                jmp(); break;
            case 0x9d: case 0xad: case 0xbd:
                jsr(); break;
            case 0x86: case 0x96: case 0xa6: case 0xb6:
                lda(); break;
            case 0xc6: case 0xd6: case 0xe6: case 0xf6:
                ldb(); break;
            case 0xcc: case 0xdc: case 0xec: case 0xfc:
                ldd(); break;
            case 0x10ce: case 0x10de: case 0x10ee: case 0x10fe:
                lds(); break;
            case 0xce: case 0xde: case 0xee: case 0xfe:
                ldu(); break;
            case 0x8e: case 0x9e: case 0xae: case 0xbe:
                ldx(); break;
            case 0x108e: case 0x109e: case 0x10ae: case 0x10be:
                ldy(); break;
            case 0x32:
                leas(); break;
            case 0x33:
                leau(); break;
            case 0x30:
                leax(); break;
            case 0x31:
                leay(); break;
            case 0x48:
                lsla(); break;
            case 0x58:
                lslb(); break;
            case 0x08: case 0x68: case 0x78:
                lsl(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x44:
            case 0x44: case 0x45:
                lsra(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x54:
            case 0x54: case 0x55:
                lsrb(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x04: case 0x64: case 0x74:
            case 0x04: case 0x05: case 0x64: case 0x65:
            case 0x74: case 0x75:
                lsr(); break;
            case 0x3d:
                mul(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x40:
            case 0x40: case 0x41:
                nega(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x50:
            case 0x50: case 0x51:
                negb(); break;
            // BDA - Adding in undocumented 6809 instructions
    //      case 0x00: case 0x60: case 0x70:
            case 0x00: case 0x01: case 0x60: case 0x61:
            case 0x70: case 0x71:
                neg(); break;
            // BDA - Adding in undocumented 6809 instructions
            // NEG/COM combination instruction for direct page
            case 0x02:
                if (cc.getC() == 1)
                    com();
                else
                    neg();
                break;
            case 0x12:
                nop(); break;
            case 0x8a: case 0x9a: case 0xaa: case 0xba:
                ora(); break;
            case 0xca: case 0xda: case 0xea: case 0xfa:
                orb(); break;
            case 0x1a:
                orcc(); break;
            case 0x34:
                pshs(); break;
            case 0x36:
                pshu(); break;
            case 0x35:
                puls(); break;
            case 0x37:
                pulu(); break;
            // BDA - Adding in undocumented 6809 instructions
            case 0x3e:
                reset(); break;
            case 0x49:
                rola(); break;
            case 0x59:
                rolb(); break;
            case 0x09: case 0x69: case 0x79:
                rol(); break;
            case 0x46:
                rora(); break;
            case 0x56:
                rorb(); break;
            case 0x06: case 0x66: case 0x76:
                ror(); break;
            case 0x3b:
                rti(); break;
            case 0x39:
                rts(); break;
            case 0x82: case 0x92: case 0xa2: case 0xb2:
                sbca(); break;
            case 0xc2: case 0xd2: case 0xe2: case 0xf2:
                sbcb(); break;
            case 0x1d:
                sex(); break;
            case 0x97: case 0xa7: case 0xb7:
                sta(); break;
            case 0xd7: case 0xe7: case 0xf7:
                stb(); break;
            case 0xdd: case 0xed: case 0xfd:
                std(); break;
            case 0x10df: case 0x10ef: case 0x10ff:
                sts(); break;
            case 0xdf: case 0xef: case 0xff:
                stu(); break;
            case 0x9f: case 0xaf: case 0xbf:
                stx(); break;
            case 0x109f: case 0x10af: case 0x10bf:
                sty(); break;
            case 0x80: case 0x90: case 0xa0: case 0xb0:
                suba(); break;
            case 0xc0: case 0xd0: case 0xe0: case 0xf0:
                subb(); break;
            case 0x83: case 0x93: case 0xa3: case 0xb3:
                subd(); break;
            case 0x3f:
                swi(); break;
            case 0x103f:
                swi2(); break;
            case 0x113f:
                swi3(); break;
            case 0x1f:
                tfr(); break;
            case 0x4d:
                tsta(); break;
            case 0x5d:
                tstb(); break;
            case 0x0d: case 0x6d: case 0x7d:
                tst(); break;
            case 0x1024:
                lbcc(); break;
            case 0x1025:
                lbcs(); break;
            case 0x1027:
                lbeq(); break;
            case 0x102c:
                lbge(); break;
            case 0x102e:
                lbgt(); break;
            case 0x1022:
                lbhi(); break;
            case 0x102f:
                lble(); break;
            case 0x1023:
                lbls(); break;
            case 0x102d:
                lblt(); break;
            case 0x102b:
                lbmi(); break;
            case 0x1026:
                lbne(); break;
            case 0x102a:
                lbpl(); break;
            case 0x1021:
                lbrn(); break;
            case 0x1028:
                lbvc(); break;
            case 0x1029:
                lbvs(); break;
*/
            default:
                // BDA - make invalid instructions a nop
                // to distinquish between 6309
    //          nop(); break;
                invalid("instruction"); break;
        }


    }

    public int getSignedByte(int value) {
        if (value < 0x80) {
            return value;
        } else {
            return -((~value & 0x7f) + 1);
        }
    }

    public int getSignedWord(int value) {
        if (value < 0x8000) {
            return value;
        } else {
            return -((~value & 0x7fff) + 1);
        }
    }

    /**
     * Find 16-bit register name in post-byte.
     * 00 = X
     * 01 = Y
     * 10 = U
     * 11 = S
     */
    private Word refreg(int post) {
        post &= 0x60;
        post >>= 5;

        if (post == 0) {
            return x;
        } else if (post == 1) {
            return y;
        } else if (post == 2) {
            return u;
        } else {
            return s;
        }
    }

    /**
     * Get value of referenced 8-bit register.
     * 0x08 = A
     * 0x09 = B
     * 0x0a = CC
     * otherwise DP
     */
    private int byterefreg(int r) {
        if (r == 0x08) {
            return a.intValue();
        } else if (r == 0x09) {
            return b.intValue();
        } else if (r == 0x0a) {
            return cc.intValue();
        } else {
            return dp.intValue();
        }
    }

    private int wordrefreg(int r) {
        if (r == 0x00) {
            return d.intValue();
        } else if (r == 0x01) {
            return x.intValue();
        } else if (r == 0x02) {
            return y.intValue();
        } else if (r == 0x03) {
            return u.intValue();
        } else if (r == 0x04) {
            return s.intValue();
        } else {
            return pc;
        }
    }

    private int fetch_operand() {
        int ret = 0;
        int addr;

        if (mode == immediate) {
            ret = fetch();
        } else if (mode == relative) {
            ret = fetch();
        } else if (mode == extended) {
            addr = fetch_word();
            ret = read(addr);
        } else if (mode == direct) {
            addr = (dp.intValue() << 8) | fetch();
            ret = read(addr);
        } else if (mode == indexed) {
            int post = fetch();
            do_predecrement(post);
            addr = do_effective_address(post);
            ret = read(addr);
            do_postincrement(post);
        } else {
            invalid("addressing mode");
        }

        return ret;
    }

    private int fetch_word_operand() {
        int ret = 0;
        int addr;

        if (mode == immediate) {
            ret = fetch_word();
        } else if (mode == relative) {
            ret = fetch_word();
        } else if (mode == extended) {
            addr = fetch_word();
            ret = read_word(addr);
        } else if (mode == direct) {
            addr = dp.intValue() << 8 | fetch();
            ret = read_word(addr);
        } else if (mode == indexed) {
            int post = fetch();
            do_predecrement(post);
            addr = do_effective_address(post);
            do_postincrement(post);
            ret = read_word(addr);
        } else {
            invalid("addressing mode");
        }

        return ret;
    }

    private int fetch_effective_address() {
        int addr = 0;

        if (mode == extended) {
            addr = fetch_word();
        } else if (mode == direct) {
            addr = dp.intValue() << 8 | fetch();
        } else if (mode == indexed) {
            int post = fetch();
            do_predecrement(post);
            addr = do_effective_address(post);
            do_postincrement(post);
        } else {
            invalid("addressing mode");
        }

        return addr;
    }

    /**
     * Calculate indirect addressing.
     */
    private int do_effective_address(int post) {
        int addr = 0;
        int sOffset;
        int uOffset;

        if ((post & 0x80) == 0x00) {
            addr = refreg(post).intValue() + extend5(post & 0x1f);   // Constant 5-bit offset from register
        } else {
            switch (post & 0x1f) {
                case 0x00:  // Increment by 1 (done elsewhere)
                case 0x02:  // Decrement by 1 (done elsewhere)
                    addr = refreg(post).intValue();
                    break;
                case 0x01:  // Increment by 2 (done elsewhere)
                case 0x03:  // Decrement by 2 (done elsewhere)
                case 0x11:  // Increment by 2 (done elsewhere)
                case 0x13:  // Decrement by 2 (done elsewhere)
                    addr = refreg(post).intValue();
                    break;
                case 0x04:  // Non-Indirect No offset
                case 0x14:  // Indirect No offset
                    addr = refreg(post).intValue();
                    break;
                case 0x05:  // Non-Indirect B-register offset
                case 0x15:  // Indirect B-register offset
                    addr = b.getSigned() + refreg(post).intValue();
                    break;
                case 0x06:  // Non-Indirect A-register offset
                case 0x16:  // Indirect A-register offset
                    addr = a.getSigned() + refreg(post).intValue();
                    break;
                case 0x08:  // Non-Indirect 8-bit offset
                case 0x18:  // Indirect 8-bit offset
                    addr = refreg(post).intValue() + extend8(fetch());
                    break;
                case 0x09:  // Non-Indirect 16-bit offset
                case 0x19:  // Indirect 16-bit offset
                    sOffset = getSignedWord(fetch_word());
                    addr = refreg(post).intValue() + sOffset;
                    break;
                case 0x0b:   // Non-Indirect D-register offset
                case 0x1b:   //  Indirect D-register offset
                    sOffset = refreg(post).getSigned();
                    addr = getSignedWord(d.intValue() + sOffset);
                    break;
                case 0x0c:   // Non-Indirect Constant 8-bit offset from PC
                case 0x1c:   // Indirect Constant 8-bit offset from PC
                    uOffset = extend8(fetch());
                    addr = pc + uOffset;
                    break;
                case 0x0d:   // Non-Indirect Constant 16-bit offset from PC
                case 0x1d:   // Indirect Constant 16-bit offset from PC
                    sOffset = getSignedWord(fetch_word());
                    addr = pc + sOffset;
                    break;
                case 0x1f:   // Extended indirect
                    addr = fetch_word();
                    break;
                default:
                    invalid("indirect addressing postbyte");
                    break;
            }

            /* Do extra indirection */
            if ((post & 0x10) != 0) {
                addr = read_word(addr);
            }
        }

        return addr;
    }

    // Bit extend operations
    private int extend5(int x) {
        if ((x & 0x10) != 0) {
            return x | 0xffe0;
        } else {
            return x;
        }
    }

    private int extend8(int x) {
        if ((x & 0x80) != 0) {
            return x | 0xff00;
        } else {
            return x;
        }
    }

    private void do_postincrement(int post) {
        switch (post & 0x9f) {
            case 0x80:
                refreg(post).add(1);
                break;
            case 0x90:
                invalid("postincrement");
                break;
            case 0x81: case 0x91:
                refreg(post).add(2);
                break;
        }
    }

    private void do_predecrement(int post) {
        switch (post & 0x9f) {
            case 0x82:
                refreg(post).add(-1);
                break;
            case 0x92:
                invalid("predecrement");
                break;
            case 0x83: case 0x93:
                refreg(post).add(-2);
                break;
        }
    }

    private void setBitZ(Register ref) {
        cc.setZ(ref.intValue() == 0 ? 1 : 0);
    }

    /**
     * Set CC bit N if value is negative.
     */
    private void setBitN(Register ref) {
        cc.setN(ref.btst(ref.WIDTH - 1));
    }

    /**
     * Add Accumulator B into index register X.
     */
    private void abx() {
        x.add(b.intValue());
    }

    private void help_adc(UByte refB) {
        int m = fetch_operand();

        {
            UByte t = UByte.valueOf((refB.intValue() & 0x0f) + (m & 0x0f) + cc.getC());
            cc.setH(t.btst(4));      // Half carry
        }

        {
            UByte t = UByte.valueOf((refB.intValue() & 0x7f) + (m & 0x7f) + cc.getC());
            cc.setV(t.btst(7));      // Bit 7 carry in
        }

        {
            Word t = Word.valueOf(refB.intValue() + m + cc.getC());
            cc.setC(t.btst(8));      // Bit 7 carry out
            refB.set(t.intValue());
        }

    //  cc.bit_v ^= cc.bit_c;
        //cc.bit_n = refB.btst(7);
        setBitN(refB);
        setBitZ(refB);
    }

    /**
     * Add with carry into accumulator A.
     */
    private void adca() {
        help_adc(a);
    }

    /**
     * Add with carry into accumulator B.
     */
    private void adcb() {
        help_adc(b);
    }


    private void help_add(UByte refB) {
        int m = fetch_operand();

        {
            UByte t = UByte.valueOf((refB.intValue() & 0x0f) + (m & 0x0f));
            cc.setH(t.btst(4));      // Half carry
        }

        {
            UByte t = UByte.valueOf((refB.intValue() & 0x7f) + (m & 0x7f));
            cc.setV(t.btst(7));      // Bit 7 carry in
        }

        {
            Word t = Word.valueOf(refB.intValue() + m);
            cc.setC(t.btst(8));      // Bit 7 carry out
            refB.set(t.intValue() & 0xff);
        }

    //  cc.bit_v ^= cc.bit_c;
        cc.setN(refB.btst(7));
        setBitZ(refB);
    }

    /**
     * Add memory into accumulator A.
     */
    private void adda() {
        help_add(a);
    }

    /**
     * Add memory into accumulator B.
     */
    private void addb() {
        help_add(b);
    }

    /**
     * Add memory into accumulator D.
     */
    private void addd() {
        int m = fetch_word_operand();

        {
            int intD = d.intValue();
            Word t = Word.valueOf((intD & 0x7fff) + (m & 0x7fff));
            //cc.bit_v = btst(intD ^ m ^ t ^ (t >> 1), 15);
            cc.setV(t.btst(15));      // Bit 15 carry in
            cc.setC(t.btst(16));
            d.set(t.intValue());
        }

    //  cc.bit_v ^= cc.bit_c;
        cc.setN(d.btst(15));
        setBitZ(d);
    }

    private void help_and(UByte refB) {
        refB.set(refB.intValue() & fetch_operand());
        cc.setN(refB.btst(7));
        setBitZ(refB);
        cc.setV(0);
    }

    /**
     * AND memory into accumulator A.
     */
    private void anda() {
        help_and(a);
    }

    /**
     * AND memory into accumulator B.
     */
    private void andb() {
        help_and(b);
    }

    /**
     * AND Immediate Data into Condition Code register.
     */
    private void andcc() {
        cc.set(cc.intValue() & fetch());
    }

    private void help_asr(UByte refB) {   
        cc.setC(refB.btst(0));
        refB.set(refB.intValue() >> 1);    /* Shift word right */
        cc.setN(refB.btst(6));
        if (cc.getN() != 0) {
            refB.bset(7);
        }
        setBitZ(refB);
    }

    /**
     * Arithmetic Shift Right accumulator A.
     */
    private void asra() {
        help_asr(a);
    }

    /**
     * Arithmetic Shift Right accumulator B.
     */
    private void asrb() {
        help_asr(b);
    }

    /**
     * Arithmetic Shift Right memory byte.
     */
    private void asr() {
        int addr = fetch_effective_address();
        int m = read(addr);

        UByte mbyte = UByte.valueOf(m);
        help_asr(mbyte);
        write(addr, mbyte);
    }


    /**
     * Branch on Carry Clear.
     */
    private void bcc() {
        do_br(!cc.isSetC());
    }

    /**
     * Long Branch on Carry Clear.
     */
    private void lbcc() {
        do_lbr(!cc.isSetC());
    }

    /**
     * Branch on Carry Set.
     */
    private void bcs() {
        do_br(cc.isSetC());
    }

    /**
     * Long Branch on Carry Set.
     */
    private void lbcs() {
        do_lbr(cc.isSetC());
    }

    private void beq() {
        do_br(cc.isSetZ());
    }

    private void lbeq() {
        do_lbr(cc.isSetZ());
    }

    //FIXME: test logic
    private void bge() {
        //do_br(!(cc.bit_n ^ cc.bit_v));
        do_br(cc.isSetN() == cc.isSetV());
    }

    //FIXME: test logic
    private void lbge() {
        //do_lbr(!(cc.bit_n ^ cc.bit_v));
        do_lbr(cc.isSetN() == cc.isSetV());
    }

    //FIXME: test logic
    private void bgt() {
        //do_br(!(cc.bit_z | (cc.bit_n ^ cc.bit_v)));
        do_br(!cc.isSetZ() && (cc.isSetN() == cc.isSetV()));
    }

    //FIXME: test logic
    private void lbgt() {
        //do_lbr(!(cc.bit_z | (cc.bit_n ^ cc.bit_v)));
        do_lbr(!cc.isSetZ() && (cc.isSetN() == cc.isSetV()));
    }


    private void bhi() {
        do_br(!(cc.isSetC() || cc.isSetZ()));
    }

    private void lbhi() {
        do_lbr(!(cc.isSetC() || cc.isSetZ()));
    }

/*
    private void bita() {
        help_bit(a);
    }

    private void bitb() {
        help_bit(b);
    }

    private void help_bit(UByte arg) {
        UByte t = arg & fetch_operand();
        cc.bit_n = btst(t, 7);
        cc.bit_v = 0;
        setBitZ(t);
    }
*/
    private void ble() {
        //do_br(cc.bit_z | (cc.bit_n ^ cc.bit_v));
        do_br(cc.isSetZ() || (cc.isSetN() != cc.isSetV()));
    }

    private void lble() {
        //do_lbr(cc.bit_z | (cc.bit_n ^ cc.bit_v));
        do_lbr(cc.isSetZ() || (cc.isSetN() != cc.isSetV()));
    }

    private void bls() {
        do_br(cc.isSetC() || cc.isSetZ());
    }

    private void lbls() {
        do_lbr(cc.isSetC() || cc.isSetZ());
    }

    private void blt() {
        //do_br(cc.bit_n ^ cc.bit_v);
        do_br(cc.isSetN() != cc.isSetV());
    }

    private void lblt() {
        //do_lbr(cc.bit_n ^ cc.bit_v);
        do_lbr(cc.isSetN() != cc.isSetV());
    }

    private void bmi() {
        do_br(cc.isSetN());
    }

    private void lbmi() {
        do_lbr(cc.isSetN());
    }

    private void bne() {
        do_br(!cc.isSetZ());
    }

    private void lbne() {
        do_lbr(!cc.isSetZ());
    }

    private void bpl() {
        do_br(!cc.isSetN());
    }

    private void lbpl() {
        do_lbr(!cc.isSetN());
    }

    private void bra() {
        do_br(true);
    }

    private void lbra() {
        do_lbr(true);
    }

    private void brn() {
        do_br(false);
    }

    private void lbrn() {
        do_lbr(false);
    }
/*
    private void bsr() {
        UByte    relAddr = fetch();
        write(--s, pc);
        write(--s, (pc >> 8));
        pc += extend8(relAddr);
    }

    private void lbsr() {
        Word    relAddr = fetch_word();
        write(--s, pc);
        write(--s, (pc >> 8));
        pc += relAddr;
    }
*/
    private void bvc() {
        do_br(!cc.isSetV());
    }

    private void lbvc() {
        do_lbr(!cc.isSetV());
    }

    private void bvs() {
        do_br(cc.isSetV());
    }

    private void lbvs() {
        do_lbr(cc.isSetV());
    }

    private void clra() {
        help_clr(a);
    }

    private void clrb() {
        help_clr(b);
    }

    private void clr() {
        int addr = fetch_effective_address();
        UByte m = UByte.valueOf(read(addr));
        help_clr(m);
        write(addr, m);
    }

    private void help_clr(UByte refB) {
        cc.setN(0);
        cc.setZ(1);
        cc.setV(0);
        cc.setC(0);
        refB.set(0);
    }
/*
    private void cmpa() {
        help_cmp(a);
    }

    private void cmpb() {
        help_cmp(b);
    }

    private void help_cmp(UByte x) {
        UByte    m = fetch_operand();
        int t = x - m;
         
        cc.bit_h = ((t & 0x0f) < (m & 0x0f)); // half-carry
        cc.bit_v = btst((UByte)(x^m^t^(t>>1)), 7);
        cc.bit_c = btst((Word)t, 8);
         
        cc.bit_n = btst((UByte)t, 7);
        cc.bit_z = !t;
    }

    private void cmpd() {
        help_cmp(d);
    }

    private void cmpx() {
        help_cmp(x);
    }

    private void cmpy() {
        help_cmp(y);
    }

    private void cmpu() {
        help_cmp(u);
    }

    private void cmps() {
        help_cmp(s);
    }

    private void help_cmp(Word x) {
        int m = fetch_word_operand();
        int t = x - m;
         
        cc.bit_v = btst((DWord)(x^m^t^(t>>1)), 15);
        cc.bit_c = btst((DWord)t, 16);
         
        cc.bit_n = btst((DWord)t, 15);
        cc.bit_z = !t;
    }

    private void coma() {
        help_com(a);
    }

    private void comb() {
        help_com(b);
    }

    private void com() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_com(m);
        write(addr, m);
    }

    private void help_com(UByte x) {
        x = ~x;
        cc.bit_c = 1;
        cc.bit_v = 0;
        cc.bit_n = btst(x, 7);
        cc.bit_z = !x;
    }

    private void daa() {
        UByte    c = 0;
        UByte    lsn = (a & 0x0f);
        UByte    msn = (a & 0xf0) >> 4;

        if (cc.bit_h || (lsn > 9)) {
            c |= 0x06;
        }

        if (cc.bit_c ||
            (msn > 9) ||
            ((msn > 8) && (lsn > 9))) {
            c |= 0x60;
        }

        {
            Word    t = (Word)a + c;
            cc.bit_c = btst(t, 8);
            a = (UByte)t;
        }

        cc.bit_n = btst(a, 7);
        cc.bit_z = !a;
    }

    private void deca() {
        help_dec(a);
    }

    private void decb() {
        help_dec(b);
    }

    private void dec() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_dec(m);
        write(addr, m);
    }

    private void help_dec(UByte x) {
        cc.bit_v = (x == 0x80);
        x = x - 1;
        cc.bit_n = btst(x, 7);
        cc.bit_z = !x;
    }

    private void eora() {
        help_eor(a);
    }

    private void eorb() {
        help_eor(b);
    }

    private void help_eor(UByte x) {
        x = x ^ fetch_operand();
        cc.bit_v = 0;
        cc.bit_n = btst(x, 7);
        cc.bit_z = !x;
    }

    static void swap(UByte r1, UByte r2) {
        UByte    t;
        t = r1; r1 = r2; r2 = t;
    }

    static void swap(Word r1, Word r2) {
        Word    t;
        t = r1; r1 = r2; r2 = t;
    }

    private void exg() {
        int r1, r2;
        UByte    w = fetch();
        r1 = (w & 0xf0) >> 4;
        r2 = (w & 0x0f) >> 0;
        if (r1 <= 5) {
            if (r2 > 5) {
                invalid("exchange register");
                return;
            }
            swap(wordrefreg(r2), wordrefreg(r1));
        } else if (r1 >= 8 && r2 <= 11) {
            if (r2 < 8 || r2 > 11) {
                invalid("exchange register");
                return;
            }
            swap(byterefreg(r2), byterefreg(r1));
        } else  {
            invalid("exchange register");
            return;
        }
    }

    private void inca() {
        help_inc(a);
    }

    private void incb() {
        help_inc(b);
    }

    private void inc() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_inc(m);
        write(addr, m);
    }

    private void help_inc(UByte x) {
        cc.bit_v = (x == 0x7f);
        x = x + 1;
        cc.bit_n = btst(x, 7);
        cc.bit_z = !x;
    }

    private void jmp() {
        pc = fetch_effective_address();
    }

    private void jsr() {
        Word    addr = fetch_effective_address();
        write(--s, pc);
        write(--s, pc >> 8);
        pc = addr;
    }

    private void lda() {
        help_ld(a);
    }

    private void ldb() {
        help_ld(b);
    }

    private void help_ld(UByte regB) {
        regB = fetch_operand();
        cc.bit_n = btst(regB, 7);
        cc.bit_v = 0;
        cc.bit_z = !regB;
    }

    private void ldd() {
        help_ld(d);
    }

    private void ldx() {
        help_ld(x);
    }

    private void ldy() {
        help_ld(y);
    }

    private void lds() {
        help_ld(s);
    }

    private void ldu() {
        help_ld(u);
    }

    private void help_ld(Word regW) {
        int val = fetch_word_operand();
        regW.set(val);
        cc.bit_n = regW.btst(15);
        cc.bit_v = 0;
        setBitZ(refW);
    }

    private void leax() {
        x = fetch_effective_address();
        setBitZ(x);
    }

    private void leay() {
        y = fetch_effective_address();
        setBitZ(y);
    }

    private void leas() {
        s = fetch_effective_address();
    }

    private void leau() {
        u = fetch_effective_address();
    }

    private void lsla() {
        help_lsl(a);
    }

    private void lslb() {
        help_lsl(b);
    }

    private void lsl() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_lsl(m);
        write(addr, m);
    }

    private void help_lsl(UByte regB) {
        cc.bit_c = btst(regB, 7);
        cc.bit_v = btst(regB, 7) ^ btst(regB, 6);
        regB <<= 1;
        cc.bit_n = btst(regB, 7);
        cc.bit_z = !regB;
    }

    private void lsra() {
        help_lsr(a);
    }

    private void lsrb() {
        help_lsr(b);
    }

    private void lsr() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_lsr(m);
        write(addr, m);
    }

    private void help_lsr(UByte regB) {
        cc.bit_c = btst(regB, 0);
        regB >>= 1;    // Shift word right
        cc.bit_n = 0;
        cc.bit_z = !regB;
    }

    private void mul() {
        d = a * b;
        cc.bit_c = btst(b, 7);
        cc.bit_z = !d;
    }

    private void nega() {
        help_neg(a);
    }

    private void negb() {
        help_neg(b);
    }

    private void neg() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_neg(m);
        write(addr, m);
    }

    private void help_neg(UByte regB) {
        cc.bit_v = (regB == 0x80);
        {
            Word t = (Word)((~regB) & 0xff) + 1;
            regB = t & 0xff;
        }

        setBitN(regB);
        setBitZ(regB);
        cc.bit_c = (regB != 0);
    }
*/
    private void nop() {
    }

    private void ora() {
        help_or(a);
    }

    private void orb() {
        help_or(b);
    }

    private void help_or(UByte regB) {
        regB.set(regB.intValue() | fetch_operand());
        cc.setV(0);
        setBitN(regB);
        setBitZ(regB);
    }
/*
    private void orcc() {
        cc.all |= fetch_operand();
    }

    private void pshs() {
        help_psh(fetch(), s, u);
    }

    private void pshu() {
        help_psh(fetch(), u, s);
    }

    private void help_psh(UByte w, Word s, Word u) {
        if (btst(w, 7)) {
            write(--s, pc);
            write(--s, (pc >> 8));
        }
        if (btst(w, 6)) {
            write(--s, u);
            write(--s, (u >> 8));
        }
        if (btst(w, 5)) {
            write(--s, y);
            write(--s, (y >> 8));
        }
        if (btst(w, 4)) {
            write(--s, x);
            write(--s, (x >> 8));
        }
        if (btst(w, 3)) write(--s, dp);
        if (btst(w, 2)) write(--s, b);
        if (btst(w, 1)) write(--s, a);
        if (btst(w, 0)) write(--s, cc.all);
    }
*/
    /**
     * PULS: Pull Registers from Hardware Stack.
     * The stack grows downwards, and this means that when you PULL, the
     * stack pointer is increased.
     */
/*
    private void puls() {
        int w = fetch();
        help_pul(w, s, u);
    }

    private void pulu() {
        int w = fetch();
        help_pul(w, u, s);
    }

    private void help_pul(UByte w, Word s, Word u) {
        if (btst(w, 0)) cc.all = read(s++);
        if (btst(w, 1)) a = read(s++);
        if (btst(w, 2)) b = read(s++);
        if (btst(w, 3)) dp = read(s++);
        if (btst(w, 4)) {
            x = read_word(s); s += 2;
        }
        if (btst(w, 5)) {
            y = read_word(s); s += 2;
        }
        if (btst(w, 6)) {
            u = read_word(s); s += 2;
        }
        if (btst(w, 7)) {
            pc = read_word(s); s += 2;
        }
    }

    private void rola() {
        help_rol(a);
    }

    private void rolb() {
        help_rol(b);
    }

    private void rol() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_rol(m);
        write(addr, m);
    }

    private void help_rol(UByte regB) {
        int oc = cc.bit_c;
        cc.bit_v = btst(regB, 7) ^ btst(regB, 6);
        cc.bit_c = btst(regB, 7);
        regB = regB << 1;
        if (oc) bset(regB, 0);
        cc.bit_n = btst(regB, 7);
        cc.bit_z = !regB;
    }

    private void rora() {
        help_ror(a);
    }

    private void rorb() {
        help_ror(b);
    }

    private void ror() {
        Word    addr = fetch_effective_address();
        UByte    m = read(addr);
        help_ror(m);
        write(addr, m);
    }

    private void help_ror(UByte regB) {
        int oc = cc.bit_c;
        cc.bit_c = btst(regB, 0);
        regB = regB >> 1;
        if (oc) bset(regB, 7);
        cc.bit_n = btst(regB, 7);
        cc.bit_z = !regB;
    }

    private void rti() {
        help_pul(0x01, s, u);
        if (cc.bit_e) {
            help_pul(0xfe, s, u);
        }
    }

    private void rts() {
        pc = read_word(s);
        s += 2;
    }

    private void sbca() {
        help_sbc(a);
    }

    private void sbcb() {
        help_sbc(b);
    }

    private void help_sbc(UByte regB) {
        UByte    m = fetch_operand();
        int t = regB - m - cc.bit_c;
         
        cc.bit_h = ((t & 0x0f) < (m & 0x0f)); // half-carry
        cc.bit_v = btst((UByte)(regB^m^t^(t>>1)), 7);
        cc.bit_c = btst((Word)t, 8);
         
        cc.bit_n = btst((UByte)t, 7);
        cc.bit_z = !t;
        regB = t & 0xff;
    }

    private void sex() {
        setBitN(b);
        a.set(cc.isSetN ? 255 : 0);
        setBitZ(a);
    }

    private void sta() {
        help_st(a);
    }

    private void stb() {
        help_st(b);
    }

    private void help_st(UByte data) {
        Word    addr = fetch_effective_address();
        write(addr, data);
        cc.bit_v = 0;
        cc.bit_n = btst(data, 7);
        cc.bit_z = !data;
    }

    private void std() {
        help_st(d);
    }

    private void stx() {
        help_st(x);
    }

    private void sty() {
        help_st(y);
    }

    private void sts() {
        help_st(s);
    }

    private void stu() {
        help_st(u);
    }

    private void help_st(Word dataW) {
        Word    addr = fetch_effective_address();
        write_word(addr, dataW);
        cc.bit_v = 0;
        cc.bit_n = btst(dataW, 15);
        cc.bit_z = !dataW;
    }

    private void suba() {
        help_sub(a);
    }

    private void subb() {
        help_sub(b);
    }

    private void help_sub(UByte regB) {
        UByte    m = fetch_operand();
        int t = regB - m;
         
        cc.bit_v = btst((UByte)(regB^m^t^(t>>1)), 7);
        cc.bit_c = btst((Word)t, 8);
         
        cc.bit_n = btst((UByte)t, 7);
        cc.bit_z = !t;
        regB = t & 0xff;
    }

    private void subd() {
        Word    m = fetch_word_operand();
        int t = d - m;
         
        cc.bit_v = btst((DWord)(d^m^t^(t>>1)), 15);
        cc.bit_c = btst((DWord)t, 16);
         
        cc.bit_n = btst((DWord)t, 15);
        cc.bit_z = !t;
        d = t &0xffff;
    }

    void swi() {
        cc.bit_e = 1;
        help_psh(0xff, s, u);
        cc.bit_f = cc.bit_i = 1;
        pc = read_word(0xfffa);
    }

    void swi2() {
        cc.bit_e = 1;
        help_psh(0xff, s, u);
        pc = read_word(0xfff4);
    }

    void swi3() {
        cc.bit_e = 1;
        help_psh(0xff, s, u);
        pc = read_word(0xfff2);
    }

    private void tfr() {
        int r1, r2;
        UByte    w = fetch();
        r1 = (w & 0xf0) >> 4;
        r2 = (w & 0x0f) >> 0;
        if (r1 <= 5) {
            if (r2 > 5) {
                invalid("transfer register");
                return;
            }
            wordrefreg(r2) = wordrefreg(r1);
        } else if (r1 >= 8 && r2 <= 11) {
            if (r2 < 8 || r2 > 11) {
                invalid("transfer register");
                return;
            }
            byterefreg(r2) = byterefreg(r1);
        } else  {
            invalid("transfer register");
            return;
        }
    }
*/
    private void tsta() {
        help_tst(a);
    }

    private void tstb() {
        help_tst(b);
    }

    private void tst() {
        int addr = fetch_effective_address();
        UByte m = UByte.valueOf(read(addr));
        help_tst(m);
    }

    private void help_tst(UByte dataB) {
        cc.setV(0);
        setBitN(dataB);
        setBitZ(dataB);
    }

    private void do_br(boolean test) {
        int offset = extend8(fetch_operand());
        if (test) {
            pc += offset;
        }
    }

    private void do_lbr(boolean test) {
        int offset = fetch_word_operand();
        if (test) {
            pc += offset;
        }
    }

}
