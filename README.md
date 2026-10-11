# MiniRedis Java

MiniRedis Java is a small educational project: a Redis-like command layer built from scratch in Java.

The point of the project is not to ship a production Redis clone. The point is to learn the mechanics underneath one: storing keys and values, handling commands, returning typed results, and growing the system one tested step at a time.

## Current State

The project currently has an in-memory store and a command dispatcher.

Implemented behavior:

- `SET key value` stores a string value.
- `GET key` returns the stored string or a null-style result when the key is missing.
- `EXISTS key` returns `1` or `0`.
- `DEL key` removes a key and returns `1` or `0`.
- `INCR key` increments integer-like string values, creates missing counters at `1`, and reports errors for invalid numbers or overflow.
- Commands are matched case-insensitively.

The code also has typed command result classes for status, string, integer, null, and error responses. These are not RESP wire protocol responses yet; they are internal Java result objects used by the dispatcher and tests.

What does not exist yet:

- TCP server.
- RESP parser or serializer.
- Real Redis client compatibility.
- Persistence.
- Expiration / TTL.
- Concurrency model.
- Multiple data types beyond strings.

## Project Layout

```text
src/main/java/dev/miniredis/
  MiniRedisStore.java        # in-memory key/value behavior
  CommandDispatcher.java     # command-name + argument dispatch
  CommandResult.java         # result marker interface
  *Result.java               # typed result objects

src/test/java/dev/miniredis/
  MiniRedisStoreTest.java
  CommandDispatcherTest.java
  CommandResultTest.java
```

## Build And Test

This is a Maven project targeting Java 21.

```powershell
.\mvnw.cmd test
```

If Maven is installed globally, this should also work:

```powershell
mvn test
```

## Development Philosophy

This repository is intentionally being built by hand. AI assistance is used as a scribe and reviewer, not as the implementation author.

That means:

- production Java code should be written by the developer;
- documentation can be drafted or maintained with AI help;
- tests, compiler errors, and code review notes can be inspected with AI help;
- implemented features should only be claimed when they exist in the repository.

The goal is to be able to say, truthfully:

> I wrote this myself, and I understand why it works.

## Suggested Next Checkpoints

Likely next learning checkpoints are:
TTL → Data Structures → RESP + TCP → Multiple Clients → Persistence

Each checkpoint should stay small enough to test and explain before moving on.
