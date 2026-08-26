# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Quarkus extension enabling Quarkus apps to be written in Groovy. Standard Quarkiverse extension layout: each extension module has a `deployment` module (build-time processing, augmentation) and a `runtime` module (what ships in the app).

## Build & test

Requirements: Java 21+, Maven 3.8+, Docker 23+ (native tests), GraalVM 25+ (optional, for local native builds).

```sh
mvn -Dquickly                    # fast build, skips tests/docs/validators/enforcer
mvn clean install                # full JVM build with tests + validators
mvn clean install -Dnative       # + native image tests (GraalVM installed locally)
mvn clean install -Dnative -Dquarkus.native.container-build   # native tests via container, no local GraalVM needed
```

Run a single test: standard Maven, e.g. `mvn -pl integration-tests/basic test -Dtest=SomeTest`.

CI (`.github/workflows/build.yml`) runs on JDK 21 and 25 with:
```sh
mvn clean install formatter:validate -Dnative -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-25 -B
```

Code is auto-formatted on `process-sources` via `formatter-maven-plugin` and imports sorted/unused-removed via `impsort-maven-plugin` (both bound in the root `pom.xml`) — just build, don't hand-format.

## Repo layout

- `extensions/core` — the main extension. `deployment/.../GroovyProcessor.java` registers Groovy as a build item and wires `GroovyCompilationProvider` (implements Quarkus's `CompilationProvider` SPI to compile `.groovy` sources alongside Java during augmentation). `runtime/.../GroovyRecorder.java` is the bytecode-recorder invoked at build time to set up runtime behavior; `runtime/graal/GroovySubstitutions.java` holds GraalVM native-image substitutions for Groovy internals. `runtime/src/main/codestarts/` holds the Groovy codestart templates used by `quarkus create` for various extensions (RESTEasy, reactive routes, picocli, GraphQL, health checks, WebSockets, Spring Web, etc.) — these are Quarkus codestarts, not regular source, and use `.tpl.qute.groovy` Qute templating for class-name substitution.
- `extensions/hibernate-orm-panache` and `extensions/hibernate-reactive-panache` — Groovy-friendly Panache active-record/repository support (`PanacheEntity(Base)`, `PanacheRepository(Base)`, `PanacheQuery`) for ORM and Hibernate Reactive respectively. Each has a deployment-side bytecode enhancer (`PanacheJpaRepositoryEnhancer`, `EntityToPersistenceUnitUtil`) that rewrites Groovy Panache entities/repositories at build time, mirroring how quarkus-core does it for Java.
- `extensions/jaxb` — deployment-side ASM visitor/enhancer (`GroovyJAXBClassVisitor`, `GroovyJAXBEnhancer`) that fixes up Groovy-compiled classes so JAXB annotation processing works the same as on javac output.
- `extensions/junit5` — deployment support so `@QuarkusTest` works correctly against Groovy-compiled test classes.
- `integration-tests/*` — one module per extension/feature scenario (`basic`, `hibernate-orm-panache`, `hibernate-reactive-panache`, `rest`, `resteasy`, `resteasy-reactive`, `shared-library`), each a runnable Quarkus app exercising the extension end-to-end, including native-image ITs. `shared-library` demonstrates a Groovy extension-method library (`extensions/*.groovy`-style) consumed from another module.
- `examples/*` — standalone sample apps (`resteasy`, `reactive-routes`, `spring-web`, `gradle-resteasy`) showing usage in a real project, including one built with Gradle instead of Maven.
- `docs/` — Antora-based extension documentation, built into a separate `docs` Maven module (only active outside release builds).

Root `pom.xml` only aggregates `extensions`; the `it-examples` profile (active outside `-DperformRelease`) additionally pulls in `integration-tests` and `examples` so they aren't part of a release build.

## Working across deployment/runtime pairs

Each extension's `deployment` module reads Jandex-indexed classes and build items at augmentation time and typically produces `Recorder` calls (in `runtime`) that get replayed at native-image/runtime startup. When changing behavior, check both sides: the `*Processor.java` in `deployment` (build-time logic, build item wiring) and the corresponding `*Recorder.java` in `runtime` (what actually executes in the running app) usually need to change together.