package com.irvin.casinovidas.slotmachine;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── Tragamonedas del servidor ────────────────────────────────────────────

    private static final ModConfigSpec.ConfigValue<Integer> SERVER_BET_COST = BUILDER
            .comment("Monedas que cuesta cada giro (se cobran del balance del jugador).")
            .defineInRange("server_bet_cost", 10, 1, 1_000_000);

    private static final ModConfigSpec.ConfigValue<Double> SERVER_WIN_CHANCE_3 = BUILDER
            .comment("Probabilidad de sacar 3 iguales (premio de ITEM). Rango: 0.0 - 1.0")
            .define("server_triple_win_chance", 0.05, Config::validateDouble);

    private static final ModConfigSpec.ConfigValue<Double> SERVER_WIN_CHANCE_2 = BUILDER
            .comment("Probabilidad de sacar 2 iguales (premio de MONEDAS). Rango: 0.0 - 1.0")
            .define("server_double_win_chance", 0.15, Config::validateDouble);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_PRIZES = BUILDER
            .comment("Premios de ITEM al sacar 3 iguales. Se elige uno AL AZAR segun su peso.\n" +
                     "Formato: 'namespace:item|cantidad|peso'\n" +
                     "  cantidad - items que se entregan (1-64)\n" +
                     "  peso     - probabilidad relativa frente a los demas premios (0-100, admite decimales)\n" +
                     "Sirve cualquier item de Minecraft o de otro mod (si el mod no esta instalado, la linea se ignora).\n" +
                     "Ejemplo: [\"minecraft:diamond|2|5\", \"minecraft:netherite_scrap|1|0.5\"]")
            .defineListAllowEmpty("server_prizes",
                    List.of("minecraft:emerald|8|10", "minecraft:gold_ingot|8|10",
                            "minecraft:diamond|2|5", "minecraft:netherite_scrap|1|1"),
                    () -> "minecraft:diamond|1|1",
                    Config::validatePrizeEntry);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SERVER_COIN_PRIZES = BUILDER
            .comment("Premios de MONEDAS al sacar 2 iguales (premio de consuelo).\n" +
                     "Formato: 'monedas|peso'\n" +
                     "Ejemplo: [\"15|5\", \"40|2\", \"150|0.5\"]")
            .defineListAllowEmpty("server_coin_prizes",
                    List.of("15|5", "40|2", "150|0.5"),
                    () -> "15|5",
                    Config::validateCoinPrize);

    private static final ModConfigSpec.ConfigValue<Boolean> DOUBLE_TRIPLE_PAYOUT = BUILDER
            .comment("Solo como respaldo: si no hay premios de item configurados y sale un triple,\n" +
                     "se paga un premio de monedas y, con esto en true, se duplica.")
            .define("double_triple_payout", true);

    private static final ModConfigSpec.ConfigValue<Boolean> SERVER_SHOW_PRIZE_PANEL = BUILDER
            .comment("Mostrar el panel de premios al lado de la tragamonedas.")
            .define("server_show_prize_panel", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int serverBetCost;
    public static double serverTripleWinChance;
    public static double serverDoubleWinChance;
    public static List<ServerPrize> serverPrizes = List.of();
    public static List<CoinPrize> serverCoinPrizes = List.of();
    public static boolean doubleTriplePayout;
    public static boolean serverShowPrizePanel;

    /** Premio en monedas (2 iguales). */
    public record CoinPrize(int coins, double chance) {}

    /** Premio en item (3 iguales). {@code chance} es un peso relativo. */
    public record ServerPrize(Item item, int amount, double chance) {
        public ItemStack toStack() {
            return new ItemStack(item, amount);
        }
    }

    private static boolean validateDouble(final Object obj) {
        return obj instanceof final Double d && d >= 0.0 && d <= 1.0;
    }

    private static boolean validateCoinPrize(final Object obj) {
        if (!(obj instanceof String entry)) return false;
        String[] parts = entry.split("\\|");
        if (parts.length != 2) return false;
        try {
            return Integer.parseInt(parts[0].trim()) > 0 && Double.parseDouble(parts[1].trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validatePrizeEntry(final Object obj) {
        if (!(obj instanceof String s)) return false;
        String[] parts = s.split("\\|");
        if (parts.length != 2 && parts.length != 3) return false;
        try {
            ResourceLocation.parse(parts[0].trim());
        } catch (Exception e) {
            return false;
        }
        try {
            if (parts.length == 3) {
                int amount = Integer.parseInt(parts[1].trim());
                if (amount < 1 || amount > 64) return false;
            }
            double chance = Double.parseDouble(parts[parts.length - 1].trim());
            return chance >= 0.0 && chance <= 100.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        serverBetCost = SERVER_BET_COST.get();
        serverTripleWinChance = SERVER_WIN_CHANCE_3.get();
        serverDoubleWinChance = SERVER_WIN_CHANCE_2.get();
        doubleTriplePayout = DOUBLE_TRIPLE_PAYOUT.get();
        serverShowPrizePanel = SERVER_SHOW_PRIZE_PANEL.get();

        serverPrizes = SERVER_PRIZES.get().stream()
                .map(entry -> {
                    String[] parts = entry.split("\\|");
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[0].trim()));
                    int amount = parts.length == 3
                            ? Math.min(64, Math.max(1, Integer.parseInt(parts[1].trim())))
                            : 1;
                    double chance = Double.parseDouble(parts[parts.length - 1].trim());
                    return new ServerPrize(item, amount, chance);
                })
                // getValue devuelve AIR (no null) cuando el id no existe, por ejemplo si falta un mod
                .filter(p -> p.item() != null && p.item() != Items.AIR && p.chance() > 0.0)
                .collect(Collectors.toList());

        serverCoinPrizes = SERVER_COIN_PRIZES.get().stream()
                .map(entry -> {
                    String[] p = entry.split("\\|");
                    return new CoinPrize(Math.max(1, Integer.parseInt(p[0].trim())), Double.parseDouble(p[1].trim()));
                })
                .filter(p -> p.chance() > 0.0)
                .collect(Collectors.toList());
    }
}
