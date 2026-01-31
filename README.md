# Agentlang IntelliJ Plugin

This plugin provides:
- Syntax highlighting via the bundled TextMate grammar.
- LSP features by launching the Agentlang language server with Node.js.

## JDK

This build expects Java 17. On macOS, `./gradlew` will auto-select JDK 17 if available. Otherwise, set `JAVA_HOME`:

```shell
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

## Build

The plugin automatically downloads the Agentlang LSP server from npm during build:

```shell
cd /path/to/agentlang-intellij
./gradlew buildPlugin
```

### Specify Agentlang Version

By default, the build uses agentlang version 0.9.5. To use a different version:

```shell
./gradlew -PagentlangVersion=0.9.4 buildPlugin
```

### Use Local Agentlang Repo (for development)

If you're developing agentlang locally, you can use your local repo instead of npm:

```shell
cd /path/to/agentlang
npm run build

cd /path/to/agentlang-intellij
export AGENTLANG_REPO=/path/to/agentlang
./gradlew buildPlugin
```

Or via gradle property:

```shell
./gradlew -PagentlangRepo=/path/to/agentlang buildPlugin
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

Install the generated `agentlang-intellij-0.0.2.zip` via Settings | Plugins | Install from Disk.

## Notes

- The build automatically downloads the Agentlang LSP server from npm and bundles it in the plugin.
- The TextMate grammar is bundled in the plugin.
- A working `node` executable must be available on the PATH at runtime.
- LSP support requires IDE 2025.3+ (the LSP API is in core and not a separate plugin).
- `./gradlew` uses Gradle 8.13 via the wrapper.
- `config.al` files use the same TextMate grammar as other `.al` files but do not attach to the LSP server.

## Publishing
See `PUBLISH.md`.

## License
MIT
