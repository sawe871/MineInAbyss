[![Mine in Abyss](https://user-images.githubusercontent.com/16233018/75004708-02cc4800-543a-11ea-8bb3-a9184d9311a0.png)](https://mineinabyss.com)

# Mine In Abyss

Mine In Abyss is a plugin for spigot that is made for the Mine In Abyss minecraft server.
The server aims to recreate the world of [Made in Abyss](https://en.wikipedia.org/wiki/Made_in_Abyss) within Minecraft.
You can join our [Discord](https://discord.gg/qWAMBSK) for more information about the server. Visit our site [mineinabyss.com](https://mineinabyss.com) for news and our social media links.

## Features
* Custom Item/Artifact system using [Geary](https://github.com/MineInAbyss/Geary)
* Previously using [Looty](https://github.com/MineInAbyss/Looty) for relics
    * A number of premade artifacts such as [blaze reap](http://madeinabyss.wikia.com/wiki/Blaze_Reap)
    * API for creating own artifacts
    * Custom loot spawning as entities on map
    * Will be replaced in many aspects by Geary
* Extremely deep world using [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld)
    * Stacked sections automatically and seamlessly teleported betwen
* The Curse of Ascension (planned to be moved to a separate plugin called [AbyssialCurse](https://github.com/MineInAbyss/Abysscurse))
    * Varying curse effects mimicking the curses in the manga/anime as close as possible
* Using [Idofront](https://github.com/MineInAbyss/Idofront) API
   * Shares commonly used code between our projects
   * Many extension functions and Kotlin specific features
   * Command API (which we aren't using here yet!)
* GUIs made with [Guiy](https://github.com/MineInAbyss/guiy)
   * Lots of features and nice to code with

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You will need a working Spigot/Paper server. Get the [Paper jar](https://papermc.io/downloads), then install Paper with [this guide](https://paper.readthedocs.io/en/latest/server/getting-started.html). 
We are currently running a `1.15.2` server, and our plugins are designed to work with this version of Spigot. 

### Setup

We recommend using IntelliJ as your IDE. You can look at the quick guide below, or find a full guide [here](https://github.com/MineInAbyss/MineInAbyss/wiki/Spigot-Plugin-Setup-Guide-and-contributing-to-this-project) for a detailed installation walkthrough.

1. Clone this repository
2. Clone [DeeperWorld](https://github.com/Derongan/DeeperWorld) into the same directory, i.e:
```
Projects
└───DeeperWorld
└───MineInAbyss
```
3. Enter the MineInAbyss directory
4. Build the project with gradle
    * Linux/OSX: `./gradlew build`
    * Windows: `gradlew.bat build`
5. Copy the jar from `MineInAbyss/MineInAbyss/build/libs` to the spigot plugin directory
6. Copy the jar from `DeeperWorld/build/libs` to the spigot plugin directory
7. Update the config files based on the samples to work for your use case

## Contributing

Talk to us on the Discord if you want to help. 
