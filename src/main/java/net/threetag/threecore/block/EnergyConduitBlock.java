package net.threetag.threecore.block;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.energy.CapabilityEnergy;
import net.threetag.threecore.ThreeCoreServerConfig;
import net.threetag.threecore.tileentity.EnergyConduitTileEntity;
import net.threetag.threecore.util.energy.EnergyUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public class EnergyConduitBlock extends Block implements IWaterLoggable {

    private static final Direction[] FACING_VALUES = Direction.values();
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<ConduitConnection> NORTH = EnumProperty.create("north", ConduitConnection.class);
    public static final EnumProperty<ConduitConnection> EAST = EnumProperty.create("east", ConduitConnection.class);
    public static final EnumProperty<ConduitConnection> SOUTH = EnumProperty.create("south", ConduitConnection.class);
    public static final EnumProperty<ConduitConnection> WEST = EnumProperty.create("west", ConduitConnection.class);
    public static final EnumProperty<ConduitConnection> UP = EnumProperty.create("up", ConduitConnection.class);
    public static final EnumProperty<ConduitConnection> DOWN = EnumProperty.create("down", ConduitConnection.class);
    public static final EnumProperty<ConduitDirection> DIRECTION = EnumProperty.create("direction", ConduitDirection.class);
    public static final Map<Direction, EnumProperty<ConduitConnection>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (enumMap) -> {
        enumMap.put(Direction.NORTH, NORTH);
        enumMap.put(Direction.EAST, EAST);
        enumMap.put(Direction.SOUTH, SOUTH);
        enumMap.put(Direction.WEST, WEST);
        enumMap.put(Direction.UP, UP);
        enumMap.put(Direction.DOWN, DOWN);
    });
    protected final VoxelShape[] shapes;
    protected final ConduitType type;

    public EnergyConduitBlock(Properties properties, ConduitType type, float apothem) {
        super(properties);
        this.type = type;
        this.shapes = this.makeShapes(apothem);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, false).with(NORTH, ConduitConnection.NONE).with(EAST, ConduitConnection.NONE).with(SOUTH, ConduitConnection.NONE).with(WEST, ConduitConnection.NONE).with(UP, ConduitConnection.NONE).with(DOWN, ConduitConnection.NONE));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("block.threecore.conduit_tooltip", new StringTextComponent(this.type.getTransferRate().getAsInt() + "").mergeStyle(TextFormatting.GOLD), new StringTextComponent(EnergyUtil.ENERGY_UNIT).mergeStyle(TextFormatting.GRAY)).mergeStyle(TextFormatting.GRAY));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyConduitTileEntity(this.type);
    }

    private VoxelShape[] makeShapes(float apothem) {
        float f = 0.5F - apothem;
        float f1 = 0.5F + apothem;
        VoxelShape voxelshape = Block.makeCuboidShape(f * 16.0F, f * 16.0F, f * 16.0F, f1 * 16.0F, f1 * 16.0F, f1 * 16.0F);
        VoxelShape[] avoxelshape = new VoxelShape[FACING_VALUES.length];

        for (int i = 0; i < FACING_VALUES.length; ++i) {
            Direction direction = FACING_VALUES[i];
            avoxelshape[i] = VoxelShapes.create(0.5D + Math.min(-apothem, (double) direction.getXOffset() * 0.5D), 0.5D + Math.min(-apothem, (double) direction.getYOffset() * 0.5D), 0.5D + Math.min(-apothem, (double) direction.getZOffset() * 0.5D), 0.5D + Math.max(apothem, (double) direction.getXOffset() * 0.5D), 0.5D + Math.max(apothem, (double) direction.getYOffset() * 0.5D), 0.5D + Math.max(apothem, (double) direction.getZOffset() * 0.5D));
        }

        VoxelShape[] avoxelshape1 = new VoxelShape[64];

        for (int k = 0; k < 64; ++k) {
            VoxelShape voxelshape1 = voxelshape;

            for (int j = 0; j < FACING_VALUES.length; ++j) {
                if ((k & 1 << j) != 0) {
                    voxelshape1 = VoxelShapes.or(voxelshape1, avoxelshape[j]);
                }
            }

            avoxelshape1[k] = voxelshape1;
        }

        return avoxelshape1;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.shapes[this.getShapeIndex(state)];
    }

    protected int getShapeIndex(BlockState state) {
        int i = 0;

        for (int j = 0; j < FACING_VALUES.length; ++j) {
            if (state.get(FACING_TO_PROPERTY_MAP.get(FACING_VALUES[j])) != ConduitConnection.NONE) {
                i |= 1 << j;
            }
        }

        return i;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.makeConnections(context.getWorld(), context.getPos());
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return makeConnections(worldIn, currentPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    public BlockState makeConnections(IBlockReader blockReader, BlockPos pos) {
        FluidState fluidState = blockReader.getFluidState(pos);
        boolean waterLogged = fluidState.isTagged(FluidTags.WATER) && fluidState.getLevel() == 8;
        ConduitConnection down = getConnection(blockReader, pos, Direction.DOWN);
        ConduitConnection up = getConnection(blockReader, pos, Direction.UP);
        ConduitConnection north = getConnection(blockReader, pos, Direction.NORTH);
        ConduitConnection south = getConnection(blockReader, pos, Direction.SOUTH);
        ConduitConnection east = getConnection(blockReader, pos, Direction.EAST);
        ConduitConnection west = getConnection(blockReader, pos, Direction.WEST);
        ConduitDirection direction = getDirection(north.hasConnection(), south.hasConnection(), east.hasConnection(), west.hasConnection(), up.hasConnection(), down.hasConnection());
        return this.getDefaultState().with(WATERLOGGED, waterLogged).with(DIRECTION, direction).with(DOWN, down).with(UP, up).with(NORTH, north).with(EAST, east).with(SOUTH, south).with(WEST, west);
    }

    public static ConduitConnection getConnection(IBlockReader reader, BlockPos pos, Direction direction) {
        TileEntity tileEntity = reader.getTileEntity(pos.offset(direction));

        if (tileEntity != null) {
            if (tileEntity instanceof EnergyConduitTileEntity)
                return ConduitConnection.CONDUIT;
            else if (tileEntity.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).isPresent())
                return ConduitConnection.MACHINE;
        }

        return ConduitConnection.NONE;
    }

    public static ConduitDirection getDirection(boolean north, boolean south, boolean east, boolean west, boolean up, boolean down) {
        if (north && south && !east && !west && !up && !down)
            return ConduitDirection.X;
        if (!north && !south && east && west && !up && !down)
            return ConduitDirection.Z;
        if (!north && !south && !east && !west && up && down)
            return ConduitDirection.Y;
        return ConduitDirection.MULTIPLE;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DIRECTION, WATERLOGGED);
    }

    public enum ConduitConnection implements IStringSerializable {

        NONE("none"), CONDUIT("conduit"), MACHINE("machine");

        private String name;

        ConduitConnection(String name) {
            this.name = name;
        }

        public boolean hasConnection() {
            return this != NONE;
        }

        @Override
        public String getString() {
            return this.name;
        }
    }

    public enum ConduitDirection implements IStringSerializable {

        X("x"), Y("y"), Z("z"), MULTIPLE("multiple");

        private String name;

        ConduitDirection(String name) {
            this.name = name;
        }

        public boolean hasCorner() {
            return this != MULTIPLE;
        }

        @Override
        public String getString() {
            return this.name;
        }
    }

    public enum ConduitType {

        GOLD(ThreeCoreServerConfig.ENERGY.GOLD_CONDUIT, "gold"),
        COPPER(ThreeCoreServerConfig.ENERGY.COPPER_CONDUIT, "copper"),
        SILVER(ThreeCoreServerConfig.ENERGY.SILVER_CONDUIT, "silver");

        protected IntSupplier transferRate;
        protected String name;

        ConduitType(ForgeConfigSpec.ConfigValue<Integer> transferRate, String name) {
            this.transferRate = transferRate::get;
            this.name = name;
        }

        public IntSupplier getTransferRate() {
            return transferRate;
        }

        public String getName() {
            return name;
        }

        public static ConduitType getByName(String name) {
            for (ConduitType type : values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return values()[0];
        }
    }

}
