package net.leitingsd.litematicaconversion.gui;

import java.io.File;
import java.util.List;
import java.util.Locale;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.gui.GuiMainMenu.ButtonListenerChangeMenu;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.leitingsd.litematicaconversion.ConversionManager;
import net.leitingsd.litematicaconversion.ConversionManager.ConversionResult;
import net.leitingsd.litematicaconversion.gui.widgets.WidgetListConversion;
import net.minecraft.client.MinecraftClient;

public class GuiSchematicConversion extends GuiSchematicBrowserBase
{
    private final ConversionManager conversionManager = new ConversionManager();

    private ButtonGeneric countButton;
    private ButtonGeneric startButton;
    private boolean converting;

    public GuiSchematicConversion()
    {
        super(12, 24);

        this.title = StringUtils.translate("litematica-conversion.gui.title.schematic_conversion");
    }

    @Override
    protected WidgetSchematicBrowser createListWidget(int listX, int listY)
    {
        return new WidgetListConversion(listX, listY, 100, 100, this, this.getSelectionListener());
    }

    @Override
    public String getBrowserContext()
    {
        return "litematica_conversion";
    }

    @Override
    public File getDefaultDirectory()
    {
        return DataManager.getSchematicsBaseDirectory();
    }

    @Override
    public int getMaxInfoHeight()
    {
        return this.getBrowserHeight() + 10;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int x = 12;
        int y = this.height - 26;

        // 待转换数量
        this.countButton = new ButtonGeneric(x, y, this.getStringWidth(this.getCountText()) + 12, 20, this.getCountText());
        this.countButton.setEnabled(false);
        this.addButton(this.countButton, (button, mouseButton) -> {});
        x += this.countButton.getWidth() + 4;

        // 加入待转换列表
        String label = StringUtils.translate("litematica-conversion.gui.button.add_to_conversion_list");
        int buttonWidth = this.getStringWidth(label) + 20;
        ButtonGeneric addButton = new ButtonGeneric(x, y, buttonWidth, 20, label);
        this.addButton(addButton, (button, mouseButton) -> this.addSelectedSchematic());
        x += buttonWidth + 4;

        // 清空待转换列表
        label = StringUtils.translate("litematica-conversion.gui.button.clear_list");
        buttonWidth = this.getStringWidth(label) + 12;
        ButtonGeneric clearButton = new ButtonGeneric(x, y, buttonWidth, 20, label);
        this.addButton(clearButton, (button, mouseButton) -> this.clearPendingList());
        x += buttonWidth + 4;

        // 开始转换
        label = StringUtils.translate(this.converting
                ? "litematica-conversion.gui.button.converting"
                : "litematica-conversion.gui.button.start_conversion");
        buttonWidth = this.getStringWidth(label) + 16;
        this.startButton = new ButtonGeneric(x, y, buttonWidth, 20, label);
        this.startButton.setEnabled(this.converting == false &&
                ConversionSelectionManager.getInstance().isEmpty() == false);
        this.addButton(this.startButton, (button, mouseButton) -> this.startConversion());

        // 返回主菜单
        ButtonListenerChangeMenu.ButtonType type = ButtonListenerChangeMenu.ButtonType.MAIN_MENU;
        label = StringUtils.translate(type.getLabelKey());
        buttonWidth = this.getStringWidth(label) + 20;
        x = this.width - buttonWidth - 10;
        ButtonGeneric mainMenu = new ButtonGeneric(x, y, buttonWidth, 20, label);
        this.addButton(mainMenu, new ButtonListenerChangeMenu(type, this.getParent()));
    }

    @Override
    public void removed()
    {
        super.removed();

        ConversionSelectionManager.getInstance().clear();
    }

    private String getCountText()
    {
        return StringUtils.translate("litematica-conversion.gui.label.pending_count",
                ConversionSelectionManager.getInstance().size());
    }

    private void addSelectedSchematic()
    {
        DirectoryEntry entry = this.getListWidget() != null
                ? this.getListWidget().getLastSelectedEntry()
                : null;

        if (entry == null)
        {
            this.addMessage(MessageType.ERROR, "litematica-conversion.gui.message.no_selection");
            return;
        }

        File file = entry.getFullPath();

        if (file.exists() == false || file.isFile() == false || file.canRead() == false)
        {
            this.addMessage(MessageType.ERROR, "litematica-conversion.gui.message.cant_read", file.getName());
            return;
        }

        // 只允许 .litematic
        if (file.getName().toLowerCase(Locale.ROOT).endsWith(".litematic") == false)
        {
            this.addMessage(MessageType.ERROR, "litematica-conversion.gui.message.not_litematic");
            return;
        }

        boolean added = ConversionSelectionManager.getInstance().add(file);

        if (added)
        {
            this.addMessage(MessageType.SUCCESS, "litematica-conversion.gui.message.added", file.getName());
            this.initGui(); // 刷新计数
        }
        else
        {
            this.addMessage(MessageType.WARNING, "litematica-conversion.gui.message.duplicate", file.getName());
        }
    }

    private void clearPendingList()
    {
        if (ConversionSelectionManager.getInstance().isEmpty())
        {
            this.addMessage(MessageType.WARNING, "litematica-conversion.gui.message.list_empty");
            return;
        }

        ConversionSelectionManager.getInstance().clear();
        this.addMessage(MessageType.SUCCESS, "litematica-conversion.gui.message.list_cleared");
        this.initGui();
    }

    private void startConversion()
    {
        if (this.converting)
        {
            return;
        }

        List<File> files = ConversionSelectionManager.getInstance().getFiles();

        if (files.isEmpty())
        {
            this.addMessage(MessageType.ERROR, "litematica-conversion.gui.message.list_empty");
            return;
        }

        this.converting = true;
        this.startButton.setEnabled(false);
        this.startButton.setDisplayString(StringUtils.translate("litematica-conversion.gui.button.converting"));

        this.conversionManager.startConversion(files, result ->
                MinecraftClient.getInstance().execute(() -> this.onConversionDone(result)));
    }

    private void onConversionDone(ConversionResult result)
    {
        this.converting = false;

        // 转换结束后清空列表
        ConversionSelectionManager.getInstance().clear();

        this.initGui();

        String summary = StringUtils.translate("litematica-conversion.gui.message.conversion_done",
                result.successCount, result.failCount, result.skippedCount);

        String details = result.messages.toString()
                .replace("\r", "")
                .replace("\n", "\\n");

        String warning = StringUtils.translate("litematica-conversion.gui.result.palette_warning");

        String text = summary + "\\n" + details + "\\n" + warning;

        MessageType type = result.failCount > 0 ? MessageType.WARNING : MessageType.SUCCESS;

        if (MinecraftClient.getInstance().currentScreen == this)
        {
            this.addGuiMessage(type, 20000, "%s", text);
        }
        else
        {
            InfoUtils.showInGameMessage(type, 20000, "%s", text);
        }
    }
}