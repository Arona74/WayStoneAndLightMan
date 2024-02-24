package top.prefersmin.waystoneandlightman.util;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import net.minecraft.world.entity.player.Player;
import top.prefersmin.waystoneandlightman.config.ModConfig;
import top.prefersmin.waystoneandlightman.vo.TeleportCostVo;

/**
 * 工具类
 *
 * @author PrefersMin
 * @version 1.0
 */
public class CostUtil {

    public static TeleportCostVo TeleportCostCalculate(Player player, int distance) {

        TeleportCostVo teleportCost = new TeleportCostVo();

        // 判断是否处于创造模式
        if (player.getAbilities().instabuild) {
            return teleportCost.free();
        }

        int moneyCostPerHundredMeter = ModConfig.moneyCostPerHundredMeter;
        int minimumCost = ModConfig.minimumCost;
        int maximumCost = ModConfig.maximumCost;
        int moneyValue = (ModConfig.roundUp ? (int) Math.ceil(distance / (double) 100) : (int) Math.floor(distance / (double) 100)) * moneyCostPerHundredMeter;

        int moneyInt = Math.min(Math.max(moneyValue, minimumCost), maximumCost);

        if (moneyInt == 0) {
            return teleportCost.free();
        }

        // 格式化货币
        MoneyValue moneyCost = CoinValue.fromNumber("main", moneyInt);

        // 获取货币持有者
        IMoneyHolder handler = MoneyAPI.API.GetPlayersMoneyHandler(player);

        // 判断是否有能力支付传送费用
        boolean canPlayerAfford = handler.getStoredMoney().containsValue(moneyCost) && handler.extractMoney(moneyCost, true).isEmpty();

        return teleportCost.canAfford(canPlayerAfford).cost(moneyCost);

    }

}
