# MiniRedis Java

MiniRedis is a small Redis-like server built from scratch in Java 21. It stores strings in memory, supports key expiration, and accepts a subset of RESP2 over TCP.

I'm building it to learn how a TCP server turns bytes from a client into commands and replies.

## Current State

The server supports six commands. Command names are case-insensitive.

- `SET key value` stores a string value and clears any previous TTL.
- `GET key` returns the stored string or a RESP null bulk string when the key is missing.
- `EXISTS key` returns `1` or `0`.
- `DEL key` removes a key and its expiration metadata, returning `1` or `0`.
- `INCR key` increments integer-like string values, preserves an existing TTL, creates missing counters at `1`, and reports errors for invalid numbers or overflow.
- `EXPIRE key seconds` sets or replaces a TTL, returning `1` for a live key or `0` for a missing or expired key. Invalid TTL numbers and arithmetic overflow return error results.

A non-positive TTL removes an existing key immediately. Expiration is lazy. The store removes expired keys on access and accepts an injected `Clock` for deterministic tests.

## Request Flow

`MiniRedisServer` accepts a TCP connection on port `6379`. For each request, `RespRequestParser` reads a command from the socket. `CommandDispatcher` validates the command and calls `MiniRedisStore`. `RespSerializer` converts the resulting `CommandResult` to RESP2 bytes, which the server writes back to the same socket.

The serializer writes five response types.

- Status results become simple strings, such as `+OK\r\n`.
- Integer results become RESP integers, such as `:56\r\n`.
- Error results become simple errors with an `-ERR ` prefix.
- Null results become null bulk strings (`$-1\r\n`).
- String results become bulk strings whose length counts UTF-8 content bytes. Empty strings remain distinct from missing values, and embedded CRLF is preserved.

`RespRequestParser` reads one command from an `InputStream` as a non-empty RESP array of UTF-8 bulk strings and returns a `ParsedCommand` containing the command name and arguments.

The parser preserves empty strings and embedded CRLF, checks lengths in bytes, and rejects malformed UTF-8, invalid framing, and numeric headers containing anything other than ASCII decimal digits. Limits are 1,024 array elements, 1,048,576 bytes per bulk string, and 64 bytes per header.

## Run on Windows

With JDK 21 available on your `PATH`, run the following in PowerShell from the repository root.

```powershell
.\mvnw.cmd compile
java -cp target/classes dev.miniredis.MiniRedisServer
```

Leave that window open. With Docker Desktop running in Linux container mode, open a second PowerShell window and run the Redis 7 client.

```powershell
docker run redis:7 redis-cli -h host.docker.internal -p 6379 SET name Gavin
```

On November 23, 2026, Redis 7 `redis-cli` returned `OK` for this `SET` request on Windows with Docker Desktop. That check covers `SET` only.

### Connection behavior

- One server run accepts one connection. That connection can send multiple commands and share the same in-memory store.
- A clean disconnect between requests stops the request loop and closes the listener. Restart the server before another one-shot `redis-cli` command. Restarting clears the store.
- Malformed RESP or EOF partway through a request raises `IOException` and stops the server without a RESP reply. A well-formed request with an unknown command or invalid arguments gets a RESP error reply, and the connection stays open.

## Project Layout

```text
src/main/java/dev/miniredis/
  MiniRedisServer.java       # TCP listener and request loop
  MiniRedisStore.java        # in-memory key/value behavior
  CommandDispatcher.java     # command-name + argument dispatch
  CommandResult.java         # result marker interface
  *Result.java               # typed result objects
  RespSerializer.java        # RESP2 response bytes
  RespRequestParser.java     # RESP command requests from an InputStream
  ParsedCommand.java         # command name and arguments

src/test/java/dev/miniredis/
  MiniRedisServerTest.java   # loopback TCP tests and port validation
  MiniRedisStoreTest.java
  CommandDispatcherTest.java
  CommandResultTest.java
  RespSerializerTest.java
  RespRequestParserTest.java
  UnknownResult.java         # test-only unsupported result helper
  MutableClock.java          # test-only controllable clock
```

## Run the JUnit Tests

This is a Maven project targeting Java 21.

```powershell
.\mvnw.cmd test
```

If Maven is installed globally, you can run `mvn test`.

The TCP tests use a loopback socket to check `SET` and `GET` on one connection, clean disconnect, and malformed input. The rest of the suite covers the store, dispatcher, result types, serializer, and parser. Time-based tests use fixed or controllable clocks. GitHub Actions builds and tests with Java 21 on pushes to `main` and pull requests targeting `main`.

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

The next checkpoint is to accept a second client after the first disconnects while keeping the same store. Concurrent clients, persistence, and additional data types are later work.
