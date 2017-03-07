package org.roug.osnine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InstructionsTest {

    private static final int LOCATION = 0x1e20;

    private MC6809 myTestCPU;

    @Before
    public void setUp() {
        myTestCPU = new MC6809(0x2000);
    }

    /**
     * Load a short program into memory.
     */
    private void loadProg(int[] instructions) {
        myTestCPU.write_word(0xfffe, LOCATION);
        int respc = myTestCPU.read_word(0xfffe);
        assertEquals(LOCATION, respc);

        for (int i = 0; i < instructions.length; i++) {
            myTestCPU.write(i + LOCATION, instructions[i]);
        }
        myTestCPU.reset();
    }

    private void setCC_A_B_DP_X_Y_S_U(int cc, int a, int b, int dp, int x, int y, int s, int u) {
        myTestCPU.cc.set(cc);
        myTestCPU.a.set(a);
        myTestCPU.b.set(b);
        myTestCPU.dp.set(dp);
        myTestCPU.x.set(x);
        myTestCPU.y.set(y);
        myTestCPU.s.set(s);
        myTestCPU.u.set(u);
    }

    private void chkCC_A_B_DP_X_Y_S_U(int cc, int a, int b, int dp, int x, int y, int s, int u) {
        assertEquals(cc, myTestCPU.cc.intValue());
        assertEquals(a, myTestCPU.a.intValue());
        assertEquals(b, myTestCPU.b.intValue());
        assertEquals(dp, myTestCPU.dp.intValue());
        assertEquals(x, myTestCPU.x.intValue());
        assertEquals(y, myTestCPU.y.intValue());
        assertEquals(s, myTestCPU.s.intValue());
        assertEquals(u, myTestCPU.u.intValue());
    }

    @Test
    public void testABX() throws IOException {
        int instructions[] = {
            0x3a // ABX
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(0, 0, 0xCE, 0, 0x8006, 0, 0, 0);
        myTestCPU.execute();
        assertEquals(0x3a, myTestCPU.ir);
        assertEquals(LOCATION + 1, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(0, 0, 0xCE, 0, 0x80D4, 0, 0, 0);
    }


    @Test
    public void testADCANoCarry() {
        int instructions[] = {
            0x89, // ADCA
            0x02  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(0, 5, 0, 0, 0, 0, 0, 0);
//      myTestCPU.cc.bit_c = 0;
//      myTestCPU.a.set(5);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
//      assertEquals(7, myTestCPU.a.intValue());
//      assertEquals(0, myTestCPU.cc.getC());
        chkCC_A_B_DP_X_Y_S_U(0, 7, 0, 0, 0, 0, 0, 0);
    }


    @Test
    public void testADCAWithCarry() {
        int instructions[] = {
            0x89, // ADCA
            0x22  // value
        };
        loadProg(instructions);
        //myTestCPU.cc.bit_c = 1;
        //myTestCPU.a.set(0x14);
        setCC_A_B_DP_X_Y_S_U(CC.Cmask, 0x14, 0, 0, 0, 0, 0, 0);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(0, 0x37, 0, 0, 0, 0, 0, 0);
        //assertEquals(0x37, myTestCPU.a.intValue());
        //assertEquals(0, myTestCPU.cc.getC());
    }

    /*
     * Test that half-carry is set.
     */
    @Test
    public void testADCAWithHalfCarry() {
        int instructions[] = {
            0x89, // ADCA
            0x2B  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(CC.Cmask, 0x14, 0, 0, 0, 0, 0, 0);
        myTestCPU.execute();
        chkCC_A_B_DP_X_Y_S_U(CC.Hmask, 0x40, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Add 0x02 to A=0x04.
     */
    @Test
    public void testADDANoCarry() {
        int instructions[] = {
            0x8B, // ADDA
            0x02, // value
        };
        loadProg(instructions);
        myTestCPU.cc.bit_c = 0;
        myTestCPU.a.set(0x04);
        myTestCPU.b.set(0x05);
        myTestCPU.execute();
        assertEquals(0x8B, myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
        assertEquals(0x06, myTestCPU.a.intValue());
        assertEquals(0x05, myTestCPU.b.intValue());
        assertFalse(myTestCPU.cc.isSetH());
        assertFalse(myTestCPU.cc.isSetN());
        assertFalse(myTestCPU.cc.isSetZ());
        assertFalse(myTestCPU.cc.isSetV());
        assertFalse(myTestCPU.cc.isSetC());
    }

    /**
     * Add 0x02B0 to D=0x0405.
     */
    @Test
    public void testADDDNoCarry() {
        int instructions[] = {
            0xC3, // ADDD
            0x02, // value
            0xB0  // value
        };
        loadProg(instructions);
        myTestCPU.cc.bit_c = 0;
        myTestCPU.a.set(0x04);
        myTestCPU.b.set(0x05);
        myTestCPU.execute();
        assertEquals(0xC3, myTestCPU.ir);
        assertEquals(LOCATION + 3, myTestCPU.pc.intValue());
        assertEquals(0x06, myTestCPU.a.intValue());
        assertEquals(0xB5, myTestCPU.b.intValue());
        assertEquals(0x06B5, myTestCPU.d.intValue());
        assertEquals(0, myTestCPU.cc.bit_c);
    }

    @Test
    public void testANDA() {
	myTestCPU.a.set(0x8B);
	myTestCPU.dp.set(0x0A);
	myTestCPU.cc.set(0x32);
	myTestCPU.write(0x0AEF, 0x0F);
	myTestCPU.write(0x0B00, 0x94);
	myTestCPU.write(0x0B01, 0xEF);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
        assertEquals(0x0B, myTestCPU.a.intValue());
	assertEquals(0x30, myTestCPU.cc.intValue());
    }

    @Test
    public void testANDCC() {
	myTestCPU.cc.set(0x79);
	myTestCPU.write(0x0B00, 0x1C);
	myTestCPU.write(0x0B01, 0xAF);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x29, myTestCPU.cc.intValue());
    }

    /**
     * Shift a byte a 0x0402, because DP = 0x04.
     */
    @Test
    public void testASRMemoryByte() {
        int instructions[] = {
            0x07, // ASR
            0x02  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(0, 0, 0, 4, 0, 0, 0, 0);
        myTestCPU.write(0x0402, 0xf1);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(0x09, 0, 0, 4, 0, 0, 0, 0);
        int result = myTestCPU.read(0x0402);
        assertEquals(0xf8, result);
    }

    /**
     * Don't branch because Z is on.
     */
    @Test
    public void testBGTWithZ() {
        int instructions[] = {
            0x2E, // BGT
            17 // Jump forward 17 bytes
        };
        loadProg(instructions);
        myTestCPU.cc.set(CC.Zmask);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
    }

    /**
     * Branch because N and V are on.
     */
    @Test
    public void testBGTWithNandV() {
        int instructions[] = {
            0x2E, // BGT
            0x17 // Jump forward 0x17 bytes
        };
        loadProg(instructions);
        myTestCPU.cc.set(CC.Nmask + CC.Vmask);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2 + 0x17, myTestCPU.pc.intValue());
    }

    /**
     * Branch because Z, N and V are off.
     */
    @Test
    public void testBGTWithNandVoff() {
        int instructions[] = {
            0x2E, // BGT
            0x17 // Jump forward 0x17 bytes
        };
        loadProg(instructions);
        myTestCPU.cc.set(CC.Cmask);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2 + 0x17, myTestCPU.pc.intValue());
    }

    @Test
    public void testBITimm() {
	myTestCPU.a.set(0x8B);
	myTestCPU.cc.set(0x0F);
	myTestCPU.write(0x0B00, 0x85);
	myTestCPU.write(0x0B01, 0xAA);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x8B, myTestCPU.a.intValue());
	assertEquals(0x09, myTestCPU.cc.intValue());
        assertEquals(1, myTestCPU.cc.getN());
        assertEquals(0, myTestCPU.cc.getZ());
        assertEquals(0, myTestCPU.cc.getV());
        assertEquals(1, myTestCPU.cc.getC());
    }

    @Test
    public void testBRAForward() {
        int instructions[] = {
            0x20, // BRA
            17 // Jump forward 17 bytes
        };
        loadProg(instructions);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2 + 17, myTestCPU.pc.intValue());
    }

    @Test
    public void testBRABackward() {
        int instructions[] = {
            0x20, // BRA
            170 // Jump backward 170 - 256 = 86 bytes
        };
        loadProg(instructions);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2 - 86, myTestCPU.pc.intValue());
    }

    @Test
    public void testBSRBackward() {
        int instructions[] = {
            0x8d, // BSR
            170 // Jump backward 170 - 256 = 86 bytes
        };
        loadProg(instructions);
        myTestCPU.s.set(0x300);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 2 - 86, myTestCPU.pc.intValue());
        assertEquals(0x2fe, myTestCPU.s.intValue());
        assertEquals(0x22, myTestCPU.read(0x2ff));
        assertEquals(0x1e, myTestCPU.read(0x2fe));
    }

    @Test
    public void testLBSRbackward() {
        int instructions[] = {
            0x17, // LBSR
            0xF8,
            0xD5
        };
        loadProg(instructions);
        myTestCPU.s.set(0x300);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 3 - 0x072B, myTestCPU.pc.intValue());
        assertEquals(0x2fe, myTestCPU.s.intValue());
        assertEquals(0x23, myTestCPU.read(0x2ff));
        assertEquals(0x1e, myTestCPU.read(0x2fe));
    }

    @Test
    public void testLBSRforward() {
        int instructions[] = {
            0x17, // LBSR
            0x03,
            0x72
        };
        loadProg(instructions);
        myTestCPU.s.set(0x300);
        myTestCPU.execute();
        // The size of the instruction is 2 bytes.
        assertEquals(LOCATION + 3 + 0x0372, myTestCPU.pc.intValue());
        assertEquals(0x2fe, myTestCPU.s.intValue());
        assertEquals(0x23, myTestCPU.read(0x2ff));
        assertEquals(0x1e, myTestCPU.read(0x2fe));
    }

    /**
     * Clear byte in extended mode.
     */
    @Test
    public void CLRMemoryByteExtended() {
        int instructions[] = {
            0x7F, // CLR
            0x0F,  // value
            0x23  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(0, 0, 0, 4, 0, 0, 0, 0);
        myTestCPU.write(0x0F23, 0xE2);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 3, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 4, 0, 0, 0, 0);
        int result = myTestCPU.read(0x0F23);
        assertEquals(0, result);
    }

    /**
     * Clear byte in direct mode with DP = 0x0B.
     */
    @Test
    public void CLRMemoryByteDirect() {
        int instructions[] = {
            0x0F, // CLR
            0x23  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(0, 0, 0, 0x0B, 0, 0, 0, 0);
        myTestCPU.write(0x0B23, 0xE2);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 0x0B, 0, 0, 0, 0);
        int result = myTestCPU.read(0x0B23);
        assertEquals(0, result);
    }

    /**
     * Clear byte in indirect mode relative to Y and increment Y.
     */
    @Test
    public void CLRMemoryByteIndexedYinc() {
        int instructions[] = {
            0x6F, // CLR
            0xA0  // value
        };
        loadProg(instructions);
        setCC_A_B_DP_X_Y_S_U(CC.Nmask, 0, 0, 0, 0, 0x899, 0, 0);
        myTestCPU.write(0x0899, 0xE2);
        myTestCPU.write(0x089A, 0x22);
        myTestCPU.execute();
        assertEquals(instructions[0], myTestCPU.ir);
        assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 0, 0, 0x89A, 0, 0);
        int result = myTestCPU.read(0x0899);
        assertEquals(0, result);
    }

    /**
     * Test indirect mode: CMPA ,Y+
     * We're subtracting 0xff from 0xff and incrementing Y
     */
    @Test
    public void testCMP() {
        // Set up a byte to test at address 0x205
        myTestCPU.write(0x205, 0xff);
        myTestCPU.cc.clear();
        // Set register Y to point to that location
        myTestCPU.y.set(0x205);
        myTestCPU.a.set(0xff);
        // Two bytes of instruction
        myTestCPU.write(0xB00, 0xA1);
        myTestCPU.write(0xB01, 0xA0);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0xB00 + 2, myTestCPU.pc.intValue());
        chkCC_A_B_DP_X_Y_S_U(CC.Hmask + CC.Zmask, 0xff, 0, 0, 0, 0x206, 0, 0);
    }

    @Test
    public void testCMP16ext() {
	myTestCPU.x.set(0x5410);
	myTestCPU.cc.set(0x23);
	myTestCPU.write(0x0B33, 0x54);
	myTestCPU.write(0x0B34, 0x10);

	myTestCPU.write(0x0B00, 0xBC);
	myTestCPU.write(0x0B01, 0x0B);
	myTestCPU.write(0x0B02, 0x33);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x5410, myTestCPU.x.intValue());
	assertEquals(0x24, myTestCPU.cc.intValue());
    }

    /**
     * Complement a memory location.
     */
    @Test
    public void testCOMdirect() {
        myTestCPU.cc.clear();
	myTestCPU.dp.set(0x02);
        myTestCPU.write(0x0200, 0x07);
	// Two bytes of instruction
	myTestCPU.write(0xB00, 0x03); // COM
	myTestCPU.write(0xB01, 0x00); // Direct page addr + 0
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xF8, myTestCPU.read(0x0200));
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(1, myTestCPU.cc.getC());
    }

    /**
     * Complement register A.
     */
    @Test
    public void testCOMA() {
        myTestCPU.cc.clear();
        myTestCPU.a.set(0x74);
        myTestCPU.write(0xB00, 0x43);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x8B, myTestCPU.a.intValue());
        assertEquals(0x09, myTestCPU.cc.intValue());
    }

    @Test
    public void testDAA() {
        myTestCPU.write(0xB00, 0x19);
        myTestCPU.cc.clear();
        myTestCPU.a.set(0x7f);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x85, myTestCPU.a.intValue());
        assertEquals(0x08, myTestCPU.cc.intValue());
    }

    /**
     * Decrement register A.
     */
    @Test
    public void testDECA() {
        myTestCPU.cc.clear();
        myTestCPU.a.set(0x32);
        myTestCPU.write(0xB00, 0x4A);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x31, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());

        // Test 0x80 - special case
        myTestCPU.a.set(0x80);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x7F, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(1, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());

        // Test 0x00 - special case
        myTestCPU.a.set(0x00);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0xFF, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());
    }

    @Test
    public void testEORAindexed() {
        myTestCPU.y.set(0x12F0);
        myTestCPU.a.set(0xF2);
        myTestCPU.cc.set(0x03);
        myTestCPU.write(0x12F8, 0x98);
        myTestCPU.write(0xB00, 0xA8);
        myTestCPU.write(0xB01, 0x28);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
	assertEquals(0x98, myTestCPU.read(0x12F8));
        assertEquals(0x6A, myTestCPU.a.intValue());
        assertEquals(0x01, myTestCPU.cc.intValue());
        assertEquals(0x12F0, myTestCPU.y.intValue());
    }

    /**
     * Exchange A and DP.
     */
    @Test
    public void testEXGadp() {
        myTestCPU.cc.clear();
        myTestCPU.a.set(0x7f);
        myTestCPU.dp.set(0xf6);
        myTestCPU.write(0xB00, 0x1E);
        myTestCPU.write(0xB01, 0x8B);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x7f, myTestCPU.dp.intValue());
        assertEquals(0xf6, myTestCPU.a.intValue());
    }

    /**
     * Exchange D and X.
     */
    @Test
    public void testEXGdx() {
        myTestCPU.write(0xB00, 0x1E);
        myTestCPU.write(0xB01, 0x01);
        myTestCPU.cc.clear();
        myTestCPU.d.set(0x117f);
        myTestCPU.x.set(0xff16);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0xff16, myTestCPU.d.intValue());
        assertEquals(0x117f, myTestCPU.x.intValue());
    }

    /**
     * Increment register A.
     */
    @Test
    public void testINCA() {
        myTestCPU.cc.clear();
        myTestCPU.a.set(0x32);
        myTestCPU.write(0xB00, 0x4C);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x33, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());

        // Test 0x7F - special case
        myTestCPU.a.set(0x7F);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x80, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(1, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());

        // Test 0xFF - special case
        myTestCPU.a.set(0xFF);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x00, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(1, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(0, myTestCPU.cc.getC());
    }

    /**
     * Test the JSR - Jump to Subroutine - instruction.
     * INDEXED mode:   JSR   D,Y
     */
    @Test
    public void testJSR() {
        // Set up a word to test at address 0x205
        myTestCPU.write_word(0x205, 0x03ff);
        // Set register D
        myTestCPU.d.set(0x105);
        // Set register Y to point to that location minus 5
        myTestCPU.y.set(0x200);
        // Set register S to point to 0x915
        myTestCPU.s.set(0x915);
        // Two bytes of instruction
        myTestCPU.write(0xB00, 0xAD);
        myTestCPU.write(0xB01, 0xAB);
        myTestCPU.write(0xB02, 0x11); // Junk
        myTestCPU.write(0xB03, 0x22); // Junk
        myTestCPU.pc.set(0xB00);
        myTestCPU.cc.clear();
        myTestCPU.execute();
        chkCC_A_B_DP_X_Y_S_U(0, 0x01, 0x05, 0, 0, 0x200, 0x913, 0);
        assertEquals(0x200, myTestCPU.y.intValue());
        assertEquals(0x105, myTestCPU.d.intValue());
        assertEquals(0x913, myTestCPU.s.intValue());
        assertEquals(0x305, myTestCPU.pc.intValue());
    }

