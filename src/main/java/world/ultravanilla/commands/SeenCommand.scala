package world.ultravanilla.commands

import world.ultravanilla.UltraVanilla
import world.ultravanilla.reference.Palette
import world.ultravanilla.reference.Users
import world.ultravanilla.serializable.Position
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util._

object SeenCommand {
  val COLOR = ChatColor.of("#fce192")
}

class SeenCommand(val instance: UltraVanilla) extends UltraCommand(instance) with CommandExecutor with TabExecutor {
  this.color = SeenCommand.COLOR

  def sendLastSeen(sender: CommandSender, player: OfflinePlayer, timeZone: TimeZone) = {
    val lastSeen = player.getLastPlayed
    sender.sendMessage(
      Palette.NOUN + player.getName + SeenCommand.COLOR + " was last seen on " + Palette.OBJECT + getDate(lastSeen, timeZone)
    )
    if (UltraVanilla.isStaff(sender)) {
      sendPromotionInfo(sender, player, timeZone)
      sendLocationInfo(sender, player)
    }
  }

  def sendFirstJoined(sender: CommandSender, player: OfflinePlayer, timeZone: TimeZone) = {
    val firstJoined = player.getFirstPlayed
    sender.sendMessage(
      Palette.NOUN + player.getName + SeenCommand.COLOR + " first joined on " + Palette.OBJECT + getDate(firstJoined, timeZone)
    )
  }

  def sendPromotionInfo(sender: CommandSender, player: OfflinePlayer, timeZone: TimeZone) = plugin.async(() => {
    sender.sendMessage(
      SeenCommand.COLOR + "Last promoted: " + Palette.OBJECT + getDate(
        UltraVanilla.getPlayerConfig(player).getLong("last-promotion"),
        timeZone
      )
    )
    val nextRole = plugin.getRoleShouldHave(player)
    if (nextRole != null) {
      sender.sendMessage(
        String.format(
          "%sNext promotion: %s%s",
          SeenCommand.COLOR,
          Palette.OBJECT,
          getDate(plugin.getNextRoleDate(player), timeZone)
        )
      )
      if (!plugin.hasRightRole(player))
        sender.sendMessage(
          String.format(
            "%sThey should be %s%s %sby now!",
            SeenCommand.COLOR,
            plugin.getRoleColor(nextRole),
            nextRole,
            SeenCommand.COLOR
          )
        )
    }
  })

  def sendLocationInfo(sender: CommandSender, player: OfflinePlayer) = {
    val position = UltraVanilla.getPlayerConfig(player).get(Users.LOGOUT_LOCATION).asInstanceOf[Position]
    if (position != null) {
      val textComponent = new TextComponent("Last logout location: ")
      textComponent.setColor(SeenCommand.COLOR)
      val component = new TextComponent
      component.setColor(ChatColor.WHITE)
      component.setText(position.toStringTrimmed)
      component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, position.getTpCommand))
      textComponent.addExtra(component)
      sender.sendMessage(textComponent)
    }
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    var timeZone = TimeZone.getDefault
    if (sender.isInstanceOf[Player]) {
      val player = sender.asInstanceOf[Player]
      val zone = UltraVanilla.getPlayerConfig(player.getUniqueId).getString("timezone")
      if (!(zone == null || zone.isEmpty)) timeZone = TimeZone.getTimeZone(zone)
    }
    val player = plugin.getServer.getOfflinePlayer(args(0))
    if (!player.hasPlayedBefore) {
      sender.sendMessage(Palette.NOUN + player.getName + SeenCommand.COLOR + " has not played here before.")
      return true
    }
    command.getName match {
      case "seen" =>
        if (args.length != 2) return false
        if (args(1).equalsIgnoreCase("first")) {
          sendFirstJoined(sender, player, timeZone)
          return true
        } else if (args(1).equalsIgnoreCase("last")) {
          sendLastSeen(sender, player, timeZone)
          return true
        } else return false
      case "firstjoined" =>
        sendFirstJoined(sender, player, timeZone)
        return true
      case "lastseen" =>
        sendLastSeen(sender, player, timeZone)
        return true
    }
    false
  }

  def getDate(time: Long, timezone: TimeZone) = if (time == 0) {
    "a long time ago"
  } else {
    val date = new Date(time)
    val df = new SimpleDateFormat(plugin.getCommandString("seen.format.date-format"))
    df.setTimeZone(timezone)
    df.format(date)
  }

  override def onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array[String]) = {
    val suggestions = new java.util.ArrayList[String]
    if (args.length == 1)
      java.util.Arrays
        .stream(plugin.getServer.getOfflinePlayers)
        .forEach((p) => suggestions.add(p.getName))
    else if (command.getName == "seen") {
      suggestions.add("first")
      suggestions.add("last")
    }
    getSuggestions(suggestions, args)
  }
}
