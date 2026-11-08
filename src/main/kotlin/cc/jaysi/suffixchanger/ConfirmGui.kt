package cc.jaysi.suffixchanger

import cc.jaysi.suffixchanger.AddSuffixGui.plugin
import cc.jaysi.suffixchanger.SuffixChanger.Companion.api
import cc.jaysi.suffixchanger.SuffixChanger.Companion.toSuffixComponent
import cc.jaysi.suffixchanger.SuffixChanger.Companion.unitalic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.luckperms.api.node.matcher.NodeMatcher
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.SuffixNode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window

object ConfirmGui {
    fun inventory(player: Player, suffixRank: String, action: ConfirmAction, newSuffix: String = "") {
        val suffix = if (action != ConfirmAction.TRACK) SuffixChanger.getSuffix(suffixRank)
            .toSuffixComponent() else Component.text(
            suffixRank,
            NamedTextColor.WHITE
        ).unitalic()
        val actionComponent = Component.text(
            when (action) {
                ConfirmAction.REMOVE -> "Confirm Removing $suffixRank"
                ConfirmAction.CLEAR -> "Confirm Clearing $suffixRank"
                ConfirmAction.EDIT -> "Confirm Editing $suffixRank"
                ConfirmAction.TRACK -> "Creating \"$suffixRank\""
            }, NamedTextColor.RED
        )

        val cancelItem = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val cancelMeta = cancelItem.itemMeta
        cancelMeta.displayName(
            Component.text("Cancel", NamedTextColor.RED).unitalic().decoration(TextDecoration.BOLD, true)
        )
        cancelItem.itemMeta = cancelMeta

        val confirmItem = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        val confirmMeta = cancelItem.itemMeta
        confirmMeta.displayName(
            Component.text("Confirm", NamedTextColor.GREEN).unitalic().decoration(TextDecoration.BOLD, true)
        )
        if (action != ConfirmAction.TRACK) {
            confirmMeta.lore(
                listOf(
                    Component.text("Warning! This action can not be reversed!", NamedTextColor.RED).unitalic()
                )
            )
        }
        confirmItem.itemMeta = confirmMeta

        val detailsItem = ItemStack(Material.BLUE_STAINED_GLASS_PANE)
        val detailsMeta = cancelItem.itemMeta
        detailsMeta.displayName(suffix)
        if (action != ConfirmAction.EDIT && action != ConfirmAction.TRACK) {
            detailsMeta.lore(
                listOf(
                    Component.text(
                        "This is currently being used by ${
                            api.userManager.searchAll(
                                NodeMatcher.key(InheritanceNode.builder(suffixRank).build())
                            ).join().size
                        } player(s)", NamedTextColor.GRAY
                    ).unitalic(),
                    Component.text(
                        "${
                            api.userManager.searchAll(
                                NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())
                            ).join().size
                        } player(s) currently have access to this suffix", NamedTextColor.GRAY
                    ).unitalic()
                )
            )
        } else if (action == ConfirmAction.EDIT) {
            detailsMeta.lore(
                listOf(
                    Component.text(
                        "This is currently being used by ${
                            api.userManager.searchAll(
                                NodeMatcher.key(InheritanceNode.builder(suffixRank).build())
                            ).join().size
                        } player(s)", NamedTextColor.GRAY
                    ).unitalic(),
                    Component.text(
                        "${
                            api.userManager.searchAll(
                                NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build())
                            ).join().size
                        } player(s) currently have access to this suffix", NamedTextColor.GRAY
                    ).unitalic(),
                    Component.text("From:", NamedTextColor.WHITE).unitalic(),
                    suffix,
                    Component.text("To:", NamedTextColor.WHITE).unitalic(),
                    newSuffix.toSuffixComponent()
                )
            )
        } else {
            detailsMeta.lore(
                listOf(
                    Component.text("The track $suffixRank is required, but does not exist.", NamedTextColor.GRAY)
                        .unitalic(),
                    Component.text("If you want to change the name of the track,", NamedTextColor.GRAY).unitalic(),
                    Component.text("or if you already have a track for suffixes,", NamedTextColor.GRAY).unitalic(),
                    Component.text("then change the \"track\" setting in the config.", NamedTextColor.GRAY).unitalic(),
                    Component.text(
                        "Press \"Confirm\" to create a new track named \"$suffixRank\".",
                        NamedTextColor.GRAY
                    ).unitalic()
                )
            )
        }
        detailsItem.itemMeta = detailsMeta

        val upperGui = Gui.builder()
            .setStructure("X # Z # Y")
            .addIngredient('X', Item.builder().setItemProvider(cancelItem).addClickHandler {
                player.closeInventory()
            })
            .addIngredient('Y', Item.builder().setItemProvider(confirmItem).addClickHandler {
                when (action) {
                    ConfirmAction.REMOVE -> {
                        val userManager = api.userManager
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build()))
                            .join().keys.forEach {
                                val user =
                                    if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                        .join()!!
                                user.data().remove(InheritanceNode.builder(suffixRank).build())
                                userManager.saveUser(user)
                            }
                        api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")
                            ?.removeGroup(suffixRank)
                        api.groupManager.deleteGroup(api.groupManager.getGroup(suffixRank)!!)
                            .join()
                        api.trackManager.saveTrack(
                            api.trackManager.getTrack(
                                SuffixChanger.oConfig.getString("track") ?: "suffixes"
                            )!!
                        )
                    }

                    ConfirmAction.CLEAR -> {
                        val userManager = api.userManager
                        userManager.searchAll(NodeMatcher.key(PermissionNode.builder("suffix.$suffixRank").build()))
                            .join().keys.forEach {
                                val user =
                                    if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                        .join()!!
                                user.data().remove(PermissionNode.builder("suffix.$suffixRank").build())
                                userManager.saveUser(user)
                            }
                        userManager.searchAll(NodeMatcher.key(InheritanceNode.builder(suffixRank).build()))
                            .join().keys.forEach {
                                val user =
                                    if (userManager.isLoaded(it)) userManager.getUser(it)!! else userManager.loadUser(it)
                                        .join()!!
                                user.data().remove(InheritanceNode.builder(suffixRank).build())
                                userManager.saveUser(user)
                            }
                    }

                    ConfirmAction.EDIT -> {
                        api.groupManager.modifyGroup(suffixRank) {
                            it.data().remove(
                                SuffixNode.builder(
                                    SuffixChanger.getRawSuffix(suffixRank),
                                    api.groupManager.getGroup(suffixRank)?.weight?.orElse(3) ?: 3
                                ).build()
                            )
                            it.data().add(
                                SuffixNode.builder(
                                    newSuffix,
                                    api.groupManager.getGroup(suffixRank)?.weight?.orElse(3) ?: 3
                                ).build()
                            )
                        }.join()
                    }

                    ConfirmAction.TRACK -> {
                        player.closeInventory()
                        api.trackManager.createAndLoadTrack(suffixRank).thenRun {
                            plugin.server.scheduler.runTask(plugin, Runnable {
                                if (player.isOnline) SuffixGui.inventory(player)
                            })
                        }
                    }
                }
                player.closeInventory()
            })
            .addIngredient('Z', Item.simple(detailsItem))

        Window.builder()
            .setUpperGui(upperGui)
            .setTitle(actionComponent)
            .open(player)
    }

    fun editSuffix(player: Player, suffixRank: String) {
        val suffixItem = ItemStack(Material.PAPER)
        val meta = suffixItem.itemMeta
        meta.displayName(Component.text(SuffixChanger.getRawSuffix(suffixRank)))
        suffixItem.itemMeta = meta
        var text = ""
        val guiBuilder = Gui.builder()
            .setStructure("Z # X")
            .addIngredient('Z', suffixItem)
            .addIngredient(
                'X',
                Item.builder().setItemProvider(
                    ItemBuilder(Material.ARROW).setCustomName(
                        SuffixChanger.getRawSuffix(suffixRank).toSuffixComponent()
                    )
                ).addClickHandler {
                    inventory(player, suffixRank, ConfirmAction.EDIT, text)
                })
        AnvilWindow.builder()
            .addRenameHandler { newText -> text = newText }
            .setTitle("Change Suffix")
            .setUpperGui(guiBuilder)
            .open(player)
    }

    enum class ConfirmAction {
        REMOVE,
        CLEAR,
        EDIT,
        TRACK
    }
}
