package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Vector2d;

import java.util.List;

import static java.lang.Math.min;

public interface ITerminalDevice {

    List<ExposedFieldWrapper> fields();

    String name();

    default ExposedFieldType exposedFieldType(){return getExposedField().type;}

    default void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo){
        var field = fields().stream().filter(f -> f.type == type).findFirst().orElse(null);
        if(field == null)return;
        field.min_max = new Vector2d(min, max);
        field.directionOptional = openTo;
    }

    default ExposedFieldWrapper getExposedField(){return ExposedFieldWrapper.EMPTY;}

    default void accept(int signal, Direction direction){
        fields()
                .stream()
                .filter(f -> f.directionOptional.test(direction))
                .forEach(f -> f.apply(signal));
    }

    default void reset(){
        fields().forEach(ExposedFieldWrapper::reset);
    }


    default void syncClient(BlockPos pos, Level world){
        var availableFields =
                fields()
                .stream()
                .map(e -> new ExposedFieldMessage(
                                e.type,
                                e.min_max.x,
                                e.min_max.y,
                                e.directionOptional
                        )
                )
                .toList();
        var p = new ExposedFieldSyncClientPacket(availableFields, pos);
        AllPackets.sendToNear(world, pos, 16, p);
    }

    default void handleClient(List<ExposedFieldMessage> messages){
        for(int i =0; i <  min(fields().size(), messages.size()); i++){
            fields().get(i).directionOptional = messages.get(i).openTo();
            fields().get(i).min_max.x = messages.get(i).min();
            fields().get(i).min_max.y = messages.get(i).max();
        }
    }

}
