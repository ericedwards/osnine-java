package org.roug.osnine.rbf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class DiskTest {

    private Disk disk;

    @Before
    public void mapDisk() throws Exception {
        String resourcefile = DiskTest.class.getClassLoader().getResource("OS9-SYSTEM.dsk").getFile();
        disk = new Disk(resourcefile);
        assertNotNull(disk);
    }

    /**
     * Get the root LSN of the disk.
     */
    @Test
    public void rootLSN() throws Exception {
        assertEquals(0x12, disk.getRootLSN());
    }

    /**
     * Get the FileDescriptor of the root directory.
     */
    @Test
    public void getFDForRoot() throws Exception {
        FileDescriptor fd = new FileDescriptor();
        fd = disk.readFileDescriptor(0x12);
        assertEquals(0xBF, fd.getAttributes());
        assertEquals(0x140, fd.getSize());
    }

    /**
     * Get the LSN of the CMDS directory.
     */
    @Test
    public void getLSNforCMDS() throws Exception {
        assertEquals(0x4E, disk.getLSNForPath("CMDS"));
    }

    /**
     * Get the LSN of the top directory.
     */
    @Test
    public void getLSNforEmpty() throws Exception {
        assertEquals(0x12, disk.getLSNForPath(""));
    }

    /**
     * Get the LSN of the top directory.
     */
    @Test
    public void getLSNforSlash() throws Exception {
        assertEquals(0x12, disk.getLSNForPath("/"));
    }

    /**
     * Get the LSN for some positions in the CMDS directory.
     * The directory is 0x06A0 bytes long.
     */
    @Test
    public void getLSNforPosition() {
        FileDescriptor fd = disk.readFileDescriptor(0x4E);
        //PathDescriptor pathDesc = new PathDescriptor(disk, fd, 0x80);
        int p = fd.getLSNFromPosition(0);
        assertEquals(0x4F, p);

        p = fd.getLSNFromPosition(0x200);
        assertEquals(0x51, p);

        p = fd.getLSNFromPosition(0x700);
        assertEquals(-1, p);
    }
}
