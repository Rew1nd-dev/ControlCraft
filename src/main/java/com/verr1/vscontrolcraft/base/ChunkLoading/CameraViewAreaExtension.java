package com.verr1.vscontrolcraft.base.ChunkLoading;

import com.verr1.vscontrolcraft.ControlCraft;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public class CameraViewAreaExtension {
    private static final Long2ObjectOpenHashMap<ChunkRenderDispatcher.RenderChunk> SECTIONS = new Long2ObjectOpenHashMap<>();
    private static ChunkRenderDispatcher chunkRenderDispatcher;
    private static int minSectionY;
    private static int maxSectionY;

    private CameraViewAreaExtension() {}

    public static void allChanged(ChunkRenderDispatcher newFactory, Level level) {
        chunkRenderDispatcher = newFactory;
        minSectionY = level.getMinSection();
        maxSectionY = level.getMaxSection();
    }

    public static ChunkRenderDispatcher.RenderChunk provideSection(long sectionPos) {
        return SECTIONS.computeIfAbsent(sectionPos, CameraViewAreaExtension::createSection);
    }

    private static ChunkRenderDispatcher.RenderChunk createSection(long sectionPos) {
        BlockPos sectionOrigin = SectionPos.of(sectionPos).origin();

        return chunkRenderDispatcher.new RenderChunk(0, sectionOrigin.getX(), sectionOrigin.getY(), sectionOrigin.getZ());
    }

    public static void setDirty(int cx, int cy, int cz, boolean playerChanged) {
        ChunkRenderDispatcher.RenderChunk section = rawFetch(cx, cy, cz, false);

        if (section != null)
            section.setDirty(playerChanged);
    }

    public static void onChunkUnload(int sectionX, int sectionZ) {
        for (int sectionY = minSectionY; sectionY < maxSectionY; sectionY++) {
            long sectionPos = SectionPos.asLong(sectionX, sectionY, sectionZ);
            ChunkRenderDispatcher.RenderChunk section = SECTIONS.get(sectionPos);

            if (section != null) {
                section.releaseBuffers();
                ControlCraft.LOGGER.info("dump view area of: " + sectionX + " " + sectionZ);
                SECTIONS.remove(sectionPos);
            }
        }
    }

    public static ChunkRenderDispatcher.RenderChunk rawFetch(int cx, int cy, int cz, boolean generateNew) {
        if (cy < minSectionY || cy >= maxSectionY)
            return null;

        long sectionPos = SectionPos.asLong(cx, cy, cz);

        return generateNew ? provideSection(sectionPos) : SECTIONS.get(sectionPos);
    }

    public static void clear() {
        for (ChunkRenderDispatcher.RenderChunk section : SECTIONS.values()) {
            section.releaseBuffers();
        }

        SECTIONS.clear();
    }

    public static int minSectionY() {
        return minSectionY;
    }

    public static int maxSectionY() {
        return maxSectionY;
    }
}
