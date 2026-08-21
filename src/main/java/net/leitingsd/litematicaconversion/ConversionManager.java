package net.leitingsd.litematicaconversion;

import net.leitingsd.litematicaconversion.converter.SchematicConverter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConversionManager
{
    public static class ConversionResult
    {
        public int successCount;
        public int failCount;
        public int skippedCount;

        public final StringBuilder messages =
                new StringBuilder();
    }

    public void startConversion(
            List<File> files,
            Consumer<ConversionResult> callback
    )
    {
        CompletableFuture.runAsync(() ->
        {
            ConversionResult result =
                    new ConversionResult();

            for (File file : files)
            {
                String fileName = file.getName();

                if (!fileName
                        .toLowerCase()
                        .endsWith(".litematic"))
                {
                    continue;
                }

                try
                {
                    SchematicConverter source =
                            SchematicConverter.createFromFile(
                                    file.toPath()
                            );

                    if (source == null)
                    {
                        result.messages
                                .append("Failed to read: ")
                                .append(fileName)
                                .append('\n');

                        result.failCount++;
                        continue;
                    }

                    int version =
                            source.getSchematicVersion();

                    if (version < 7)
                    {
                        result.messages
                                .append(
                                        "Skipped (already v6 or lower): "
                                )
                                .append(fileName)
                                .append('\n');

                        result.skippedCount++;
                        continue;
                    }

                    String newName =
                            "v6_" + fileName;

                    Path outputPath =
                            new File(
                                    file.getParentFile(),
                                    newName
                            ).toPath();

                    SchematicConverter v6Schematic =
                            SchematicConverter
                                    .createEmptySchematicFromExisting(
                                            source
                                    );

                    v6Schematic.downgradeV7toV6Schematic(
                            source
                    );

                    if (v6Schematic.writeToFile(
                            outputPath,
                            true
                    ))
                    {
                        result.messages
                                .append("Converted: ")
                                .append(fileName)
                                .append(" -> ")
                                .append(newName)
                                .append('\n');

                        result.successCount++;
                    }
                    else
                    {
                        result.messages
                                .append("Failed to write: ")
                                .append(newName)
                                .append('\n');

                        result.failCount++;
                    }
                }
                catch (Exception e)
                {
                    result.messages
                            .append("Error converting ")
                            .append(fileName)
                            .append(": ")
                            .append(
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : e.getClass().getSimpleName()
                            )
                            .append('\n');

                    result.failCount++;

                    LitematicaConversion.logger.error(
                            "Error converting file {}",
                            fileName,
                            e
                    );
                }
            }

            callback.accept(result);
        });
    }
}