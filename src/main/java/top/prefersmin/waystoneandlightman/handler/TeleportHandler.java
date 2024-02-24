package top.prefersmin.waystoneandlightman.handler;

import com.mojang.logging.LogUtils;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.blay09.mods.waystones.api.WaystoneTeleportEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import top.prefersmin.waystoneandlightman.WayStoneAndLightMan;
import top.prefersmin.waystoneandlightman.config.ModConfig;
import top.prefersmin.waystoneandlightman.util.CostUtil;
import top.prefersmin.waystoneandlightman.vo.TeleportCostVo;

/**
 * 监听传送事件
 *
 * @author PrefersMin
 * @version 1.1
 */
@Mod.EventBusSubscriber(modid = WayStoneAndLightMan.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeleportHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    private static String getMoneyCostString(MoneyValue moneyCost) {

        String moneyCostString = moneyCost.getString();

        if (ModConfig.forceEnableChineseLanguage) {

            moneyCostString = moneyCostString.replace("c", "铜币 ")
                    .replace("i", "铁币 ")
                    .replace("g", "金币 ")
                    .replace("e", "绿宝石币 ")
                    .replace("d", "钻石币 ")
                    .replace("n", "下界合金币 ");

        }

        return moneyCostString;

    }

    /**
     * 监听方法
     *
     * @param event 传送事件
     */
    @SubscribeEvent
    public void onWayStoneTeleport(WaystoneTeleportEvent.Pre event) {
        Entity teleportedEntity = event.getContext().getEntity();
        if (teleportedEntity instanceof Player player) {

            // 判断是否处于创造模式
            if (player.getAbilities().instabuild) {
                return;
            }

            // 计算距离
            BlockPos pos = event.getContext().getTargetWaystone().getPos();
            int distance = (int) player.position().distanceTo(pos.getCenter());

            // 计算传送费用并判断余额是否足以支付传送费用
            TeleportCostVo teleportCostVo = CostUtil.TeleportCostCalculate(player, distance);

            // 执行消费或取消传送
            if (teleportCostVo.isCanAfford()) {
                MoneyAPI.API.GetPlayersMoneyHandler(player).extractMoney(teleportCostVo.getCost(), false);
            } else {
                event.setCanceled(true);
            }

            // 判断是否强制使用中文
            String moneyCostString = getMoneyCostString(teleportCostVo.getCost());

            // 控制台输出
            if (ModConfig.enableConsoleLog) {
                LOGGER.info("--------------------------------------------");
                LOGGER.info(Component.translatable("gui.teleportLog", player.getName().getString(), distance).getString());
                if (teleportCostVo.isCanAfford()) {
                    LOGGER.info(Component.translatable("gui.teleportCost", moneyCostString).getString());
                } else {
                    LOGGER.info(Component.translatable("gui.notSufficientFundsLog", moneyCostString).getString());
                }
                LOGGER.info("--------------------------------------------");
            }

            if (teleportCostVo.getCost().isFree()) {
                return;
            }

            // 局内提示
            if (ModConfig.enableCostTip) {
                Component message;
                if (teleportCostVo.isCanAfford()) {
                    message = Component.translatable("gui.alertMoneyCost", moneyCostString);
                } else {
                    message = Component.translatable("gui.notSufficientFunds");
                }
                player.displayClientMessage(message, true);
            }

        }
    }

}
