package crazypants.enderio.machine.crafter;

import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.RenderMappers;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.render.IRenderMapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCrafter extends AbstractMachineBlock<TileCrafter> {
  
  public static BlockCrafter create() {
    PacketHandler.INSTANCE.registerMessage(PacketCrafter.class,PacketCrafter.class,PacketHandler.nextID(), Side.SERVER);
    BlockCrafter res = new BlockCrafter();
    res.init();
    return res;
  }
  
  protected BlockCrafter() {
    super(ModObject.blockCrafter, TileCrafter.class);
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TileCrafter) {
      return new ContainerCrafter(player.inventory, (TileCrafter) te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TileCrafter) {
      return new GuiCrafter(player.inventory, (TileCrafter) te);
    }
    return null;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_CRAFTER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper getRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

}
