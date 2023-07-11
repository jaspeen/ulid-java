# ulid-java
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jaspeen/ulid-java)](https://central.sonatype.com/artifact/io.github.jaspeen/ulid-java)
[![javadoc](https://javadoc.io/badge2/io.github.jaspeen/ulid-java/javadoc.svg)](https://javadoc.io/doc/io.github.jaspeen/ulid-java)

Generate and parse ULIDs in Crockford base32 text and binary representations.

See [ULID specification](https://github.com/ulid/spec) for more info

## Key points
* Java 8+
* API similar to java.util.UUID
* Optional monotonic generator
* Optional hibernate type and ID generator

## Install
### Maven
```xml
<dependency>
    <groupId>io.github.jaspeen</groupId>
    <artifactId>ulid-java</artifactId>
    <version>0.0.3</version>
</dependency>
```
### Gradle
```groovy
dependencies {
    implementation 'io.github.jaspeen:ulid-java:0.0.3'
}
```

## Usage

### ULID generation
```java
ULID ulid = ULID.random();
String crockfordBase32 = ulid.toString();
byte[] binary = ulid.toBytes();
```

### Parsing
```java
ULID parsedFromString = ULID.fromString("3ZFXZQYZVZFXZQYZVZFXZQYZVZ");
ULID parsedFromBytes = ULID.fromBytes(
        new byte[] {127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127});
assertEquals(parsedFromString, parsedFromBytes);
```

### UUID compatibility
```java
ULID.random().toUUID();
ULID.fromUUID(UUID.randomUUID());
```

### Monotonic ULID generation
```java
MonotonicULID.random();
```

### Hibernate ID generator
Hibernate is not added as transitive dependency, it should be specified additionally
```java
@Entity
class ULIDEntity {
    @Id
    @GeneratedValue(generator = "ulid")
    @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
    private ULID id;
}
 
 // This will generate UUID using ULID algorithm providing ordered keys 
 // while keeping other stuff same
@Entity 
class UUIDEntity {
    @Id
    @GeneratedValue(generator = "ulid")
    @GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
    private UUID id;
}
```
Generator can be defined in package-info.java for all entities instead of field annotation in every entity
```java
@GenericGenerator(name = "ulid", strategy = "io.github.jaspeen.ulid.hibernate.ULIDIdGenerator")
package my.service.model;

import org.hibernate.annotations.GenericGenerator;
```