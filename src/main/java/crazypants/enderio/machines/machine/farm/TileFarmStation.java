package crazypants.enderio.machines.machine.farm;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.vecmath.Vector4f;
import com.mojang.authlib.GameProfile;

import crazypants.enderio.base.capacitor.DefaultCapacitorData;
import crazypants.enderio.base.config.Config;
import crazypants.enderio.base.farming.FarmNotification;
import crazypants.enderio.base.farming.FarmingAction;
import crazypants.enderio.base.farming.FarmingTool;
import crazypants.enderio.base.farming.IFarmer;
import crazypants.enderio.base.farming.farmers.IHarvestResult;
import crazypants.enderio.base.farming.registry.Commune;
import crazypants.enderio.base.machine.baselegacy.AbstractPoweredTaskEntity;
import crazypants.enderio.base.machine.baselegacy.SlotDefinition;
import crazypants.enderio.base.machine.fakeplayer.FakePlayerEIO;
import crazypants.enderio.base.machine.interfaces.IPoweredTask;
import crazypants.enderio.base.machine.task.ContinuousTask;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.base.power.PowerHandlerUtil;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.IMachineRecipe.ResultStack;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.render.ranged.IRanged;
import crazypants.enderio.base.render.ranged.RangeParticle;
import crazypants.enderio.machines.network.PacketHandler;
import crazypants.enderio.util.Prep;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.BlockPosContext;

import static crazypants.enderio.base.config.Config.farmEvictEmptyRFTools;
import static crazypants.enderio.base.config.Config.farmStopOnNoOutputSlots;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_BASE_SIZE;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_BONUS_SIZE;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_POWER_BUFFER;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_POWER_INTAKE;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_POWER_USE;
import static crazypants.enderio.machines.capacitor.CapacitorKey.FARM_STACK_LIMIT;

@Storable
public class TileFarmStation extends AbstractPoweredTaskEntity implements IFarmer, IPaintable.IPaintableTileEntity, IRanged {

  private static final int TICKS_PER_WORK = 20;

  private BlockPos lastScanned;
  private FakePlayerEIO farmerJoe;
  private static GameProfile FARMER_PROFILE = new GameProfile(UUID.fromString("c1ddfd7f-120a-4437-8b64-38660d3ec62d"), "[EioFarmer]");

  public static final int NUM_TOOL_SLOTS = 3;

  private static final int minToolSlot = 0;
  private static final int maxToolSlot = -1 + NUM_TOOL_SLOTS;

  public static final int NUM_FERTILIZER_SLOTS = 2;

  public static final int minFirtSlot = maxToolSlot + 1;
  public static final int maxFirtSlot = maxToolSlot + NUM_FERTILIZER_SLOTS;

  public static final int NUM_SUPPLY_SLOTS = 4;

  public static final int minSupSlot = maxFirtSlot + 1;
  public static final int maxSupSlot = maxFirtSlot + NUM_SUPPLY_SLOTS;

  @Store
  private int lockedSlots = 0x00;

  public Set<FarmNotification> notification = EnumSet.noneOf(FarmNotification.class);
  public boolean sendNotification = false;

  private boolean wasActive;

  public TileFarmStation() {
    super(new SlotDefinition(9, 6, 1), FARM_POWER_INTAKE, FARM_POWER_BUFFER, FARM_POWER_USE);
  }

  @Override
  public int getFarmSize() {
    return (int) (FARM_BASE_SIZE.getFloat(getCapacitorData()) + FARM_BONUS_SIZE.getFloat(getCapacitorData()));
  }

  public int getFarmBaseSize() {
    return (int) (FARM_BASE_SIZE.getFloat(DefaultCapacitorData.BASIC_CAPACITOR) + FARM_BONUS_SIZE.getFloat(DefaultCapacitorData.BASIC_CAPACITOR));
  }

  public void actionPerformed(boolean isAxe) {
    if (isAxe) {
      usePower(Config.farmAxeActionEnergyUseRF);
    } else {
      usePower(Config.farmActionEnergyUseRF);
    }
    clearNotification();
  }

