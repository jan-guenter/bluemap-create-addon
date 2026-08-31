/*
 * SPDX-License-Identifier: MIT
 *
 * JSON-model emission follows BlueMap's MIT resource-model conventions with
 * Create's exact item-vault connectivity and RECTANGLE texture selection.
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
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Preserves the authored vault cube while replacing all six CT surfaces. */
final class VaultConnectedEmitter {

    private static final String BLOCK_ID = "create:item_vault";
    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    VaultConnectedEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        VaultConnectedTexture.GroupKey group = group(BLOCK_ID, block);
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (group == null || !preflight(group, model)) {
            return false;
        }

        int modelStart = target.getTileModel().size();
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            int elementStart = target.getTileModel().size();
            emitElement(group, model, element, variant, block, target);
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
        Key top = Key.parse("create:block/vault/vault_top_"
                + (group.large() ? "large" : "medium"));
        var texture = resourcePack.getTextures().get(top);
        if (texture != null) {
            mapColor.add(new Color().set(texture.getColorPremultiplied()));
            mapColor.flatten().straight();
        }
        return true;
    }

    private boolean preflight(VaultConnectedTexture.GroupKey group, Model model) {
        if (model == null || model.getElements() == null
                || model.getElements().length == 0) {
            return false;
        }
        boolean found = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            for (MapEntry entry : faces(element)) {
                found = true;
                Key source = entry.face().getTexture()
                        .getTexturePath(model.getTextures()::get);
                if (source == null || resourcePack.getTextures().get(source) == null) {
                    return false;
                }
            }
        }
        if (!found) {
            return false;
        }
        String size = group.large() ? "large" : "medium";
        for (String surface : new String[]{"top", "bottom", "front", "side"}) {
            if (resourcePack.getTextures().get(Key.parse(
                    "create:block/vault/vault_" + surface + '_' + size
            )) == null) {
                return false;
            }
        }
        return true;
    }

    private static java.util.List<MapEntry> faces(Element element) {
        return element.getFaces().entrySet().stream()
                .map(entry -> new MapEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void emitElement(
            VaultConnectedTexture.GroupKey group,
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
        emitFace(group, model, element, Direction.DOWN, variant, block, target,
                x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        emitFace(group, model, element, Direction.UP, variant, block, target,
                x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        emitFace(group, model, element, Direction.NORTH, variant, block, target,
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        emitFace(group, model, element, Direction.SOUTH, variant, block, target,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        emitFace(group, model, element, Direction.WEST, variant, block, target,
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        emitFace(group, model, element, Direction.EAST, variant, block, target,
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void emitFace(
            VaultConnectedTexture.GroupKey group,
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
        Direction worldDirection = worldDirection(localDirection, variant);
        if (face == null || worldDirection == null) {
            return;
        }
        Key source = face.getTexture().getTexturePath(model.getTextures()::get);
        VaultConnectedTexture.Material shifted = VaultConnectedTexture.material(
                group.axis(), group.large(), fromBlueMap(worldDirection),
                source.getFormatted()
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
            int cell = VaultConnectedTexture.index(
                    context(group, fromBlueMap(worldDirection), block)
            );
            for (float[] corner : corners) {
                VaultConnectedTexture.Uv uv = VaultConnectedTexture.connectedUv(
                        cell, corner[0], corner[1]
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
        LightData own = block.getLightData();
        LightData outside = block.getNeighborBlock(
                vector.getX(), vector.getY(), vector.getZ()
        ).getLightData();
        int sunlight = Math.max(own.getSkyLight(), outside.getSkyLight());
        int blocklight = Math.max(
                Math.max(own.getBlockLight(), outside.getBlockLight()),
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

    private VaultConnectedTexture.Context context(
            VaultConnectedTexture.GroupKey own,
            CreateDirection face,
            BlockNeighborhood block
    ) {
        VaultConnectedTexture.Frame frame = VaultConnectedTexture.frame(
                own.axis(), face
        ).orElseThrow();
        CreateDirection up = frame.up();
        CreateDirection right = frame.right();
        return new VaultConnectedTexture.Context(
                sameAt(own, up, block),
                sameAt(own, up.opposite(), block),
                sameAt(own, right.opposite(), block),
                sameAt(own, right, block)
        );
    }

    private boolean sameAt(
            VaultConnectedTexture.GroupKey own,
            CreateDirection direction,
            BlockNeighborhood block
    ) {
        var neighbor = block.getNeighborBlock(
                direction.x(), direction.y(), direction.z()
        );
        return VaultConnectedTexture.sameGroup(
                own, group(BLOCK_ID, neighbor.getBlockState().getId().getFormatted(), neighbor)
        );
    }

    private static VaultConnectedTexture.GroupKey group(
            String expectedId,
            BlockNeighborhood block
    ) {
        return group(expectedId, block.getBlockState().getId().getFormatted(), block);
    }

    private static VaultConnectedTexture.GroupKey group(
            String expectedId,
            String actualId,
            de.bluecolored.bluemap.core.world.block.ExtendedBlock block
    ) {
        if (!expectedId.equals(actualId)
                || !(block.getBlockEntity() instanceof VaultBlockEntityData data)) {
            return null;
        }
        VaultConnectedTexture.Position controller = data.effectiveController(
                block.getX(), block.getY(), block.getZ()
        );
        return VaultConnectedTexture.group(
                actualId, block.getBlockState().getProperties(), controller
        ).orElse(null);
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

    private record MapEntry(Direction direction, Face face) {
    }
}
