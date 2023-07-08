package io.github.jaspeen.ulid;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Universally Unique Lexicographically Sortable Identifier
 * <p>
 * Binary compatible with UUID.
 * <p>
 * Can be used instead of {@link java.util.UUID} or only to generate {@link java.util.UUID} objects.
 * <p>
 * <b>Usage:</b>
 * <p>
 * <i>Generate random</i>
 * <pre>
 *     ULID ulid = ULID.random();
 *     ulid.toString(); // Crockford Base32 encoded string
 *     ulid.toBytes(); // 16 bytes binary representation
 * </pre>
 * <i>Parse</i>
 * <pre>
 *     ULID.fromString("3ZFXZQYZVZFXZQYZVZFXZQYZVZ")
 *     ULID.fromBytes(new byte[16])
 * </pre>
 * <i>UUID generation</i>
 * <pre>
 *     ULID.random().toUUID();
 *     // can be useful for compatibility
 *     ULID.fromUUID(UUID.randomUUID());
 * </pre>
 * @see <a href="https://github.com/ulid/spec">ULID spec</a>
 */
public class ULID implements Serializable, Comparable<ULID>{
    /**
     * Character encoded value length
     */
    public static final int STR_LENGTH = 26;

    /**
     * Binary length
     */
    public static final int BIN_LENGTH = 16;

    /**
     * Entropy length
     */
    public static final int ENTROPY_LENGTH = 10;

    /**
     * Minimum allowed timestamp value.
     */
    public static final long MIN_TIME = 0x0L;

    /**
     * Maximum allowed timestamp value. Encoded value can encode up to 0x0003ffffffffffffL but ULID
     * binary/byte representation states that timestamp will only be 48-bits.
     */
    public static final long MAX_TIME = 0x0000ffffffffffffL;

    /**
     * Crockford Base32 characters mapping
     */
    private static final char[] C = new char[]{ //
            // 0  1     2     3     4     5     6     7     //
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, //
            // 8  9     A     B     C     D     E     F     //
            0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, //
            // G  H     J     K     M     N     P     Q     //
            0x47, 0x48, 0x4a, 0x4b, 0x4d, 0x4e, 0x50, 0x51, //
            // R  S     T     V     W     X     Y     Z     //
            0x52, 0x53, 0x54, 0x56, 0x57, 0x58, 0x59, 0x5a  //
    };