  @Override
  public boolean tillBlock(@Nonnull BlockPos plantingLocation) {
    BlockPos dirtLoc = plantingLocation.down();
    Block dirtBlock = getBlock(dirtLoc);
    if (dirtBlock == Blocks.FARMLAND) {
      return true;
    } else {
      ItemStack tool = getTool(FarmingTool.HOE);
      if (Prep.isInvalid(tool)) {
        if (dirtBlock == Blocks.DIRT || dirtBlock == Blocks.GRASS) {
          setNotification(FarmNotification.NO_HOE);
        }
        // else we don't know if the ground can even be tilled, so no notification
        return false;
      }

      boolean doDamage = world.rand.nextFloat() < Config.farmToolTakeDamageChance && canDamage(tool);
      if (!doDamage) {
        tool = tool.copy();
      }

      int origDamage = tool.getItemDamage();
      EnumActionResult itemUse = tool.getItem().onItemUse(farmerJoe, world, dirtLoc, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5f, 0.5f, 0.5f);

      if (itemUse != EnumActionResult.SUCCESS) {
        return false;
      }

      if (doDamage) {
        if (origDamage == tool.getItemDamage()) {
          tool.damageItem(1, farmerJoe);
        }

        if (Prep.isInvalid(tool) || tool.getCount() == 0 || tool.getItemDamage() >= tool.getMaxDamage()) { // TODO 1.11
          destroyTool(FarmingTool.HOE);
          markDirty();
        }
      }

      world.playSound(dirtLoc.getX() + 0.5F, dirtLoc.getY() + 0.5F, dirtLoc.getZ() + 0.5F, SoundEvents.BLOCK_GRASS_STEP, SoundCategory.BLOCKS,
          (Blocks.FARMLAND.getSoundType().getVolume() + 1.0F) / 2.0F, Blocks.FARMLAND.getSoundType().getPitch() * 0.8F, false);
      actionPerformed(false);
      return true;
    }
  }

  public int getMaxLootingValue() {
    int result = 0;
    for (int i = minToolSlot; i <= maxToolSlot; i++) {
      if (Prep.isValid(inventory[i])) {
        int level = getLootingValue(inventory[i]);
        if (level > result) {
          result = level;
        }
      }
    }
    return result;
  }

  public boolean hasHoe() {
    return hasTool(FarmingTool.HOE);
  }

  public boolean hasAxe() {
    return hasTool(FarmingTool.AXE);
  }

  public boolean hasShears() {
    return hasTool(FarmingTool.SHEARS);
  }

  public int getAxeLootingValue() {
    return getLootingValue(FarmingTool.AXE);
  }

  // public void damageAxe(Block blk, BlockPos bc) {
  // damageTool(FarmingTool.AXE, blk, bc, 1);
  // }
  //
  // public void damageHoe(int i, BlockPos bc) {
  // damageTool(FarmingTool.HOE, null, bc, i);
  // }
  //
  // public void damageShears(Block blk, BlockPos bc) {
  // damageTool(FarmingTool.SHEARS, blk, bc, 1);
  // }

  @Override
  public boolean hasTool(@Nonnull FarmingTool type) {
    return getTool(type) != null;
  }

  private boolean isDryRfTool(ItemStack stack) {
    if (!farmEvictEmptyRFTools || Prep.isInvalid(stack)) {
      return false;
    }
    IEnergyStorage cap = PowerHandlerUtil.getCapability(stack, null);
    if (cap == null) {
      return false;
    }
    return cap.getMaxEnergyStored() > 0 && cap.getEnergyStored() <= 0;
  }

  @Override
  public @Nonnull ItemStack getTool(@Nonnull FarmingTool type) {
    for (int i = minToolSlot; i <= maxToolSlot; i++) {
      if (FarmingTool.isBrokenTinkerTool(inventory[i]) || isDryRfTool(inventory[i])) {
        for (int j = slotDefinition.minOutputSlot; j <= slotDefinition.maxOutputSlot; j++) {
          if (Prep.isInvalid(inventory[j])) {
            inventory[j] = inventory[i];
            inventory[i] = Prep.getEmpty();
            markDirty();
            break;
          }
        }
      } else if (type.itemMatches(inventory[i]) && inventory[i].getCount() > 0) {
        switch (type) {
        case AXE:
          removeNotification(FarmNotification.NO_AXE);
          break;
        case HOE:
          removeNotification(FarmNotification.NO_HOE);
          break;
        case TREETAP:
          removeNotification(FarmNotification.NO_TREETAP);
          break;
        default:
          break;
        }
        return inventory[i];
      }
    }
    return Prep.getEmpty();
  }

