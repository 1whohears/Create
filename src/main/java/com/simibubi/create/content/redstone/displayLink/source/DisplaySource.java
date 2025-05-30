package com.simibubi.create.content.redstone.displayLink.source;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayBoardTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.NBTProcessors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class DisplaySource extends DisplayBehaviour {

	public static final List<MutableComponent> EMPTY = ImmutableList.of(Components.empty());
	public static final MutableComponent EMPTY_LINE = Components.empty();
	public static final MutableComponent WHITESPACE = Components.literal(" ");

	public abstract List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats);

	public void transferData(DisplayLinkContext context, DisplayTarget activeTarget, int line) {
		DisplayTargetStats stats = activeTarget.provideStats(context);

		if (activeTarget instanceof DisplayBoardTarget fddt) {
			List<List<MutableComponent>> flapDisplayText = provideFlapDisplayText(context, stats);
			fddt.acceptFlapText(line, flapDisplayText, context);
		}

		List<MutableComponent> text = provideText(context, stats);
		if (text.isEmpty())
			text = EMPTY;

		if (activeTarget.requiresComponentSanitization())
			for (MutableComponent component : text)
				if (NBTProcessors.textComponentHasClickEvent(component))
					return; // Naughty
		
		activeTarget.acceptText(line, text, context);
	}

	public void onSignalReset(DisplayLinkContext context) {};

	public void populateData(DisplayLinkContext context) {};

	public int getPassiveRefreshTicks() {
		return 100;
	};

	public boolean shouldPassiveReset() {
		return true;
	}

	protected String getTranslationKey() {
		return id.getPath();
	}

	public Component getName() {
		return Components.translatable(id.getNamespace() + ".display_source." + getTranslationKey());
	}

	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
		loadFlapDisplayLayout(context, flapDisplay, layout);
	}

	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay,
		FlapDisplayLayout layout) {
		if (!layout.isLayout("Default"))
			layout.loadDefault(flapDisplay.getMaxCharCount());
	}

	public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
		return provideText(context, stats).stream()
			.map(Arrays::asList)
			.toList();
	}

	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder,
		boolean isFirstLine) {}

}
