# Publishing Agentlang IntelliJ Plugin

## Prereqs

- JetBrains Marketplace account.
- Plugin ID matches `ai.agentlang.intellij` in `src/main/resources/META-INF/plugin.xml`.
- Agentlang repo built (`npm run build`) so `out/language/main.cjs` exists.
- `node` available on PATH.

## Environment variables

Set these in your shell before publishing:

```bash
export PUBLISH_TOKEN="your_marketplace_token"
export CERTIFICATE_CHAIN="$(cat /path/to/chain.pem)"
export PRIVATE_KEY="$(cat /path/to/private.key)"
export PRIVATE_KEY_PASSWORD="optional_key_password"
```

Optional channel:

```bash
export PUBLISH_CHANNEL="beta"
```

## Publish

```bash
./gradlew buildPlugin
./gradlew signPlugin publishPlugin
```

## Release checklist

- Bump `version` in `build.gradle.kts`.
- Update change notes in `src/main/resources/META-INF/plugin.xml`.
- Ensure `AGENTLANG_REPO` points to the Agentlang repo if not `../agentlang`.

## Notes

- The Gradle tasks use `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, and `PUBLISH_TOKEN`.
- If you see `A restricted method in java.lang.System has been called` on newer JDKs, it is a harmless warning.
