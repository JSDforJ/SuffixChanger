package cc.jaysi.suffixchanger

import cc.jaysi.suffixchanger.SuffixChanger.Companion.api
import cc.jaysi.suffixchanger.SuffixChanger.Companion.stripSuffix
import cc.jaysi.suffixchanger.SuffixChanger.Companion.toSuffixComponent
import cc.jaysi.suffixchanger.SuffixChanger.Companion.unitalic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.node.types.DisplayNameNode
import net.luckperms.api.node.types.SuffixNode
import net.luckperms.api.node.types.WeightNode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.SmithingWindow

object AddSuffixGui {
    lateinit var plugin: SuffixChanger

    data class NewRankData(
        var rankName: String? = null,
        var suffix: String? = null,
        var weight: Int = SuffixChanger.oConfig.getInt("group_weight")
    )

    private val data: HashMap<Player, NewRankData> = HashMap<Player, NewRankData>()

    fun getNameItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.SPRUCE_SIGN)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (data[player]?.rankName == null) {
            meta.displayName(Component.text("Change Name", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            meta.setEnchantmentGlintOverride(true)
        } else {
            meta.displayName(Component.text(data[player]?.rankName!!).decoration(TextDecoration.ITALIC, false))
        }
        stack.itemMeta = meta
        return stack
    }

    fun getSuffixItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.NAME_TAG)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (data[player]?.suffix == null) {
            meta.displayName(
                Component.text("Change Suffix", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
            )
            meta.setEnchantmentGlintOverride(true)
        } else {
            meta.displayName(
                Component.empty().decoration(TextDecoration.ITALIC, false).append(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(data[player]?.suffix!!.stripSuffix())
                )
            )
        }
        stack.itemMeta = meta
        return stack
    }

    fun getWeightItemStack(player: Player): ItemStack {
        val stack = ItemStack.of(Material.ANVIL)
        val meta = stack.itemMeta
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.displayName(Component.text(data[player]?.weight ?: 3).decoration(TextDecoration.ITALIC, false))
        stack.itemMeta = meta
        return stack
    }

    private fun getConfirmItemStack(player: Player): ItemStack {
        if (data[player]?.rankName == null || data[player]?.suffix == null) {
            val stack = ItemStack.of(Material.RED_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.RED).decoration(TextDecoration.BOLD, true).unitalic()
            )
            val lore = ArrayList<Component>()
            if (data[player]?.rankName == null) {
                lore.add(Component.text("You need to select a name for the rank.", NamedTextColor.RED).unitalic())
            }
            if (data[player]?.suffix == null) {
                lore.add(Component.text("You need to select a suffix.", NamedTextColor.RED).unitalic())
            }
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        } else if (api.trackManager.getTrack(
                SuffixChanger.oConfig.getString("track") ?: "suffixes"
            )?.groups!!.any { it == data[player]?.rankName }
        ) {
            val stack = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true).unitalic()
            )
            val lore = ArrayList<Component>()
            lore.add(Component.text("This rank already exists!", NamedTextColor.RED).unitalic())
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        } else {
            val stack = ItemStack.of(Material.GREEN_STAINED_GLASS_PANE)
            val meta = stack.itemMeta

            meta.displayName(
                Component.text("Confirm Suffix", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true).unitalic()
            )
            meta.setEnchantmentGlintOverride(true)
            val lore = ArrayList<Component>()
            lore.add(
                Component.text("Rank name: ", NamedTextColor.AQUA).unitalic()
                    .append(Component.text(data[player]?.rankName!!, NamedTextColor.WHITE))
            )
            lore.add(
                Component.text("Suffix:", NamedTextColor.AQUA).unitalic().append(
                    Component.text(
                        " ",
                        NamedTextColor.WHITE
                    )
                ).append(LegacyComponentSerializer.legacyAmpersand().deserialize(data[player]?.suffix!!.stripSuffix()))
            )
            lore.add(
                Component.text("Weight: ", NamedTextColor.AQUA).unitalic()
                    .append(Component.text(data[player]?.weight.toString(), NamedTextColor.WHITE))
            )
            meta.lore(lore)

            stack.itemMeta = meta
            return stack
        }
    }

    fun inventory(player: Player, isDoing: Boolean = false) {
        if (!isDoing) data[player] = NewRankData()

        val guiBuilder = Gui.builder()
            .setStructure("X Y Z C")
            .addIngredient('X', Item.builder().setItemProvider(getNameItemStack(player)).addClickHandler {
                selectName(player)
            })
            .addIngredient('Y', Item.builder().setItemProvider(getSuffixItemStack(player)).addClickHandler {
                selectSuffix(player)
            })
            .addIngredient('Z', Item.builder().setItemProvider(getWeightItemStack(player)).addClickHandler {
                selectWeight(player)
            })
            .addIngredient('C', Item.builder().setItemProvider(getConfirmItemStack(player)).addClickHandler {
                if (!player.hasPermission("suffixchanger.admin")) {
                    player.sendMessage(
                        Component.text(
                            "You do not have permission to use this command.",
                            NamedTextColor.DARK_RED
                        )
                    )
                } else {
                    val dat = data[player]!!
                    api.groupManager.createAndLoadGroup(dat.rankName!!.lowercase()).thenRun {
                        api.groupManager.modifyGroup(dat.rankName!!.lowercase()) {
                            it.data().add(DisplayNameNode.builder("Suffix: ${dat.rankName!!.lowercase()}").build())
                            it.data().add(WeightNode.builder(dat.weight).build())
                            it.data().add(SuffixNode.builder(dat.suffix!!, 3).build())
                        }
                        api.trackManager.getTrack(SuffixChanger.oConfig.getString("track") ?: "suffixes")!!
                            .appendGroup(api.groupManager.getGroup(dat.rankName!!.lowercase())!!)
                        api.trackManager.saveTrack(
                            api.trackManager.getTrack(
                                SuffixChanger.oConfig.getString("track") ?: "suffixes"
                            )!!
                        )
                        player.sendMessage(
                            Component.text(
                                "Successfully created group ${dat.rankName!!.lowercase()} with suffix ",
                                NamedTextColor.WHITE
                            ).append(dat.suffix!!.toSuffixComponent()).append(
                                Component.text(" and weight ${dat.weight}", NamedTextColor.WHITE)
                            )
                        )
                        data.remove(player)
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            if (player.isOnline) player.closeInventory()
                        })
                    }
                }
            })

        SmithingWindow.builder()
            .setTitle(Component.text("Add Suffix"))
            .setUpperGui(guiBuilder)
            .open(player)
    }

    private fun selectName(player: Player) {
        val nameItem = ItemStack(Material.PAPER)
        if (data[player]?.rankName != null) {
            val meta = nameItem.itemMeta
            meta.displayName(data[player]?.rankName?.let { Component.text(it, NamedTextColor.WHITE) })
            nameItem.itemMeta = meta
        }
        val guiBuilder = Gui.builder()
            .setStructure("Z # X")
            .addIngredient('Z', nameItem)
            .addIngredient(
                'X',
                Item.builder().setItemProvider(ItemBuilder(Material.ARROW).setCustomName("Return")).addClickHandler {
                    inventory(player, true)
                })
        AnvilWindow.builder()
            .addRenameHandler { text -> data[player]?.rankName = text }
            .setTitle("Change Rank Name")
            .setUpperGui(guiBuilder)
            .open(player)
    }

    private fun selectSuffix(player: Player) {
        val guiBuilder = Gui.builder()
            .setStructure("Z # X")
            .addIngredient('Z', getSuffixItemStack(player))
            .addIngredient(
                'X',
                Item.builder().setItemProvider(ItemBuilder(Material.ARROW).setCustomName("Return")).addClickHandler {
                    inventory(player, true)
                })
        AnvilWindow.builder()
            .addRenameHandler { text -> data[player]?.suffix = text }
            .setTitle("Change Suffix")
            .setUpperGui(guiBuilder)
            .open(player)
    }

    private fun selectWeight(player: Player) {
        val guiBuilder = Gui.builder()
            .setStructure("Z # X")
            .addIngredient('Z', getWeightItemStack(player))
            .addIngredient(
                'X',
                Item.builder().setItemProvider(ItemBuilder(Material.ARROW).setCustomName("Return")).addClickHandler {
                    inventory(player, true)
                })
        AnvilWindow.builder()
            .addRenameHandler { text -> data[player]?.weight = text.toInt() }
            .setTitle("Change Rank Weight")
            .setUpperGui(guiBuilder)
            .open(player)
    }
}