  // @Override
  // TODO what is this needed for ?
  public void damageTool(FarmingTool type, Block blk, BlockPos bc, int damage) {
    ItemStack tool = getTool(type);
    if (Prep.isInvalid(tool)) {
      return;
    }

    float rand = world.rand.nextFloat();
    if (rand >= Config.farmToolTakeDamageChance) {
      return;
    }

    IBlockState bs = getBlockState(bc);

    boolean canDamage = canDamage(tool);
    if (type == FarmingTool.AXE) {
      tool.getItem().onBlockDestroyed(tool, world, bs, bc, farmerJoe);
    } else if (type == FarmingTool.HOE) {
      int origDamage = tool.getItemDamage();
      tool.getItem().onItemUse(farmerJoe, world, bc, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5f, 0.5f, 0.5f);
      if (origDamage == tool.getItemDamage() && canDamage) {
        tool.damageItem(1, farmerJoe);
      }
    } else if (canDamage) {
      tool.damageItem(1, farmerJoe);
    }

    if (Prep.isInvalid(tool) || tool.getCount() == 0 || (canDamage && tool.getItemDamage() >= tool.getMaxDamage())) {
      destroyTool(type);
      markDirty();
    }
  }

  private boolean canDamage(ItemStack stack) {
    return Prep.isValid(stack) && stack.isItemStackDamageable() && stack.getItem().isDamageable();
  }

  // TODO 1.11 clean up
  private void destroyTool(FarmingTool type) {
    for (int i = minToolSlot; i <= maxToolSlot; i++) {
      if (Prep.isValid(inventory[i]) && type.itemMatches(inventory[i]) && inventory[i].getCount() == 0) {
        inventory[i] = Prep.getEmpty();
        return;
      }
    }
  }

  @Override
  public int getLootingValue(@Nonnull FarmingTool tool) {
    return getLootingValue(getTool(tool));
  }

  private int getLootingValue(ItemStack stack) {
    return Math.max(EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack), EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
  }

  @Override
  public @Nonnull FakePlayerEIO getFakePlayer() {
    return farmerJoe;
  }

  public @Nonnull Block getBlock(@Nonnull BlockPos posIn) {
    return getBlockState(posIn).getBlock();
  }

  @Override
  public @Nonnull IBlockState getBlockState(@Nonnull BlockPos posIn) {
    return world.getBlockState(posIn);
  }

  public boolean isOpen(BlockPos bc) {
    Block block = getBlock(bc);
    IBlockState bs = getBlockState(bc);
    return block.isAir(bs, world, bc) || block.isReplaceable(world, bc);
  }

  @Override
  public void setNotification(@Nonnull FarmNotification note) {
    if (!notification.contains(note)) {
      notification.add(note);
      sendNotification = true;
    }
  }

  public void removeNotification(FarmNotification note) {
    if (notification.remove(note)) {
      sendNotification = true;
    }
  }

  @Override
  public void clearNotification() {
    if (hasNotification()) {
      notification.clear();
      sendNotification = true;
    }
  }

  public boolean hasNotification() {
    return !notification.isEmpty();
  }

  private void sendNotification() {
    PacketHandler.sendToAllAround(new PacketUpdateNotification(this, notification), this);
  }

  @Override
  public boolean isMachineItemValidForSlot(int i, @Nonnull ItemStack stack) {
    if (Prep.isInvalid(stack)) {
      return false;
    }
    if (i <= maxToolSlot) {
      if (FarmingTool.isTool(stack)) {
        for (int j = minToolSlot; j <= maxToolSlot; j++) {
          if (FarmingTool.getToolType(stack).itemMatches(inventory[j])) {
            return false;
          }
        }
        return true;
      }
      return false;
    } else if (i <= maxFirtSlot) {
      return Fertilizer.isFertilizer(stack);
    } else if (i <= maxSupSlot) {
      return (Prep.isValid(inventory[i]) || !isSlotLocked(i)) && Commune.instance.canPlant(stack);
    } else {
      return false;
    }
  }

