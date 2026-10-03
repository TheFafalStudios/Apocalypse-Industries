package dev.apocalypse.importertest;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.data.price.*;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.TicketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("importer_regression")
public class ImporterRegression {
  static int checks;

  public ImporterRegression() {
    NeoForge.EVENT_BUS.addListener(this::run);
  }

  public static class Electric extends ImporterBlockEntity {
    Electric(ServerLevel level) {
      super(new BlockPos(0, 80, 0), ItemBlockReg.IMPORTER.get().defaultBlockState());
      setLevel(level);
      currency = BigDecimal.valueOf(1000);
    }

    public double getProgressPerTick() {
      return 1;
    }

    public int getEnergyUsagePerTick() {
      return 0;
    }

    public void markUpdated() {}
  }

  public static class Mechanical extends MechanicalImporterBlockEntity {
    Mechanical(ServerLevel level) {
      super(new BlockPos(0, 80, 0), ItemBlockReg.MECHANICAL_IMPORTER.get().defaultBlockState());
      setLevel(level);
      currency = BigDecimal.valueOf(1000);
    }

    public double getProgressPerTick() {
      return 1;
    }

    public void tick() {}

    public void markUpdated() {}
  }

  static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
    checks++;
  }

  static Field field(Object o, String name) throws Exception {
    for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass())
      try {
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        return f;
      } catch (NoSuchFieldException ignored) {
      }
    throw new NoSuchFieldException(name);
  }

  static Object call(Object o, String name, Class<?>[] types, Object... args) throws Exception {
    return o.getClass().getMethod(name, types).invoke(o, args);
  }

  static ItemDescription desc(Item item) {
    return ItemDescription.ofItem(new ItemStack(item));
  }

  static ItemStack ticket(Item... items) {
    ItemStack t = new ItemStack(ItemBlockReg.IMPORTER_TICKET_ITEM.get());
    TicketItem.setItems(t, Arrays.stream(items).map(ImporterRegression::desc).toList());
    TicketItem.setMaxUsage(t, 100);
    return t;
  }

  static void slot(Object m, int n, ItemStack stack) throws Exception {
    call(m, "setItem", new Class[] {int.class, ItemStack.class}, n, stack);
  }

  static ItemStack slot(Object m, int n) throws Exception {
    return (ItemStack) call(m, "getItem", new Class[] {int.class}, n);
  }

  static BigDecimal balance(Object m) throws Exception {
    return (BigDecimal) call(m, "getBalance", new Class[] {});
  }

  static void tick(Object m, ServerLevel level) {
    if (m instanceof Electric e)
      ImporterBlockEntity.tick(level, e.getBlockPos(), e.getBlockState(), e);
    else {
      Mechanical e = (Mechanical) m;
      MechanicalImporterBlockEntity.tick(level, e.getBlockPos(), e.getBlockState(), e);
    }
  }

  static void buy(Object m, ItemStack stack, ItemDescription d, double price, ItemStack t)
      throws Exception {
    call(
        m,
        "buyItems",
        new Class[] {ItemStack.class, ItemDescription.class, double.class, ItemStack.class},
        stack,
        d,
        price,
        t);
  }

  static void clear(Object m) throws Exception {
    for (int i = 3; i < 9; i++) slot(m, i, ItemStack.EMPTY);
  }

  static int count(Object m, Item item) throws Exception {
    int n = 0;
    for (int i = 3; i < 9; i++) {
      ItemStack s = slot(m, i);
      if (s.is(item)) n += s.getCount();
    }
    return n;
  }

  void test(Object m, ServerLevel level, String name) throws Exception {
    System.out.println("IMPORTER TEST " + name + " fresh diorite");
    slot(m, 9, ticket(Items.DIORITE));
    tick(m, level);
    check(count(m, Items.DIORITE) == 1, name + " fresh diorite");
    clear(m);
    // Reset selection to start the exact reported sequence.
    field(m, "selectedItem").set(m, null);
    slot(m, 9, ticket(Items.ANDESITE));
    tick(m, level);
    check(count(m, Items.ANDESITE) == 1, name + " andesite");
    clear(m);
    System.out.println("IMPORTER TEST " + name + " switching andesite -> diorite");
    BigDecimal before = balance(m);
    ItemStack t = ticket(Items.DIORITE);
    slot(m, 9, t);
    tick(m, level);
    check(count(m, Items.DIORITE) == 1, name + " switched diorite");
    check(balance(m).compareTo(before.subtract(BigDecimal.valueOf(3))) == 0, name + " new price");
    check(t.getDamageValue() == 1, name + " one ticket use");
    clear(m);
    slot(m, 9, ticket(Items.ANDESITE));
    tick(m, level);
    check(count(m, Items.ANDESITE) == 1, name + " switch back");
    clear(m);
    field(m, "selectedItem").set(m, desc(Items.DIORITE));
    slot(m, 9, ticket(Items.ANDESITE, Items.DIORITE));
    tick(m, level);
    check(count(m, Items.DIORITE) == 1, name + " preserve valid choice");
    clear(m);
    slot(m, 9, ItemStack.EMPTY);
    tick(m, level);
    check(field(m, "selectedItem").get(m) == null, name + " removed ticket clears selection");
    t = ticket();
    slot(m, 9, t);
    tick(m, level);
    check(t.getDamageValue() == 0, name + " empty manifest unchanged");
    t = ticket(Items.AIR);
    slot(m, 9, t);
    before = balance(m);
    tick(m, level);
    check(
        balance(m).compareTo(before) == 0 && t.getDamageValue() == 0,
        name + " air manifest no transaction");
    check((float) field(m, "progress").get(m) == 0, name + " invalid progress reset");
    t = ticket(Items.DIORITE);
    before = balance(m);
    buy(m, ItemStack.EMPTY, desc(Items.DIORITE), 3, t);
    buy(m, new ItemStack(Items.DIORITE), null, 3, t);
    buy(m, new ItemStack(Items.DIORITE), desc(Items.DIORITE), 3, ItemStack.EMPTY);
    for (double price :
        new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      buy(m, new ItemStack(Items.DIORITE), desc(Items.DIORITE), price, t);
      check(
          balance(m).compareTo(before) == 0 && t.getDamageValue() == 0,
          name + " rejected direct price " + price);
      PriceManager.getPricesInfo(desc(Items.DIORITE)).importerBuyPrice = price;
      slot(m, 9, t);
      tick(m, level);
      check(
          balance(m).compareTo(before) == 0 && t.getDamageValue() == 0,
          name + " rejected tick price " + price);
    }
    PriceManager.getPricesInfo(desc(Items.DIORITE)).importerBuyPrice = 3;
    check(count(m, Items.DIORITE) == 0, name + " invalid inputs no output");
    t = ticket(Items.DIORITE);
    TicketItem.setMaxProcessCount(t, 0);
    buy(m, new ItemStack(Items.DIORITE), desc(Items.DIORITE), 3, t);
    check(
        t.getDamageValue() == 0 && balance(m).compareTo(before) == 0, name + " zero process count");
    field(m, "currency").set(m, BigDecimal.ZERO);
    t = ticket(Items.DIORITE);
    slot(m, 9, t);
    tick(m, level);
    check(count(m, Items.DIORITE) == 0 && t.getDamageValue() == 0, name + " insufficient balance");
    field(m, "currency").set(m, BigDecimal.valueOf(1000));
    for (int i = 3; i < 9; i++) slot(m, i, new ItemStack(Items.STONE, 64));
    buy(m, new ItemStack(Items.DIORITE), desc(Items.DIORITE), 3, t);
    tick(m, level);
    check(
        balance(m).compareTo(BigDecimal.valueOf(1000)) == 0 && t.getDamageValue() == 0,
        name + " full output");
    clear(m);
    TicketItem.setMaxProcessCount(t, 130);
    buy(m, new ItemStack(Items.DIORITE), desc(Items.DIORITE), 3, t);
    check(count(m, Items.DIORITE) == 130, name + " multi-stack output");
    check(balance(m).compareTo(BigDecimal.valueOf(610)) == 0, name + " multi-stack cost");
    check(t.getDamageValue() == 1, name + " multi-stack one use");
    System.out.println("IMPORTER TEST " + name + " passed");
  }

  void run(ServerStartedEvent event) {
    try {
      PriceManager.resetData();
      PriceManager.addPriceInfo(
          desc(Items.ANDESITE), new PricesItemPriceInfo(-1, -1, 2, null, null));
      PriceManager.addPriceInfo(
          desc(Items.DIORITE), new PricesItemPriceInfo(-1, -1, 3, null, null));
      test(new Electric(event.getServer().overworld()), event.getServer().overworld(), "electric");
      test(
          new Mechanical(event.getServer().overworld()),
          event.getServer().overworld(),
          "mechanical");
      System.out.println("IMPORTER REGRESSION PASS checks=" + checks);
    } catch (Throwable e) {
      System.out.println("IMPORTER REGRESSION FAIL");
      e.printStackTrace();
    }
    event.getServer().halt(false);
  }
}
