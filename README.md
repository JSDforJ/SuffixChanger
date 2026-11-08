<p align="center">
  [<img src="https://cdn.modrinth.com/data/C3bNc2TF/images/5de7fd5b43387b9ddb2e1b2cd0655e9e5e795a7b.png" />](https://modrinth.com/plugin/suffixchanger)
</p>

# SuffixChanger
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Let players choose their suffix with a simple-to-use GUI. Also contains admin gui for easily creating new suffixes.

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

# Install

[Download the plugin](https://github.com/Window5000/SuffixChanger/releases/latest) and add it to your ``plugins`` folder.

## Dependencies

This plugin requires [LuckPerms](https://luckperms.net/download) to function.

For suffixes to show in chat you need another plugin to format the chat.

# Usage

Create a group for each of your suffixes, then add them all to a track called ``suffixes``. Before players can change their suffix, you need to give them the permissions ``suffix.[group name of suffix]`` of every suffix they're allowed to use.

New suffixes can be easily created with either ``/addsuffix`` or the orange button in ``/suffix``.

## Commands:
- ``/suffix`` Open the SuffixChanger GUI
- ``/reloadsuffixchanger`` Reloads the SuffixChanger config
- ``/addsuffix <group_name> <suffix...(use as many args here as you wish)>`` Add a group with the specified suffix to luckperms

## Permissions:
  - ``suffix.[group name of suffix]`` for every suffix the player is allowed to use
  - ``suffixchanger.suffix`` to open the GUI
  - ``suffixchanger.addsuffix`` deprecated, use ``suffixchanger.admin`` instead
  - ``suffixchanger.reload`` to reload the SuffixChanger config
  - ``suffixchanger.admin`` to access add, edit, and remove suffixes

# Configuration
  - ``title`` changes the title that is used for the GUI, supports [MiniMessage](https://docs.advntr.dev/minimessage/format.html)
  - ``watermark`` set to false to disable the watermark under the leftover buttons
  - ``obfuscate`` set to true to obfuscate suffixes that the players isn't allowed to use
  - ``track`` advanced, determines which track to use for suffixes
  - ``group_weight`` advanced, determines what weight the group should have in Luckperms when using /addsuffix

# Contributing

PRs accepted.

Small note: If editing the Readme, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

# License

[GNU GPL](LICENSE) © Window5
