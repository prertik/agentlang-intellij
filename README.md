# Agentlang IntelliJ Plugin

This plugin provides:
- Syntax highlighting via the bundled TextMate grammar.
- LSP features by launching the Agentlang language server with Node.js.

## Prereqs

```shell
cd /path/to/agentlang
npm run build
```

## JDK

This build expects Java 17. On macOS, `./gradlew` will auto-select JDK 17 if available. Otherwise, set `JAVA_HOME`:

```shell
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

## Build

```shell
cd /path/to/agentlang-intellij
./gradlew buildPlugin
```

## Run in IDE (default WebStorm 2025.3.2 or local install)

```shell
cd /path/to/agentlang-intellij
./gradlew runIde
```

## Override target IDE

```shell
cd /path/to/agentlang-intellij
./gradlew -PideType=IU -PideVersion=2025.1 runIde
```

## Use local IDE install

```shell
cd /path/to/agentlang-intellij
./gradlew -PideLocalPath="/Applications/WebStorm.app/Contents" runIde
```

If you have WebStorm installed at `~/Applications/WebStorm.app`, the build uses it automatically.

## Install in IDE

```shell
cd /path/to/agentlang-intellij
ls build/distributions
```

Install the generated `agentlang-intellij-0.1.0.zip` via Settings | Plugins | Install from Disk.

## Configure Agentlang repo path (if not a sibling)

Default lookup is `../agentlang` relative to this plugin directory.

```shell
export AGENTLANG_REPO=/path/to/agentlang
./gradlew buildPlugin
```

```shell
./gradlew -PagentlangRepo=/path/to/agentlang buildPlugin
```

## Notes

- The build copies `out/language/main.cjs` into the plugin resources automatically. The TextMate grammar is bundled in
  the plugin.
- A working `node` executable must be available on the PATH.
- LSP support requires IDE 2025.3+ (the LSP API is in core and not a separate plugin).
- `./gradlew` uses Gradle 8.13 via the wrapper.
- `config.al` files use the same TextMate grammar as other `.al` files but do not attach to the LSP server.

## Publishing
See `PUBLISH.md`.

## License
MIT
