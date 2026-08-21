package net.leitingsd.litematicaconversion.mixins;

import fi.dy.masa.litematica.gui.GuiMainMenu;
import fi.dy.masa.litematica.selection.SelectionMode;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.leitingsd.litematicaconversion.gui.GuiSchematicConversion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMainMenu.class, remap = false)
public abstract class GuiMainMenuMixin extends GuiBase
{
    @Inject(method = "initGui", at = @At("RETURN"), remap = false)
    private void litematicaConversion$addButtons(CallbackInfo ci)
    {
        String labelConvert = StringUtils.translate("litematica-conversion.gui.button.convert_schematic");

        int width = this.getStringWidth(labelConvert) + 30;

        if (FabricLoader.getInstance().isModLoaded("syncmatica"))
        {
            width = Math.max(width,
                    this.getStringWidth(StringUtils.translate("syncmatica.gui.button.view_syncmatics")) + 30);
            width = Math.max(width,
                    this.getStringWidth(StringUtils.translate("syncmatica.gui.button.material_gatherings")) + 30);
        }

        for (SelectionMode mode : SelectionMode.values())
        {
            String label = StringUtils.translate("litematica.gui.button.area_selection_mode", mode.getDisplayName());
            width = Math.max(width, this.getStringWidth(label) + 10);
        }

        final int x = 52 + 2 * width;

        final int y = 118;

        GuiSchematicConversion gui = new GuiSchematicConversion();
        gui.setParent(this);

        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, labelConvert);

        this.addButton(button, (btn, mouseButton) -> GuiBase.openGui(gui));
    }
}