  @Override
  public void doUpdate() {
    super.doUpdate();
    if (isActive() != wasActive) {
      wasActive = isActive();
      world.checkLightFor(EnumSkyBlock.BLOCK, pos);
    }
  }

  @Override
  protected boolean checkProgress(boolean redstoneChecksPassed) {
    if (shouldDoWorkThisTick(6 * 60 * 20)) {
      clearNotification();
    }
    if (redstoneChecksPassed) {
      usePower();
      if (canTick(redstoneChecksPassed)) {
        doTick();
      }
    }
    return false;
  }

  protected boolean canTick(boolean redstoneChecksPassed) {
    if (!shouldDoWorkThisTick(2)) {
      return false;
    }
    if (getEnergyStored() < getPowerUsePerTick()) {
      setNotification(FarmNotification.NO_POWER);
      return false;
    }
    return true;
  }

  protected void doTick() {
    if (sendNotification && shouldDoWorkThisTick(TICKS_PER_WORK)) {
      sendNotification = false;
      sendNotification();
    }

    if (!hasPower() && Config.farmActionEnergyUseRF > 0 && Config.farmAxeActionEnergyUseRF > 0) {
      setNotification(FarmNotification.NO_POWER);
      return;
    }
    removeNotification(FarmNotification.NO_POWER);

    if (farmerJoe == null) {
      farmerJoe = new FakePlayerEIO(world, getLocation(), FARMER_PROFILE);
      farmerJoe.setOwner(getOwner());
      farmerJoe.world = new PickupWorld(world, farmerJoe);
    }

    BlockPos bc = null;
    IBlockState bs = null;
    int infiniteLoop = 20;
    while (bc == null || bc.equals(getPos()) || !world.isBlockLoaded(bc)
        || !PermissionAPI.hasPermission(getOwner().getAsGameProfile(), BlockFarmStation.permissionFarming, new BlockPosContext(farmerJoe, bc, bs, null))) {
      if (infiniteLoop-- <= 0) {
        return;
      }
      bc = getNextCoord();
      bs = getBlockState(bc);
    }

    Block block = bs.getBlock();

    if (isOpen(bc)) {
      Commune.instance.prepareBlock(this, bc, block, bs);
      bs = getBlockState(bc);
      block = bs.getBlock();
    }

    if (isOutputFull()) {
      setNotification(FarmNotification.OUTPUT_FULL);
      return;
    }
    removeNotification(FarmNotification.OUTPUT_FULL);

    if (!hasPower() && Config.farmActionEnergyUseRF > 0 && Config.farmAxeActionEnergyUseRF > 0) {
      setNotification(FarmNotification.NO_POWER);
      return;
    }

    if (!isOpen(bc)) {
      IHarvestResult harvest = Commune.instance.harvestBlock(this, bc, block, bs);
      if (harvest != null) {
        boolean done = false;
        if (harvest.getHarvestedBlocks() != null && !harvest.getHarvestedBlocks().isEmpty()) {
          PacketFarmAction pkt = new PacketFarmAction(harvest.getHarvestedBlocks());
          PacketHandler.sendToAllAround(pkt, this);
          done = true;
        }
        if (harvest.getDrops() != null) {
          for (EntityItem ei : harvest.getDrops()) {
            if (ei != null) {
              insertHarvestDrop(ei, bc);
              if (!ei.isDead) {
                world.spawnEntity(ei);
              }
              done = true;
            }
          }
        }
        if (done) {
          return;
        }
      }
    }

    if (!hasPower() && (Config.farmBonemealActionEnergyUseRF > 0 || Config.farmBonemealTryEnergyUseRF > 0)) {
      setNotification(FarmNotification.NO_POWER);
      return;
    }

    if (hasBonemeal() && bonemealCooldown-- <= 0 && random.nextFloat() <= .75f) {
      Fertilizer fertilizer = Fertilizer.getInstance(inventory[minFirtSlot]);
      if ((fertilizer.applyOnPlant() != isOpen(bc)) || (fertilizer.applyOnAir() == world.isAirBlock(bc))) {
        startUsingItem(inventory[minFirtSlot]);
        if (fertilizer.apply(inventory[minFirtSlot], farmerJoe, world, bc)) {
          inventory[minFirtSlot] = endUsingItem(false).get(0); // FIXME ???
          PacketHandler.sendToAllAround(new PacketFarmAction(bc), this);
          if (Prep.isValid(inventory[minFirtSlot]) && inventory[minFirtSlot].getCount() == 0) {
            inventory[minFirtSlot] = Prep.getEmpty(); // TODO 1.11 remove
          }
          usePower(Config.farmBonemealActionEnergyUseRF);
          bonemealCooldown = 16;
        } else {
          usePower(Config.farmBonemealTryEnergyUseRF);
          bonemealCooldown = 4;
        }
        endUsingItem(false);
      }
    }
  }

