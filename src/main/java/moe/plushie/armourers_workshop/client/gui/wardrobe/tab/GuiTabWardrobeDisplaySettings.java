package moe.plushie.armourers_workshop.client.gui.wardrobe.tab;

import org.lwjgl.opengl.GL11;

import moe.plushie.armourers_workshop.client.gui.GuiHelper;
import moe.plushie.armourers_workshop.client.gui.controls.GuiCheckBox;
import moe.plushie.armourers_workshop.client.gui.controls.GuiTabPanel;
import moe.plushie.armourers_workshop.client.gui.wardrobe.GuiWardrobe;
import moe.plushie.armourers_workshop.common.capability.entityskin.IEntitySkinCapability;
import moe.plushie.armourers_workshop.common.capability.wardrobe.player.IPlayerWardrobeCap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTabWardrobeDisplaySettings extends GuiTabPanel {

    //private static final ResourceLocation TEXTURE = new ResourceLocation(LibGuiResources.WARDROBE);
    
    private EntityPlayer entityPlayer;
    private IEntitySkinCapability skinCapability;
    private IPlayerWardrobeCap wardrobeCapability;
    
    private boolean[] armourOverride;
    
    private GuiCheckBox[] armourOverrideCheck;
    
    String guiName = "wardrobe";
    
    public GuiTabWardrobeDisplaySettings(int tabId, GuiScreen parent, EntityPlayer entityPlayer, IEntitySkinCapability skinCapability, IPlayerWardrobeCap wardrobeCapability) {
        super(tabId, parent, false);
        this.entityPlayer = entityPlayer;
        this.skinCapability = skinCapability;
        this.wardrobeCapability = wardrobeCapability;
        armourOverride = new boolean[4];
        for (int i = 0; i < armourOverride.length; i++) {
            EntityEquipmentSlot slot = EntityEquipmentSlot.values()[i + 2];
            armourOverride[i] = wardrobeCapability.getArmourOverride(slot);
        }
    }
    
    @Override
    public void initGui(int xPos, int yPos, int width, int height) {
        super.initGui(xPos, yPos, width, height);
        armourOverrideCheck = new GuiCheckBox[4];
        armourOverrideCheck[0] = new GuiCheckBox(2, 83, 27, GuiHelper.getLocalizedControlName(guiName, "renderHeadArmour"), !armourOverride[3]);
        armourOverrideCheck[1] = new GuiCheckBox(3, 83, 47, GuiHelper.getLocalizedControlName(guiName, "renderChestArmour"), !armourOverride[2]);
        armourOverrideCheck[2] = new GuiCheckBox(4, 83, 67, GuiHelper.getLocalizedControlName(guiName, "renderLegArmour"), !armourOverride[1]);
        armourOverrideCheck[3] = new GuiCheckBox(5, 83, 87, GuiHelper.getLocalizedControlName(guiName, "renderFootArmour"), !armourOverride[0]);
        
        buttonList.add(armourOverrideCheck[0]);
        buttonList.add(armourOverrideCheck[1]);
        buttonList.add(armourOverrideCheck[2]);
        buttonList.add(armourOverrideCheck[3]);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof GuiCheckBox) {
            for (int i = 0; i < armourOverride.length; i++) {
                armourOverride[i] = !armourOverrideCheck[3 - i].isChecked();
            }
        }
        
        if (button.id >= 1) {
            for (int i = 0; i < armourOverride.length; i++) {
                EntityEquipmentSlot slot = EntityEquipmentSlot.values()[i + 2];
                wardrobeCapability.setArmourOverride(slot, armourOverride[i]);
            }
            wardrobeCapability.sendUpdateToServer();
        }
    }

    @Override
    public void drawBackgroundLayer(float partialTickTime, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        
        //Top half of GUI. (active tab)
        //this.drawTexturedModalRect(this.x, this.y, 0, 0, 236, 151);
        
        //Bottom half of GUI. (player inventory)
        //this.drawTexturedModalRect(this.x + 29, this.y + 151, 29, 151, 178, 89);
    }
    
    @Override
    public void drawForegroundLayer(int mouseX, int mouseY, float partialTickTime) {
        super.drawForegroundLayer(mouseX, mouseY, partialTickTime);
        // Draw player preview.
        GL11.glPushMatrix();
        GL11.glTranslated(-x, -y, 0);
        ((GuiWardrobe)parent).drawPlayerPreview(x, y, mouseX, mouseY);
        GL11.glPopMatrix();
    }
}
