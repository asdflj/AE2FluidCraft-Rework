package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidLevelMaintainer;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import com.glodblock.github.network.CPacketUpdateFluidLevel;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;

public class GuiFluidLevelMaintainer extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_level_maintainer.png");

    private final ContainerFluidLevelMaintainer cont;
    private final GuiNumberBox[] maintain = new GuiNumberBox[TileFluidLevelMaintainer.MAX_FLUID];
    private final GuiNumberBox[] request = new GuiNumberBox[TileFluidLevelMaintainer.MAX_FLUID];

    public GuiFluidLevelMaintainer(InventoryPlayer ipl, TileFluidLevelMaintainer tile) {
        super(new ContainerFluidLevelMaintainer(ipl, tile));
        this.cont = (ContainerFluidLevelMaintainer) inventorySlots;
        this.ySize = 214;
        for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
            maintain[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 39, this.guiTop + 22 + i * 20, 42, 10, Integer.class);
            request[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 102, this.guiTop + 22 + i * 20, 42, 10, Integer.class);
        }
    }

    protected void keyTyped(char character, int key) throws IOException {

        if (!this.checkHotbarKeys(key)) {
            GuiNumberBox focus = null;
            int id = 0;
            for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
                if (maintain[i].isFocused()) {
                    focus = maintain[i];
                    id = i;
                }
                if (request[i].isFocused()) {
                    focus = request[i];
                    id = i + 10;
                }
            }
            if (focus != null) {
                if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && focus.textboxKeyTyped(character, key)) {
                    try {
                        String out = focus.getText();

                        boolean fixed;
                        for(fixed = false; out.startsWith("0") && out.length() > 1; fixed = true) {
                            out = out.substring(1);
                        }

                        if (fixed) {
                            focus.setText(out);
                        }

                        if (out.isEmpty()) {
                            out = "0";
                        }

                        long result = Long.parseLong(out);
                        if (result < 0L) {
                            focus.setText("1");
                            result = 1;
                        }

                        FluidCraft.proxy.netHandler.sendToServer(new CPacketUpdateFluidLevel(id, (int) result));

                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            else {
                super.keyTyped(character, key);
            }
        }

    }

    @Override
    public void initGui() {
        super.initGui();
        TileFluidLevelMaintainer tile = this.cont.getTile();
        IItemHandler inv = tile.getInventoryHandler();
        for (int i = 0; i < inv.getSlots(); i ++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                this.maintain[i].setText(String.valueOf(inv.getStackInSlot(i).getCount()));
            }
            else {
                this.maintain[i].setText("0");
            }
            this.request[i].setText(String.valueOf(tile.getRequest()[i]));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        for (int i = 0; i < TileFluidLevelMaintainer.MAX_FLUID; i ++) {
            this.configNumberBar(request[i], xCoord, yCoord, btn);
            this.configNumberBar(maintain[i], xCoord, yCoord, btn);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    private void configNumberBar(GuiNumberBox bar, int xCoord, int yCoord, int btn) {
        bar.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && bar.isFocused()) {
            bar.setText("");
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }
}