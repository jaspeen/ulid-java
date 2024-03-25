package io.github.jaspeen.ulid.hibernate;


import java.io.Serializable;
import java.sql.Types;
import java.util.UUID;

import io.github.jaspeen.ulid.ULID;
import org.hibernate.internal.util.BytesHelper;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

/**
 * Descriptor for {@link ULID} handling.
 */
public class ULIDTypeDescriptor extends AbstractJavaType<ULID> {
    public static final ULIDTypeDescriptor
            INSTANCE = new ULIDTypeDescriptor();

    public ULIDTypeDescriptor() {
        super(ULID.class);
    }

    public String toString(ULID value) {
        return ULIDTypeDescriptor.ToStringTransformer.INSTANCE.transform(value);
    }

    public ULID fromString(String string) {
        return ULIDTypeDescriptor.ToStringTransformer.INSTANCE.parse(string);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return indicators.getTypeConfiguration().getJdbcTypeRegistry().getDescriptor(Types.VARCHAR);
    }

    @SuppressWarnings({"unchecked"})
    public <X> X unwrap(ULID value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (ULID.class.isAssignableFrom(type)) {
            return (X) ULIDTypeDescriptor.PassThroughTransformer.INSTANCE.transform(value);
        }
        if (UUID.class.isAssignableFrom(type)) {
            return (X) ULIDTypeDescriptor.ToUUIDTransformer.INSTANCE.transform(value);
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) ULIDTypeDescriptor.ToStringTransformer.INSTANCE.transform(value);
        }
        if (byte[].class.isAssignableFrom(type)) {
            return (X) ULIDTypeDescriptor.ToBytesTransformer.INSTANCE.transform(value);
        }
        throw unknownUnwrap(type);
    }

    public <X> ULID wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (value instanceof ULID) {
            return ULIDTypeDescriptor.PassThroughTransformer.INSTANCE.parse(value);
        }
        if (value instanceof UUID) {
            return ULIDTypeDescriptor.ToUUIDTransformer.INSTANCE.parse(value);
        }
        if (value instanceof String) {
            return ULIDTypeDescriptor.ToStringTransformer.INSTANCE.parse(value);
        }
        if (value instanceof byte[]) {
            return ULIDTypeDescriptor.ToBytesTransformer.INSTANCE.parse(value);
        }
        throw unknownWrap(value.getClass());
    }

    public interface ValueTransformer {
        Serializable transform(ULID ulid);

        ULID parse(Object value);
    }

    public static class PassThroughTransformer implements
                                               ValueTransformer {
        public static final PassThroughTransformer
                INSTANCE = new PassThroughTransformer();

        public ULID transform(ULID ulid) {
            return ulid;
        }

        public ULID parse(Object value) {
            return (ULID) value;
        }
    }

    public static class ToUUIDTransformer implements
                                          ValueTransformer {
        public static final ToUUIDTransformer
                INSTANCE = new ToUUIDTransformer();

        public UUID transform(ULID ulid) {
            return ulid.toUUID();
        }

        public ULID parse(Object value) {
            return ULID.fromUUID((UUID) value);
        }
    }

    public static class ToStringTransformer implements
                                            ULIDTypeDescriptor.ValueTransformer {
        public static final ULIDTypeDescriptor.ToStringTransformer
                INSTANCE = new ULIDTypeDescriptor.ToStringTransformer();

        public String transform(ULID ulid) {
            return ulid.toString();
        }

        public ULID parse(Object value) {
            return ULID.fromString((String) value);
        }
    }

    public static class ToBytesTransformer implements
                                           ULIDTypeDescriptor.ValueTransformer {
        public static final ULIDTypeDescriptor.ToBytesTransformer
                INSTANCE = new ULIDTypeDescriptor.ToBytesTransformer();

        public byte[] transform(ULID ulid) {
            byte[] bytes = new byte[16];
            BytesHelper.fromLong(ulid.getMsb(), bytes, 0);
            BytesHelper.fromLong(ulid.getLsb(), bytes, 8);
            return bytes;
        }

        public ULID parse(Object value) {
            byte[] bytea = (byte[]) value;
            return new ULID(BytesHelper.asLong(bytea, 0), BytesHelper.asLong(bytea, 8));
        }
    }
}
