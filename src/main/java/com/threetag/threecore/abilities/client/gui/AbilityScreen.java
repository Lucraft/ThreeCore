package com.threetag.threecore.abilities.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.abilities.AbilityHelper;
import com.threetag.threecore.abilities.IAbilityContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AbilityScreen extends Screen {

    private static final ResourceLocation WINDOW = new ResourceLocation(ThreeCore.MODID, "textures/gui/abilities/window.png");
    private static final ResourceLocation TABS = new ResourceLocation(ThreeCore.MODID, "textures/gui/abilities/tabs.png");
    public static final ResourceLocation WIDGETS = new ResourceLocation(ThreeCore.MODID, "textures/gui/abilities/widgets.png");

    private final int guiWidth = 252;
    private final int guiHeight = 201;
    private List<AbilityTabGui> tabs = Lists.newLinkedList();
    private AbilityTabGui selectedTab = null;
    private boolean isScrolling;

    public AbilityScreen() {
        super(new StringTextComponent(""));
        this.tabs.clear();
        this.selectedTab = null;
        AtomicInteger index = new AtomicInteger();
        AbilityHelper.getAbilityContainerList().forEach((f) -> {
            IAbilityContainer container = f.apply(Minecraft.getInstance().player);
            if (container != null) {
                this.tabs.add(AbilityTabGui.create(Minecraft.getInstance(), this, index.get(), container));
                index.getAndIncrement();
            }
        });
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int type) {
        if (type == 0) {
            int i = (this.width - guiWidth) / 2;
            int j = (this.height - guiHeight) / 2;

            for (AbilityTabGui tab : this.tabs) {
                if (tab.isMouseOver(i, j, p_mouseClicked_1_, p_mouseClicked_3_)) {
                    this.selectedTab = tab;
                    break;
                }
            }
        }
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, type);
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double x, double y) {
        if (p_mouseDragged_5_ != 0) {
            this.isScrolling = false;
            return false;
        } else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.scroll(x, y);
            }

            return true;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int i = (this.width - guiWidth) / 2;
        int j = (this.height - guiHeight) / 2;
        this.renderBackground();
        this.renderInside(mouseX, mouseY, i, j);
        this.renderWindow(i, j);
        this.renderToolTips(mouseX, mouseY, i, j);
    }

    public void renderWindow(int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        RenderHelper.disableStandardItemLighting();
        this.minecraft.getTextureManager().bindTexture(WINDOW);
        this.blit(x, y, 0, 0, guiWidth, guiHeight);
        if (this.tabs.size() > 0) {
            this.minecraft.getTextureManager().bindTexture(TABS);

            for (AbilityTabGui tab : this.tabs) {
                tab.drawTab(x, y, tab == this.selectedTab);
            }

            GlStateManager.enableRescaleNormal();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();

            for (AbilityTabGui tab : this.tabs) {
                tab.drawIcon(x, y);
            }

            GlStateManager.disableBlend();
        }

        this.font.drawString(I18n.format("gui.threecore.abilities"), (float) (x + 8), (float) (y + 6), 4210752);
    }

    private void renderInside(int mouseX, int mouseY, int x, int y) {
        AbilityTabGui tab = this.selectedTab;
        if (tab == null) {
            fill(x + 9, y + 18, x + 9 + AbilityTabGui.innerWidth, y + 18 + AbilityTabGui.innerHeight, -16777216);
            String s = I18n.format("advancements.empty");
            int i = this.font.getStringWidth(s);
            this.font.drawString(s, (float) (x + 9 + 117 - i / 2), (float) (y + 18 + 56 - 9 / 2), -1);
            this.font.drawString(":(", (float) (x + 9 + 117 - this.font.getStringWidth(":(") / 2), (float) (y + 18 + 113 - 9), -1);
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) (x + 9), (float) (y + 18), -400.0F);
            GlStateManager.enableDepthTest();
            tab.drawContents();
            GlStateManager.popMatrix();
            GlStateManager.depthFunc(515);
            GlStateManager.disableDepthTest();
        }
    }

    private void renderToolTips(int mouseX, int mouseY, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            GlStateManager.pushMatrix();
            GlStateManager.enableDepthTest();
            GlStateManager.translatef((float) (x + 9), (float) (y + 18), 400.0F);
            this.selectedTab.drawToolTips(mouseX - x - 9, mouseY - y - 18, x, y, this);
            GlStateManager.disableDepthTest();
            GlStateManager.popMatrix();
        }

        if (this.tabs.size() > 0) {
            for (AbilityTabGui tab : this.tabs) {
                if (tab.isMouseOver(x, y, (double) mouseX, (double) mouseY)) {
                    this.renderTooltip(tab.getTitle().getFormattedText(), mouseX, mouseY);
                }
            }
        }

    }
}
