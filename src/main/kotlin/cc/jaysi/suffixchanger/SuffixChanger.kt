package cc.jaysi.suffixchanger

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.types.DisplayNameNode
import net.luckperms.api.node.types.SuffixNode
import net.luckperms.api.node.types.WeightNode
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class SuffixChanger : JavaPlugin(), CommandExecutor {
    companion object {
        lateinit var api: LuckPerms
        lateinit var oConfig: FileConfiguration
        const val BSTATS_ID = 23488

        fun getRawSuffix(roup: String): String {
            return api.groupManager.getGroup(roup)!!.cachedData.metaData.suffix.toString()
        }

        fun getSuffix(roup: String): String {
            return "&r&f" + getRawSuffix(roup)
        }

        fun addPermission(user: User, permission: String) {
            // Add the permission
            user.data().add(Node.builder(permission).build())

            // Now we need to save changes.
            api.userManager.saveUser(user)
        }

        fun removePermission(user: User, permission: String) {
            // Add the permission
            user.data().remove(Node.builder(permission).build())

            // Now we need to save changes.
            api.userManager.saveUser(user)
        }

        fun getUser(player: Player): User {
            return api.getPlayerAdapter(Player::class.java).getUser(player)
        }

        fun String.stripSuffix(): String {
            val match = Regex("&(.) +").find(this) ?: return this
            if (match.groupValues[1].isEmpty()) return this
            return this.replaceFirst(Regex("&(.) +"), "&${match.groupValues[1]}")
        }

        fun Component.unitalic(): Component {
            return this.decoration(TextDecoration.ITALIC, false)
        }

        fun String.toSuffixComponent(): Component {
            return Component.empty().unitalic().color(NamedTextColor.WHITE)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(this.stripSuffix()))
        }
    }

    override fun onEnable() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
            if (provider != null) {
                oConfig = config
                config.addDefault(
                    "title",
                    "<gradient:dark_red:red>Suffix</gradient><gradient:dark_blue:blue>Changer</gradient>"
                )
                config.setInlineComments(
                    "title",
                    listOf("changes the title that is used for the GUI, supports MiniMessage")
                )
                config.addDefault("watermark", true)
                config.setInlineComments(
                    "watermark",
                    listOf("set to false to disable the watermark under the leftover buttons")
                )
                config.addDefault("obfuscate", true)
                config.setInlineComments(
                    "obfuscate",
                    listOf("set to true to obfuscate suffixes that the players isn't allowed to use")
                )
                config.addDefault("track", "suffixes")
                config.setInlineComments("track", listOf("advanced, determines which track to use for suffixes"))
                config.addDefault("group_weight", 3)
                config.setInlineComments(
                    "group_weight",
                    listOf("advanced, determines what weight the group should have in Luckperms when using /addsuffix")
                )
                config.options().copyDefaults(true)
                saveConfig()
                api = provider.provider
                this.getCommand("suffix")!!.setExecutor(this)
                this.getCommand("addsuffix")!!.setExecutor(this)
                this.getCommand("reloadsuffixchanger")!!.setExecutor(this)

                AddSuffixGui.plugin = this

                val metrics = Metrics(this, BSTATS_ID)
                metrics.addCustomChart(
                    SimplePie(
                        "watermark"
                    ) { if (config.getBoolean("watermark")) "true" else "false" })
                metrics.addCustomChart(
                    SimplePie(
                        "obfuscate"
                    ) { if (config.getBoolean("obfuscate")) "true" else "false" })
                metrics.addCustomChart(
                    SimplePie(
                        "title"
                    ) { if (config.getString("title") == "<gradient:dark_red:red>Suffix</gradient><gradient:dark_blue:blue>Changer</gradient>") "default" else "custom" })
                metrics.addCustomChart(
                    SimplePie(
                        "luckperms"
                    ) { provider.plugin.pluginMeta.version })
                metrics.addCustomChart(
                    SimplePie(
                        "vault"
                    ) { Bukkit.getPluginManager().isPluginEnabled("Vault").toString() })
                metrics.addCustomChart(
                    SimplePie(
                        "suffix_count"
                    ) {
                        (api.trackManager.getTrack(config.getString("track") ?: "suffixes")?.groups?.size
                            ?: 0).toString()
                    })
                metrics.addCustomChart(
                    SimplePie(
                        "track"
                    ) { if (config.getString("track") == "suffixes") "default" else "custom" })
                metrics.addCustomChart(
                    SimplePie(
                        "default_weight"
                    ) { config.getInt("group_weight").toString() })
            } else {
                logger.warning("Could not find LuckPerms! This plugin is required.")
                Bukkit.getPluginManager().disablePlugin(this)
            }
        } else {
            /*
             * We inform about the fact that LuckPerms isn't installed and then
             * disable this plugin to prevent issues.
             */
            logger.warning("Could not find LuckPerms! This plugin is required.")
            Bukkit.getPluginManager().disablePlugin(this)
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name.lowercase() == "suffix") {
                if (!sender.hasPermission("suffixchanger.suffix")) {
                    sender.sendMessage(Bukkit.permissionMessage())
                    return true
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(this) {
                    SuffixGui.inventory(sender)
                }
                return true
            }
        }

        if (command.name.lowercase() == "reloadsuffixchanger" || command.name.lowercase() == "suffixchangerreload") {
            if (!sender.hasPermission("suffixchanger.reload") && sender !is ConsoleCommandSender) {
                sender.sendMessage(Bukkit.permissionMessage())
                return true
            }

            reloadConfig()
            saveDefaultConfig()
            oConfig = config
            sender.sendMessage(Component.text("Reloaded SuffixChanger!", NamedTextColor.GREEN))
        }

        if (command.name.lowercase() == "addsuffix") {
            if (!sender.hasPermission("suffixchanger.admin") && sender !is ConsoleCommandSender) {
                sender.sendMessage(Bukkit.permissionMessage())
                return true
            }
            if (args.size < 2) {
                return false
            }

            var suffix = ""
            var i = 1
            while (i < args.size - 1) {
                suffix += "${args[i]} "
                i++
            }
            suffix += args.last()

            api.groupManager.createAndLoadGroup(args[0].lowercase()).thenRun {
                api.groupManager.modifyGroup(args[0].lowercase()) {
                    it.data().add(DisplayNameNode.builder("Suffix: ${args[0].lowercase()}").build())
                    it.data().add(WeightNode.builder(config.getInt("weight")).build())
                    it.data().add(SuffixNode.builder(suffix, 3).build())
                }
                api.trackManager.getTrack(config.getString("track") ?: "suffixes")!!
                    .appendGroup(api.groupManager.getGroup(args[0].lowercase())!!)
                api.trackManager.saveTrack(api.trackManager.getTrack(config.getString("track") ?: "suffixes")!!)
                sender.sendMessage(Component.text("Successfully created group ${args[0].lowercase()} with suffix ${suffix.stripSuffix()}"))
            }
        }

        return true
    }
}
