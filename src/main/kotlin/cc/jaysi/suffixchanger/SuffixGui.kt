package cc.jaysi.suffixchanger

import cc.jaysi.suffixchanger.SuffixChanger.Companion.stripSuffix
import cc.jaysi.suffixchanger.SuffixChanger.Companion.unitalic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window


object SuffixGui {
    val name = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.name
    val version = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.version
    val authors = Bukkit.getPluginManager().getPlugin("SuffixChanger")?.pluginMeta?.authors

    fun inventory(player: Player) {
        if (SuffixChanger.api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes") == null) {
            if (player.hasPermission("suffixchanger.admin")) {
                ConfirmGui.inventory(
                    player,
                    SuffixChanger.oConfig.getString("track") ?: "suffixes",
                    ConfirmGui.ConfirmAction.TRACK
                )
            } else {
                player.sendMessage(
                    Component.text(
                        "Suffix Changer has not yet been set up, please contact an administrator to fix this issue.",
                        NamedTextColor.RED
                    )
                )
            }
            return
        }

        val suffixes = ArrayList<String>()

        val track = SuffixChanger.api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")
        for (group in track!!.groups) {
            if (player.hasPermission("suffix.$group")) {
                suffixes.add(0, group)
            } else {
                suffixes.add(group)
            }
        }

        fun getSuffix(roup: String): String = SuffixChanger.getSuffix(roup)

        fun createItem(suffix: Component, suffixName: String): ItemStack {
            if (player.hasPermission("suffix.$suffixName")) {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.setEnchantmentGlintOverride(true)
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                meta.displayName(Component.empty().unitalic().append(suffix))
                if (!player.hasPermission("suffixchanger.admin")) {
                    meta.lore(
                        listOf(
                            Component.text("You have access to this suffix!", NamedTextColor.GREEN).unitalic(),
                            Component.text("Click to select", NamedTextColor.GRAY).unitalic()
                        )
                    )
                } else {
                    meta.lore(
                        listOf(
                            Component.text("You have access to this suffix!", NamedTextColor.GREEN).unitalic(),
                            Component.text("Left click to select", NamedTextColor.GRAY).unitalic(),
                            Component.text("Right click to delete", NamedTextColor.GRAY).unitalic(),
                            Component.text("Press ").unitalic().color(NamedTextColor.GRAY).append(
                                Component.keybind().keybind("key.swapOffhand").build()
                                    .append(Component.text(" to edit"))
                            ),
                            /*Component.text("Press ").unitalic().color(NamedTextColor.GRAY).append(
                                Component.keybind().keybind("key.drop").build().unitalic()
                                    .append(Component.text(" to clear access to this suffix for all players"))
                            )*/
                        )
                    )
                }
                item.itemMeta = meta
                return item
            } else {
                val item = ItemStack(Material.NAME_TAG)
                val meta = item.itemMeta
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                if (SuffixChanger.oConfig.getBoolean("obfuscate"))
                    meta.displayName(Component.empty().unitalic().append(suffix).decorate(TextDecoration.OBFUSCATED))
                else
                    meta.displayName(Component.empty().unitalic().append(suffix))
                if (!player.hasPermission("suffixchanger.admin")) {
                    meta.lore(
                        listOf(
                            Component.text("You do not have access to this suffix!", NamedTextColor.RED).unitalic()
                        )
                    )
                } else {
                    meta.lore(
                        listOf(
                            Component.text("You do not have access to this suffix!", NamedTextColor.RED).unitalic(),
                            Component.text("Right click to delete", NamedTextColor.GRAY).unitalic(),
                            Component.text("Press ").unitalic().color(NamedTextColor.GRAY).append(
                                Component.keybind().keybind("key.swapOffhand").build()
                                    .append(Component.text(" to edit"))
                            ),
                            Component.text("Press ").unitalic().color(NamedTextColor.GRAY).append(
                                Component.keybind().keybind("key.drop").build().unitalic()
                                    .append(Component.text(" to clear access to this suffix for all players"))
                            )
                        )
                    )
                }
                item.itemMeta = meta
                return item
            }
        }

        // GUI

        val backItem = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
        val backMeta = backItem.itemMeta
        backMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        backMeta.displayName(
            Component.text(
                "Previous Page"
            ).decoration(TextDecoration.ITALIC, false)
        )
        backItem.itemMeta = backMeta

        val back: BoundItem = BoundItem.pagedBuilder()
            .setItemProvider(backItem)
            .addClickHandler { _, gui, _ -> gui.page-- }
            .build()

        val forwardItem = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
        val forwardMeta = forwardItem.itemMeta
        forwardMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        forwardMeta.displayName(
            Component.text(
                "Previous Page"
            ).decoration(TextDecoration.ITALIC, false)
        )
        forwardItem.itemMeta = forwardMeta

        val forward: BoundItem = BoundItem.pagedBuilder()
            .setItemProvider(forwardItem)
            .addClickHandler { _, gui, _ -> gui.page++ }
            .build()

        val closeItem = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
        val closeMeta = closeItem.itemMeta
        closeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        closeMeta.displayName(
            Component.text("Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
        )
        closeItem.itemMeta = closeMeta

        val close = Item.builder()
            .setItemProvider(closeItem)
            .addClickHandler { player.closeInventory() }
            .build()

        val items: ArrayList<Item> = ArrayList()
        for (suffix in suffixes) {
            items.add(
                Item.builder().setItemProvider(
                    createItem(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(getSuffix(suffix).stripSuffix()),
                        suffix
                    )
                ).addClickHandler { _, click ->
                    if (player.hasPermission("suffixchanger.admin") && click.clickType == ClickType.RIGHT) {
                        player.closeInventory()
                        ConfirmGui.inventory(player, suffix, ConfirmGui.ConfirmAction.REMOVE)
                    } else if (player.hasPermission("suffixchanger.admin") && click.clickType == ClickType.DROP) {
                        player.closeInventory()
                        ConfirmGui.inventory(player, suffix, ConfirmGui.ConfirmAction.CLEAR)
                    } else if (player.hasPermission("suffixchanger.admin") && click.clickType == ClickType.SWAP_OFFHAND) {
                        player.closeInventory()
                        ConfirmGui.editSuffix(player, suffix)
                    } else if (player.hasPermission("suffix.$suffix")) {
                        for (suffix2 in SuffixChanger.api.trackManager.getTrack(
                            SuffixChanger.oConfig.getString("track") ?: "suffixes"
                        )!!.groups) {
                            SuffixChanger.removePermission(SuffixChanger.getUser(player), "group.$suffix2")
                        }
                        SuffixChanger.addPermission(SuffixChanger.getUser(player), "group.$suffix")
                        player.sendMessage(
                            Component.text("Your suffix is now ").color(NamedTextColor.AQUA)
                                .append(
                                    LegacyComponentSerializer.legacyAmpersand()
                                        .deserialize(getSuffix(suffix).stripSuffix())
                                )
                        )
                    }
                }.build()
            )
        }

        val backgroundItem = ItemStack.of(Material.BLUE_STAINED_GLASS_PANE)
        val backgroundMeta = backgroundItem.itemMeta
        backgroundMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (SuffixChanger.oConfig.getBoolean("watermark")) {
            backgroundMeta.displayName(
                Component.text("$name v$version", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false)
            )
            val lore = ArrayList<Component>()
            if (authors != null) {
                lore.add(
                    Component.text("Made by:", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false)
                )
                for (author in authors) {
                    lore.add(
                        Component.text("- $author", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
                    )
                }
            }
            backgroundMeta.lore(lore)
        } else {
            backgroundMeta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false))
        }
        backgroundItem.itemMeta = backgroundMeta

        val adminStack = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
        val adminMeta = adminStack.itemMeta
        adminMeta.displayName(Component.text("Add Suffix", NamedTextColor.GREEN).unitalic())
        adminStack.itemMeta = adminMeta

        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# # # < c > # # ${if (player.hasPermission("suffixchanger.admin")) "a" else "#"}",
            )
            .addIngredient('#', Item.simple(backgroundItem))
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', back)
            .addIngredient('>', forward)
            .addIngredient('c', close)
            .addIngredient('a', Item.builder().setItemProvider(adminStack).addClickHandler {
                AddSuffixGui.inventory(player)
            })
            .setContent(items)

        Window.builder()
            .setTitle(
                MiniMessage.miniMessage().deserialize(
                    SuffixChanger.oConfig.getString("title")
                        ?: "<gradient:dark_red:red>Suffix</gradient><gradient:dark_blue:blue>Changer</gradient>"
                )
            )
            .setUpperGui(gui)
            .open(player)
    }
}