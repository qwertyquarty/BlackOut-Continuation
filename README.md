![banner](https://raw.githubusercontent.com/HYPE115/BlackOut-1.21.11/main/src/main/resources/assets/blackout/logo.png)

# BlackOut Continuation

A community-maintained port of the original **BlackOut** addon for **Meteor Client**, updated for *Newer Minecraft Version**.

This fork keeps BlackOut compatible with newer versions while preserving the original features and gameplay experience.

---

## Features

- Minecraft **1.21.11** support
- Original BlackOut modules and HUD elements with some changes
- Compatibility fixes and maintenance

---

## Installation

1. Install **Fabric Loader**
2. Install a compatible version of **Meteor Client**
3. Download the latest **BlackOut** release
4. Put the `.jar` files into your `mods` folder
5. Launch Minecraft with the Fabric profile

---

## Building

```bash
git clone https://github.com/HYPE115/BlackOut-1.21.11.git
cd BlackOut-1.21.11
./gradlew build
````

The compiled `.jar` will be located in:

```
build/libs/
```

---

## Credits

Based on the original **BlackOut** addon.

**Original contributors:**

* KassuK
* Doogie13 (block mining calculations & step offsets)
* RickyTheRaccoon (InvSwitch)
* H1ggsK (some changes)

**Porting:**

* Ported using **mc-mod-porter** by reqsery
  [https://github.com/reqsery/mc-mod-porter](https://github.com/reqsery/mc-mod-porter)

Additional manual fixes were made for **Minecraft 1.21.11**, **Fabric**, and the latest **Meteor Client** versions.

---

## License

This project follows the same license as the original **BlackOut** addon.

All original code, assets, and contributions belong to their respective authors.

See the `LICENSE` file for more information.
