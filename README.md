# ulid-java

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
...
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
...
<dependency>
    <groupId>com.github.jaspeen</groupId>
    <artifactId>ulid-java</artifactId>
    <version>92935f63c6</version>
</dependency>
```
### Gradle
```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.jaspeen:ulid-java:92935f63c6'
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