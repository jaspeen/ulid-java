package io.github.jaspeen.ulid.hibernate;

import io.github.jaspeen.ulid.ULID;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.util.Properties;
import java.util.UUID;

/**
 * Hibernate ID generator using {@link ULID} generation.
 * <p>
 * Supports {@link ULID}, {@link UUID}, String, and byte[] field types.
 * <p>
 * Usage:
 * <pre>
 *    &#64;Entity
 *    class ULIDEntity {
 *        &#64;Id
 *        &#64;GeneratedValue(generator = "ulid")
 *        &#64;GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
 *        private ULID id;
 *    }
 *
 *    &#64;Entity
 *    class UUIDEntity {
 *        &#64;Id
 *        &#64;GeneratedValue(generator = "ulid")
 *        &#64;GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
 *        private UUID id;
 *    }
 * </pre>
 */
public class ULIDIdGenerator implements IdentifierGenerator {

    private ULIDTypeDescriptor.ValueTransformer valueTransformer;

    @Override public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws
                                                                                                   MappingException {
        if (ULID.class.isAssignableFrom(type.getReturnedClass())) {
            valueTransformer = ULIDTypeDescriptor.PassThroughTransformer.INSTANCE;
        } else if (UUID.class.isAssignableFrom(type.getReturnedClass())) {
            valueTransformer = ULIDTypeDescriptor.ToUUIDTransformer.INSTANCE;
        } else if (String.class.isAssignableFrom(type.getReturnedClass())) {
            valueTransformer = ULIDTypeDescriptor.ToStringTransformer.INSTANCE;
        } else if (byte[].class.isAssignableFrom(type.getReturnedClass())) {
            valueTransformer = ULIDTypeDescriptor.ToBytesTransformer.INSTANCE;
        } else {
            throw new HibernateException(
                    "Unanticipated return type [" + type.getReturnedClass().getName() + "] for ULID conversion");
        }
    }

    @Override public Object generate(SharedSessionContractImplementor session, Object object) throws
                                                                                                    HibernateException {
        Object id = session.getEntityPersister(null, object)
                .getClassMetadata().getIdentifier(object, session);
        if (id != null) {
            return id;
        }
        ULID val = ULID.random();
        return valueTransformer.transform(val);
    }
}
