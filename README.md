# AutoViaUpdater
AutoViaUpdater is a plugin for Spigot/Bungeecord/Velocity that automates the updating of Vias such as ViaVersion, ViaRewind, ViaBackwards, and ViaRewind-Legacy.

Spigot Link: https://www.spigotmc.org/resources/autoviaupdater.109331/

# Features
- Compatible with Minecraft versions 1.8 and higher for both Spigot/Velocity/Bungeecord, including the latest versions.

- Automatically downloads successful builds of ViaVersion, ViaBackwards, ViaRewind, and ViaRewind-Legacy-Support, as well as their dev versions, from Jenkins.

- Config.yml that allows you to specify which Vias to update and whether to use the dev version of each plugin.

# Planned Features
- If you have any suggestions or feature requests, please create a new issue in the project's GitHub repository.

# Installation
- Download the latest release of the plugin from the releases page.
- Copy the downloaded .jar file to the plugins directory of your Minecraft server.
- Make sure that none of the Vias are already installed because it will automatically install them.
- Start the server. Note: The first time you start the server, you will need to restart it again since it installs all the Vias the first time and actually enables them on the second restart.
- After installation, AutoViaUpdater will automatically update all Vias to the latest successful build at the Check-interval specified in the config.yml file.

# License
AutoViaUpdater is released under the MIT License. See the LICENSE file for more information.
