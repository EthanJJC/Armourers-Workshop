package moe.plushie.armourers_workshop.common.items.paintingtool;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import moe.plushie.armourers_workshop.ArmourersWorkshop;
import moe.plushie.armourers_workshop.api.common.painting.IPantableBlock;
import moe.plushie.armourers_workshop.common.blocks.ModBlocks;
import moe.plushie.armourers_workshop.common.items.AbstractModItem;
import moe.plushie.armourers_workshop.common.lib.EnumGuiId;
import moe.plushie.armourers_workshop.common.lib.LibItemNames;
import moe.plushie.armourers_workshop.common.lib.LibModInfo;
import moe.plushie.armourers_workshop.common.network.PacketHandler;
import moe.plushie.armourers_workshop.common.network.messages.client.MessageClientToolPaintBlock;
import moe.plushie.armourers_workshop.common.painting.IBlockPainter;
import moe.plushie.armourers_workshop.common.painting.PaintRegistry;
import moe.plushie.armourers_workshop.common.painting.PaintType;
import moe.plushie.armourers_workshop.common.painting.tool.IConfigurableTool;
import moe.plushie.armourers_workshop.common.painting.tool.ToolOption;
import moe.plushie.armourers_workshop.common.painting.tool.ToolOptions;
import moe.plushie.armourers_workshop.common.tileentities.TileEntityArmourer;
import moe.plushie.armourers_workshop.common.undo.UndoManager;
import moe.plushie.armourers_workshop.utils.ModSounds;
import moe.plushie.armourers_workshop.utils.TranslateUtils;
import moe.plushie.armourers_workshop.utils.UtilColour;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemShadeNoiseTool extends AbstractModItem implements IConfigurableTool, IBlockPainter {

    public ItemShadeNoiseTool() {
        super(LibItemNames.SHADE_NOISE_TOOL);
        setSortPriority(16);
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = worldIn.getBlockState(pos);
        ItemStack stack = player.getHeldItem(hand);
        
        if (state.getBlock() instanceof IPantableBlock) {
            if (!worldIn.isRemote) {
                UndoManager.begin(player);
            }
            if (ToolOptions.FULL_BLOCK_MODE.getValue(stack)) {
                for (int i = 0; i < 6; i++) {
                    usedOnBlockSide(stack, player, worldIn, pos, state.getBlock(), EnumFacing.values()[i], facing == EnumFacing.values()[i]);
                }
            } else {
                usedOnBlockSide(stack, player, worldIn, pos, state.getBlock(), facing, true);
            }
            if (!worldIn.isRemote) {
                UndoManager.end(player);
                if (ToolOptions.FULL_BLOCK_MODE.getValue(stack)) {
                    worldIn.playSound(null, pos, ModSounds.NOISE, SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.2F + 0.9F);
                } else {
                    worldIn.playSound(null, pos, ModSounds.NOISE, SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.2F + 1.5F);
                }
            }
            return EnumActionResult.SUCCESS;
        }
        
        if (state.getBlock() == ModBlocks.armourer & player.isSneaking()) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te != null && te instanceof TileEntityArmourer) {
                    ((TileEntityArmourer)te).toolUsedOnArmourer(this, worldIn, stack, player);
                }
            }
            return EnumActionResult.SUCCESS;
        }
        
        return EnumActionResult.PASS;
    }
    
    @Override
    public void usedOnBlockSide(ItemStack stack, EntityPlayer player, World world, BlockPos pos, Block block, EnumFacing face, boolean spawnParticles) {
        int intensity = ToolOptions.INTENSITY.getValue(stack);
        IPantableBlock worldColourable = (IPantableBlock) block;
        if (worldColourable.isRemoteOnly(world, pos, face) & world.isRemote) {
            byte[] rgbt = new byte[4];
            int oldColour = worldColourable.getColour(world, pos, face);
            PaintType oldPaintType = worldColourable.getPaintType(world, pos, face);
            Color c = UtilColour.addShadeNoise(new Color(oldColour), intensity);
            rgbt[0] = (byte)c.getRed();
            rgbt[1] = (byte)c.getGreen();
            rgbt[2] = (byte)c.getBlue();
            rgbt[3] = (byte)oldPaintType.getId();
            if (block == ModBlocks.boundingBox && oldPaintType == PaintRegistry.PAINT_TYPE_NONE) {
                rgbt[3] = (byte)PaintRegistry.PAINT_TYPE_NORMAL.getId();
            }
            MessageClientToolPaintBlock message = new MessageClientToolPaintBlock(pos, face, rgbt);
            PacketHandler.networkWrapper.sendToServer(message);
        } else if(!worldColourable.isRemoteOnly(world, pos, face) & !world.isRemote) {
            int oldColour = worldColourable.getColour(world, pos, face);
            byte oldPaintType = (byte) worldColourable.getPaintType(world, pos, face).getId();
            int newColour = UtilColour.addShadeNoise(new Color(oldColour), intensity).getRGB();
            UndoManager.blockPainted(player, world, pos, oldColour, oldPaintType, face);
            ((IPantableBlock) block).setColour(world, pos, newColour, face);
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.isSneaking()) {
            if (worldIn.isRemote) {
                playerIn.openGui(ArmourersWorkshop.getInstance(), EnumGuiId.TOOL_OPTIONS.ordinal(), worldIn, 0, 0, 0);
            }
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int intensity = ToolOptions.INTENSITY.getValue(stack);
        tooltip.add(TranslateUtils.translate("item.armourers_workshop:rollover.intensity", intensity));
        tooltip.add(TranslateUtils.translate("item.armourers_workshop:rollover.openSettings"));
    }
    
    @Override
    public void getToolOptions(ArrayList<ToolOption<?>> toolOptionList) {
        toolOptionList.add(ToolOptions.FULL_BLOCK_MODE);
        toolOptionList.add(ToolOptions.INTENSITY);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                if (ToolOptions.FULL_BLOCK_MODE.getValue(stack)) {
                    return new ModelResourceLocation(new ResourceLocation(LibModInfo.ID, getTranslationKey()), "normal");
                } else {
                    return new ModelResourceLocation(new ResourceLocation(LibModInfo.ID, getTranslationKey() + "-small"), "normal");
                }
            }
        });
        ModelBakery.registerItemVariants(this, new ModelResourceLocation(new ResourceLocation(LibModInfo.ID, getTranslationKey()), "normal"), new ModelResourceLocation(new ResourceLocation(LibModInfo.ID, getTranslationKey() + "-small"), "normal"));
    }
}
