package dev.apocalypse.importerfix;
import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.MechanicalImporterMenu;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import me.khajiitos.jackseconomy.util.SlottedItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


import me.khajiitos.jackseconomy.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.*;
@Mixin(value=MechanicalImporterBlockEntity.class, remap=false)
public abstract class MechanicalImporterBlockEntityMixin extends TransactionKineticMachineBlockEntity {
    protected MechanicalImporterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type,pos,state); }
    @Shadow @Final private static int[] slotsOutput;
    @Shadow @Final private static int slotTicket;
    @Shadow public ItemDescription selectedItem;
    @Shadow private float progress;
    @Shadow public abstract double getProgressPerTick();
    @Shadow public abstract BigDecimal getTotalBalance();
    @Shadow private boolean doesRedstoneSettingMatchWorld(Level level, BlockPos pos) { throw new AssertionError(); }
    /**
     * @author Apocalypse Industries contributors
     * @reason Apply the upstream importer ticket-switch and purchase-loop safety fix.
     */
    @Overwrite
    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalImporterBlockEntity importer) {
        MechanicalImporterBlockEntityMixin self = (MechanicalImporterBlockEntityMixin)(Object) importer;
        self.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (self.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxImporterBalance.get())) < 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack inputItem = self.getItem(i);

                if (inputItem.getItem() instanceof CurrencyItem coin) {
                    self.currency = self.currency.add(coin.value.multiply(new BigDecimal(inputItem.getCount())));
                    inputItem.setCount(0);
                }
            }
        }

        ItemStack ticketItemStack = self.items.get(slotTicket);
        List<ItemDescription> items = TicketItem.getItems(ticketItemStack);

        ItemDescription selectedDescription = null;
        ItemStack itemStackToAdd = ItemStack.EMPTY;

        // A replacement manifest may no longer contain the previous selection.
        if (items.isEmpty()) {
            self.selectedItem = null;
        } else if (self.selectedItem == null || !items.contains(self.selectedItem)) {
            self.selectedItem = items.get(0);
        }

        if (self.selectedItem != null) {
            for (ItemDescription itemDescription : items) {
                if (self.selectedItem.equals(itemDescription)) {
                    itemStackToAdd = itemDescription.createItemStack();
                    selectedDescription = itemDescription;
                    break;
                }
            }
        }

        double price = self.selectedItem == null ? -1 : PriceManager.getImporterBuyPrice(self.selectedItem, 1);

        if (selectedDescription == null || itemStackToAdd.isEmpty() || !Double.isFinite(price) || price <= 0) {
            self.progress = 0.f;
            self.markUpdated();
            return;
        }

        if (ticketItemStack.isEmpty() || !self.canAddItem(itemStackToAdd, slotsOutput) || self.currency.compareTo(new BigDecimal(price)) < 0) {
            if (self.progress >= 0.f) {
                self.progress = Math.max(0.f, self.progress - 0.01f);
            }
        } else {
            double progressPerTick = self.getProgressPerTick();

            if (!self.doesRedstoneSettingMatchWorld(level, pos) || progressPerTick <= 0) {
                if (self.progress >= 0.f) {
                    self.progress = Math.max(0.f, self.progress - 0.01f);
                }
            } else {
                self.progress += progressPerTick;

                if (self.progress >= 1.f) {
                    self.buyItems(itemStackToAdd, selectedDescription, price, ticketItemStack);
                    self.progress = 0.f;

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 3, 0.2, 0.15, 0.2, 0.25);
                }
            }
        }

        self.markUpdated();
    }
    /**
     * @author Apocalypse Industries contributors
     * @reason Apply the upstream importer ticket-switch and purchase-loop safety fix.
     */
    @Overwrite
    public void buyItems(ItemStack itemStackToBuy, ItemDescription selectedDescription, double price, ItemStack ticketItem) {
        // Never enter the purchase loop with an empty stack or an invalid divisor.
        if (itemStackToBuy.isEmpty() || selectedDescription == null || ticketItem.isEmpty()
                || !Double.isFinite(price) || price <= 0) {
            return;
        }

        Supplier<Integer> availableSpaces = () -> {
            int spaces = 0;

            for (int slotNumber: slotsOutput) {
                ItemStack slot = getItem(slotNumber);

                if (slot.isEmpty()) {
                    spaces += itemStackToBuy.getMaxStackSize();
                } else if (slot.is(itemStackToBuy.getItem())) {
                    spaces += slot.getMaxStackSize() - slot.getCount();
                }
            }

            return spaces;
        };

        Supplier<BigDecimal> amountAffordable = () -> getBalance().divide(BigDecimal.valueOf(price), RoundingMode.FLOOR);

        int maxProcesses = TicketItem.getMaxProcessCount(ticketItem);
        int processCount = 0;

        while (processCount < maxProcesses && availableSpaces.get() > 0 && amountAffordable.get().intValue() > 0) {
            ItemStack stack = itemStackToBuy.copyWithCount(
                    amountAffordable.get()
                            .min(BigDecimal.valueOf(Math.min(availableSpaces.get(), Math.min(maxProcesses - processCount, itemStackToBuy.getMaxStackSize())))).intValue()
            );
            // Every iteration must make progress, even if a stack becomes invalid.
            if (stack.isEmpty()) break;
            int purchasedCount = stack.getCount();
            BigDecimal totalPrice = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(purchasedCount));

            if (!canAddItem(stack, slotsOutput) || getBalance().compareTo(totalPrice) < 0) break;
            currency = currency.subtract(totalPrice);
            addItem(stack, slotsOutput);

            processCount += purchasedCount;
        }

        if (processCount == 0) return;

        TicketItem.handleDamageWithSound(ticketItem, 1, level, worldPosition);

        PurchaseManager.addPurchase(PurchaseManager.Purchase.of(
                selectedDescription,
                processCount,
                processCount * price,
                PurchaseManager.timestamp(),
                PurchaseManager.PurchaseSource.IMPORTER
        ), this.worldPosition, (ServerLevel) this.level);
    }
}
