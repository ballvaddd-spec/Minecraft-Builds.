package soq.royalrags.client;

import java.util.EnumMap;
import java.util.Map;

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
    private static final int TEX = 256;

    // Semantic 32x32 texture cells. Each style paints these differently.
    private static final int BASE = 0;
    private static final int DARK = 1;
    private static final int LIGHT = 2;
    private static final int ACCENT = 3;
    private static final int METAL = 4;
    private static final int GOLD = 5;
    private static final int WHITE = 6;
    private static final int FUR = 7;
    private static final int GEM = 8;
    private static final int SECONDARY = 9;
    private static final int DIRTY = 10;
    private static final int CHAIN = 11;
    private static final int RED = 12;
    private static final int BLUE = 13;
    private static final int SHOE = 14;
    private static final int CAPE = 15;

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
        LEATHER("leather"),
        CHAINMAIL("chainmail"),
        IRON("iron"),
        GOLD("gold"),
        DIAMOND("diamond"),
        NETHERITE("netherite");

        final Identifier texture;

        Style(String name) {
            this.texture = Identifier.of(MOD_ID, "textures/armor/" + name + ".png");
        }
    }

    private static final class Renderer implements ArmorRenderer {
        private final Style style;
        private final Map<EquipmentSlot, ArmorEntityModel<LivingEntity>> models = new EnumMap<>(EquipmentSlot.class);

        Renderer(Style style) {
            this.style = style;
            models.put(EquipmentSlot.HEAD, build(style, EquipmentSlot.HEAD));
            models.put(EquipmentSlot.CHEST, build(style, EquipmentSlot.CHEST));
            models.put(EquipmentSlot.LEGS, build(style, EquipmentSlot.LEGS));
            models.put(EquipmentSlot.FEET, build(style, EquipmentSlot.FEET));
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack,
                           LivingEntity entity, EquipmentSlot slot, int light,
                           BipedEntityModel<LivingEntity> contextModel) {
            ArmorEntityModel<LivingEntity> model = models.get(slot);
            if (model == null) return;

            contextModel.copyBipedStateTo(model);
            setVisible(model, slot);
            ArmorRenderer.renderPart(matrices, consumers, light, stack, model, style.texture);
        }
    }

    private static ArmorEntityModel<LivingEntity> build(Style style, EquipmentSlot slot) {
        // Vanilla biped cubes are left transparent in our atlas. They provide the correct
        // animation pivots while every visible garment is a custom child cube.
        ModelData data = ArmorEntityModel.getModelData(new Dilation(0.02F));
        decorate(data.getRoot(), style, slot);
        return new ArmorEntityModel<>(TexturedModelData.of(data, TEX, TEX).createModel());
    }

    private static void setVisible(ArmorEntityModel<LivingEntity> model, EquipmentSlot slot) {
        model.setVisible(false);
        model.hat.visible = false;

        switch (slot) {
            case HEAD -> model.head.visible = true;
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> { }
        }
    }

    private static void decorate(ModelPartData root, Style style, EquipmentSlot slot) {
        ModelPartData head = root.getChild("head");
        ModelPartData body = root.getChild("body");
        ModelPartData rightArm = root.getChild("right_arm");
        ModelPartData leftArm = root.getChild("left_arm");
        ModelPartData rightLeg = root.getChild("right_leg");
        ModelPartData leftLeg = root.getChild("left_leg");

        switch (style) {
            case LEATHER -> leather(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
            case CHAINMAIL -> chainmail(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
            case IRON -> iron(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
            case GOLD -> gold(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
            case DIAMOND -> diamond(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
            case NETHERITE -> netherite(slot, head, body, rightArm, leftArm, rightLeg, leftLeg);
        }
    }

    // -------------------------------------------------------------------------
    // LEATHER — ragged tramp
    // -------------------------------------------------------------------------
    private static void leather(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                                ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            cube(h, "hood_top", BASE, -4.45F, -8.65F, -4.35F, 8.9F, 1.3F, 8.7F);
            cube(h, "hood_left", DARK, -4.65F, -7.6F, -4.25F, 1.25F, 7.6F, 8.5F);
            cube(h, "hood_right", DARK, 3.4F, -7.6F, -4.25F, 1.25F, 7.6F, 8.5F);
            cube(h, "hood_back", BASE, -3.55F, -7.5F, 3.35F, 7.1F, 7.8F, 1.3F);
            cube(h, "hood_brow", DIRTY, -3.55F, -7.65F, -4.75F, 7.1F, 1.25F, 1.0F);
            cube(h, "hood_patch", ACCENT, 1.2F, -8.95F, -2.8F, 2.1F, 0.55F, 2.4F);
            cube(h, "torn_tab_l", SECONDARY, -4.7F, -1.4F, 2.3F, 1.2F, 2.4F, 1.0F);
            cube(h, "torn_tab_r", SECONDARY, 3.5F, -0.7F, 2.7F, 1.0F, 1.8F, 0.9F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "shirt", LIGHT, -4.05F, 0.2F, -2.22F, 8.1F, 11.4F, 4.44F);
            cube(b, "jacket_left", BASE, -4.4F, -0.15F, -2.5F, 3.25F, 11.9F, 5.0F);
            cube(b, "jacket_right", DARK, 1.15F, -0.15F, -2.5F, 3.25F, 11.9F, 5.0F);
            cube(b, "lapel_left", SECONDARY, -2.95F, 0.4F, -2.9F, 1.25F, 6.0F, 0.75F);
            cube(b, "lapel_right", DIRTY, 1.7F, 0.4F, -2.9F, 1.25F, 5.3F, 0.75F);
            cube(b, "rope_belt", DIRTY, -4.45F, 8.4F, -2.65F, 8.9F, 1.1F, 5.3F);
            cube(b, "rope_knot", SECONDARY, 2.45F, 8.15F, -3.2F, 1.25F, 1.8F, 1.0F);
            cube(b, "patch_chest", ACCENT, -3.65F, 3.1F, -2.95F, 2.1F, 2.7F, 0.6F);
            rotated(b, "tail_left", BASE, -4.2F, 9.3F, 1.9F, 3.8F, 5.8F, 0.85F,
                    0F, 0F, 0F, -8F, 0F, 3F);
            rotated(b, "tail_right", DARK, 0.45F, 9.4F, 1.9F, 3.55F, 4.6F, 0.85F,
                    0F, 0F, 0F, -5F, 0F, -4F);
            sleeve(ra, "r", BASE, ACCENT, true);
            sleeve(la, "l", DARK, DIRTY, false);
            cube(ra, "rag_cuff_r", SECONDARY, -3.05F, 8.7F, -2.3F, 4.15F, 2.1F, 4.6F);
            cube(la, "rag_cuff_l", SECONDARY, -1.1F, 9.2F, -2.3F, 4.15F, 1.6F, 4.6F);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "rag_waist", DARK, -4.15F, 9.4F, -2.28F, 8.3F, 2.2F, 4.56F);
            pants(rl, "r", DARK, ACCENT, true);
            pants(ll, "l", SECONDARY, DIRTY, false);
            cube(rl, "knee_patch_r", ACCENT, -2.25F, 4.0F, -2.65F, 4.4F, 3.1F, 0.75F);
            cube(ll, "knee_patch_l", DIRTY, -2.25F, 5.0F, -2.65F, 4.4F, 2.5F, 0.75F);
        } else if (slot == EquipmentSlot.FEET) {
            cube(rl, "boot_r", SHOE, -2.45F, 7.2F, -2.5F, 4.9F, 4.7F, 5.0F);
            cube(rl, "sole_r", DARK, -2.55F, 10.8F, -3.0F, 5.1F, 1.15F, 5.7F);
            cube(ll, "boot_l", DIRTY, -2.5F, 6.6F, -2.45F, 5.0F, 5.25F, 4.9F);
            cube(ll, "sole_l", DARK, -2.6F, 10.8F, -2.9F, 5.2F, 1.15F, 5.6F);
        }
    }

    // -------------------------------------------------------------------------
    // CHAINMAIL — street fighter
    // -------------------------------------------------------------------------
    private static void chainmail(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                                  ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            cube(h, "hood_top", CHAIN, -4.25F, -8.35F, -4.2F, 8.5F, 1.15F, 8.4F);
            cube(h, "hood_side_l", CHAIN, -4.5F, -7.5F, -4.0F, 1.0F, 7.5F, 8.0F);
            cube(h, "hood_side_r", CHAIN, 3.5F, -7.5F, -4.0F, 1.0F, 7.5F, 8.0F);
            cube(h, "cap_crown", DARK, -4.45F, -9.1F, -4.4F, 8.9F, 2.3F, 8.8F);
            cube(h, "cap_red_panel", RED, -1.1F, -9.4F, -4.65F, 4.5F, 0.7F, 4.8F);
            rotated(h, "cap_brim", RED, -3.7F, -7.2F, -7.1F, 7.4F, 0.75F, 3.1F,
                    0F, 0F, 0F, -7F, 0F, 0F);
            cube(h, "ear_chain_l", METAL, -4.75F, -4.5F, -2.9F, 0.75F, 3.8F, 1.25F);
            cube(h, "ear_chain_r", METAL, 4.0F, -4.5F, -2.9F, 0.75F, 3.8F, 1.25F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "shirt", DARK, -4.05F, 0.15F, -2.2F, 8.1F, 11.6F, 4.4F);
            cube(b, "jacket", BASE, -4.35F, -0.2F, -2.5F, 8.7F, 12.1F, 5.0F);
            cube(b, "zip", METAL, -0.35F, 0.4F, -2.9F, 0.7F, 9.3F, 0.65F);
            cube(b, "stripe_l", RED, -4.55F, 1.2F, -2.85F, 1.0F, 8.1F, 0.6F);
            cube(b, "stripe_r", RED, 3.55F, 1.2F, -2.85F, 1.0F, 8.1F, 0.6F);
            cube(b, "chain_neck", GOLD, -3.0F, 0.7F, -3.25F, 6.0F, 1.0F, 0.8F);
            cube(b, "chain_drop", GOLD, -1.1F, 1.5F, -3.25F, 2.2F, 3.1F, 0.8F);
            cube(b, "logo", RED, -1.5F, 5.4F, -3.15F, 3.0F, 2.6F, 0.7F);
            shoulder(ra, "r", BASE, RED, true, false);
            shoulder(la, "l", BASE, RED, false, false);
            cube(ra, "chain_cuff_r", CHAIN, -3.0F, 8.7F, -2.3F, 4.05F, 2.0F, 4.6F);
            cube(la, "chain_cuff_l", CHAIN, -1.05F, 8.7F, -2.3F, 4.05F, 2.0F, 4.6F);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "street_waist", DARK, -4.1F, 9.5F, -2.25F, 8.2F, 2.0F, 4.5F);
            pants(rl, "r", BASE, RED, true);
            pants(ll, "l", BASE, RED, false);
            cube(rl, "side_stripe_r", RED, -2.5F, 0.0F, -2.1F, 0.65F, 11.2F, 4.2F);
            cube(ll, "side_stripe_l", RED, 1.85F, 0.0F, -2.1F, 0.65F, 11.2F, 4.2F);
            cube(rl, "cargo_r", SECONDARY, -2.75F, 4.0F, -1.8F, 1.0F, 3.0F, 3.6F);
        } else if (slot == EquipmentSlot.FEET) {
            sneaker(rl, "r", DARK, RED);
            sneaker(ll, "l", DARK, RED);
        }
    }

    // -------------------------------------------------------------------------
    // IRON — bodyguard
    // -------------------------------------------------------------------------
    private static void iron(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                             ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            cube(h, "hair_top", SECONDARY, -4.15F, -8.55F, -4.05F, 8.3F, 1.1F, 8.1F);
            cube(h, "hair_back", SECONDARY, -4.2F, -7.7F, 3.35F, 8.4F, 3.0F, 0.95F);
            cube(h, "glasses_bar", DARK, -4.65F, -5.15F, -5.0F, 9.3F, 0.75F, 0.8F);
            cube(h, "glass_l", DARK, -3.65F, -5.8F, -5.15F, 3.0F, 2.1F, 0.75F);
            cube(h, "glass_r", DARK, 0.65F, -5.8F, -5.15F, 3.0F, 2.1F, 0.75F);
            cube(h, "temple_l", DARK, -4.75F, -5.0F, -3.8F, 0.65F, 0.55F, 4.2F);
            cube(h, "temple_r", DARK, 4.1F, -5.0F, -3.8F, 0.65F, 0.55F, 4.2F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "shirt", WHITE, -4.05F, 0.15F, -2.22F, 8.1F, 11.6F, 4.44F);
            cube(b, "vest", BASE, -3.7F, 0.1F, -2.62F, 7.4F, 10.6F, 5.05F);
            cube(b, "shirt_collar_l", WHITE, -2.9F, -0.5F, -2.95F, 2.4F, 2.6F, 0.75F);
            cube(b, "shirt_collar_r", WHITE, 0.5F, -0.5F, -2.95F, 2.4F, 2.6F, 0.75F);
            cube(b, "tie_knot", RED, -0.75F, 0.3F, -3.25F, 1.5F, 1.4F, 0.8F);
            cube(b, "tie", RED, -0.65F, 1.5F, -3.2F, 1.3F, 6.4F, 0.72F);
            cube(b, "belt", DARK, -4.15F, 8.7F, -2.58F, 8.3F, 1.0F, 5.16F);
            cube(b, "buckle", METAL, -0.9F, 8.5F, -3.0F, 1.8F, 1.4F, 0.65F);
            shoulder(ra, "r", BASE, WHITE, true, false);
            shoulder(la, "l", BASE, WHITE, false, false);
            cube(ra, "shirt_cuff_r", WHITE, -3.0F, 9.2F, -2.25F, 4.0F, 1.5F, 4.5F);
            cube(la, "shirt_cuff_l", WHITE, -1.0F, 9.2F, -2.25F, 4.0F, 1.5F, 4.5F);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "formal_waist", DARK, -4.05F, 9.7F, -2.25F, 8.1F, 1.8F, 4.5F);
            pants(rl, "r", DARK, SECONDARY, true);
            pants(ll, "l", DARK, SECONDARY, false);
            cube(rl, "crease_r", SECONDARY, -0.25F, 0.3F, -2.45F, 0.5F, 10.8F, 0.55F);
            cube(ll, "crease_l", SECONDARY, -0.25F, 0.3F, -2.45F, 0.5F, 10.8F, 0.55F);
        } else if (slot == EquipmentSlot.FEET) {
            dressShoe(rl, "r", SHOE, METAL);
            dressShoe(ll, "l", SHOE, METAL);
        }
    }

    // -------------------------------------------------------------------------
    // GOLD — rich
    // -------------------------------------------------------------------------
    private static void gold(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                             ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            cube(h, "cap_top", GOLD, -4.4F, -9.0F, -4.35F, 8.8F, 2.1F, 8.7F);
            cube(h, "cap_band", WHITE, -4.55F, -7.25F, -4.5F, 9.1F, 1.0F, 9.0F);
            rotated(h, "cap_brim", GOLD, -3.5F, -6.7F, -6.5F, 7.0F, 0.7F, 2.7F,
                    0F, 0F, 0F, -5F, 0F, 0F);
            cube(h, "glasses_l", DARK, -3.65F, -5.5F, -5.1F, 3.0F, 2.0F, 0.75F);
            cube(h, "glasses_r", DARK, 0.65F, -5.5F, -5.1F, 3.0F, 2.0F, 0.75F);
            cube(h, "glasses_bridge", GOLD, -0.65F, -4.9F, -5.15F, 1.3F, 0.5F, 0.7F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "white_jacket", WHITE, -4.35F, -0.2F, -2.52F, 8.7F, 12.0F, 5.04F);
            cube(b, "black_shirt", DARK, -2.75F, 0.25F, -2.9F, 5.5F, 9.8F, 0.75F);
            cube(b, "gold_lapel_l", GOLD, -3.9F, 0.0F, -3.02F, 1.6F, 7.4F, 0.65F);
            cube(b, "gold_lapel_r", GOLD, 2.3F, 0.0F, -3.02F, 1.6F, 7.4F, 0.65F);
            cube(b, "chain_top", GOLD, -3.0F, 1.2F, -3.35F, 6.0F, 0.7F, 0.8F);
            cube(b, "chain_mid", GOLD, -2.3F, 2.2F, -3.37F, 4.6F, 0.7F, 0.82F);
            cube(b, "chain_low", GOLD, -1.6F, 3.2F, -3.39F, 3.2F, 0.7F, 0.84F);
            cube(b, "gold_buttons", GOLD, -0.35F, 5.0F, -3.2F, 0.7F, 4.1F, 0.7F);
            shoulder(ra, "r", WHITE, GOLD, true, true);
            shoulder(la, "l", WHITE, GOLD, false, true);
            cube(ra, "watch", GOLD, -3.1F, 8.7F, -2.45F, 4.2F, 1.15F, 4.9F);
            cube(la, "gold_cuff_l", GOLD, -1.1F, 9.0F, -2.35F, 4.1F, 1.0F, 4.7F);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "luxury_waist", WHITE, -4.1F, 9.6F, -2.3F, 8.2F, 1.9F, 4.6F);
            pants(rl, "r", WHITE, GOLD, true);
            pants(ll, "l", WHITE, GOLD, false);
            cube(rl, "gold_stripe_r", GOLD, -2.5F, 0.0F, -2.1F, 0.65F, 11.2F, 4.2F);
            cube(ll, "gold_stripe_l", GOLD, 1.85F, 0.0F, -2.1F, 0.65F, 11.2F, 4.2F);
        } else if (slot == EquipmentSlot.FEET) {
            dressShoe(rl, "r", WHITE, GOLD);
            dressShoe(ll, "l", WHITE, GOLD);
            cube(rl, "toe_gold_r", GOLD, -2.45F, 9.1F, -3.65F, 4.9F, 2.45F, 1.3F);
            cube(ll, "toe_gold_l", GOLD, -2.45F, 9.1F, -3.65F, 4.9F, 2.45F, 1.3F);
        }
    }

    // -------------------------------------------------------------------------
    // DIAMOND — prince
    // -------------------------------------------------------------------------
    private static void diamond(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                                ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            crown(h, false);
            cube(h, "blue_headband", BLUE, -4.35F, -7.45F, -4.35F, 8.7F, 1.15F, 8.7F);
            cube(h, "side_jewel_l", GEM, -4.75F, -7.2F, -1.0F, 0.9F, 1.9F, 2.0F);
            cube(h, "side_jewel_r", GEM, 3.85F, -7.2F, -1.0F, 0.9F, 1.9F, 2.0F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "prince_coat", BLUE, -4.35F, -0.2F, -2.5F, 8.7F, 12.0F, 5.0F);
            cube(b, "white_shirt", WHITE, -2.1F, 0.25F, -2.95F, 4.2F, 9.4F, 0.75F);
            cube(b, "gold_center", GOLD, -0.55F, 0.2F, -3.2F, 1.1F, 10.0F, 0.7F);
            cube(b, "gold_lapel_l", GOLD, -3.65F, 0.0F, -3.0F, 1.15F, 8.1F, 0.65F);
            cube(b, "gold_lapel_r", GOLD, 2.5F, 0.0F, -3.0F, 1.15F, 8.1F, 0.65F);
            cube(b, "gem_clasp_l", GEM, -3.25F, 0.55F, -3.4F, 1.25F, 1.25F, 0.85F);
            cube(b, "gem_clasp_r", GEM, 2.0F, 0.55F, -3.4F, 1.25F, 1.25F, 0.85F);
            cube(b, "chest_chain", GOLD, -2.1F, 3.0F, -3.35F, 4.2F, 0.7F, 0.8F);
            epaulette(ra, "r", BLUE, GOLD, true);
            epaulette(la, "l", BLUE, GOLD, false);
            cube(ra, "white_cuff_r", WHITE, -3.05F, 9.0F, -2.35F, 4.1F, 1.5F, 4.7F);
            cube(la, "white_cuff_l", WHITE, -1.05F, 9.0F, -2.35F, 4.1F, 1.5F, 4.7F);
            cape(b, "prince", BLUE, WHITE, GOLD, false);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "prince_waist", BLUE, -4.1F, 9.5F, -2.3F, 8.2F, 2.0F, 4.6F);
            cube(b, "waist_gold", GOLD, -4.25F, 9.35F, -2.65F, 8.5F, 0.8F, 5.3F);
            pants(rl, "r", BLUE, GOLD, true);
            pants(ll, "l", BLUE, GOLD, false);
            cube(rl, "white_panel_r", WHITE, -1.6F, 1.0F, -2.5F, 3.2F, 7.0F, 0.6F);
            cube(ll, "white_panel_l", WHITE, -1.6F, 1.0F, -2.5F, 3.2F, 7.0F, 0.6F);
        } else if (slot == EquipmentSlot.FEET) {
            royalBoot(rl, "r", BLUE, WHITE, GOLD);
            royalBoot(ll, "l", BLUE, WHITE, GOLD);
        }
    }

    // -------------------------------------------------------------------------
    // NETHERITE — king
    // -------------------------------------------------------------------------
    private static void netherite(EquipmentSlot slot, ModelPartData h, ModelPartData b,
                                  ModelPartData ra, ModelPartData la, ModelPartData rl, ModelPartData ll) {
        if (slot == EquipmentSlot.HEAD) {
            crown(h, true);
            cube(h, "dark_headband", DARK, -4.35F, -7.55F, -4.35F, 8.7F, 1.2F, 8.7F);
            cube(h, "crown_ruby_front", GEM, -1.25F, -9.85F, -5.05F, 2.5F, 2.6F, 0.85F);
            cube(h, "crown_ruby_l", RED, -4.9F, -9.0F, -1.0F, 0.9F, 2.0F, 2.0F);
            cube(h, "crown_ruby_r", RED, 4.0F, -9.0F, -1.0F, 0.9F, 2.0F, 2.0F);
        } else if (slot == EquipmentSlot.CHEST) {
            cube(b, "king_coat", DARK, -4.45F, -0.25F, -2.55F, 8.9F, 12.2F, 5.1F);
            cube(b, "red_front_l", RED, -3.25F, 0.2F, -3.0F, 2.0F, 10.1F, 0.7F);
            cube(b, "red_front_r", RED, 1.25F, 0.2F, -3.0F, 2.0F, 10.1F, 0.7F);
            cube(b, "gold_center", GOLD, -0.45F, 0.1F, -3.25F, 0.9F, 10.6F, 0.75F);
            cube(b, "ruby", GEM, -1.25F, 2.0F, -3.65F, 2.5F, 2.7F, 0.95F);
            cube(b, "gold_chain_top", GOLD, -3.05F, 0.9F, -3.42F, 6.1F, 0.7F, 0.85F);
            cube(b, "gold_chain_low", GOLD, -2.2F, 5.1F, -3.42F, 4.4F, 0.7F, 0.85F);
            furCollar(b);
            kingShoulder(ra, "r", true);
            kingShoulder(la, "l", false);
            cube(ra, "gold_cuff_r", GOLD, -3.15F, 8.5F, -2.45F, 4.25F, 2.0F, 4.9F);
            cube(la, "gold_cuff_l", GOLD, -1.1F, 8.5F, -2.45F, 4.25F, 2.0F, 4.9F);
            cape(b, "king", RED, DARK, GOLD, true);
        } else if (slot == EquipmentSlot.LEGS) {
            cube(b, "king_waist", DARK, -4.15F, 9.3F, -2.35F, 8.3F, 2.3F, 4.7F);
            cube(b, "king_belt_gold", GOLD, -4.3F, 9.2F, -2.7F, 8.6F, 0.9F, 5.4F);
            cube(b, "king_buckle_gem", GEM, -1.0F, 8.95F, -3.05F, 2.0F, 1.5F, 0.7F);
            pants(rl, "r", DARK, RED, true);
            pants(ll, "l", DARK, RED, false);
            cube(rl, "knee_gold_r", GOLD, -2.35F, 4.8F, -2.75F, 4.7F, 2.4F, 0.85F);
            cube(ll, "knee_gold_l", GOLD, -2.35F, 4.8F, -2.75F, 4.7F, 2.4F, 0.85F);
            cube(rl, "knee_gem_r", GEM, -0.8F, 5.15F, -3.05F, 1.6F, 1.5F, 0.65F);
            cube(ll, "knee_gem_l", GEM, -0.8F, 5.15F, -3.05F, 1.6F, 1.5F, 0.65F);
        } else if (slot == EquipmentSlot.FEET) {
            royalBoot(rl, "r", DARK, RED, GOLD);
            royalBoot(ll, "l", DARK, RED, GOLD);
            cube(rl, "king_toe_r", GOLD, -2.5F, 9.0F, -3.7F, 5.0F, 2.7F, 1.35F);
            cube(ll, "king_toe_l", GOLD, -2.5F, 9.0F, -3.7F, 5.0F, 2.7F, 1.35F);
        }
    }

    // -------------------------------------------------------------------------
    // Reusable garment pieces
    // -------------------------------------------------------------------------
    private static void sleeve(ModelPartData arm, String side, int main, int patch, boolean right) {
        float x = right ? -3.15F : -1.05F;
        cube(arm, "sleeve_" + side, main, x, -1.8F, -2.25F, 4.2F, 11.6F, 4.5F);
        cube(arm, "patch_" + side, patch, x - 0.1F, 3.2F, -2.55F, 2.2F, 3.2F, 0.65F);
    }

    private static void pants(ModelPartData leg, String side, int main, int detail, boolean right) {
        cube(leg, "pants_" + side, main, -2.25F, 0.0F, -2.2F, 4.5F, 11.2F, 4.4F);
        if (right) {
            cube(leg, "seam_" + side, detail, -2.5F, 0.2F, -1.9F, 0.55F, 10.6F, 3.8F);
        } else {
            cube(leg, "seam_" + side, detail, 1.95F, 0.2F, -1.9F, 0.55F, 10.6F, 3.8F);
        }
    }

    private static void shoulder(ModelPartData arm, String side, int main, int trim, boolean right, boolean luxury) {
        float x = right ? -3.55F : -1.05F;
        cube(arm, "sleeve_" + side, main, right ? -3.05F : -0.95F, -1.8F, -2.25F, 4.0F, 11.5F, 4.5F);
        cube(arm, "shoulder_" + side, main, x, -2.35F, -2.7F, 4.6F, luxury ? 3.0F : 2.5F, 5.4F);
        cube(arm, "shoulder_trim_" + side, trim, x - 0.1F, -2.55F, -2.85F, 4.8F, 0.8F, 5.7F);
    }

    private static void epaulette(ModelPartData arm, String side, int main, int trim, boolean right) {
        float x = right ? -3.75F : -1.05F;
        cube(arm, "sleeve_" + side, main, right ? -3.05F : -0.95F, -1.8F, -2.25F, 4.0F, 11.5F, 4.5F);
        cube(arm, "epaulette_" + side, main, x, -2.8F, -3.0F, 4.9F, 2.8F, 6.0F);
        cube(arm, "epaulette_gold_" + side, trim, x - 0.15F, -3.05F, -3.15F, 5.2F, 0.75F, 6.3F);
        cube(arm, "epaulette_fringe_" + side, trim, x + 0.2F, -0.3F, -2.8F, 4.4F, 1.2F, 5.6F);
    }

    private static void kingShoulder(ModelPartData arm, String side, boolean right) {
        float x = right ? -3.95F : -1.15F;
        cube(arm, "king_sleeve_" + side, DARK, right ? -3.1F : -0.95F, -1.9F, -2.3F, 4.05F, 11.7F, 4.6F);
        cube(arm, "pauldron_" + side, GOLD, x, -3.2F, -3.3F, 5.1F, 3.6F, 6.6F);
        cube(arm, "pauldron_red_" + side, RED, x + 0.45F, -2.5F, -3.55F, 4.2F, 2.2F, 0.8F);
        cube(arm, "pauldron_gem_" + side, GEM, x + 1.45F, -2.95F, -3.85F, 1.8F, 1.8F, 0.7F);
        cube(arm, "pauldron_fur_" + side, FUR, x + 0.2F, 0.1F, -3.0F, 4.7F, 1.25F, 6.0F);
    }

    private static void furCollar(ModelPartData body) {
        cube(body, "fur_back", FUR, -4.9F, -1.35F, 2.1F, 9.8F, 3.2F, 1.5F);
        cube(body, "fur_left", FUR, -5.15F, -1.25F, -2.7F, 2.0F, 3.5F, 5.4F);
        cube(body, "fur_right", FUR, 3.15F, -1.25F, -2.7F, 2.0F, 3.5F, 5.4F);
        cube(body, "fur_front_l", FUR, -3.4F, -1.0F, -3.15F, 2.3F, 2.9F, 0.85F);
        cube(body, "fur_front_r", FUR, 1.1F, -1.0F, -3.15F, 2.3F, 2.9F, 0.85F);
    }

    private static void cape(ModelPartData body, String name, int outer, int inner, int trim, boolean king) {
        float width = king ? 11.2F : 10.0F;
        float height = king ? 17.2F : 15.3F;
        float x = -width / 2.0F;
        rotated(body, name + "_cape_outer", outer, x, 0.0F, 0.0F, width, height, 0.8F,
                0F, -0.4F, 2.55F, 8F, 0F, 0F);
        rotated(body, name + "_cape_inner", inner, x + 0.4F, 0.4F, -0.15F, width - 0.8F, height - 0.8F, 0.35F,
                0F, -0.4F, 2.35F, 8F, 0F, 0F);
        rotated(body, name + "_cape_left_trim", trim, x - 0.25F, 0.0F, -0.1F, 0.8F, height, 1.0F,
                0F, -0.4F, 2.7F, 8F, 0F, 0F);
        rotated(body, name + "_cape_right_trim", trim, -x - 0.55F, 0.0F, -0.1F, 0.8F, height, 1.0F,
                0F, -0.4F, 2.7F, 8F, 0F, 0F);
        if (king) {
            rotated(body, name + "_cape_fur", FUR, x - 0.3F, -0.8F, -0.25F, width + 0.6F, 2.2F, 1.2F,
                    0F, -0.4F, 2.75F, 8F, 0F, 0F);
        }
    }

    private static void sneaker(ModelPartData leg, String side, int main, int accent) {
        cube(leg, "shoe_" + side, main, -2.55F, 7.3F, -3.2F, 5.1F, 4.5F, 5.8F);
        cube(leg, "shoe_stripe_" + side, accent, -2.65F, 8.2F, -3.45F, 5.3F, 1.0F, 0.65F);
        cube(leg, "shoe_sole_" + side, WHITE, -2.65F, 10.7F, -3.45F, 5.3F, 1.15F, 6.1F);
    }

    private static void dressShoe(ModelPartData leg, String side, int main, int trim) {
        cube(leg, "dress_shoe_" + side, main, -2.45F, 8.2F, -3.25F, 4.9F, 3.55F, 5.7F);
        cube(leg, "dress_toe_" + side, trim, -2.25F, 9.3F, -3.65F, 4.5F, 1.8F, 0.85F);
        cube(leg, "dress_sole_" + side, DARK, -2.55F, 10.9F, -3.35F, 5.1F, 0.9F, 5.9F);
    }

    private static void royalBoot(ModelPartData leg, String side, int main, int secondary, int trim) {
        cube(leg, "boot_" + side, main, -2.55F, 5.5F, -2.7F, 5.1F, 6.4F, 5.4F);
        cube(leg, "boot_panel_" + side, secondary, -1.55F, 6.1F, -3.0F, 3.1F, 4.8F, 0.65F);
        cube(leg, "boot_trim_top_" + side, trim, -2.7F, 5.25F, -2.9F, 5.4F, 1.0F, 5.8F);
        cube(leg, "boot_trim_bottom_" + side, trim, -2.65F, 10.25F, -3.2F, 5.3F, 1.0F, 6.1F);
    }

    private static void crown(ModelPartData head, boolean king) {
        int band = king ? GOLD : GOLD;
        float extra = king ? 0.25F : 0.0F;
        float top = king ? -13.0F : -12.1F;

        cube(head, "crown_band", band, -4.65F-extra, -9.2F, -4.65F-extra, 9.3F+extra*2, 1.8F, 9.3F+extra*2);
        cube(head, "crown_front", band, -4.2F, -10.0F, -5.0F, 8.4F, 1.0F, 1.0F);

        crownSpike(head, "spike_l", -3.8F, top + 1.1F, king ? 2.0F : 1.8F, king);
        crownSpike(head, "spike_ml", -2.15F, top + 0.4F, king ? 2.0F : 1.8F, king);
        crownSpike(head, "spike_mid", -0.9F, top, king ? 2.1F : 1.8F, king);
        crownSpike(head, "spike_mr", 1.0F, top + 0.4F, king ? 2.0F : 1.8F, king);
        crownSpike(head, "spike_r", 2.6F, top + 1.1F, king ? 2.0F : 1.8F, king);

        cube(head, "front_gem", king ? GEM : BLUE, -0.8F, -9.85F, -5.4F, 1.6F, 1.7F, 0.8F);
        cube(head, "gem_l", GEM, -3.15F, -9.55F, -5.35F, 1.1F, 1.1F, 0.75F);
        cube(head, "gem_r", GEM, 2.05F, -9.55F, -5.35F, 1.1F, 1.1F, 0.75F);
    }

    private static void crownSpike(ModelPartData head, String name, float x, float y, float height, boolean king) {
        cube(head, name, king ? GOLD : GOLD, x, y, -4.95F, 1.35F, height, 1.2F);
        cube(head, name + "_tip", king ? RED : GEM, x + 0.25F, y - 0.75F, -5.1F, 0.85F, 0.9F, 0.85F);
    }

    // -------------------------------------------------------------------------
    // Low-level model helpers
    // -------------------------------------------------------------------------
    private static void cube(ModelPartData parent, String name, int cell,
                             float x, float y, float z, float sx, float sy, float sz) {
        int u = (cell % 8) * 32;
        int v = (cell / 8) * 32;
        parent.addChild(name,
                ModelPartBuilder.create().uv(u, v).cuboid(x, y, z, sx, sy, sz),
                ModelTransform.NONE);
    }

    private static void rotated(ModelPartData parent, String name, int cell,
                                float x, float y, float z, float sx, float sy, float sz,
                                float pivotX, float pivotY, float pivotZ,
                                float pitchDeg, float yawDeg, float rollDeg) {
        int u = (cell % 8) * 32;
        int v = (cell / 8) * 32;
        parent.addChild(name,
                ModelPartBuilder.create().uv(u, v).cuboid(x, y, z, sx, sy, sz),
                ModelTransform.of(
                        pivotX, pivotY, pivotZ,
                        (float)Math.toRadians(pitchDeg),
                        (float)Math.toRadians(yawDeg),
                        (float)Math.toRadians(rollDeg)));
    }
}
