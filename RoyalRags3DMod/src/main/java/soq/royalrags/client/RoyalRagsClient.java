package soq.royalrags.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public final class RoyalRagsClient implements ClientModInitializer {
    private static final String MOD_ID = "royalrags";

    @Override
    public void onInitializeClient() {
        reg(Style.LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        reg(Style.CHAINMAIL, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        reg(Style.IRON, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        reg(Style.GOLD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        reg(Style.DIAMOND, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        reg(Style.NETHERITE, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    }

    private static void reg(Style style, ItemConvertible... items) {
        ArmorRenderer.register(new Renderer(style), items);
    }

    private enum Style {
        LEATHER("leather"), CHAINMAIL("chainmail"), IRON("iron"),
        GOLD("gold"), DIAMOND("diamond"), NETHERITE("netherite");

        final Identifier texture;
        Style(String name) {
            texture = Identifier.of(MOD_ID, "textures/armor/" + name + ".png");
        }
    }

    private static final class Renderer implements ArmorRenderer {
        private final Style style;
        private final ArmorEntityModel<LivingEntity> model;

        Renderer(Style style) {
            this.style = style;
            this.model = build(style);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack,
                           LivingEntity entity, EquipmentSlot slot, int light,
                           BipedEntityModel<LivingEntity> contextModel) {
            contextModel.copyBipedStateTo(model);
            visible(model, slot);
            ArmorRenderer.renderPart(matrices, consumers, light, stack, model, style.texture);
        }
    }

    private static ArmorEntityModel<LivingEntity> build(Style style) {
        ModelData data = ArmorEntityModel.getModelData(new Dilation(0.62F));
        decorate(data.getRoot(), style);
        return new ArmorEntityModel<>(TexturedModelData.of(data, 128, 128).createModel());
    }

    private static void visible(ArmorEntityModel<LivingEntity> m, EquipmentSlot slot) {
        m.setVisible(false);
        m.hat.visible = false;
        switch (slot) {
            case HEAD -> m.head.visible = true;
            case CHEST -> {
                m.body.visible = true;
                m.rightArm.visible = true;
                m.leftArm.visible = true;
            }
            case LEGS -> {
                m.body.visible = true;
                m.rightLeg.visible = true;
                m.leftLeg.visible = true;
            }
            case FEET -> {
                m.rightLeg.visible = true;
                m.leftLeg.visible = true;
            }
            default -> { }
        }
    }

    private static void decorate(ModelPartData root, Style style) {
        ModelPartData h = root.getChild("head");
        ModelPartData b = root.getChild("body");
        ModelPartData ra = root.getChild("right_arm");
        ModelPartData la = root.getChild("left_arm");
        ModelPartData rl = root.getChild("right_leg");
        ModelPartData ll = root.getChild("left_leg");

        switch (style) {
            case LEATHER -> {
                c(h,"hood_top",64,0,-4.7F,-8.8F,-4.7F,9.4F,2.0F,9.4F);
                c(h,"hood_back",64,12,-4.8F,-7.2F,3.5F,9.6F,7.5F,1.7F);
                c(b,"rope_belt",64,24,-4.8F,8.0F,-2.8F,9.6F,1.5F,5.6F);
                c(b,"tail_l",80,24,-4.4F,10.0F,1.9F,4.0F,5.5F,1.0F);
                c(b,"tail_r",90,24,0.4F,10.0F,1.9F,4.0F,4.2F,1.0F);
                c(ra,"patch_r",64,36,-3.9F,3.0F,-2.7F,4.8F,4.0F,5.4F);
                c(la,"patch_l",76,36,-0.9F,4.0F,-2.7F,4.8F,3.0F,5.4F);
                c(rl,"boot_r",64,48,-2.6F,8.0F,-2.6F,5.2F,4.3F,5.2F);
                c(ll,"boot_l",76,48,-2.6F,8.0F,-2.6F,5.2F,4.3F,5.2F);
            }
            case CHAINMAIL -> {
                c(h,"cap",64,0,-4.8F,-9.2F,-4.8F,9.6F,2.0F,9.6F);
                c(h,"cap_brim",64,12,-4.0F,-7.5F,-6.5F,8.0F,0.9F,3.0F);
                c(b,"hood_collar",64,20,-5.1F,-0.6F,-3.0F,10.2F,3.0F,6.0F);
                c(b,"chain",86,20,-2.8F,2.4F,-3.1F,5.6F,1.0F,0.8F);
                c(ra,"shoulder_r",64,32,-4.4F,-2.1F,-3.2F,5.7F,4.2F,6.4F);
                c(la,"shoulder_l",78,32,-1.3F,-2.1F,-3.2F,5.7F,4.2F,6.4F);
                c(rl,"shoe_r",64,48,-2.7F,7.0F,-3.0F,5.4F,5.3F,6.0F);
                c(ll,"shoe_l",78,48,-2.7F,7.0F,-3.0F,5.4F,5.3F,6.0F);
            }
            case IRON -> {
                c(h,"glasses",64,0,-4.7F,-5.3F,-5.0F,9.4F,2.0F,1.0F);
                c(b,"suit",64,12,-4.7F,-0.3F,-2.8F,9.4F,12.7F,5.6F);
                c(b,"tie",86,12,-1.0F,1.0F,-3.25F,2.0F,7.5F,0.7F);
                c(ra,"shoulder_r",64,36,-4.6F,-2.2F,-3.1F,6.0F,4.3F,6.2F);
                c(la,"shoulder_l",78,36,-1.4F,-2.2F,-3.1F,6.0F,4.3F,6.2F);
                c(rl,"shoe_r",64,52,-2.7F,9.0F,-3.5F,5.4F,3.2F,6.2F);
                c(ll,"shoe_l",78,52,-2.7F,9.0F,-3.5F,5.4F,3.2F,6.2F);
            }
            case GOLD -> {
                c(h,"gold_cap",64,0,-4.8F,-9.1F,-4.8F,9.6F,2.4F,9.6F);
                c(h,"gold_band",64,14,-4.9F,-7.3F,-4.9F,9.8F,1.6F,9.8F);
                c(b,"luxury_jacket",64,24,-4.9F,-0.4F,-2.9F,9.8F,12.8F,5.8F);
                c(b,"thick_chain",88,24,-3.3F,1.5F,-3.4F,6.6F,1.4F,0.9F);
                c(ra,"gold_shoulder_r",64,42,-4.7F,-2.4F,-3.3F,6.2F,4.5F,6.6F);
                c(la,"gold_shoulder_l",80,42,-1.5F,-2.4F,-3.3F,6.2F,4.5F,6.6F);
                c(rl,"shoe_r",64,58,-2.7F,9.0F,-3.4F,5.4F,3.3F,6.1F);
                c(ll,"shoe_l",80,58,-2.7F,9.0F,-3.4F,5.4F,3.3F,6.1F);
            }
            case DIAMOND -> {
                crown(h,false);
                c(b,"prince_coat",64,28,-4.9F,-0.5F,-2.9F,9.8F,13.0F,5.8F);
                c(b,"cape",88,28,-5.1F,0.0F,2.5F,10.2F,15.0F,1.0F);
                c(b,"clasp",110,28,-3.5F,0.7F,-3.35F,7.0F,1.5F,0.7F);
                c(ra,"epaulette_r",64,48,-4.9F,-2.6F,-3.4F,6.4F,4.6F,6.8F);
                c(la,"epaulette_l",82,48,-1.5F,-2.6F,-3.4F,6.4F,4.6F,6.8F);
                c(rl,"boot_r",64,66,-2.7F,7.5F,-2.8F,5.4F,4.8F,5.6F);
                c(ll,"boot_l",80,66,-2.7F,7.5F,-2.8F,5.4F,4.8F,5.6F);
            }
            case NETHERITE -> {
                crown(h,true);
                c(b,"king_coat",64,32,-5.1F,-0.7F,-3.0F,10.2F,13.4F,6.0F);
                c(b,"fur_collar",90,32,-5.6F,-1.5F,-3.3F,11.2F,3.6F,6.6F);
                c(b,"royal_cape",64,76,-5.7F,-0.4F,2.6F,11.4F,17.0F,1.3F);
                c(b,"chest_gem",94,76,-1.4F,2.2F,-3.6F,2.8F,3.0F,0.8F);
                c(ra,"pauldron_r",64,96,-5.2F,-3.0F,-3.7F,6.9F,5.4F,7.4F);
                c(la,"pauldron_l",84,96,-1.7F,-3.0F,-3.7F,6.9F,5.4F,7.4F);
                c(rl,"boot_r",64,114,-2.8F,7.0F,-3.0F,5.6F,5.3F,6.0F);
                c(ll,"boot_l",82,114,-2.8F,7.0F,-3.0F,5.6F,5.3F,6.0F);
            }
        }
    }

    private static void crown(ModelPartData h, boolean king) {
        float e = king ? 0.35F : 0.0F;
        c(h,"crown_band",64,0,-4.8F-e,-9.3F,-4.8F-e,9.6F+e*2,2.0F,9.6F+e*2);
        c(h,"crown_left",64,14,-4.2F,-12.2F-(king?.7F:0),-5.0F,2.1F,4.5F+(king?.7F:0),1.4F);
        c(h,"crown_mid",72,14,-1.0F,-13.0F-(king?.9F:0),-5.0F,2.0F,5.3F+(king?.9F:0),1.4F);
        c(h,"crown_right",80,14,2.1F,-12.2F-(king?.7F:0),-5.0F,2.1F,4.5F+(king?.7F:0),1.4F);
        if (king) c(h,"crown_gem",88,14,-1.0F,-10.5F,-5.55F,2.0F,2.0F,.7F);
    }

    private static void c(ModelPartData p, String n, int u, int v,
                          float x, float y, float z, float sx, float sy, float sz) {
        p.addChild(n, ModelPartBuilder.create().uv(u,v).cuboid(x,y,z,sx,sy,sz), ModelTransform.NONE);
    }
}