  @Override
  public FakePlayerEIO startUsingItem(@Nonnull ItemStack item) {
    farmerJoe.inventory.mainInventory.set(0, item);
    farmerJoe.inventory.currentItem = 0;
    return farmerJoe;
  }

  @Override
  public FakePlayerEIO startUsingItem(@Nonnull FarmingTool tool) {
    return startUsingItem(getTool(tool));
  }

  @Override
  public @Nonnull NNList<ItemStack> endUsingItem(boolean trashHandItem) {
    NNList<ItemStack> ret = new NNList<>();
    if (!trashHandItem) {
      ret.add(farmerJoe.inventory.mainInventory.get(0));
      farmerJoe.inventory.mainInventory.set(0, Prep.getEmpty());
    }

    NonNullList<ItemStack> inv = farmerJoe.inventory.mainInventory;
    for (int slot = 0; slot < inv.size(); slot++) {
      ItemStack stack = inv.get(slot);
      if (Prep.isValid(stack)) {
        inv.set(slot, ItemStack.EMPTY);
        insertItem(stack);
      }
    }

    return ret;
  }

  // TODO this and startUsingItem could be default impl
  @Override
  public @Nonnull NNList<ItemStack> endUsingItem(@Nonnull FarmingTool tool) {
    return endUsingItem(false);
  }

  // TODO no idea what to do with these
  @Override
  public void handleExtraItems(@Nonnull NNList<ItemStack> items, @Nullable BlockPos pos) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean checkAction(@Nonnull FarmingAction action, @Nonnull FarmingTool tool) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void registerAction(@Nonnull FarmingAction action, @Nonnull FarmingTool tool) {
    // TODO Auto-generated method stub

  }

