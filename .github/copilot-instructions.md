# Copilot / AI Agent Instructions — Autotorch

Short: This is a small Fabric *client-only* Minecraft mod that auto-places torches from the player's offhand when the block light is below a configured threshold.

## Quick start (what to run)
- Build: Windows: `.
  gradlew.bat build` (produces mod jar in `build/libs`)
- Run a dev client: `.
  gradlew.bat runClient` (or use the Gradle `runClient` task in the IDE)
- Edit versions: `gradle.properties` (minecraft_version, loader_version, fabric_version, etc.)
- Java: project uses Java 21 (toolchain configured in `build.gradle`).

## Big picture / architecture
- Purpose: client-side mod that automatically places torches when conditions are met.
- Main components:
  - `AutotorchClient.java` (src/main/java): the ClientModInitializer. Registers config, keybind, and tick handler that performs checks and places torches.
  - `ModConfig.java`: autoconfig-backed POJO for user settings (registered with `AutoConfig.register(..., GsonConfigSerializer::new)`).
  - `ModMenuIntegration.java`: integrates Cloth Config with ModMenu to provide an in-game config screen.
  - `resources/fabric.mod.json` and `assets/.../lang/en_us.json`: mod metadata and translations.

## Patterns & conventions to follow (concrete, repo-specific)
- Configs: use the Auto Config library (me.shedaniel.autoconfig). Add fields to `ModConfig` and annotate with `@Comment` and `@ConfigEntry.*` where appropriate. Translation keys for config labels should follow `text.autoconfig.autotorch.option.<name>`.
  - Config file on disk: `.minecraft/config/autotorch.json` (the mod uses the default AutoConfig storage behavior).
  - Example: `@ConfigEntry.BoundedDiscrete(min = 1, max = 14) int lightLevel = 4;`
- Key bindings: register using `KeyBindingHelper.registerKeyBinding(...)` and localize the key description using `autotorch.autotorch.toggle` and `category.autotorch.main` (see `AutotorchClient`).
- Tick handling: uses `ClientTickEvents.END_CLIENT_TICK.register(this::tick)` and performs null checks for `client.player` and `client.world` before acting.
- Torch logic: check `player.getOffHandStack().getItem()` against `TorchSet` and use `client.interactionManager.interactBlock` and `interactItem` to trigger placement.
- AccuratePlacement side-effect: the feature fakes player rotation by sending `PlayerMoveC2SPacket.LookAndOnGround`—beware server-side anti-cheat implications.

## Integration points & external dependencies
- Fabric Loader / Fabric API (see `gradle.properties` and `build.gradle` dependencies).
- Cloth Config & ModMenu are used for in-game configuration UI (see `ModMenuIntegration.java`).
- Keep `fabric.mod.json` metadata accurate (id `autotorch`, license `LGPL-3.0-or-later`, environment `client`).

## Tests & Debugging
- There are no unit tests in the repo. To debug, run `gradlew runClient` and attach your IDE debugger to the Minecraft client process.
- Logs and console output during `runClient` are the primary way to observe behavior. Inspect the tick method and `offHandRightClickBlock` for placement flow.

## Files to edit for common tasks (examples)
- Add a config option: modify `src/main/java/.../ModConfig.java` (add field + `@Comment`), add UI label to `assets/autotorch/lang/en_us.json` using `text.autoconfig.autotorch.option.<name>`.
- Add a translation or message: edit `src/main/resources/assets/autotorch/lang/en_us.json` (keys like `autotorch.message.enabled`).
- Change mod metadata: edit `src/main/resources/fabric.mod.json` (version uses `${version}` from gradle build).

## Versioning & CI notes
- Version metadata is computed in `build.gradle` using Git and `GITHUB_RUN_NUMBER` (see `getVersionMetadata()`); CI builds will append `build.<n>`.

## Licensing & headers
- Source files include a GNU LGPL header (see `AutotorchClient.java`) and `fabric.mod.json` lists `LGPL-3.0-or-later`. Preserve license headers when editing; do not remove them.

## Do / Don't quick checklist ✅/❌
- ✅ Use `AutoConfig` for configuration fields and keep translation keys in `assets/.../lang/en_us.json`.
- ✅ Use `gradlew runClient` to test locally and attach a debugger for step-through debugging.
- ✅ Keep `fabric.mod.json` `id` and `environment: client` unchanged unless intentionally changing target platform.
- ❌ Do not remove license headers or re-license code.
- ❌ Avoid sending rotation packets (accuratePlacement) on servers that forbid such behavior—document any changes that affect network behavior.