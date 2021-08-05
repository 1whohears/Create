package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.model.ArrayModelRenderer;
import com.jozufozu.flywheel.backend.model.ModelRenderer;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.core.model.WorldModel;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RenderedContraption extends ContraptionRenderInfo {

	private final ContraptionLighter<?> lighter;

	public final MaterialManager<ContraptionProgram> materialManager;
	public final ContraptionInstanceManager kinetics;

	private final Map<RenderType, ModelRenderer> renderLayers = new HashMap<>();

	private Matrix4f modelViewPartial;
	private AxisAlignedBB lightBox;

	public RenderedContraption(Contraption contraption, PlacementSimulationWorld renderWorld) {
		super(contraption, renderWorld);
		this.lighter = contraption.makeLighter();
		this.materialManager = MaterialManager.builder(CreateContexts.CWORLD)
				.setGroupFactory(ContraptionGroup.forContraption(this))
				.setIgnoreOriginCoordinate(true)
				.build();
		this.kinetics = new ContraptionInstanceManager(this, materialManager);

		buildLayers();
		if (Backend.getInstance().canUseInstancing()) {
			buildInstancedTiles();
			buildActors();
		}
	}

	public ContraptionLighter<?> getLighter() {
		return lighter;
	}

	public void doRenderLayer(RenderType layer, ContraptionProgram shader) {
		ModelRenderer structure = renderLayers.get(layer);
		if (structure != null) {
			setup(shader);
			structure.draw();
		}
	}

	public void beginFrame(BeginFrameEvent event) {
		super.beginFrame(event);

		if (!isVisible()) return;

		kinetics.beginFrame(event.getInfo());

		Vector3d cameraPos = event.getCameraPos();

		modelViewPartial = ContraptionMatrices.createModelViewPartial(contraption.entity, AnimationTickHolder.getPartialTicks(), cameraPos);

		lightBox = GridAlignedBB.toAABB(lighter.lightVolume.getTextureVolume())
				.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
	}

	void setup(ContraptionProgram shader) {
		if (modelViewPartial == null || lightBox == null) return;
		shader.bind(modelViewPartial, lightBox);
		lighter.lightVolume.bind();
	}

	void invalidate() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}
		renderLayers.clear();

		lighter.lightVolume.delete();

		materialManager.delete();
		kinetics.invalidate();
	}

	private void buildLayers() {
		for (ModelRenderer buffer : renderLayers.values()) {
			buffer.delete();
		}

		renderLayers.clear();

		List<RenderType> blockLayers = RenderType.chunkBufferLayers();

		for (RenderType layer : blockLayers) {
			Supplier<IModel> layerModel = () -> new WorldModel(renderWorld, layer, contraption.getBlocks().values());

			ModelRenderer renderer;
			if (Backend.getInstance().compat.vertexArrayObjectsSupported())
				renderer = new ArrayModelRenderer(layerModel);
			else
				renderer = new ModelRenderer(layerModel);

			renderLayers.put(layer, renderer);
		}
	}

	private void buildInstancedTiles() {
		Collection<TileEntity> tileEntities = contraption.maybeInstancedTileEntities;
		if (!tileEntities.isEmpty()) {
			for (TileEntity te : tileEntities) {
				if (InstancedRenderRegistry.getInstance()
						.canInstance(te.getType())) {
					World world = te.getLevel();
					BlockPos pos = te.getBlockPos();
					te.setLevelAndPosition(renderWorld, pos);
					kinetics.add(te);
					te.setLevelAndPosition(world, pos);
				}
			}
		}
	}

	private void buildActors() {
		contraption.getActors().forEach(kinetics::createActor);
	}
}
