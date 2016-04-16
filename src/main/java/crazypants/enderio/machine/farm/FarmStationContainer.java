package crazypants.enderio.machine.farm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.enderio.core.client.gui.widget.GhostBackgroundItemSlot;
import com.enderio.core.client.gui.widget.GhostSlot;

import crazypants.enderio.config.Config;
import crazypants.enderio.item.darksteel.DarkSteelItems;
import crazypants.enderio.machine.gui.AbstractMachineContainer;

public class FarmStationContainer extends AbstractMachineContainer<TileFarmStation> {

  // TODO: This is a mess. Someone should make some nice, hand-selected lists of
  // what to put in here.

  static private final List<ItemStack> slotItemsStacks1 = new ArrayList<ItemStack>();
  static private final List<ItemStack> slotItemsStacks2 = new ArrayList<ItemStack>();
  static private final List<ItemStack> slotItemsStacks3 = new ArrayList<ItemStack>();
  static public final List<ItemStack> slotItemsSeeds = new ArrayList<ItemStack>();
  static public final List<ItemStack> slotItemsProduce = new ArrayList<ItemStack>();
  static public final List<ItemStack> slotItemsFertilizer = new ArrayList<ItemStack>();
  static {
    for (Item item : new Item[] { Items.wooden_hoe, Items.stone_hoe, Items.iron_hoe, Items.golden_hoe, Items.diamond_hoe }) {
      slotItemsStacks1.add(new ItemStack(item));
    }
    slotItemsStacks1.addAll(Config.farmHoes);
    for (Item item : new Item[] { Items.wooden_axe, Items.stone_axe, Items.iron_axe, Items.golden_axe, Items.diamond_axe, DarkSteelItems.itemDarkSteelAxe }) {
      slotItemsStacks2.add(new ItemStack(item));
    }
    for (Item item : new Item[] { Items.shears, DarkSteelItems.itemDarkSteelShears, GameRegistry.findItem("IC2", "itemTreetap") }) {
      slotItemsStacks3.add(new ItemStack(item));
    }
    slotItemsSeeds.add(new ItemStack(Items.wheat_seeds));
    slotItemsSeeds.add(new ItemStack(Blocks.carrots));
    slotItemsSeeds.add(new ItemStack(Blocks.potatoes));
    slotItemsSeeds.add(new ItemStack(Blocks.red_mushroom));
    slotItemsSeeds.add(new ItemStack(Blocks.brown_mushroom));
    slotItemsSeeds.add(new ItemStack(Blocks.nether_wart));
    slotItemsSeeds.add(new ItemStack(Blocks.sapling));
    slotItemsSeeds.add(new ItemStack(Items.reeds));
    slotItemsProduce.add(new ItemStack(Blocks.log, 1, 0));
    slotItemsProduce.add(new ItemStack(Blocks.wheat));
    slotItemsProduce.add(new ItemStack(Blocks.leaves, 1, 0));
    slotItemsProduce.add(new ItemStack(Items.apple));
    slotItemsFertilizer.add(new ItemStack(Items.dye, 1, 15));
  }

  static private final Random rand = new Random();

  private static final int ROW_TOOLS = 19;
  private static final int ROW_IO = 44;

  private static final int COL_TOOLS = 44;
  private static final int COL_INPUT = 53;
  private static final int COL_FERTILIZER = 116;
  private static final int COL_OUTPUT = 107;

  private static final int SLOT_SIZE = 18;
  private static final int ONE   = 0 * SLOT_SIZE;
  private static final int TWO   = 1 * SLOT_SIZE;
  private static final int THREE = 2 * SLOT_SIZE;

  private static final SlotPoint[] points = new SlotPoint[] { //

  new SlotPoint(COL_TOOLS + ONE, ROW_TOOLS, slotItemsStacks1), //
      new SlotPoint(COL_TOOLS + TWO, ROW_TOOLS, slotItemsStacks2), //
      new SlotPoint(COL_TOOLS + THREE, ROW_TOOLS, slotItemsStacks3),

      new SlotPoint(COL_FERTILIZER + ONE, ROW_TOOLS, slotItemsFertilizer), //
      new SlotPoint(COL_FERTILIZER + TWO, ROW_TOOLS, slotItemsFertilizer),

      new SlotPoint(COL_INPUT + ONE, ROW_IO + ONE, slotItemsSeeds), //
      new SlotPoint(COL_INPUT + TWO, ROW_IO + ONE, slotItemsSeeds), //
      new SlotPoint(COL_INPUT + ONE, ROW_IO + TWO, slotItemsSeeds), //
      new SlotPoint(COL_INPUT + TWO, ROW_IO + TWO, slotItemsSeeds),

      new SlotPoint(COL_OUTPUT + ONE, ROW_IO + ONE, slotItemsProduce), //
      new SlotPoint(COL_OUTPUT + TWO, ROW_IO + ONE, slotItemsProduce), //
      new SlotPoint(COL_OUTPUT + THREE, ROW_IO + ONE, slotItemsProduce), //
      new SlotPoint(COL_OUTPUT + ONE, ROW_IO + TWO, slotItemsProduce), //
      new SlotPoint(COL_OUTPUT + TWO, ROW_IO + TWO, slotItemsProduce), //
      new SlotPoint(COL_OUTPUT + THREE, ROW_IO + TWO, slotItemsProduce), //
  };

  public FarmStationContainer(InventoryPlayer inventory, TileFarmStation te) {
    super(inventory, te);
  }

  @Override
  protected void addMachineSlots(InventoryPlayer playerInv) {
    int i=0;
    for (SlotPoint p : points) {
      final int slot = i;
      i++;
      addSlotToContainer(p.s = new Slot(getInv(), slot, p.x, p.y) {
        @Override
        public boolean isItemValid(ItemStack itemStack) {
          return getInv().isItemValidForSlot(slot, itemStack);
        }

        @Override
        public int getSlotStackLimit() {             
          return getInv().getInventoryStackLimit(slot);
        }
      });
    }

  }

  private static void clean(List<ItemStack> list) {
    Iterator<ItemStack> iterator = list.iterator();
    while (iterator.hasNext()) {
      final ItemStack o = iterator.next();
      if (o == null || o.getItem() == null) {
        iterator.remove();
      }
    }
  }

  public void createGhostSlots(List<GhostSlot> slots) {
    clean(slotItemsStacks1);
    clean(slotItemsStacks2);
    clean(slotItemsStacks3);
    clean(slotItemsFertilizer);
    clean(slotItemsSeeds);
    clean(slotItemsProduce);

    for (SlotPoint p : points) {
      slots.add(new GhostBackgroundItemSlot(p.ghosts.get(rand.nextInt(p.ghosts.size())), p.s));
    }
  }

  @Override
  public Point getPlayerInventoryOffset() {
    return new Point(8,87);
  }

  @Override
  public Point getUpgradeOffset() {
    return new Point(12,63);
  }

  private static class SlotPoint {
    int x, y;
    List<ItemStack> ghosts;
    // It's a bit of a hack having the slot in a static field, but it is only used on the client, and there only one instance of the GUI can exist at any time,
    // so it works.
    Slot s = null;

    SlotPoint(int x, int y, List<ItemStack> ghosts) {
      this.x = x;
      this.y = y;
      this.ghosts = ghosts;
    }

  }

}
