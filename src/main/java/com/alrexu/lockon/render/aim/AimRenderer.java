package com.alrexu.lockon.render.aim;

import com.alrexu.lockon.logic.LockOn;
import com.alrexu.lockon.render.type.RenderTypes;
import com.alrexu.lockon.utils.VectorUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class AimRenderer {
	public static ResourceLocation AIM = new ResourceLocation("lockon", "textures/sprite/aim.png");
	private final LockOn lockOn;
	private boolean removed = false;
	private int tick = 0;

	public LockOn getLockOn() {
		return lockOn;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void tick() {
		tick++;
	}

	public int getTick() {
		return tick;
	}

	public AimRenderer(LockOn lockOn) {
		this.lockOn = lockOn;
	}

	public void render(RenderWorldLastEvent event) {
		if (removed || lockOn.isUnLocked()) {
			lockOn.unLock();
			removed = true;
			return;
		}
		Vector3d position = lockOn.getPoint(event.getPartialTicks());
		Minecraft mc = Minecraft.getInstance();
		MatrixStack stack = event.getMatrixStack();

		float scale;
		if (lockOn.isTargetingPoint()) scale = 1;
		else if (lockOn.isTargetingEntity()) {
			Entity entity = lockOn.getTargetEntity();
			scale = Math.max(entity.getBbHeight(), entity.getBbWidth());
		} else return;
		scale /= 1.1;
		if (tick < 5) scale *= (5.25 - (tick + event.getPartialTicks())) * 4;

		IRenderTypeBuffer.Impl bufferAim = mc.renderBuffers().bufferSource();
		IVertexBuilder builderAim = bufferAim.getBuffer(RenderTypes.AIM);

		ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
		Vector3d camPos = camera.getPosition();
		Vector3f look = camera.getLookVector();
		Vector3d cameraVec = new Vector3d(look.x(), look.y(), look.z());

		stack.pushPose();
		{

			stack.translate(-camPos.x, -camPos.y, -camPos.z);
			stack.translate(position.x(), position.y(), position.z());
			stack.scale(scale, scale, scale);
			stack.mulPose(Vector3f.YN.rotationDegrees((float) VectorUtils.toYaw(cameraVec)));
			stack.mulPose(Vector3f.XP.rotationDegrees((float) VectorUtils.toPitch(cameraVec)));

			Matrix4f mat = stack.last().pose();
			builderAim.vertex(mat, -1, -1, 0).uv(0, 1).endVertex();
			builderAim.vertex(mat, 1, -1, 0).uv(1, 1).endVertex();
			builderAim.vertex(mat, 1, 1, 0).uv(1, 0).endVertex();
			builderAim.vertex(mat, -1, 1, 0).uv(0, 0).endVertex();

		}
		stack.popPose();
		bufferAim.endBatch();

		//I added this to make it work well, but do not know why it works
		IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
		IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
		buffer.endBatch();
	}
}
