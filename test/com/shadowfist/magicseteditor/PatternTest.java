package com.shadowfist.magicseteditor;

import static org.junit.Assert.*;

import org.junit.Test;

public class PatternTest {

	@Test
	public void testOneBFAisBolded()
	{
		assertEquals("<b>Toughness</b>: 1. This should bold just toughness.",
				Main.toFormattedText("Toughness: 1. This should bold just toughness."));
	}

	@Test
	public void testTwoBFAsareFoundAndBolded()
	{
		assertEquals("<b>Unique</b>. <b>Assassinate</b>. This should bold both.",
				Main.toFormattedText("Unique. Assassinate. This should bold both."));
	}

	@Test
	public void testBFAandDesignatorIsFoundAndBolded()
	{
		assertEquals("<b>Not Cumulative</b>. All <i>Fire</i> and <i>Netherworld</i> are smoked.",
				Main.toFormattedText("Not Cumulative. All <Fire> and <Netherworld> are smoked."));
	}
}
