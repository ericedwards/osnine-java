package org.roug.osnine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MemorySegmentTest {

    /**
     * 
     */
    @Test
    public void writeTop2() {
        MC6809 cpu = new MC6809();
        cpu.write(0xffff, 1);
        assertEquals(1, cpu.read(0xffff));
    }

    @Test
    public void oneExtraMemoryBank() {
        MC6809 cpu = new MC6809(8192);
        cpu.write(0xffff, 1);
        assertEquals(1, cpu.read(0xffff));

        cpu.write(0x0400, 0x34);
        assertEquals(0x34, cpu.read(0x0400));
    }

    @Test
    public void with6850() {
        MC6809 cpu = new MC6809(8192);
        cpu.write(0xffff, 1);
        assertEquals(1, cpu.read(0xffff));

        cpu.write(0x0400, 0x34);
        assertEquals(0x34, cpu.read(0x0400));

        MC6850 uart = new MC6850(0xb000);
        cpu.addMemorySegment(uart);
        for (int i = 0; i < 60; i++) {
            cpu.write(0xb000, 0x40);
        }
        cpu.write(0xb000, 0x0a);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oversizeMemoryBank() {
        MemoryBank mb = new MemoryBank(70000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalRead() {
        MemoryBank mb = new MemoryBank(10000, 1024);
        mb.write(900, 65);
    }

}