    private static final byte[] V = new byte[]{ //
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 3
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 7
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 11
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 15
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 19
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 23
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 27
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 31
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 35
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 39
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 43
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 47
         (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, // 51
         (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, // 55
         (byte) 0x08, (byte) 0x09, (byte) 0xff, (byte) 0xff, // 59
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 63
         (byte) 0xff, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, // 67
         (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10, // 71
         (byte) 0x11, (byte) 0x01, (byte) 0x12, (byte) 0x13, // 75
         (byte) 0x01, (byte) 0x14, (byte) 0x15, (byte) 0x00, // 79
         (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, // 83
         (byte) 0x1a, (byte) 0xff, (byte) 0x1b, (byte) 0x1c, // 87
         (byte) 0x1d, (byte) 0x1e, (byte) 0x1f, (byte) 0xff, // 91
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, // 95
         (byte) 0xff, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, // 99
         (byte) 0x0d, (byte) 0x0e, (byte) 0x0f, (byte) 0x10, // 103
         (byte) 0x11, (byte) 0x01, (byte) 0x12, (byte) 0x13, // 107
         (byte) 0x01, (byte) 0x14, (byte) 0x15, (byte) 0x00, // 111
         (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, // 115
         (byte) 0x1a, (byte) 0xff, (byte) 0x1b, (byte) 0x1c, // 119
         (byte) 0x1d, (byte) 0x1e, (byte) 0x1f, (byte) 0xff, // 123
         (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};// 127

    /**
     * * <pre>
     * 0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++++++++
     * |                      32_bit_uint_time_high                    |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  msb
     * |     16_bit_uint_time_low      |       16_bit_uint_random      |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++++++++
     * |                       32_bit_uint_random                      |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  lsb
     * |                       32_bit_uint_random                      |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++++++++
     * </pre>
     */

    private final long msb;
    private final long lsb;

    public ULID(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Returns 64 most significant bits of 128bit binary representation.
     */
    public long getMsb() {
        return msb;
    }

    /**
     * Returns 64 least significant bits of 128bit binary representation.
     */
    public long getLsb() {
        return lsb;
    }

    /**
     * Convert to UUID as direct byte copy.
     */
    public UUID toUUID() {
        return new UUID(msb, lsb);
    }

    /**
     * Returns a ULID as Crockford's base32 encoded string (26 characters).
     */
    @Override
    public String toString() {
        char[] chars = new char[26];

        // time
        chars[0] = C[(byte) (((msb >>> 56 & 0xff) >>> 5) & 0x1f)];
        chars[1] = C[(byte) (msb >>> 56 & 0x1f)];
        chars[2] = C[(byte) ((msb >>> 48 & 0xff) >>> 3)];
        chars[3] = C[(byte) ((((msb >>> 48 & 0xff) << 2) | ((msb >>> 40 & 0xff) >>> 6)) & 0x1f)];
        chars[4] = C[(byte) (((msb >>> 40 & 0xff) >>> 1) & 0x1f)];
        chars[5] = C[(byte) ((((msb >>> 40 & 0xff) << 4) | ((msb >>> 32 & 0xff) >>> 4)) & 0x1f)];
        chars[6] = C[(byte) ((((msb >>> 32 & 0xff) << 1) | ((msb >>> 24 & 0xff) >>> 7)) & 0x1f)];
        chars[7] = C[(byte) (((msb >>> 24 & 0xff) >>> 2) & 0x1f)];
        chars[8] = C[(byte) ((((msb >>> 24 & 0xff) << 3) | ((msb >>> 16 & 0xff) >>> 5)) & 0x1f)];
        chars[9] = C[(byte) (msb >>> 16 & 0x1f)];

        // entropy
        chars[10] = C[(byte) ((msb >>> 8 & 0xff) >>> 3)];
        chars[11] = C[(byte) ((((msb >>> 8 & 0xff) << 2) | ((msb & 0xff) >>> 6)) & 0x1f)];
        chars[12] = C[(byte) (((msb & 0xff) >>> 1) & 0x1f)];
        chars[13] = C[(byte) ((((msb & 0xff) << 4) | ((lsb >>> 56 & 0xff) >>> 4)) & 0x1f)];
        chars[14] = C[(byte) ((((lsb >>> 56 & 0xff) << 1) | ((lsb >>> 48 & 0xff) >>> 7)) & 0x1f)];
        chars[15] = C[(byte) (((lsb >>> 48 & 0xff) >>> 2) & 0x1f)];
        chars[16] = C[(byte) ((((lsb >>> 48 & 0xff) << 3) | ((lsb >>> 40 & 0xff) >>> 5)) & 0x1f)];
        chars[17] = C[(byte) (lsb >>> 40 & 0x1f)];
        chars[18] = C[(byte) ((lsb >>> 32 & 0xff) >>> 3)];
        chars[19] = C[(byte) ((((lsb >>> 32 & 0xff) << 2) | ((lsb >>> 24 & 0xff) >>> 6)) & 0x1f)];
        chars[20] = C[(byte) (((lsb >>> 24 & 0xff) >>> 1) & 0x1f)];
        chars[21] = C[(byte) ((((lsb >>> 24 & 0xff) << 4) | ((lsb >>> 16 & 0xff) >>> 4)) & 0x1f)];
        chars[22] = C[(byte) ((((lsb >>> 16 & 0xff) << 1) | ((lsb >>> 8 & 0xff) >>> 7)) & 0x1f)];
        chars[23] = C[(byte) (((lsb >>> 8 & 0xff) >>> 2) & 0x1f)];
        chars[24] = C[(byte) ((((lsb >>> 8 & 0xff) << 3) | ((lsb & 0xff) >>> 5)) & 0x1f)];
        chars[25] = C[(byte) (lsb & 0x1f)];

        return new String(chars);
    }

    @Override
    public int hashCode() {
        long hilo = msb ^ lsb;
        return ((int)(hilo >> 32)) ^ (int) hilo;
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != ULID.class))
            return false;
        ULID other = (ULID)obj;
        return msb == other.msb && lsb == other.lsb;
    }

    @Override
    public int compareTo(ULID val) {
        return this.msb < val.msb ? -1 :
                (this.msb > val.msb ? 1 :
                 (Long.compare(this.lsb, val.lsb)));
    }

    /**
     * Returns binary representation of ULID as byte array.
     */
    public byte[] toBytes() {
        return new byte[] {
                (byte) (msb >> 56 & 0xff),
                (byte) (msb >> 48 & 0xff),
                (byte) (msb >> 40 & 0xff),
                (byte) (msb >> 32 & 0xff),
                (byte) (msb >> 24 & 0xff),
                (byte) (msb >> 16 & 0xff),
                (byte) (msb >> 8 & 0xff),
                (byte) (msb & 0xff),
                (byte) (lsb >> 56 & 0xff),
                (byte) (lsb >> 48 & 0xff),
                (byte) (lsb >> 40 & 0xff),
                (byte) (lsb >> 32 & 0xff),
                (byte) (lsb >> 24 & 0xff),
                (byte) (lsb >> 16 & 0xff),
                (byte) (lsb >> 8 & 0xff),
                (byte) (lsb & 0xff)
        };
    }

    /**
     * Returns the timestamp part of the ULID as a long.
     */
    public long getTimestamp() {
        return msb >>> 16;
    }

    /**
     * Returns the entropy part of the ULID as a byte array.
     */
    public byte[] getEntropy() {
        return new byte[]{
            (byte) (msb >> 8 & 0xff),
            (byte) (msb & 0xff),
            (byte) (lsb >> 56 & 0xff),
            (byte) (lsb >> 48 & 0xff),
            (byte) (lsb >> 40 & 0xff),
            (byte) (lsb >> 32 & 0xff),
            (byte) (lsb >> 24 & 0xff),
            (byte) (lsb >> 16 & 0xff),
            (byte) (lsb >> 8 & 0xff),
            (byte) (lsb & 0xff)
        };
    }

    private static long bytesToLong(byte[] src, int offset) {
        return ((long) src[offset] & 0xff) << 56
              | ((long) src[offset + 1] & 0xff) << 48
              | ((long) src[offset + 2] & 0xff) << 40
              | ((long) src[offset + 3] & 0xff) << 32
              | ((long) src[offset + 4] & 0xff) << 24
              | ((long) src[offset + 5] & 0xff) << 16
              | ((long) src[offset + 6] & 0xff) << 8
              | ((long) src[offset + 7] & 0xff);
    }


    // factory methods

    /**
     * Generates random ULID
     */
    public static ULID random() {
        return random(ThreadLocalRandom.current());
    }

    /**
     * Generates random ULID with custom random generator
     * <p>
     * Example:
     * <pre>
     *     ULID.random(ThreadLocalRandom.current());
     * </pre>
     */
    public static ULID random(Random random) {
        byte[] entropy = new byte[ENTROPY_LENGTH];
        random.nextBytes(entropy);
        return noCheckGenerate(System.currentTimeMillis(), entropy);
    }

    /**
     * Generates ULID from raw timestamp and entropy
     * @param time 48-bit timestamp
     * @param entropy 80-bit random data
     */
    public static ULID generate(long time, byte[] entropy) {
        if (time < MIN_TIME || time > MAX_TIME) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        if (entropy == null || entropy.length != 10) {
            throw new IllegalArgumentException("Invalid entropy");
        }
        return noCheckGenerate(time, entropy);
    }
    private static ULID noCheckGenerate(long time, byte[] entropy) {
        long msb = time << 16 | ((entropy[0] & 0xff) << 8) | (entropy[1] & 0xff);
        long lsb = bytesToLong(entropy, 2);
        return new ULID(msb, lsb);
    }

    private static byte valOrFail(char c) {
        byte res;
        if (c > 122 || (res = V[c]) == (byte)0xff) {
            throw new IllegalArgumentException("Invalid ULID char " + c);
        }
        return res;
    }

    /**
     * Parse ULID from string representation
     * @param val 26-character string of Crockford Base32
     */
    public static ULID fromString(String val) {
        if (val.length() != STR_LENGTH) {
            throw new IllegalArgumentException("Invalid ULID string");
        }
        // validate and preprocess chars
        byte[] in = new byte[] {
                valOrFail(val.charAt(0)),
                valOrFail(val.charAt(1)),
                valOrFail(val.charAt(2)),
                valOrFail(val.charAt(3)),
                valOrFail(val.charAt(4)),
                valOrFail(val.charAt(5)),
                valOrFail(val.charAt(6)),
                valOrFail(val.charAt(7)),
                valOrFail(val.charAt(8)),
                valOrFail(val.charAt(9)),
                valOrFail(val.charAt(10)),
                valOrFail(val.charAt(11)),
                valOrFail(val.charAt(12)),
                valOrFail(val.charAt(13)),
                valOrFail(val.charAt(14)),
                valOrFail(val.charAt(15)),
                valOrFail(val.charAt(16)),
                valOrFail(val.charAt(17)),
                valOrFail(val.charAt(18)),
                valOrFail(val.charAt(19)),
                valOrFail(val.charAt(20)),
                valOrFail(val.charAt(21)),
                valOrFail(val.charAt(22)),
                valOrFail(val.charAt(23)),
                valOrFail(val.charAt(24)),
                valOrFail(val.charAt(25))
        };

        // Timestamp
        long msb = (long) ((in[0] << 5) | in[1]) << 56
                   | (long) ((in[2]<< 3) | (in[3] & 0xff) >>> 2) << 48
                   | (long) ((in[3]<< 6) | in[4]<< 1 | (in[5]& 0xff) >>> 4) << 40
                   | (long) ((in[5]<< 4) | (in[6]& 0xff) >>> 1) << 32
                   | (long) ((in[6]<< 7) | in[7]<< 2 | (in[8]& 0xff) >>> 3) << 24
                   | (long) ((in[8]<< 5)  | in[9]) << 16
        // Entropy
                   | (long) ((in[10]<< 3) | (in[11]& 0xff) >>> 2) << 8
                   | ((in[11]<< 6) | in[12]<< 1 | (in[13]& 0xff) >>> 4);

        long lsb = (long) ((in[13]<< 4) | (in[14]& 0xff) >>> 1) << 56
                   | (long) ((in[14]<< 7) | in[15]<< 2 | (in[16]& 0xff) >>> 3) << 48
                   | (long) ((in[16]<< 5) | in[17]) << 40
                   | (long) ((in[18]<< 3) | (in[19]& 0xff) >>> 2) << 32
                   | (long) ((in[19]<< 6) | in[20]<< 1 | (in[21]& 0xff) >>> 4) << 24
                   | (long) ((in[21]<< 4) | (in[22]& 0xff) >>> 1) << 16
                   | (long) ((in[22]<< 7) | in[23]<< 2 | (in[24]& 0xff) >>> 3) << 8
                   | ((in[24]<< 5) | in[25]);

        return new ULID(msb, lsb);
    }

    /**
     * Construct ULID from raw bytes
     * @param v 16 bytes binary data
     */
    public static ULID fromBytes(byte[] v) {
        if (v.length != BIN_LENGTH) {
            throw new IllegalArgumentException("Invalid ULID bytes length: " + v.length);
        }
        long msb = bytesToLong(v, 0);
        long lsb = bytesToLong(v, 8);
        return new ULID(msb, lsb);
    }

    /**
     * Construct ULID from UUID by direct bytes copy
     */
    public static ULID fromUUID(UUID val) {
        return new ULID(val.getMostSignificantBits(), val.getLeastSignificantBits());
    }
}
