# Breeding Control

**Breeding Control** is a lightweight NeoForge mod that lets you disable breeding for specific mobs through a simple configuration file.

It is designed for modpacks and servers that want tighter control over animal population without removing feeding mechanics entirely.

The mod allows animals to still be fed for healing (optional) or baby growth while preventing them from entering love mode.

---

## Features

- Disable breeding for **specific mobs**
- Support for **entity IDs and entity tags**
- Optional **healing with breeding food**
- Optional **baby feeding (growth acceleration)**
- Lightweight and server-friendly
- Fully configurable

The goal is to prevent uncontrolled mob breeding while keeping normal feeding gameplay intact.

---

## How it works

When a player tries to use breeding food on a configured mob:

- If the animal is **hurt**, it can optionally be healed instead of breeding
- If the animal is a **baby**, feeding can optionally speed up growth
- If the animal is a **healthy adult**, breeding is blocked

---

## Configuration

```toml
# List of entity ids or entity_type tags that cannot be bred.
# Examples:
#  "minecraft:cow"
#  "#minecraft:animals"
notBreedableMobs = ["minecraft:cow"]

# If true: using breeding food on a hurt blocked animal heals it
allowHealingWithBreedFood = true

# How much health is restored when healing
# Default: 2.0 (1 heart)
healAmount = 2.0

# If true: babies can still be fed to speed up growth
allowBabyFeeding = true