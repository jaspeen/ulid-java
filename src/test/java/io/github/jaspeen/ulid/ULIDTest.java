package io.github.jaspeen.ulid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static io.github.jaspeen.ulid.ULID.BIN_LENGTH;
import static io.github.jaspeen.ulid.ULID.ENTROPY_LENGTH;
import static org.junit.jupiter.api.Assertions.*;

class ULIDTest {
    static class Fixture {
        long time;
        byte[] entropy;
        String strValue;
        String normalized;
        byte[] binValue;

        public Fixture(long time, byte[] entropy, String strValue, String normalized, byte[] binValue) {
            this.time = time;
            this.entropy = entropy;
            this.strValue = strValue;
            this.binValue = binValue;
            this.normalized = normalized;
        }

        @Override public String toString() {
            return strValue;
        }
    }

    static Fixture f(long time, byte[] entropy, String strValue, String normalized, byte[] binValue) {
        return new Fixture(time, entropy, strValue, normalized == null ? strValue : normalized, binValue);
    }

    static byte[] allSame(int size, byte val) {
        byte[] res = new byte[size];
        Arrays.fill(res, val);
        return res;
    }

    static byte[] subArray(byte[] arr, int start, int len) {
        byte[] res = new byte[len];
        System.arraycopy(arr, start, res, 0, len);
        return res;
    }
    static Stream<Fixture>  testData() {
        byte[] bytesFor1 = new byte[] {33, 8, 66, 16, -124, 33, 8, 66, 16, -124, 33, 8, 66, 16, -124, 33};
        byte[] byteForSeq1 = new byte[] {1, 16, -56, 83, 29, 9, 82, -40, -41, 62, 17, 12, -90, 26, 84, 22};
        byte[] byteForSeq2 = new byte[] {-9, -58, 117, -66, 119, -33, 0, 68, 50, 20, -57, 66, 64, -91, -79, -82};
        String norm1 = "11111111111111111111111111";
        String norm0 = "00000000000000000000000000";

        return Stream.of(
                f(36319351833633L, subArray(bytesFor1, 6, ENTROPY_LENGTH), "LLLLLLLLLLLLLLLLLLLLLLLLLL", norm1, bytesFor1),
                f(36319351833633L, subArray(bytesFor1, 6, ENTROPY_LENGTH), "IIIIIIIIIIIIIIIIIIIIIIIIII", norm1, bytesFor1),
                f(36319351833633L, subArray(bytesFor1, 6, ENTROPY_LENGTH), "llllllllllllllllllllllllll", norm1, bytesFor1),
                f(36319351833633L, subArray(bytesFor1, 6, ENTROPY_LENGTH), "iiiiiiiiiiiiiiiiiiiiiiiiii", norm1, bytesFor1),
                f(36319351833633L, subArray(bytesFor1, 6, ENTROPY_LENGTH), "11111111111111111111111111", null, bytesFor1),
                f(1103823438081L, allSame(ENTROPY_LENGTH, (byte)1), "01040G2081040G2081040G2081", null, allSame(BIN_LENGTH, (byte)1)),
                f(0L, allSame(ENTROPY_LENGTH, (byte)0), "OOOOOOOOOOOOOOOOOOOOOOOOOO", norm0, allSame(BIN_LENGTH, (byte)0)),
                f(0L, allSame(ENTROPY_LENGTH, (byte)0), "oooooooooooooooooooooooooo", norm0, allSame(BIN_LENGTH, (byte)0)),
                f(0L, allSame(ENTROPY_LENGTH, (byte)0), "00000000000000000000000000", norm0, allSame(BIN_LENGTH, (byte)0)),
                f(0L, allSame(ENTROPY_LENGTH, (byte)0), "oooooo00OOoo000oooooo00ooo", norm0, allSame(BIN_LENGTH, (byte)0)),
                f(281474976710655L, allSame(ENTROPY_LENGTH, (byte)-1), "7ZZZZZZZZZZZZZZZZZZZZZZZZZ", null, allSame(BIN_LENGTH, (byte)-1)),
                f(140185576636287L, allSame(ENTROPY_LENGTH, (byte)127), "3ZFXZQYZVZFXZQYZVZFXZQYZVZ", null, allSame(BIN_LENGTH, (byte)127)),
                f(141289400074368L, allSame(ENTROPY_LENGTH, (byte)-128), "40G2081040G2081040G2081040", null, allSame(BIN_LENGTH, (byte)-128)),
                f(1171591994633L, subArray(byteForSeq1, 6, ENTROPY_LENGTH), "0123456789abcdefghijklmnop", "0123456789ABCDEFGH1JK1MN0P", byteForSeq1),
                f(272431751002079L, subArray(byteForSeq2, 6, ENTROPY_LENGTH), "7qrstvwxyz01234567890abcde", "7QRSTVWXYZ01234567890ABCDE", byteForSeq2)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void fromString(Fixture fix) {
        ULID ulid = ULID.fromString(fix.strValue);
        assertEquals(fix.normalized, ulid.toString());
        assertArrayEquals(fix.binValue, ulid.toBytes());
        assertEquals(fix.time, ulid.getTimestamp());
        assertArrayEquals(fix.entropy, ulid.getEntropy());
    }

    @Test
    void fromStringInvalidLength() {
        assertThrows(NullPointerException.class, () ->{
            ULID.fromString(null);
        });
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromString("");
        });
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromString("0123456789012345678901234");
        });
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromString("012345678901234567890123456");
        });
    }

    @Test
    void fromStringInvalidChars() {
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromString("uU:!;,[]()%$@`~&*(+_<>/:'{");
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void fromBytes(Fixture fix) {
        ULID ulid = ULID.fromBytes(fix.binValue);
        assertEquals(fix.normalized, ulid.toString());
        assertArrayEquals(fix.binValue, ulid.toBytes());
        assertEquals(fix.time, ulid.getTimestamp());
        assertArrayEquals(fix.entropy, ulid.getEntropy());
    }

    @Test
    void fromBytesInvalid() {
        assertThrows(NullPointerException.class, () ->{
            ULID.fromBytes(null);
        });

        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromBytes(new byte[0]);
        });
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromBytes(new byte[15]);
        });
        assertThrows(IllegalArgumentException.class, () ->{
            ULID.fromBytes(new byte[17]);
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void generate(Fixture fix) {
        ULID ulid = ULID.generate(fix.time, fix.entropy);
        assertEquals(fix.normalized, ulid.toString());
        assertArrayEquals(fix.binValue, ulid.toBytes());
        assertEquals(fix.time, ulid.getTimestamp());
        assertArrayEquals(fix.entropy, ulid.getEntropy());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void uuid(Fixture fix) {
        ULID p = ULID.fromString(fix.strValue);
        UUID uuid = p.toUUID();
        ULID ulid = ULID.fromUUID(uuid);
        assertEquals(p, ulid);
        assertEquals(fix.normalized, ulid.toString());
        assertArrayEquals(fix.binValue, ulid.toBytes());
        assertEquals(fix.time, ulid.getTimestamp());
        assertArrayEquals(fix.entropy, ulid.getEntropy());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void equalsAndHashCode(Fixture fix) {
        ULID ulid1 = ULID.fromString(fix.strValue);
        ULID ulid2 = ULID.fromString(fix.normalized);
        ULID ulid3 = ULID.random();

        assertEquals(ulid1, ulid2);
        assertEquals(ulid1.hashCode(), ulid2.hashCode());
        assertNotEquals(ulid1, ulid3);
    }

    @Test
    void compareTo() throws Exception {
        ULID u1 = ULID.random();
        Thread.sleep(10);
        ULID u2 = ULID.random();
        ULID u3 = ULID.fromString(u1.toString());

        assertEquals(u1.compareTo(u2), -1);
        assertEquals(u2.compareTo(u1), 1);
        assertEquals(u1.compareTo(u3), 0);
        assertEquals(u3.compareTo(u1), 0);
    }

    @Test
    void customRandom() {
        ULID zeroEntropyUlid = ULID.random(new Random() {
            @Override public void nextBytes(byte[] bytes) {
                Arrays.fill(bytes, (byte)0);
            }
        });

        assertArrayEquals(new byte[ENTROPY_LENGTH], zeroEntropyUlid.getEntropy());
    }

    @Test
    void timestamp() {
        ULID ulid = ULID.random();
        // within 10 sec
        assertTrue(Math.abs(ulid.getTimestamp() - System.currentTimeMillis()) < 10000);
    }

    /*@ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void testPrint(Fixture fix) {
        System.out.println("In:   " + fix.strValue);
        System.out.println("Par:  " + ULID.fromString(fix.strValue));
        long msb = ULID.fromString(fix.strValue).getMsb();
        System.out.println(msb);
        System.out.println(msb >>> 16);
        System.out.println(Long.toBinaryString(msb));
        System.out.println(Long.toBinaryString(msb >>> 16));
        System.out.println("Par1: " + ULID.fromBytes(fix.binValue));
        System.out.println("Parz: " + Long.toBinaryString(ULID.fromBytes(fix.binValue).getMsb()) + " " + Long.toBinaryString(ULID.fromBytes(fix.binValue).getLsb()));
        System.out.println("InB:  " + Arrays.toString(fix.binValue));
        System.out.println("ParB: " + Arrays.toString(ULID.fromString(fix.strValue).toBytes()));
        System.out.println("Par1B:" + Arrays.toString(ULID.fromBytes(fix.binValue).toBytes()));
    }*/
}