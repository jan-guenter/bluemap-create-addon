/*
 * SPDX-License-Identifier: MIT
 *
 * JSON emission follows BlueMap's MIT resource-model conventions while
 * applying the exact C&A modular-accumulator RECTANGLE texture behaviour.
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

/** Preserves authored accumulator geometry while applying group-aware CT. */
final class CaaAccumulatorEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;
    private static final Key SIDE_CONNECTED = Key.parse(
            "createaddition:block/modular_accumulator/block_connected"
    );
    private static final Key TOP_CONNECTED = Key.parse(
            "createaddition:block/modular_accumulator/block_top_connected"
    );

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CaaAccumulatorEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        CaaAccumulatorConnectedTexture.Group group = group(block);
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (group == null || !preflight(model)) {
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
        if (target.getTileModel().size() == modelStart) {
            return false;
        }
        target.initialize(modelStart);
        if (variant.isTransformed()) {
            target.transform(variant.getTransformMatrix());
        }
        var texture = resourcePack.getTextures().get(SIDE_CONNECTED);
        if (texture != null) {
            mapColor.add(new Color().set(texture.getColorPremultiplied()));
            mapColor.flatten().straight();
        }
        return true;
    }

    private boolean preflight(Model model) {
        if (model == null || model.getElements() == null
                || model.getElements().length == 0
                || resourcePack.getTextures().get(SIDE_CONNECTED) == null
                || resourcePack.getTextures().get(TOP_CONNECTED) == null) {
            return false;
        }
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            for (Face face : element.getFaces().values()) {
                Key source = face.getTexture().getTexturePath(model.getTextures()::get);
                if (source == null || resourcePack.getTextures().get(source) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void emitElement(
            CaaAccumulatorConnectedTexture.Group group,
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
            CaaAccumulatorConnectedTexture.Group group,
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
        if (source == null) {
            return;
        }
        var shifted = CaaAccumulatorConnectedTexture.material(
                fromBlueMap(worldDirection), source.getFormatted()
        ).orElse(null);
        Key texture = shifted == null
                ? source : Key.parse(shifted.connectedTexture());

        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);
        Vector4f uv = face.getUv();
        float[][] corners = {
                {uv.getX() / 16F, uv.getW() / 16F},
                {uv.getZ() / 16F, uv.getW() / 16F},
                {uv.getZ() / 16F, uv.getY() / 16F},
                {uv.getX() / 16F, uv.getY() / 16F}
        };
        if (shifted != null) {
            int cell = TankConnectedTexture.index(context(
                    group, fromBlueMap(worldDirection), block
            ));
            for (float[] corner : corners) {
                TankConnectedTexture.Uv connected =
                        TankConnectedTexture.connectedUv(cell, corner[0], corner[1]);
                corner[0] = connected.u();
                corner[1] = connected.v();
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
        var normal = worldDirection.toVector();
        LightData own = block.getLightData();
        LightData outside = block.getNeighborBlock(
                normal.getX(), normal.getY(), normal.getZ()
        ).getLightData();
        int sunlight = Math.max(own.getSkyLight(), outside.getSkyLight());
        int blocklight = Math.max(own.getBlockLight(), outside.getBlockLight());
        for (int triangle = start; triangle < start + 2; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, sunlight);
            mesh.setBlocklight(triangle, blocklight);
        }
    }

    private TankConnectedTexture.Context context(
            CaaAccumulatorConnectedTexture.Group own,
            CreateDirection face,
            BlockNeighborhood block
    ) {
        TankConnectedTexture.Frame frame = TankConnectedTexture.frame(face);
        return new TankConnectedTexture.Context(
                sameAt(own, frame.up(), block),
                sameAt(own, frame.up().opposite(), block),
                sameAt(own, frame.right().opposite(), block),
                sameAt(own, frame.right(), block)
        );
    }

    private boolean sameAt(
            CaaAccumulatorConnectedTexture.Group own,
            CreateDirection direction,
            BlockNeighborhood block
    ) {
        return CaaAccumulatorConnectedTexture.sameGroup(
                own,
                group(block.getNeighborBlock(direction.x(), direction.y(), direction.z()))
        );
    }

    private static CaaAccumulatorConnectedTexture.Group group(
            de.bluecolored.bluemap.core.world.block.ExtendedBlock block
    ) {
        if (!CaaAccumulatorConnectedTexture.BLOCK_ID.equals(
                block.getBlockState().getId().getFormatted()
        ) || !(block.getBlockEntity() instanceof CaaAccumulatorBlockEntityData data)) {
            return null;
        }
        CaaAccumulatorConnectedTexture.Position controller = data.effectiveController(
                block.getX(), block.getY(), block.getZ()
        );
        return controller == null ? null
                : new CaaAccumulatorConnectedTexture.Group(controller);
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
