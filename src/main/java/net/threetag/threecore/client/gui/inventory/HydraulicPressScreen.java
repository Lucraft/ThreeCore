package net.threetag.threecore.client.gui.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.threetag.threecore.ThreeCore;
import net.threetag.threecore.container.HydraulicPressContainer;
import net.threetag.threecore.util.energy.EnergyUtil;

public class HydraulicPressScreen extends ContainerScreen<HydraulicPressContainer> {

    private static final ResourceLocation HYDRAULIC_PRESS_GUI_TEXTURES = new ResourceLocation(ThreeCore.MODID, "textures/gui/container/hydraulic_press.png");

    public HydraulicPressScreen(HydraulicPressContainer hydraulicPressContainer, PlayerInventory inventory, ITextComponent title) {
        super(hydraulicPressContainer, inventory, title);
        this.ySize = 174;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
        this.playerInventoryTitleY = this.ySize - 94;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(stack, mouseX, mouseY);
        EnergyUtil.drawTooltip(stack, this.container.getEnergyStored(), this.container.getMaxEnergyStored(), this, 10, 17, 12, 40, mouseX - this.guiLeft, mouseY - this.guiTop);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(HYDRAULIC_PRESS_GUI_TEXTURES);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.blit(stack, left, top, 0, 0, this.xSize, this.ySize);
        int progress = this.container.getProgressScaled(24);
        this.blit(stack, left + 97, top + 38, 176, 0, progress + 1, 16);
        int energy = (int) (this.container.getEnergyPercentage() * 40);
        this.blit(stack, left + 10, top + 17 + 40 - energy, 176, 17 + 40 - energy, 12, energy);
        Slot slot = this.container.getSlot(1);
        if (slot.getStack().isEmpty())
            this.blit(stack, left + slot.xPos, top + slot.yPos, 200, 0, 16, 16);
    }

}
