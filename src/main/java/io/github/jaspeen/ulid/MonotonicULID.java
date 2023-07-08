package io.github.jaspeen.ulid;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Monotonic version of ULID generator.
 * <p>
 * When generating a ULID within the same millisecond
 * the random component is incremented by 1 bit in the least significant bit position (with carrying)
 * <p>
 * <i>This implementation is synchronized besides locks in Random implementation</i>
 * <p>
 * Usage:
 * <p>
 * <pre>
 *     ULID ulid = MonotonicULID.random();
 * </pre>
 * @see <a href="https://github.com/ulid/spec#monotonicity">ULID monotonicity</a>
 */
public class MonotonicULID {
    private final Random random;
    private long lastTime = 0L;
    private final byte[] lastEntropy = new byte[ULID.ENTROPY_LENGTH];

    public MonotonicULID(Random random) {
        this.random = random;
    }

    public synchronized ULID next() {
        long now = System.currentTimeMillis();
        if (lastTime == now) {
            // Entropy is big-endian (network byte order) per ULID spec
            // Increment last entropy by 1
            boolean carry = true;
            for (int i = ULID.ENTROPY_LENGTH - 1; i >= 0; i--) {
                if (carry) {
                    byte work = this.lastEntropy[i];
                    work = (byte) (work + 0x01);
                    carry = this.lastEntropy[i] == (byte) 0xff && carry;
                    this.lastEntropy[i] = work;
                }
            }
            // Last byte has carry over
            if (carry) {
                // Throw error if entropy overflows in same millisecond per ULID spec
                throw new IllegalStateException("ULID entropy overflowed for same millisecond");
            }
        } else {
            this.lastTime = now;
            this.random.nextBytes(this.lastEntropy);
        }
        return ULID.generate(now, this.lastEntropy);
    }

    public static MonotonicULID DEFAULT = new MonotonicULID(new SecureRandom());

    public static ULID random() {
        return DEFAULT.next();
    }
}
