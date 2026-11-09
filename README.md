# Melonity Client — Fabric 1.21.11

A client-side Minecraft mod starter with a compact, animated-ready ClickGUI inspired by the dark/acid-green visual language of Melonity. This is an independent implementation, not copied proprietary source.

## Requirements
- Java 21
- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API 0.141.1+1.21.11

## Build
Run `gradle build`. The remapped mod JAR is written to `build/libs/`.

Open the GUI in-game with **Right Shift**.

## Current foundation
- ClickGUI with Combat, Movement, Render, Player, Misc and HUD categories
- Module cards with working local enable/disable state
- Search field placeholder layout and settings panel foundation
- Client-only entrypoint and GitHub Actions build workflow

Gameplay modules are intentionally added in stages; entries shown in the GUI are UI foundations until their behavior is implemented.
