package net.leitingsd.litematicaconversion;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LitematicaConversion implements ClientModInitializer {
    public static final String MOD_ID = "litematicaconversion";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
    }
}
