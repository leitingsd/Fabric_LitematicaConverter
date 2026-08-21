package net.leitingsd.litematicaconversion.gui.widgets;

import java.io.File;
import java.util.List;

import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.leitingsd.litematicaconversion.gui.ConversionSelectionManager;
import net.minecraft.client.gui.DrawContext;

public class WidgetListConversion extends WidgetSchematicBrowser
{
    private static final int ROW_HEIGHT = 12;
    private static final int HEADER_HEIGHT = 16;

    public WidgetListConversion(int x, int y, int width, int height,
                                GuiSchematicBrowserBase parent, ISelectionListener<DirectoryEntry> selectionListener)
    {
        super(x, y, width, height, parent, selectionListener);
    }

    @Override
    public void drawAdditionalContents(int mouseX, int mouseY, DrawContext drawContext)
    {
        super.drawAdditionalContents(mouseX, mouseY, drawContext);

        this.drawPendingListPanel(mouseX, mouseY, drawContext);
    }

    private void drawPendingListPanel(int mouseX, int mouseY, DrawContext drawContext)
    {
        List<File> files = ConversionSelectionManager.getInstance().getFiles();

        if (files.isEmpty())
        {
            return;
        }

        int x = this.posX + this.totalWidth - this.infoWidth;
        int infoHeight = Math.min(this.infoHeight, this.parent.getMaxInfoHeight());
        int y = this.posY + infoHeight + 6;
        int width = this.infoWidth;
        int height = this.browserHeight - (y - this.posY) - 4;

        if (height < 24)
        {
            return;
        }

        RenderUtils.drawOutlinedBox(x, y, width, height, 0xB0000000, GuiBase.COLOR_HORIZONTAL_BAR);

        int textX = x + 6;
        int lineY = y + 4;

        String header = StringUtils.translate("litematica-conversion.gui.label.pending_list_header", files.size());
        this.drawString(drawContext, header, textX, lineY, 0xFFFFFFFF);
        lineY += HEADER_HEIGHT;

        for (int i = 0; i < files.size(); i++)
        {
            if (lineY + ROW_HEIGHT > y + height - 2)
            {
                break;
            }

            String name = files.get(i).getName();
            int maxWidth = width - 14;

            while (this.getStringWidth(name) > maxWidth && name.length() > 1)
            {
                name = name.substring(0, name.length() - 1);
            }

            if (GuiBase.isMouseOver(mouseX, mouseY, x + 2, lineY, width - 4, ROW_HEIGHT))
            {
                RenderUtils.drawRect(x + 2, lineY, width - 4, ROW_HEIGHT, 0x40FFFFFF);
            }

            this.drawString(drawContext, name, textX, lineY, 0xC0C0C0C0);
            lineY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.onPendingListClicked(mouseX, mouseY))
        {
            return true;
        }

        return super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean onPendingListClicked(int mouseX, int mouseY)
    {
        List<File> files = ConversionSelectionManager.getInstance().getFiles();

        if (files.isEmpty())
        {
            return false;
        }

        int x = this.posX + this.totalWidth - this.infoWidth;
        int infoHeight = Math.min(this.infoHeight, this.parent.getMaxInfoHeight());
        int y = this.posY + infoHeight + 6;
        int width = this.infoWidth;
        int height = this.browserHeight - (y - this.posY) - 4;

        if (height < 24 || mouseX < x || mouseX >= x + width ||
                mouseY < y || mouseY >= y + height)
        {
            return false;
        }

        int rowIndex = (mouseY - (y + 4 + HEADER_HEIGHT)) / ROW_HEIGHT;

        if (rowIndex >= 0 && rowIndex < files.size())
        {
            ConversionSelectionManager.getInstance().remove(files.get(rowIndex));

            this.parent.initGui();

            return true;
        }

        return false;
    }
}