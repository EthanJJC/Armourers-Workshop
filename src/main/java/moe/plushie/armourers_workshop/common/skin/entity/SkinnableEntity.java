package moe.plushie.armourers_workshop.common.skin.entity;

import moe.plushie.armourers_workshop.api.common.skin.entity.ISkinnableEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SkinnableEntity implements ISkinnableEntity {
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addRenderLayer(RenderManager renderManager) {
        Render<Entity> renderer = renderManager.getEntityClassRenderObject(getEntityClass());
        if (renderer != null && renderer instanceof RenderLivingBase) {
            LayerRenderer<? extends EntityLivingBase> layerRenderer = getLayerRenderer();
            if (layerRenderer != null) {
                ((RenderLivingBase<?>) renderer).addLayer(layerRenderer);
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    public LayerRenderer<? extends EntityLivingBase> getLayerRenderer() {
        return null;
    }
    
    @Override
    public boolean canUseWandOfStyle() {
        return true;
    }

    @Override
    public boolean canUseSkinsOnEntity() {
        return false;
    }
}
