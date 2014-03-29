package solver;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import solver.DecisionState.MOVE;
import solver.DecisionState.MoveChild;
import solver.State.Heuristic;

public class Main {

	
	
	
	public static boolean end = false;
	
	

	// private final static HashMultiset<DecisionState> visitedStates =
	// HashMultiset
	// .create();

	

	public static void main(String[] args) {
		SolverAgent.getInstance().execute();
	}

	

	
}
