package com.verr1.vscontrolcraft.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlock;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerRenderer;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlock;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlock;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlockEntity;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlock;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlockEntity;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlock;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlock;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerInstance;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerRenderer;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerRenderer;

import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllBlockEntities {
    public static final BlockEntityEntry<ChunkLoaderBlockEntity> CHUNK_LOADER_BLOCKENTITY = REGISTRATE
            .blockEntity(ChunkLoaderBlock.ID, ChunkLoaderBlockEntity::new)
            .validBlock(AllBlocks.CHUNK_LOADER)
            .register();

    public static final BlockEntityEntry<PropellerControllerBlockEntity> PROPELLER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerControllerBlock.ID, PropellerControllerBlockEntity::new)
            .instance(()-> PropellerControllerInstance::new)
            .validBlock(AllBlocks.PROPELLER_CONTROLLER)
            .renderer(() -> PropellerControllerRenderer::new)
            .register();

    public static final BlockEntityEntry<PropellerBlockEntity> PROPELLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerBlock.ID, PropellerBlockEntity::new)
            .validBlock(AllBlocks.PROPELLER_BLOCK)
            .renderer(() -> PropellerRenderer::new)
            .register();

    public static final BlockEntityEntry<TransmitterBlockEntity> TRANSMITTER_BLOCKENTITY = REGISTRATE
            .blockEntity(TransmitterBlock.ID, TransmitterBlockEntity::new)
            .validBlock(AllBlocks.TRANSMITTER_BLOCK)
            .register();

    public static final BlockEntityEntry<ReceiverBlockEntity> RECEIVER_BLOCKENTITY = REGISTRATE
            .blockEntity(ReceiverBlock.ID, ReceiverBlockEntity::new)
            .validBlock(AllBlocks.RECEIVER_BLOCK)
            .register();

    public static final BlockEntityEntry<WingControllerBlockEntity> WING_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(WingControllerBlock.ID, WingControllerBlockEntity::new)
            .validBlock(AllBlocks.WING_CONTROLLER_BLOCK)
            .register();

    public static final BlockEntityEntry<SpinalyzerBlockEntity> SPINALYZER_BLOCKENTITY = REGISTRATE
            .blockEntity(SpinalyzerBlock.ID, SpinalyzerBlockEntity::new)
            .validBlock(AllBlocks.SPINALYZER_BLOCK_BLOCK)
            .renderer(() -> SpinalyzerRenderer::new)
            .register();


    public static void register(){

    }
}
