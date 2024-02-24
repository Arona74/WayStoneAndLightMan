package top.prefersmin.waystoneandlightman.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.client.gui.widget.WaystoneButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.prefersmin.waystoneandlightman.util.CostUtil;
import top.prefersmin.waystoneandlightman.vo.TeleportCostVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mixin混入
 *
 * @author PrefersMin
 * @version 1.1
 */
@Mixin(WaystoneButton.class)
public class WaystoneButtonMixin extends Button {

    /**
     * 影射原有变量
     */
    @Final
    @Shadow(remap = false)
    private int xpLevelCost;

    /**
     * 影射原有变量
     */
    @Final
    @Shadow(remap = false)
    private IWaystone waystone;

    /**
     * 距离
     */
    @Unique
    private int wayStoneAndLightMan$distance;

    /**
     * 传送花费对象
     */
    @Unique
    private TeleportCostVo wayStoneAndLightMan$teleportCost;

    /**
     * 是否渲染距离与消耗
     */
    @Unique
    private boolean wayStoneAndLightMan$isRender;

    /**
     * 左侧消耗图标
     */
    @Unique
    private static final ResourceLocation COIN_GOLD = new ResourceLocation("lightmanscurrency", "textures/item/coin_gold.png");

    protected WaystoneButtonMixin(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, CreateNarration pCreateNarration) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pCreateNarration);
    }

    // 额外判断isRender
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void WaystoneButton(int x, int y, IWaystone waystone, int xpLevelCost, OnPress pressable, CallbackInfo ci) {

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        // 计算距离
        wayStoneAndLightMan$distance = (int) player.position().distanceTo(waystone.getPos().getCenter());
        // 计算传送费用并判断余额是否足以支付传送费用
        wayStoneAndLightMan$teleportCost = CostUtil.TeleportCostCalculate(player, wayStoneAndLightMan$distance);

        // 余额不足
        if (!wayStoneAndLightMan$teleportCost.isCanAfford() && !player.getAbilities().instabuild) {
            active = false;
        }

        // 渲染距离大于5m以上的传送石按钮
        if (wayStoneAndLightMan$distance > 5) {
            wayStoneAndLightMan$isRender = true;
        }

    }

    // 重写渲染方法
    @Inject(method = "m_87963_", at = @At("HEAD"), remap = false, cancellable = true)
    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Minecraft mc = Minecraft.getInstance();

        // 渲染传送花费
        if (wayStoneAndLightMan$isRender) {

            // 渲染距离
            if (waystone.getDimension() == mc.player.level().dimension()) {

                String distanceStr;

                if (wayStoneAndLightMan$distance < 10000 && (mc.font.width(getMessage()) < 120 || wayStoneAndLightMan$distance < 1000)) {
                    distanceStr = wayStoneAndLightMan$distance + "m";
                } else {
                    distanceStr = String.format("%.1f", wayStoneAndLightMan$distance / 1000f).replace(",0", "").replace(".0", "") + "km";
                }

                int xOffset = getWidth() - mc.font.width(distanceStr);
                guiGraphics.drawString(mc.font, distanceStr, getX() + xOffset - 4, getY() + 6, isActive() ? 0xFFFFFF : 0x9E9E9E);

            }


            // 渲染传送消耗
            guiGraphics.blit(COIN_GOLD, getX() + 2, getY() + 2, 16, 16, 16, 16, 16, 16);

            // 判断鼠标指针位置
            if (isHovered && mouseX <= getX() + 16) {

                final List<Component> tooltip = new ArrayList<>();

                // 判断余额与经验是否足以传送消耗
                boolean haveXpLevelRequirement = xpLevelCost > 0;
                boolean haveMoneyRequirement = !wayStoneAndLightMan$teleportCost.getCost().isFree();
                boolean canXpLevelAfford = Objects.requireNonNull(mc.player).experienceLevel >= xpLevelCost || mc.player.getAbilities().instabuild;

                // 经验消耗提示
                if (haveXpLevelRequirement) {
                    final var levelRequirementText = Component.translatable("gui.waystones.waystone_selection.level_requirement", xpLevelCost);
                    levelRequirementText.withStyle(canXpLevelAfford ? ChatFormatting.GREEN : ChatFormatting.RED);
                    tooltip.add(levelRequirementText);
                }

                // 余额消耗提示
                if (haveMoneyRequirement) {
                    final var moneyRequirementText = Component.translatable("gui.need", wayStoneAndLightMan$teleportCost.getCost().getString());
                    moneyRequirementText.withStyle(wayStoneAndLightMan$teleportCost.isCanAfford() ? ChatFormatting.GREEN : ChatFormatting.RED);
                    tooltip.add(moneyRequirementText);
                }

                // 没有消耗
                if (!haveXpLevelRequirement && !haveMoneyRequirement) {
                    final var moneyRequirementText = Component.translatable("gui.free");
                    moneyRequirementText.withStyle(ChatFormatting.GREEN);
                    tooltip.add(moneyRequirementText);
                }

                guiGraphics.renderTooltip(mc.font, tooltip, Optional.empty(), mouseX, mouseY + mc.font.lineHeight);

            }

        }

        // 在头部混入并提前结束方法，来达到重写方法的目的
        ci.cancel();

    }

}
