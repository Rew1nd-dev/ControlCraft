package com.verr1.vscontrolcraft.registry;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlock;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlock;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlock;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlock;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlock;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlock;
import net.minecraft.world.level.material.MapColor;


import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllBlocks {
    static {
        REGISTRATE.setCreativeTab(AllCreativeTabs.TAB);
    }

    public static final BlockEntry<ChunkLoaderBlock> CHUNK_LOADER = REGISTRATE
            .block(ChunkLoaderBlock.ID, ChunkLoaderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(TagGen.axeOrPickaxe())
            .blockstate(
                    BlockStateGen.horizontalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<PropellerControllerBlock> PROPELLER_CONTROLLER = REGISTRATE
            .block(PropellerControllerBlock.ID, PropellerControllerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(TagGen.axeOrPickaxe())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<PropellerBlock> PROPELLER_BLOCK = REGISTRATE
            .block(PropellerBlock.ID, PropellerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();


    public static final BlockEntry<TransmitterBlock> TRANSMITTER_BLOCK = REGISTRATE
            .block(TransmitterBlock.ID, TransmitterBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();


    public static final BlockEntry<ReceiverBlock> RECEIVER_BLOCK = REGISTRATE
            .block(ReceiverBlock.ID, ReceiverBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<WingControllerBlock> WING_CONTROLLER_BLOCK = REGISTRATE
            .block(WingControllerBlock.ID, WingControllerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<SpinalyzerBlock> SPINALYZER_BLOCK_BLOCK = REGISTRATE
            .block(SpinalyzerBlock.ID, SpinalyzerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static void register(){

    }
}
