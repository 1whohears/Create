package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;
import net.minecraft.core.Direction;

public class TemplateScenes {

	public static void templateMethod(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("", "");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	}

}
