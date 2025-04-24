package com.verr1.controlcraft.registry.datagen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.verr1.controlcraft.ControlCraft;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ControlCraftDataProvider implements DataProvider {
    private final DataGenerator generator;

    public ControlCraftDataProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        // 构建数据集合
        List<MassEntry> masses = Arrays.stream(VsMasses.values())
                .map(s -> new MassEntry(
                        s.ID,
                        s.mass
                ))
                .toList();

        // 转换为JSON数组
        JsonArray jsonArray = new JsonArray();
        masses.forEach(entry -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("block", ControlCraft.MODID + ":" +  entry.block());
            obj.addProperty("mass", entry.mass());
            jsonArray.add(obj);
        });

        // 构建目标路径
        Path outputPath = generator.getPackOutput().getOutputFolder()
                .resolve(Path.of(
                        "data",
                        ControlCraft.MODID,
                        "vs_mass",
                        "masses.json"
                ));

        ControlCraft.LOGGER.info("{}", outputPath.toAbsolutePath());

        try {
            Files.createDirectories(outputPath.getParent());
            // 保存JSON文件





            saveStable(cachedOutput, jsonArray, outputPath);
            if (Files.exists(outputPath)) {
                ControlCraft.LOGGER.info("file exists");
            } else {
                ControlCraft.LOGGER.info("file not exists");
            }


        } catch (Exception e) {
            throw new RuntimeException("Failed to save masses.json", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    static void saveStable(CachedOutput cachedOutput, JsonElement jsonElement, Path path) {
        try {
            ByteArrayOutputStream $$3 = new ByteArrayOutputStream();
            HashingOutputStream $$4 = new HashingOutputStream(Hashing.sha1(), $$3);
            JsonWriter $$5 = new JsonWriter(new OutputStreamWriter($$4, StandardCharsets.UTF_8));

            try {
                $$5.setSerializeNulls(false);
                $$5.setIndent("  ");
                GsonHelper.writeValue($$5, jsonElement, KEY_COMPARATOR);
            } catch (Throwable var9) {
                try {
                    $$5.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }

                throw var9;
            }

            $$5.close();
            cachedOutput.writeIfNeeded(path, $$3.toByteArray(), $$4.hash());
        } catch (IOException var10) {
            IOException $$6 = var10;
            LOGGER.error("Failed to save file to {}", path, $$6);
        }
    }

    @Override
    public @NotNull String getName() {
        return "ControlCraft Mass Data";
    }

    public record MassEntry(String block, double mass) {}
}
