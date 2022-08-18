package com.simibubi.create.foundation.utility;

import net.createmod.catnip.utility.Couple;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class FluidFormatter {

	public static String asString(long amount, boolean shorten) {
		Couple<MutableComponent> couple = asComponents(amount, shorten);
		return couple.getFirst().getString() + " " + couple.getSecond().getString();
	}

	public static Couple<MutableComponent> asComponents(long amount, boolean shorten) {
		if (shorten && amount >= 1000) {
			return Couple.create(
					new TextComponent(String.format("%.1f" , amount / 1000d)),
					CreateLang.translateDirect("generic.unit.buckets")
			);
		}

		return Couple.create(
				new TextComponent(String.valueOf(amount)),
				CreateLang.translateDirect("generic.unit.millibuckets")
		);
	}

}