//
// L
//
    @Test
    public void testLBRAForwards() {
        myTestCPU.write(0xB00, 0x16);
        myTestCPU.write_word(0xB01, 0x03FF);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
        assertEquals(0xB00 + 3 + 0x03FF, myTestCPU.pc.intValue());
    }

    @Test
    public void testLBRABackwards() {
        myTestCPU.write(0x1B00, 0x16);
        myTestCPU.write_word(0x1B01, 0xF333);
        myTestCPU.pc.set(0x1B00);
	myTestCPU.execute();
        assertEquals(0x1B00 + 3 - 0xCCD, myTestCPU.pc.intValue());
    }

    @Test
    public void testLBRNForwards() {
        myTestCPU.write_word(0xB00, 0x1021);
        myTestCPU.write_word(0xB02, 0x03FF);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
        assertEquals(0xB00 + 4, myTestCPU.pc.intValue());
    }


    /**
     * Increase the stack register by two. (LEAS +$ 2,S.)
     * LEA only allows indexed mode.
     *
     */
    @Test
    public void testLEASinc2() {
        myTestCPU.s.set(0x900);
	myTestCPU.write(0xB00, 0x32);  // LEAS
	myTestCPU.write(0xB01, 0x62);  // SP + 2 (last 5 bits)
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xB02, myTestCPU.pc.intValue());
	assertEquals(0x902, myTestCPU.s.intValue());
    }

    /**
     * Decrease the stack register by two. (LEAS -$ 2,S.)
     * LEA only allows indexed mode.
     *
     */
    @Test
    public void testLEASdec2() {
        myTestCPU.s.set(0x900);
	myTestCPU.write(0xB00, 0x32);  // LEAS
	myTestCPU.write(0xB01, 0x7E);  // SP + 2's complement of last 5 bits.
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xB02, myTestCPU.pc.intValue());
	assertEquals(0x8FE, myTestCPU.s.intValue());
    }

    @Test
    public void testLEAX_PCR() {
	int instructions[] = {
	    0x30, // LEAX        >$0013,PCR
	    0x8D,
	    0xFE,
	    0x49
	};

	int offset = 0x10000 - 0xfe49;
	// Negate 0
	loadProg(instructions);
	myTestCPU.execute();
	assertEquals(LOCATION + 4 - offset, myTestCPU.x.intValue());
	assertEquals(LOCATION + 4, myTestCPU.pc.intValue());

	// LEAX        <$93A,PCR
	myTestCPU.write(0x0846, 0x30);
	myTestCPU.write(0x0847, 0x8C);
	myTestCPU.write(0x0848, 0xF1);
        myTestCPU.pc.set(0x0846);
	myTestCPU.execute();
	assertEquals(0x0849, myTestCPU.pc.intValue());
	assertEquals(0x083a, myTestCPU.x.intValue());
    }

    /**
     * Test the LSL - Logical Shift Left instruction.
     */
    @Test
    public void testLSL() {
        // Write instruction into memory
        myTestCPU.write(0xB00, 0x48);
        // Logical Shift Left of 0xff
        myTestCPU.cc.clear();
        myTestCPU.a.set(0xff);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0xfe, myTestCPU.a.intValue());
        assertEquals(0x09, myTestCPU.cc.intValue());
    //  assertEquals(0, myTestCPU.cc.getV());
    //  assertEquals(0, myTestCPU.cc.bit.n);

        // Logical Shift Left of 1
        myTestCPU.cc.setC(0);
        myTestCPU.cc.setV(1);
        myTestCPU.a.set(1);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x02, myTestCPU.a.intValue());
        assertEquals(0, myTestCPU.cc.intValue());
    //  assertEquals(0, myTestCPU.cc.getC());
    //  assertEquals(0, myTestCPU.cc.getV());

        // Logical Shift Left of 0xB8
        myTestCPU.cc.setC(0);
        myTestCPU.cc.setV(0);
        myTestCPU.a.set(0xB8);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x70, myTestCPU.a.intValue());
        assertEquals(0x03, myTestCPU.cc.intValue());
    //  assertEquals(1, myTestCPU.cc.getC());
    //  assertEquals(1, myTestCPU.cc.getV());
    }

    @Test
    public void testMUL() {
	myTestCPU.write(0xB00, 0x3D); // Write instruction into memory
	myTestCPU.a.set(0x0C);
	myTestCPU.b.set(0x64);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x04B0, myTestCPU.d.intValue());
	assertEquals(0x04, myTestCPU.a.intValue());
	assertEquals(0xB0, myTestCPU.b.intValue());
    }

    /**
     * Test the NEG - Negate instruction.
     */
    @Test
    public void testNEG() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x40);
	// Negate 0
	myTestCPU.a.set(0);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.getC());

	// Negate 1
	myTestCPU.a.set(1);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xFF, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getC());

	// Negate 2
	myTestCPU.a.set(2);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xFE, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getC());
	assertEquals(0, myTestCPU.cc.getV());

	// Negate 0x80
	myTestCPU.a.set(0x80);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x80, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getC());
	assertEquals(1, myTestCPU.cc.getV());
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
    }

    @Test
    public void testNOP() {
        myTestCPU.write(0xB00, 0x12);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xB01, myTestCPU.pc.intValue());
    }

    /**
     * Test the subtraction instruction.
     */
    @Test
    public void testSUBB() {
	// Test IMMEDIATE mode:   SUBB $202
	//
	// Set register A to the offset
	myTestCPU.b.set(2);
	// Two bytes of instruction
	myTestCPU.write(0xB00, 0xc0);
	myTestCPU.write(0xB01, 179);
        myTestCPU.cc.clear();
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(79, myTestCPU.b.intValue());
	assertEquals(0xB02, myTestCPU.pc.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
	assertEquals(1, myTestCPU.cc.getC());
    }

    //@Test
    public void testSUBBindexed() {
	// Test INDEXED mode:   LDB   A,S where A is negative
	//
	// Set up a word to test at address 0x202
	myTestCPU.write_word(0x202, 0x73ff);
	// Set register A to the offset
	myTestCPU.b.set(0xF2);
	// Set register S to point to that location minus 2
	myTestCPU.s.set(0x210);
	// Two bytes of instruction
	myTestCPU.write(0xB00, 0xE0);
	myTestCPU.write(0xB01, 0xE6);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x210, myTestCPU.s.intValue());
	assertEquals(0x73, myTestCPU.b.intValue());
	assertEquals(0xB02, myTestCPU.pc.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

    /**
     * Test the subtraction instruction for D.
     * We subtract the content of the DP + 0
     */
    @Test
    public void testSUBD() {
	myTestCPU.a.set(0x77);
	myTestCPU.b.set(0x02);
	myTestCPU.dp.set(0x02);
        myTestCPU.write(0x0200, 0x01);
        myTestCPU.write(0x0201, 0x23);
	// Two bytes of instruction
	myTestCPU.write(0xB00, 0x93); //SUBD
	myTestCPU.write(0xB01, 0x00); // Direct page addr + 0
        myTestCPU.cc.clear();
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x75, myTestCPU.a.intValue());
	assertEquals(0xDF, myTestCPU.b.intValue());
	assertEquals(0x75DF, myTestCPU.d.intValue());
	assertEquals(0xB02, myTestCPU.pc.intValue());
    }

    /**
     * Test SWI3.
     */
    @Test
    public void testSWI3() {
        myTestCPU.write_word(0xfff2, 0x0300);
	myTestCPU.s.set(0x1000);
	myTestCPU.write(0xB00, 0x11);
	myTestCPU.write(0xB01, 0x3F);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x0300, myTestCPU.pc.intValue());
        assertEquals(0x1000 - 12, myTestCPU.s.intValue());
	assertTrue(myTestCPU.cc.isSetE());
    }

    @Test
    public void testTFR_d_y() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x1F);
	myTestCPU.write(0xB01, 0x02);
	myTestCPU.cc.clear();
	myTestCPU.d.set(0xABBA);
	myTestCPU.y.set(0x0101);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xB02, myTestCPU.pc.intValue());
	assertEquals(0xABBA, myTestCPU.d.intValue());
	assertEquals(0xABBA, myTestCPU.y.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

    @Test
    public void testTFR_s_pc() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x1F);
	myTestCPU.write(0xB01, 0x45);
	myTestCPU.cc.clear();
	myTestCPU.s.set(0x1BB1);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x1BB1, myTestCPU.pc.intValue());
	assertEquals(0x1BB1, myTestCPU.s.intValue());
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

    @Test
    public void testTFR_dp_cc() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x1F);
	myTestCPU.write(0xB01, 0xBA);
	myTestCPU.cc.clear();
	myTestCPU.dp.set(0x1B);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x1B, myTestCPU.dp.intValue());
	assertEquals(0x1B, myTestCPU.cc.intValue());
	assertEquals(0, myTestCPU.cc.getH());
	assertEquals(1, myTestCPU.cc.getI());
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(1, myTestCPU.cc.getV());
	assertEquals(1, myTestCPU.cc.getC());
    }

    /**
     * Test the instruction TST.
    /**
     * Test the instruction TST.
     * TST: The Z and N bits are affected according to the value
     * of the specified operand. The V bit is cleared.
     */
    @Test
    public void testTSTmemory() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x4D);
	// Test a = 0xff
	myTestCPU.cc.clear();
	myTestCPU.a.set(0xff);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0xff, myTestCPU.a.intValue());
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

    @Test
    public void testTSTacc01() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x4D);
	//  Test a = 0x01 and V set
	myTestCPU.cc.clear();
	myTestCPU.cc.setV(1);
	myTestCPU.a.set(0x01);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0x01, myTestCPU.a.intValue());
	assertEquals(0, myTestCPU.cc.intValue());
        assertEquals(0, myTestCPU.cc.getZ());
        assertEquals(0, myTestCPU.cc.getV());
    }

    @Test
    public void testTSTacc00() {
	// Write instruction into memory
	myTestCPU.write(0xB00, 0x4D);
	// Test a = 0x00
	myTestCPU.cc.clear();
	myTestCPU.a.set(0x00);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(0, myTestCPU.cc.getN());
	assertEquals(1, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

    @Test
    public void testTSTindirect() {
	// Test indirect mode: TST ,Y
	// Set up a byte to test at address 0x205
	myTestCPU.write(0x205, 0xff);
	// Set register Y to point to that location
	myTestCPU.y.set(0x205);
	// Two bytes of instruction
	myTestCPU.write(0xB00, 0x6d);
	myTestCPU.write(0xB01, 0xa4);
        myTestCPU.pc.set(0xB00);
	myTestCPU.execute();
	assertEquals(1, myTestCPU.cc.getN());
	assertEquals(0, myTestCPU.cc.getZ());
	assertEquals(0, myTestCPU.cc.getV());
    }

}
