package crazypants.enderio.conduit;

import com.google.common.collect.Maps;
import crazypants.enderio.base.conduit.registry.ConduitRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import java.util.Map;

public class ConduitBundleStateMapper extends StateMapperBase {

  public static void create() {
    ConduitBundleStateMapper mapper = new ConduitBundleStateMapper();
    ModelLoader.setCustomStateMapper(ConduitRegistry.getConduitModObjectNN().getBlock(), mapper);
  }

  @Override
  protected @Nonnull ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
    Map<IProperty<?>, Comparable<?>> map = Maps.<IProperty<?>, Comparable<?>> newLinkedHashMap(state.getProperties());

    map.remove(BlockConduitBundle.OPAQUE);

    return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), this.getPropertyString(map));
  }

}