  @Override
  public void registerAction(@Nonnull FarmingAction action, @Nonnull FarmingTool tool, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isSlotLocked(@Nonnull BlockPos pos) {
    // TODO Auto-generated method stub
    return false;
  }

  private int bonemealCooldown = 4; // no need to persist this

  private boolean hasBonemeal() {
    if (Prep.isValid(inventory[minFirtSlot])) {
      return true;
    }
    for (int i = minFirtSlot + 1; i <= maxFirtSlot; i++) {
      if (Prep.isValid(inventory[i])) {
        inventory[minFirtSlot] = inventory[i];
        inventory[i] = Prep.getEmpty();
        return true;
      }
    }
    return false;
  }

  private boolean isOutputFull() {
    for (int i = slotDefinition.minOutputSlot; i <= slotDefinition.maxOutputSlot; i++) {
      ItemStack curStack = inventory[i];
      if (Prep.isInvalid(curStack) || (!farmStopOnNoOutputSlots && curStack.getCount() < curStack.getMaxStackSize())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasSeed(@Nonnull ItemStack seeds, @Nonnull BlockPos bc) {
    int slot = getSupplySlotForCoord(bc);
    ItemStack inv = inventory[slot];
    return Prep.isValid(inv) && (inv.getCount() > 1 || !isSlotLocked(slot)) && inv.isItemEqual(seeds);
  }

  /*
   * Returns a fuzzy boolean:
   * 
   * <=0 - break no leaves for saplings 50 - break half the leaves for saplings 90 - break 90% of the leaves for saplings
   */
  @Override
  public int isLowOnSaplings(@Nonnull BlockPos bc) {
    int slot = getSupplySlotForCoord(bc);
    ItemStack inv = inventory[slot];

    return 90 * (Config.farmSaplingReserveAmount - (Prep.isInvalid(inv) ? 0 : inv.getCount())) / Config.farmSaplingReserveAmount; // TODO 1.11 clean up
  }

  public @Nonnull ItemStack takeSeedFromSupplies(@Nonnull ItemStack stack, @Nonnull BlockPos forBlock) {
    return takeSeedFromSupplies(stack, forBlock, true);
  }

  public @Nonnull ItemStack takeSeedFromSupplies(ItemStack stack, BlockPos forBlock, boolean matchMetadata) {
    if (Prep.isInvalid(stack) || forBlock == null) {
      return null;
    }
    int slot = getSupplySlotForCoord(forBlock);
    ItemStack inv = inventory[slot];
    if (Prep.isValid(inv)) {
      if (matchMetadata ? inv.isItemEqual(stack) : inv.getItem() == stack.getItem()) {
        if (inv.getCount() <= 1 && isSlotLocked(slot)) {
          return null;
        }

        ItemStack result = inv.copy();
        result.setCount(1);

        inv = inv.copy();
        inv.shrink(1);
        if (inv.getCount() == 0) {
          inv = Prep.getEmpty(); // TODO 1.11 clean up
        }
        setInventorySlotContents(slot, inv);
        return result;
      }
    }
    return null;
  }

  @Override
  public @Nonnull ItemStack takeSeedFromSupplies(@Nonnull BlockPos bc) {
    return takeSeedFromSupplies(getSeedTypeInSuppliesFor(bc), bc);
  }

  @Override
  public @Nonnull ItemStack getSeedTypeInSuppliesFor(@Nonnull BlockPos bc) {
    int slot = getSupplySlotForCoord(bc);
    return getSeedTypeInSuppliesFor(slot);
  }

  public ItemStack getSeedTypeInSuppliesFor(int slot) {
    ItemStack inv = inventory[slot];
    if (Prep.isValid(inv) && (inv.getCount() > 1 || !isSlotLocked(slot))) {
      return inv.copy();
    }
    return Prep.getEmpty();
  }

  public int getSupplySlotForCoord(BlockPos forBlock) {
    int xCoord = getPos().getX();
    int zCoord = getPos().getZ();
    if (forBlock.getX() <= xCoord && forBlock.getZ() > zCoord) {
      return minSupSlot;
    } else if (forBlock.getX() > xCoord && forBlock.getZ() > zCoord - 1) {
      return minSupSlot + 1;
    } else if (forBlock.getX() < xCoord && forBlock.getZ() <= zCoord) {
      return minSupSlot + 2;
    }
    return minSupSlot + 3;
  }

  private void insertHarvestDrop(Entity entity, BlockPos bc) {
    if (!world.isRemote) {
      if (entity instanceof EntityItem && !entity.isDead) {
        EntityItem item = (EntityItem) entity;
        ItemStack stack = item.getEntityItem();
        if (Prep.isValid(stack)) {
          stack = stack.copy();
          int numInserted = insertResult(stack, bc);
          stack.shrink(numInserted);
          item.setEntityItemStack(stack);
          if (stack.getCount() == 0) {
            item.setDead();
          }
        } else {
          item.setDead();
        }
      }
    }
  }

  private void insertItem(ItemStack stack) {
    int numInserted = insertResult(stack, getPos());
    stack.shrink(numInserted);
    if (Prep.isValid(stack) && stack.getCount() > 0) {
      EntityItem entityItem = new EntityItem(getWorld(), getPos().getX() + .5, getPos().getY() + .5, getPos().getZ() + .5, stack);
      getWorld().spawnEntity(entityItem);
    }
  }

  private int insertResult(ItemStack stack, BlockPos bc) {
    int slot = bc != null ? getSupplySlotForCoord(bc) : minSupSlot;
    int[] slots = new int[NUM_SUPPLY_SLOTS];
    int k = 0;
    for (int j = slot; j <= maxSupSlot; j++) {
      slots[k++] = j;
    }
    for (int j = minSupSlot; j < slot; j++) {
      slots[k++] = j;
    }

    int origSize = stack.getCount();
    stack = stack.copy();

    int inserted = 0;
    for (int j = 0; j < slots.length && inserted < stack.getCount(); j++) {
      int i = slots[j];
      ItemStack curStack = inventory[i];
      int inventoryStackLimit = getInventoryStackLimit(i);
      if (isItemValidForSlot(i, stack)) {
        if (Prep.isInvalid(curStack)) {
          if (stack.getCount() < inventoryStackLimit) {
            inventory[i] = stack.copy();
            inserted = stack.getCount();
          } else {
            inventory[i] = stack.copy();
            inserted = inventoryStackLimit;
            inventory[i].setCount(inserted);
          }
        } else if (curStack.getCount() < inventoryStackLimit && curStack.isItemEqual(stack)) {
          inserted = Math.min(inventoryStackLimit - curStack.getCount(), stack.getCount());
          inventory[i].grow(inserted);
        }
      }
    }

    stack.shrink(inserted);
    if (inserted >= origSize) {
      return origSize;
    }

    ResultStack[] in = new ResultStack[] { new ResultStack(stack) };
    mergeResults(in);
    return origSize - (Prep.isInvalid(in[0].item) ? 0 : in[0].item.getCount());
  }

  private @Nonnull BlockPos getNextCoord() {
    int size = getFarmSize();

    BlockPos loc = getPos();
    if (lastScanned == null) {
      return lastScanned = NullHelper.notnullM(loc.add(-size, 0, -size), "BlockPos.add()");
    }

    int nextX = lastScanned.getX() + 1;
    int nextZ = lastScanned.getZ();
    if (nextX > loc.getX() + size) {
      nextX = loc.getX() - size;
      nextZ += 1;
      if (nextZ > loc.getZ() + size) {
        nextX = loc.getX() - size;
        nextZ = loc.getZ() - size;
      }
    }
    return lastScanned = new BlockPos(nextX, lastScanned.getY(), nextZ);
  }

  public void toggleLockedState(int slot) {
    if (world.isRemote) {
      PacketHandler.sendToServer(new PacketFarmLockedSlot(this, slot));
    }
    setSlotLocked(slot, !isSlotLocked(slot));
  }

  public boolean isSlotLocked(int slot) {
    return (lockedSlots & (1 << slot)) != 0;
  }

  private void setSlotLocked(int slot, boolean value) {
    if (value) {
      lockedSlots = lockedSlots | (1 << slot);
    } else {
      lockedSlots = lockedSlots & ~(1 << slot);
    }
  }

  @Override
  public @Nonnull String getMachineName() {
    return MachineRecipeRegistry.FARM;
  }

  @Override
  public float getProgress() {
    return 0.5f;
  }

  @Override
  public void onCapacitorDataChange() {
    super.onCapacitorDataChange();
    currentTask = createTask(null, 0f);
  }

  @Override
  protected IPoweredTask createTask(@Nonnull IMachineRecipe nextRecipe, float chance) {
    return new ContinuousTask(getPowerUsePerTick());
  }

  @Override
  public int getInventoryStackLimit(int slot) {
    if (slot >= minSupSlot && slot <= maxSupSlot) {
      return Math.min(FARM_STACK_LIMIT.get(getCapacitorData()), 64);
    }
    return 64;
  }

  @Override
  public int getInventoryStackLimit() {
    // We return the (lowered) input slot limit here, so others who insert into us
    // will behave nicely.
    return getInventoryStackLimit(minSupSlot);
  }

  @Override
  public boolean shouldRenderInPass(int pass) {
    return pass == 1;
  }

  // RANGE

  private boolean showingRange;

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isShowingRange() {
    return showingRange;
  }

  private final static Vector4f color = new Vector4f(145f / 255f, 82f / 255f, 21f / 255f, .4f);

  @SideOnly(Side.CLIENT)
  public void setShowRange(boolean showRange) {
    if (showingRange == showRange) {
      return;
    }
    showingRange = showRange;
    if (showingRange) {
      Minecraft.getMinecraft().effectRenderer.addEffect(new RangeParticle<TileFarmStation>(this, color));
    }
  }

  @Override
  public @Nonnull BoundingBox getBounds() {
    return new BoundingBox(getPos()).expand(getRange(), 0, getRange());
  }

  public float getRange() {
    return getFarmSize();
  }

  // RANGE END

}