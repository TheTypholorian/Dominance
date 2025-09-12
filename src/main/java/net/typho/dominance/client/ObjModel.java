package net.typho.dominance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.vertex.VertexArray;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;

public interface ObjModel {
    void render(VertexConsumer consumer, int light);

    default void render(VertexConsumer consumer) {
        render(consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    default VertexArray toArray(VertexFormat format) {
        return toArray(format, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    default VertexArray toArray(VertexFormat format, int light) {
        return toArray(VertexFormat.DrawMode.QUADS, format, light);
    }

    default VertexArray toArray(VertexFormat.DrawMode mode, VertexFormat format) {
        return toArray(mode, format, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    default VertexArray toArray(VertexFormat.DrawMode mode, VertexFormat format, int light) {
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(mode, format);
        render(builder);
        VertexArray array = VertexArray.create();
        array.bind();
        array.upload(builder.end(), VertexArray.DrawUsage.STATIC);
        return array;
    }
}
