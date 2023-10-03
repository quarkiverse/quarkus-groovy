# Quarkus Groovy
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Build](https://github.com/quarkiverse/quarkus-groovy/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-groovy/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-groovy)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.groovy/quarkus-groovy?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.groovy%20AND%20a:quarkus-groovy)

Quarkus Groovy is a Quarkus extension that allows you to write Quarkus 3.4 applications in Groovy 4.0.

With Maven, add the following dependency to your `pom.xml` to get started:

```xml
<dependency>
    <groupId>io.quarkiverse.groovy</groupId>
    <artifactId>quarkus-groovy</artifactId>
    <version>${quarkusGroovyVersion}</version>
</dependency>
```

Or with Gradle, add the following dependency to your `build.gradle`:

```groovy
implementation "io.quarkiverse.groovy:quarkus-groovy:${quarkusGroovyVersion}"
```

For more information and quickstart, you can check the complete [documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-groovy/dev/index.html).

## Build

To build the extension, the requirements are the following:

* Java 11+
* Maven 3.8+
* Docker 23+
* GraalVM 22.3.1+ (optional)

To quickly build the extension with all the tests and validators disabled:

```sh
$ mvn -Dquickly
```

To build the extension with all the tests for the JVM mode and the validators enabled:

```sh
$ mvn clean install
```

To build the extension with everything enabled when GraalVM is installed on the local machine:

```sh
$ mvn clean install -Dnative
```

To build the extension with everything enabled when GraalVM is not installed on the local machine:

```sh
$ mvn clean install -Dnative -Dquarkus.native.container-build
```

## Contributors ✨

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind are welcome!
<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://stackoverflow.com/users/1997376/nicolas-filotto"><img src="https://avatars.githubusercontent.com/u/1618116?v=4?s=100" width="100px;" alt="Nicolas Filotto"/><br /><sub><b>Nicolas Filotto</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-groovy/commits?author=essobedo" title="Code">💻</a> <a href="#maintenance-essobedo" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/fernando88to"><img src="https://avatars.githubusercontent.com/u/280641?v=4?s=100" width="100px;" alt="Fernando Henrique"/><br /><sub><b>Fernando Henrique</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-groovy/commits?author=fernando88to" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://thejavaguy.org/"><img src="https://avatars.githubusercontent.com/u/11942401?v=4?s=100" width="100px;" alt="Ivan Milosavljević"/><br /><sub><b>Ivan Milosavljević</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-groovy/commits?author=TheJavaGuy" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://gastaldi.wordpress.com"><img src="https://avatars.githubusercontent.com/u/54133?v=4?s=100" width="100px;" alt="George Gastaldi"/><br /><sub><b>George Gastaldi</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-groovy/commits?author=gastaldi" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
