package dev.apocalypse.subbalance;
public final class PolicyRegression {
    private static int checks;
    private static void check(boolean b) { checks++; if(!b) throw new AssertionError("check "+checks); }
    public static void main(String[] args) {
        String[][] tiers={{"minecraft:netherite_block","225","170"},{"createbigcannons:nethersteel_block","150","145"},
            {"immersiveengineering:storage_steel","150","120"},{"dndecor:iron_sheet_metal","100","80"},
            {"create_submarine:iron_pressurizer","60","40"},{"minecraft:tinted_glass","60","40"}};
        for(var row:tiers) {
            check(Policy.cap(row[0],1500,false,false,false)==Integer.parseInt(row[1]));
            check(Policy.cap(row[0],1500,true,false,false)==Integer.parseInt(row[2]));
            check(Policy.cap(row[0],15,true,false,false)==15);
        }
        for(var e:Policy.FAMILIES.entrySet()) {
            int water=Policy.cap(e.getKey(),1500,false,false,false), lava=Policy.cap(e.getKey(),1500,true,false,false);
            check(lava<=water); check(lava<=170);
            if(!e.getValue().equals("solid_netherite"))check(lava*3.2/3<157);
        }
        check(Policy.cap("unknown:strong_block",99999,true,false,false)==80);
        check(Policy.cap("unknown:clear_panel",99999,true,true,false)==40);
        check(Policy.cap("minecraft:netherite_block",225,true,false,true)==145);
        check(Policy.cap("minecraft:obsidian",100,false,false,false)==100);
        check(Policy.cap("create_submarine:ballast_vent",400,true,false,false)==120);
        check(170*2*1.430294373/3>160 && 170*2*1.430294373/3<165);
        check(40*3.2/3<156);
        check(170*2/3<157);
        System.out.println("PASS: "+checks+" policy/boundary checks");
    }
}
