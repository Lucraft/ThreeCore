package net.threetag.threecore.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.threetag.threecore.block.EnergyConduitBlock;
import net.threetag.threecore.util.energy.EnergyStorageExt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyConduitTileEntity extends TileEntity implements ITickableTileEntity {

    public EnergyStorageExt energyStorage;
    private LazyOptional<IEnergyStorage> energyStorageLazyOptional;
    protected EnergyConduitBlock.ConduitType type;

    public EnergyConduitTileEntity(EnergyConduitBlock.ConduitType type) {
        super(TCTileEntityTypes.CONDUIT.get());
        this.type = type;
        this.energyStorage = new EnergyStorageExt(type.getTransferRate().getAsInt() * 5, type.getTransferRate().getAsInt());
        this.energyStorageLazyOptional = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void onLoad() {
        super.onLoad();

    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isRemote)
            return;

        BlockState state = this.world.getBlockState(this.pos);
        for (Direction direction : Direction.values()) {
            if (state.get(EnergyConduitBlock.FACING_TO_PROPERTY_MAP.get(direction)).hasConnection()) {
                TileEntity tileEntity = this.world.getTileEntity(pos.offset(direction));

                if (tileEntity instanceof EnergyConduitTileEntity) {
                    EnergyStorageExt otherCableEnergy = ((EnergyConduitTileEntity) tileEntity).energyStorage;
                    if (this.energyStorage.getEnergyStored() > otherCableEnergy.getEnergyStored() && Math.abs(this.energyStorage.getEnergyStored() - otherCableEnergy.getEnergyStored()) > 1) {
                        int transfer = (this.energyStorage.getEnergyStored() - otherCableEnergy.getEnergyStored()) / 2;
                        this.energyStorage.modifyEnergy(-otherCableEnergy.receiveEnergy(Math.min(transfer, this.energyStorage.getMaxExtract()), false));
                    }
                } else if (tileEntity != null) {
                    tileEntity.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(machineEnergy -> {
                        this.energyStorage.modifyEnergy(-machineEnergy.receiveEnergy(Math.min(this.energyStorage.getEnergyStored(), this.energyStorage.getMaxExtract()), false));
                    });
                }
            }
        }
    }

    @Override
    public void read(BlockState blockState, CompoundNBT nbt) {
        super.read(blockState, nbt);
        this.type = EnergyConduitBlock.ConduitType.getByName(nbt.getString("Type"));
        this.energyStorage = new EnergyStorageExt(this.type.getTransferRate().getAsInt() * 5, this.type.getTransferRate().getAsInt(), this.type.getTransferRate().getAsInt(), nbt.getInt("Energy"));

    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putString("Type", this.type.getName());
        nbt.putInt("Energy", this.energyStorage.getEnergyStored());
        return super.write(nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityEnergy.ENERGY ? this.energyStorageLazyOptional.cast() : LazyOptional.empty();
    }
}
