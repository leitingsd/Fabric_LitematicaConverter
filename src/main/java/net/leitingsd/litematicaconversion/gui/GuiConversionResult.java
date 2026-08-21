package net.leitingsd.litematicaconversion.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import net.leitingsd.litematicaconversion.ConversionManager;
import net.minecraft.client.MinecraftClient;

public class GuiConversionResult extends GuiBase
{
    private final GuiBase parent;
    private final ConversionManager.ConversionResult result;

    private static final int ROW_HEIGHT = 22;

    public GuiConversionResult(
            GuiBase parent,
            ConversionManager.ConversionResult result)
    {
        this.parent = parent;
        this.result = result;

        this.setTitle(
                StringUtils.translate(
                        "litematica-conversion.dialog.title.conversion_results"
                )
        );
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.clearWidgets();

        int width = Math.min(760, this.width - 20);
        int x = (this.width - width) / 2;

        int y = 40;

        ButtonGeneric summary =
                new ButtonGeneric(
                        x,
                        y,
                        width,
                        20,
                        String.format(
                                "%s: %d    %s: %d    %s: %d",
                                StringUtils.translate(
                                        "litematica-conversion.gui.result.success"
                                ),
                                this.result.successCount,
                                StringUtils.translate(
                                        "litematica-conversion.gui.result.failed"
                                ),
                                this.result.failCount,
                                StringUtils.translate(
                                        "litematica-conversion.gui.result.skipped"
                                ),
                                this.result.skippedCount
                        )
                );

        summary.setEnabled(false);

        this.addButton(
                summary,
                (button, mouseButton) -> {}
        );

        y += 30;

        ButtonGeneric warning =
                new ButtonGeneric(
                        x,
                        y,
                        width,
                        20,
                        StringUtils.translate(
                                "litematica-conversion.gui.result.palette_warning"
                        )
                );

        warning.setEnabled(false);
        this.addButton(
                warning,
                (button, mouseButton) -> {}
        );

        y += 30;

        String[] messages =
                this.result.messages
                        .toString()
                        .split("\\r?\\n");

        int maxRows =
                Math.max(
                        1,
                        (this.height - y - 70) /
                                ROW_HEIGHT
                );

        int rows = Math.min(
                messages.length,
                maxRows
        );

        if (messages.length == 0 ||
                (messages.length == 1 &&
                        messages[0].isEmpty()))
        {
            ButtonGeneric noMessages =
                    new ButtonGeneric(
                            x,
                            y,
                            width,
                            20,
                            StringUtils.translate(
                                    "litematica-conversion.gui.result.no_details"
                            )
                    );

            noMessages.setEnabled(false);

            this.addButton(
                    noMessages,
                    (button, mouseButton) -> {}
            );
        }
        else
        {
            for (int i = 0; i < rows; i++)
            {
                String message = messages[i];

                if (message.length() > 110)
                {
                    message =
                            message.substring(0, 107) +
                                    "...";
                }

                ButtonGeneric detail =
                        new ButtonGeneric(
                                x,
                                y + i * ROW_HEIGHT,
                                width,
                                20,
                                message
                        );

                detail.setEnabled(false);

                this.addButton(
                        detail,
                        (button, mouseButton) -> {}
                );
            }
        }

        ButtonGeneric close =
                new ButtonGeneric(
                        this.width - 110,
                        this.height - 35,
                        100,
                        20,
                        StringUtils.translate(
                                "litematica-conversion.gui.button.close"
                        )
                );

        this.addButton(
                close,
                (button, mouseButton) ->
                        this.closeResultScreen()
        );
    }

    private void closeResultScreen()
    {
        MinecraftClient.getInstance()
                .setScreen(this.parent);
    }
}