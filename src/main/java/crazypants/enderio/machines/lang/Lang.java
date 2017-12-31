package crazypants.enderio.machines.lang;

import javax.annotation.Nonnull;

import crazypants.enderio.base.lang.ILang;
import crazypants.enderio.machines.EnderIOMachines;

public enum Lang implements ILang {

  GUI_STIRGEN_OUTPUT(".gui.stirling_generator.output"),
  GUI_STIRGEN_RATE(".gui.stirling_generator.burnRate"),
  GUI_STIRGEN_SLOT(".gui.stirling_generator.upgradeslot"),
  GUI_STIRGEN_UPGRADES(".gui.stirling_generator.upgrades"),
  GUI_STIRGEN_REMAINING(".gui.stirling_generator.remaining"),
  GUI_ZOMBGEN_OUTPUT(".gui.zombie_generator.output"),
  GUI_ZOMBGEN_FTANK(".gui.zombie_generator.fuelTank"),
  GUI_ZOMBGEN_MINREQ(".gui.zombie_generator.fluid.minReq"),
  GUI_JOE_FTANK(".gui.killerJoe.fuelTank"),
  GUI_JOE_MINREQ(".gui.killerJoe.fluid.minReq"),
  GUI_COMBGEN_OUTPUT(".gui.combustion_generator.output"),
  GUI_ALLOY_MODE(".gui.alloy.mode.heading"),
  GUI_ALLOY_MODE_ALL(".gui.alloy.mode.all"),
  GUI_ALLOY_MODE_ALLOY(".gui.alloy.mode.alloy"),
  GUI_ALLOY_MODE_FURNACE(".gui.alloy.mode.furnace"),
  GUI_BUFFER_IN(".gui.buffer.in"),
  GUI_BUFFER_OUT(".gui.buffer.out"),
  GUI_FARM_BASEUSE(".gui.farm.baseUse"),
  GUI_SHOW_RANGE(".gui.ranged.showRange"),
  GUI_HIDE_RANGE(".gui.ranged.hideRange"),
  GUI_RANGE(".gui.ranged.range"),
  GUI_COMBGEN_CTANK(".gui.combustion_generator.coolantTank"),
  GUI_COMBGEN_CTANK_EMPTY(".gui.combustion_generator.coolantTank.empty"),
  GUI_COMBGEN_FTANK(".gui.combustion_generator.fuelTank"),
  GUI_COMBGEN_FTANK_EMPTY(".gui.combustion_generator.fuelTank.empty"),
  GUI_WEATHER_FTANK(".gui.weather_obelisk.fuelTank"),
  GUI_XP_STORE_1_1(".gui.xp_obelisk.button.store.1.line1"),
  GUI_XP_STORE_1_2(".gui.xp_obelisk.button.store.1.line2"),
  GUI_XP_STORE_10_1(".gui.xp_obelisk.button.store.10.line1"),
  GUI_XP_STORE_10_2(".gui.xp_obelisk.button.store.10.line2"),
  GUI_XP_STORE_ALL_1(".gui.xp_obelisk.button.store.all.line1"),
  GUI_XP_STORE_ALL_2(".gui.xp_obelisk.button.store.all.line2"),
  GUI_XP_STORE_EMPTY(".gui.xp_obelisk.button.store.empty"),
  GUI_XP_RETR_EMPTY(".gui.xp_obelisk.button.retrieve.empty"),
  GUI_XP_RETR_1_1(".gui.xp_obelisk.button.retrieve.1.line1"),
  GUI_XP_RETR_1_2(".gui.xp_obelisk.button.retrieve.1.line2"),
  GUI_XP_RETR_10_1(".gui.xp_obelisk.button.retrieve.10.line1"),
  GUI_XP_RETR_10_2(".gui.xp_obelisk.button.retrieve.10.line2"),
  GUI_XP_RETR_ALL_1(".gui.xp_obelisk.button.retrieve.all.line1"),
  GUI_XP_RETR_ALL_2(".gui.xp_obelisk.button.retrieve.all.line2"),
  GUI_XP_RETR_ALL_3(".gui.xp_obelisk.button.retrieve.all.line3"),
  SOLAR_MAXOUTPUT(".block_solar_panel.tooltip.maxoutput"),
  GUI_SOUL_USEPLAYERXP(".gui.soul_binder.useplayerxp"),
  SPAWNER_EMPTY(".block_powered_spawner.tooltip.empty"),
  GUI_SPAWNER_CAPTURE(".gui.powered_spawner.capture"),
  GUI_SPAWNER_SPAWN(".gui.powered_spawner.spawn"),
  GUI_TANK_TANK_TANK_TANK(".gui.tank.tank"),
  GUI_TANK_VOID_SLOT(".gui.tank.void.slot"),
  GUI_VAT_ITANK(".gui.vat.inputTank"),
  GUI_VAT_OTANK(".gui.vat.outputTank"),
  GUI_VAT_DUMP(".gui.vat.dump"),
  GUI_VAT_VOID(".gui.vat.void"),
  GUI_VAT_DUMP_FAIL(".chat.vat.dump.fail"),
  GUI_VAT_DUMP_ACTIVE(".chat.vat.dump.active"),
  GUI_VACUUM_RANGE_TOOLTIP(".gui.vacuum.range"),
  GUI_VACUUM_CHEST(".gui.vacuum.header.chest"),
  GUI_VACUUM_FILTER(".gui.vacuum.header.filter"),
  GUI_VACUUM_RANGE(".gui.vacuum.header.range"),
  GUI_VACUUM_INVENTORY("container.inventory"), // vanilla key
  GUI_TRANS_CHANNEL_PUBLIC(".gui.trans.publicChannel"),
  GUI_TRANS_CHANNEL_PRIVATE(".gui.trans.privateChannel"),
  GUI_TRANS_CHANNEL_ADD(".gui.trans.addChannel"),
  GUI_TRANS_CHANNEL_DELETE(".gui.trans.deleteChannel"),
  GUI_TRANS_AVAILABLE(".gui.trans.available"),
  GUI_TRANS_RECEIVE(".gui.trans.receive"),
  GUI_TRANS_SEND(".gui.trans.send"),
  GUI_TRANS_BUFFER_LOCAL(".gui.trans.buffer.local"),
  GUI_TRANS_BUFFER_UPKEEP(".gui.trans.buffer.local.upkeep"),
  GUI_TRANS_BUFFER_SHARED(".trans.buffer.shared"),
  GUI_TRANS_BUFFER_MAXIO(".gui.trans.buffer.shared.maxIo"),
  GUI_TRANS_BUFFER_STACKS(".gui.trans.buffer.item.stacks"),
  GUI_TRANS_BUFFER_SINGLES(".gui.trans.buffer.item.single"),
  GUI_TRANS_CHANNEL_RECEIVE(".gui.trans.channel.receive"),
  GUI_TRANS_CHANNEL_SEND(".gui.trans.channel.send"),
  GUI_AUTH_PUBLIC(".gui.travel_accessable.public"),
  GUI_AUTH_PRIVATE(".gui.travel_accessable.private"),
  GUI_AUTH_PROTECTED(".gui.travel_accessable.protected"),
  GUI_AUTH_PROMPT(".gui.travel_accessable.prompt"),
  GUI_AUTH_PROMPT_BUTTON(".gui.travel_accessable.promp.login"),
  GUI_AUTH_ERROR_PRIVATE(".chat.travel_accessable.error.private"),
  GUI_TELEPAD_NOFLUID(".chat.telepad.noFluid"),
  GUI_TELEPAD_TO_TRAVEL(".gui.telepad.configure.travel"),
  GUI_TELEPAD_TO_MAIN(".gui.telepad.configure.telepad"),
  GUI_TELEPAD_ERROR_BLOCKED(".gui.telepad.blocked"),
  GUI_TELEPAD_TELEPORT(".gui.telepad.teleport"),
  GUI_TELEPAD_TANK(".gui.telepad.tank"),
  GUI_VANILLA_REPAIR_COST("container.repair.cost"), // vanilla key
  GUI_TELEPAD_MAX(".gui.telepad.max"),
  JEI_SAGMILL_CHANCE(".jei.sagmill.outputchance"),
  JEI_COMBGEN_RANGE(".jei.combustion_generator.range"),
  GUI_BUFFER_MAXIO(".gui.buffer.maxio"),

  ;

  private final @Nonnull String key;

  private Lang(@Nonnull String key) {
    if (key.startsWith(".")) {
      this.key = getLang().addPrefix(key.substring(1));
    } else {
      this.key = key;
    }
  }

  @Override
  public @Nonnull String getKey() {
    return key;
  }

  @Override
  @Nonnull
  public com.enderio.core.common.Lang getLang() {
    return EnderIOMachines.lang;
  }

  static {
    for (Lang text : values()) {
      text.checkTranslation();
    }
  }

}