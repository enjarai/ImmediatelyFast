/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.injection.mixins.fast_buffer_upload;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(value = VertexBuffer.class, priority = 500)
public abstract class MixinVertexBuffer {

    @Unique
    private int vertexBufferSize;

    @Unique
    private int indexBufferSize;

    @Redirect(method = "uploadVertexBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void optimizeVertexDataUploading(int target, ByteBuffer data, int usage) {
        if (data.remaining() > this.vertexBufferSize) {
            this.vertexBufferSize = data.remaining();
            RenderSystem.glBufferData(target, data, usage);
        } else {
            final long addr = GL30C.nglMapBufferRange(target, 0, data.remaining(), GL30C.GL_MAP_WRITE_BIT | GL30C.GL_MAP_INVALIDATE_BUFFER_BIT);
            MemoryUtil.memCopy(MemoryUtil.memAddress(data), addr, data.remaining());
            GL15C.glUnmapBuffer(target);
        }
    }

    @Redirect(method = "uploadIndexBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void optimizeIndexDataUploading(int target, ByteBuffer data, int usage) {
        if (data.remaining() > this.indexBufferSize) {
            this.indexBufferSize = data.remaining();
            RenderSystem.glBufferData(target, data, usage);
        } else {
            final long addr = GL30C.nglMapBufferRange(target, 0, data.remaining(), GL30C.GL_MAP_WRITE_BIT | GL30C.GL_MAP_INVALIDATE_BUFFER_BIT);
            MemoryUtil.memCopy(MemoryUtil.memAddress(data), addr, data.remaining());
            GL15C.glUnmapBuffer(target);
        }
    }

}
