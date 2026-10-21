# MiniRedis Java

MiniRedis Java is a small educational project: a Redis-like command layer built from scratch in Java.

I'm building it to understand key/value storage, command handling, typed results, expiration, and wire-format serialization, one tested step at a time.

## Current State

The project currently has an in-memory string store, a command dispatcher, store-level TTL support, and RESP2 response serialization.

Implemented behavior:

- `SET key value` stores a string value and clears any previous TTL.
- `GET key` returns the stored string or a null-style result when the key is missing.
- `EXISTS key` returns `1` or `0`.
- `DEL key` removes a key and its expiration metadata, returning `1` or `0`.
- `INCR key` increments integer-like string values, preserves an existing TTL, creates missing counters at `1`, and reports errors for invalid numbers or overflow.
- Commands are matched case-insensitively.
- `EXPIRE key seconds` sets or replaces a TTL, returning `1` for a live key or `0` for a missing or expired key. Invalid TTL numbers and arithmetic overflow return error results.

The store's `expire(key, seconds)` method backs the dispatcher's `EXPIRE` command. A non-positive TTL removes an existing key immediately; a missing or expired key returns `0`.

Expiration is lazy: `GET` and `EXISTS` remove expired entries when accessed, and `DEL`, `INCR`, and `expire` use those checks. There is no background expiration worker. The store accepts an injected `Clock` for deterministic time behavior and defaults to `Clock.systemUTC()`.

Status, string, integer, null, and error responses are represented as typed Java result objects. The dispatcher uses shared error-message constants for consistent wording.

`RespSerializer` converts those results into UTF-8 RESP2 response bytes:

- Status results become simple strings, such as `+OK\r\n`.
- Integer results become RESP integers, such as `:56\r\n`.
- Error results become simple errors with an `-ERR ` prefix.
- Null results become null bulk strings: `$-1\r\n`.
- String results become bulk strings whose length counts UTF-8 content bytes. Empty strings remain distinct from missing values, and embedded CRLF is preserved.

Status and error messages containing either CR or LF are rejected with `IllegalArgumentException`. Java null input throws `NullPointerException`; unsupported result types throw `IllegalArgumentException`. These are developer-facing failures, not serialized client error responses. Serialization does not send bytes over a network.

What does not exist yet:

- TCP server.
- RESP request parser.
- Real Redis client compatibility.
- Persistence.
- Concurrency model.
- Multiple data types beyond strings.

## Project Layout

```text
src/main/java/dev/miniredis/
  MiniRedisStore.java        # in-memory key/value behavior
  CommandDispatcher.java     # command-name + argument dispatch
  CommandResult.java         # result marker interface
  *Result.java               # typed result objects
  RespSerializer.java        # RESP2 response bytes

src/test/java/dev/miniredis/
  MiniRedisStoreTest.java
  CommandDispatcherTest.java
  CommandResultTest.java
  RespSerializerTest.java
  UnknownResult.java         # test-only unsupported result helper
  MutableClock.java          # test-only controllable clock
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

The suite currently contains 69 tests: 30 store tests, 23 dispatcher tests, 2 result tests, and 14 serializer tests. TTL tests cover expiration at the deadline, missing and expired keys, zero and negative TTL, replacing a TTL, `SET` clearing it, and `INCR` preserving it. Regression tests also cover `GET`, `INCR`, and `DEL` accessing expired keys, deleting and recreating an expiring key, and preserving the previous TTL when a replacement overflows. Dispatcher tests cover `EXPIRE` success, invalid TTL input, overflow, and invalid arity. Time-based tests use fixed clocks or the test-only `MutableClock`, without sleeps.

Serializer tests cover all five result types, UTF-8 byte lengths, empty and multiline bulk strings, separate CR/LF rejection for status and error messages, null input, and unsupported result types.

TTL conversion and deadline calculation use exact arithmetic: overflow throws `ArithmeticException` before a new expiration is stored. GitHub Actions is configured to build and test with Java 21 on pushes and pull requests to `main`.

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

TTL regression coverage and RESP response serialization are implemented. The next planned checkpoint is RESP request parsing, followed by a single-client TCP server and a selected-command redis-cli demonstration. Additional data structures, multiple clients, and persistence remain future work.

Each checkpoint should stay small enough to test and explain before moving on.
