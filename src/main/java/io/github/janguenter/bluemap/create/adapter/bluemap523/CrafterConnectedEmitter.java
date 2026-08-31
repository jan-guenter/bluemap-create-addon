/*
 * SPDX-License-Identifier: MIT
 *
 * JSON-model emission follows BlueMap's MIT-licensed resource-model coordinate
 * and UV conventions, with Create's exact mechanical-crafter CT grouping.
 */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Preserves the authored crafter body while applying exact group-aware CT. */
final class CrafterConnectedEmitter {

    private static final String BLOCK_ID = "create:mechanical_crafter";
    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CrafterConnectedEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        String facing = block.getBlockState().getProperties().get("facing");
        CrafterConnectedTexture.GroupKey ownGroup = group(BLOCK_ID, block);
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (ownGroup == null || !preflight(model)) {
            return false;
        }

        int modelStart = target.getTileModel().size();
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            int elementStart = target.getTileModel().size();
            emitElement(ownGroup, facing, model, element, variant, block, target);
            if (target.getTileModel().size() > elementStart) {
                target.initialize(elementStart).transform(new MatrixM4f()
                        .copy(element.getRotation().getMatrix())
                        .scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE));
            }
        }
        target.initialize(modelStart);
        if (target.getTileModel().size() == modelStart) {
            return false;
        }
        if (variant.isTransformed()) {
            target.transform(variant.getTransformMatrix());
        }

        Texture base = resourcePack.getTextures().get(
                Key.parse("create:block/brass_casing")
        );
        if (base != null) {
            mapColor.add(new Color().set(base.getColorPremultiplied()));
            mapColor.flatten().straight();
        }
        return true;
    }

    private boolean preflight(Model model) {
        if (model == null || model.getElements() == null
                || model.getElements().length == 0) {
            return false;
        }
        for (CrafterConnectedTexture.Material material
                : CrafterConnectedTexture.Material.values()) {
            if (resourcePack.getTextures().get(
                    Key.parse(material.connectedTexture())
            ) == null) {
                return false;
            }
        }
        boolean hasFace = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            for (Face face : element.getFaces().values()) {
                hasFace = true;
                Key source = face.getTexture().getTexturePath(model.getTextures()::get);
                if (source == null || resourcePack.getTextures().get(source) == null) {
                    return false;
                }
            }
        }
        return hasFace;
    }

    private void emitElement(
            CrafterConnectedTexture.GroupKey ownGroup,
            String facing,
            Model model,
            Element element,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target
    ) {
        Vector3f from = element.getFrom();
        Vector3f to = element.getTo();
        float x0 = from.getX();
        float y0 = from.getY();
        float z0 = from.getZ();
        float x1 = to.getX();
        float y1 = to.getY();
        float z1 = to.getZ();
        emitFace(ownGroup, facing, model, element, Direction.DOWN, variant, block, target,
                x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        emitFace(ownGroup, facing, model, element, Direction.UP, variant, block, target,
                x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        emitFace(ownGroup, facing, model, element, Direction.NORTH, variant, block, target,
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        emitFace(ownGroup, facing, model, element, Direction.SOUTH, variant, block, target,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        emitFace(ownGroup, facing, model, element, Direction.WEST, variant, block, target,
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        emitFace(ownGroup, facing, model, element, Direction.EAST, variant, block, target,
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void emitFace(
            CrafterConnectedTexture.GroupKey ownGroup,
            String facing,
            Model model,
            Element element,
            Direction localDirection,
            Variant variant,
            BlockNeighborhood block,
            TileModelView target,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        Face face = element.getFaces().get(localDirection);
        if (face == null) {
            return;
        }
        Direction worldDirection = worldDirection(localDirection, variant);
        if (worldDirection == null || culled(face, variant, block)) {
            return;
        }

        Key source = face.getTexture().getTexturePath(model.getTextures()::get);
        CrafterConnectedTexture.Material shifted = CrafterConnectedTexture.material(
                facing, fromBlueMap(worldDirection), source.getFormatted()
        ).orElse(null);
        Key texture = shifted == null
                ? source : Key.parse(shifted.connectedTexture());

        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);

        Vector4f raw = face.getUv();
        float[][] corners = {
                {raw.getX() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getY() / 16F},
                {raw.getX() / 16F, raw.getY() / 16F}
        };
        if (shifted != null) {
            int cell = CrafterConnectedTexture.index(
                    shifted, context(ownGroup, facing, worldDirection, block)
            );
            for (float[] corner : corners) {
                CrafterConnectedTexture.Uv uv = CrafterConnectedTexture.connectedUv(
                        shifted, cell, corner[0], corner[1]
                );
                corner[0] = uv.u();
                corner[1] = uv.v();
            }
        }
        int rotation = Math.floorMod(face.getRotation() / 90, 4);
        float[] uv0 = corners[rotation];
        float[] uv1 = corners[(rotation + 1) % 4];
        float[] uv2 = corners[(rotation + 2) % 4];
        float[] uv3 = corners[(rotation + 3) % 4];
        mesh.setUvs(start, uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        mesh.setUvs(start + 1, uv0[0], uv0[1], uv2[0], uv2[1], uv3[0], uv3[1]);

        int material = textures.get(texture);
        var vector = worldDirection.toVector();
        LightData ownLight = block.getLightData();
        LightData outside = block.getNeighborBlock(
                vector.getX(), vector.getY(), vector.getZ()
        ).getLightData();
        int sunlight = Math.max(ownLight.getSkyLight(), outside.getSkyLight());
        int blocklight = Math.max(
                Math.max(ownLight.getBlockLight(), outside.getBlockLight()),
                element.getLightEmission()
        );
        for (int triangle = start; triangle < start + 2; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, sunlight);
            mesh.setBlocklight(triangle, blocklight);
        }
    }

    private boolean culled(
            Face face,
            Variant variant,
            BlockNeighborhood block
    ) {
        Direction localCull = face.getCullface();
        if (localCull == null) {
            return false;
        }
        Direction worldCull = worldDirection(localCull, variant);
        if (worldCull == null) {
            return false;
        }
        var normal = worldCull.toVector();
        var neighbor = block.getNeighborBlock(
                normal.getX(), normal.getY(), normal.getZ()
        );
        if (neighbor.getProperties().isCulling()) {
            return true;
        }
        return neighbor.getProperties().getCullingIdentical()
                && neighbor.getBlockState().equals(block.getBlockState());
    }

    private CrafterConnectedTexture.Context context(
            CrafterConnectedTexture.GroupKey ownGroup,
            String facing,
            Direction worldFace,
            BlockNeighborhood block
    ) {
        CrafterConnectedTexture.Frame frame = CrafterConnectedTexture.frame(
                facing, fromBlueMap(worldFace)
        ).orElseThrow();
        CreateDirection up = frame.up();
        CreateDirection down = up.opposite();
        CreateDirection right = frame.right();
        CreateDirection left = right.opposite();
        boolean hasUp = sameAt(ownGroup, up, block);
        boolean hasDown = sameAt(ownGroup, down, block);
        boolean hasLeft = sameAt(ownGroup, left, block);
        boolean hasRight = sameAt(ownGroup, right, block);
        return new CrafterConnectedTexture.Context(
                hasUp,
                hasDown,
                hasLeft,
                hasRight,
                hasUp && hasLeft && sameAt(ownGroup, up, left, block),
                hasUp && hasRight && sameAt(ownGroup, up, right, block),
                hasDown && hasLeft && sameAt(ownGroup, down, left, block),
                hasDown && hasRight && sameAt(ownGroup, down, right, block)
        );
    }

    private boolean sameAt(
            CrafterConnectedTexture.GroupKey ownGroup,
            CreateDirection direction,
            BlockNeighborhood block
    ) {
        return sameAt(ownGroup, direction, null, block);
    }

    private boolean sameAt(
            CrafterConnectedTexture.GroupKey ownGroup,
            CreateDirection first,
            CreateDirection second,
            BlockNeighborhood block
    ) {
        int x = first.x();
        int y = first.y();
        int z = first.z();
        if (second != null) {
            x += second.x();
            y += second.y();
            z += second.z();
        }
        var neighbor = block.getNeighborBlock(x, y, z);
        return CrafterConnectedTexture.sameGroup(
                ownGroup, group(BLOCK_ID, neighbor.getBlockState().getId().getFormatted(), neighbor)
        );
    }

    private static CrafterConnectedTexture.GroupKey group(
            String expectedId,
            BlockNeighborhood block
    ) {
        return group(expectedId, block.getBlockState().getId().getFormatted(), block);
    }

    private static CrafterConnectedTexture.GroupKey group(
            String expectedId,
            String actualId,
            de.bluecolored.bluemap.core.world.block.ExtendedBlock block
    ) {
        if (!expectedId.equals(actualId)
                || !(block.getBlockEntity() instanceof CrafterBlockEntityData data)) {
            return null;
        }
        String facing = block.getBlockState().getProperties().get("facing");
        if (CreateDirection.parse(facing).filter(CreateDirection::horizontal).isEmpty()) {
            return null;
        }
        CrafterConnectedTexture.Position controller = data.effectiveController(
                block.getX(), block.getY(), block.getZ()
        );
        return controller == null ? null
                : new CrafterConnectedTexture.GroupKey(facing, controller);
    }

    private static Direction worldDirection(Direction local, Variant variant) {
        var vector = local.toVector();
        VectorM3f transformed = new VectorM3f(
                vector.getX(), vector.getY(), vector.getZ()
        );
        if (variant.isTransformed()) {
            transformed.rotateAndScale(variant.getTransformMatrix());
        }
        int x = Math.round(transformed.x);
        int y = Math.round(transformed.y);
        int z = Math.round(transformed.z);
        for (Direction direction : Direction.values()) {
            var candidate = direction.toVector();
            if (candidate.getX() == x && candidate.getY() == y
                    && candidate.getZ() == z) {
                return direction;
            }
        }
        return null;
    }

    private static CreateDirection fromBlueMap(Direction direction) {
        return CreateDirection.valueOf(direction.name());
    }
}
