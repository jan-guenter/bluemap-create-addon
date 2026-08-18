/*
 * SPDX-License-Identifier: MIT
 *
 * JSON-model emission follows BlueMap's MIT-licensed resource-model coordinate
 * and UV conventions, with Create's exact tank grouping and RECTANGLE shift.
 */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

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
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.Map;

/** Preserves authored tank JSON geometry while applying exact group-aware CT. */
final class TankConnectedEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    TankConnectedEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            String blockId,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (variant.isTransformed()) {
            return false;
        }
        TankConnectedTexture.GroupKey ownGroup = group(blockId, block);
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (ownGroup == null || !preflight(blockId, model)) {
            return false;
        }

        int modelStart = target.getTileModel().size();
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            int elementStart = target.getTileModel().size();
            emitElement(blockId, ownGroup, model, element, block, target);
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

        Key top = Key.parse("create:fluid_tank".equals(blockId)
                ? "create:block/fluid_tank_top"
                : "create:block/creative_casing");
        Texture topTexture = resourcePack.getTextures().get(top);
        if (topTexture != null) {
            mapColor.add(new Color().set(topTexture.getColorPremultiplied()));
            mapColor.flatten().straight();
        }
        return true;
    }

    private boolean preflight(String blockId, Model model) {
        if (model == null || model.getElements() == null || model.getElements().length == 0) {
            return false;
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
                var shifted = TankConnectedTexture.material(blockId, source.getFormatted());
                if (shifted.isPresent() && resourcePack.getTextures().get(
                        Key.parse(shifted.orElseThrow().connectedTexture())
                ) == null) {
                    return false;
                }
            }
        }
        return hasFace;
    }

    private void emitElement(
            String blockId,
            TankConnectedTexture.GroupKey ownGroup,
            Model model,
            Element element,
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
        emitFace(blockId, ownGroup, model, element, Direction.DOWN, block, target,
                x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        emitFace(blockId, ownGroup, model, element, Direction.UP, block, target,
                x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        emitFace(blockId, ownGroup, model, element, Direction.NORTH, block, target,
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        emitFace(blockId, ownGroup, model, element, Direction.SOUTH, block, target,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        emitFace(blockId, ownGroup, model, element, Direction.WEST, block, target,
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        emitFace(blockId, ownGroup, model, element, Direction.EAST, block, target,
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void emitFace(
            String blockId,
            TankConnectedTexture.GroupKey ownGroup,
            Model model,
            Element element,
            Direction direction,
            BlockNeighborhood block,
            TileModelView target,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        Face face = element.getFaces().get(direction);
        if (face == null || culled(blockId, ownGroup, face, block)) {
            return;
        }
        Key source = face.getTexture().getTexturePath(model.getTextures()::get);
        var shifted = TankConnectedTexture.material(blockId, source.getFormatted());
        Key texture = shifted.isPresent()
                ? Key.parse(shifted.orElseThrow().connectedTexture()) : source;

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
        if (shifted.isPresent()) {
            int cell = TankConnectedTexture.index(context(blockId, ownGroup, direction, block));
            for (float[] corner : corners) {
                TankConnectedTexture.Uv uv = TankConnectedTexture.connectedUv(
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
        var vector = direction.toVector();
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
            String blockId,
            TankConnectedTexture.GroupKey ownGroup,
            Face face,
            BlockNeighborhood block
    ) {
        Direction cullface = face.getCullface();
        return cullface != null
                && cullface != Direction.UP && cullface != Direction.DOWN
                && sameAt(blockId, ownGroup, fromBlueMap(cullface), block);
    }

    private TankConnectedTexture.Context context(
            String blockId,
            TankConnectedTexture.GroupKey ownGroup,
            Direction face,
            BlockNeighborhood block
    ) {
        TankConnectedTexture.Frame frame = TankConnectedTexture.frame(fromBlueMap(face));
        return new TankConnectedTexture.Context(
                sameAt(blockId, ownGroup, frame.up(), block),
                sameAt(blockId, ownGroup, frame.up().opposite(), block),
                sameAt(blockId, ownGroup, frame.right().opposite(), block),
                sameAt(blockId, ownGroup, frame.right(), block)
        );
    }

    private boolean sameAt(
            String blockId,
            TankConnectedTexture.GroupKey ownGroup,
            CreateDirection direction,
            BlockNeighborhood block
    ) {
        var neighbor = block.getNeighborBlock(direction.x(), direction.y(), direction.z());
        return TankConnectedTexture.sameGroup(
                ownGroup, group(blockId, neighbor.getBlockState().getId().getFormatted(), neighbor)
        );
    }

    private static TankConnectedTexture.GroupKey group(
            String blockId,
            BlockNeighborhood block
    ) {
        return group(blockId, block.getBlockState().getId().getFormatted(), block);
    }

    private static TankConnectedTexture.GroupKey group(
            String expectedId,
            String actualId,
            de.bluecolored.bluemap.core.world.block.ExtendedBlock block
    ) {
        if (!expectedId.equals(actualId)
                || !(block.getBlockEntity() instanceof TankBlockEntityData data)) {
            return null;
        }
        TankConnectedTexture.Position controller = data.effectiveController(
                block.getX(), block.getY(), block.getZ()
        );
        return controller == null ? null
                : new TankConnectedTexture.GroupKey(actualId, controller);
    }

    private static CreateDirection fromBlueMap(Direction direction) {
        return CreateDirection.valueOf(direction.name());
    }
}
