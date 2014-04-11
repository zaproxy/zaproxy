package org.zaproxy.zap.view;

import org.junit.Test;

import java.awt.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LayoutHelperUnitTest {

	private static final int X = 1;
	private static final int Y = 2;
	private static final int WIDTH = 3;
	private static final int HEIGHT = 4;
	private static final double WEIGHT_X = 4.5;
	private static final double WEIGHT_Y = 6.7;
	private static final int FILL = 8;
	private static final int ANCHOR = 9;
	private static final Insets INSETS = new Insets(10, 11, 12, 13);


	@Test
	public void shouldResizeHorizontallyAndVerticallyByDefault() {
		// given
		// when
		GridBagConstraints constraints = LayoutHelper.getGBC(X, Y, WIDTH,WEIGHT_X);
		// then
		assertThat(constraints.fill, is(GridBagConstraints.BOTH));
	}

	@Test
	public void shouldUseNorthWestAnchorByDefault() {
		// given
		// when
		GridBagConstraints constraints = LayoutHelper.getGBC(X, Y, WIDTH,WEIGHT_X);
		// then
		assertThat(constraints.anchor, is(GridBagConstraints.NORTHWEST));
	}

	@Test
	public void shouldKeepDefaultInsetsOnGivenNullParameter() {
		// given
		// when
		GridBagConstraints constraints = LayoutHelper.getGBC(X,Y,WIDTH,WEIGHT_X,null);
		// then
		assertThat(constraints.insets, is(new Insets(0, 0, 0, 0)));
	}

	@Test
	public void shouldSetAllGivenParameters() {
		// given
		// when
		GridBagConstraints constraints = LayoutHelper.getGBC(X,Y,WIDTH,HEIGHT,WEIGHT_X,WEIGHT_Y,FILL,ANCHOR,INSETS);
		// then
		assertThat(constraints.gridx, is(X));
		assertThat(constraints.gridy, is(Y));
		assertThat(constraints.gridwidth, is(WIDTH));
		assertThat(constraints.gridheight, is(HEIGHT));
		assertThat(constraints.weightx, is(WEIGHT_X));
		assertThat(constraints.weighty, is(WEIGHT_Y));
		assertThat(constraints.fill, is(FILL));
		assertThat(constraints.anchor, is(ANCHOR));
		assertThat(constraints.insets, is(INSETS));
	}

}
