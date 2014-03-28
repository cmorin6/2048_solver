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

	public static Robot robot;
	public final static Color BORDER_COLOR = new Color(195, 182, 169);
	public final static Color COLOR_ZERO = new Color(204, 192, 179);
	public final static Color COLOR_TWO = new Color(238, 228, 218);
	public final static Color COLOR_FOUR = new Color(237, 224, 200);
	public final static Color COLOR_HEIGHT = new Color(242, 177, 121);
	public final static Color COLOR_SIXTEEN = new Color(245, 149, 99);
	public final static Color COLOR_THIRTY_TWO = new Color(246, 124, 95);
	public final static Color COLOR_SIXTY_FOUR = new Color(246, 94, 59);
	public final static Color COLOR_128 = new Color(236, 207, 114);
	public final static Color COLOR_256 = new Color(237, 204, 97);
	public final static Color COLOR_512 = new Color(237, 200, 80);
	public final static Color COLOR_1024 = new Color(237, 197, 63);
	public final static Color COLOR_2048 = new Color(237, 194, 46);

	public static boolean end = false;
	public final static Map<Integer, Integer> colorToValue = new HashMap<Integer, Integer>();
	public static float[] borderRatio = new float[2];

	// private final static HashMultiset<DecisionState> visitedStates =
	// HashMultiset
	// .create();

	public final static Heuristic heuristic = new Heuristic() {

		@Override
		public double getValue(State state) {
			double value = 0;
			for (int i = 0; i < State.GRID_SIZE; i++) {
				for (int j = 0; j < State.GRID_SIZE; j++) {
					int val = state.get(i, j);
					value += val * val;
				}
			}
			return Math.sqrt(value);
		}
	};

	public static void main(String[] args) {
		try {

			borderRatio[0] = (float) BORDER_COLOR.getGreen()
					/ (float) BORDER_COLOR.getRed();
			borderRatio[1] = (float) BORDER_COLOR.getBlue()
					/ (float) BORDER_COLOR.getRed();
			// System.out.println("[" + borderRatio[0] + "|" + borderRatio[1]
			// + "]");
			colorToValue.put(BORDER_COLOR.getRGB(), -1);
			colorToValue.put(COLOR_ZERO.getRGB(), 0);
			colorToValue.put(COLOR_TWO.getRGB(), 2);
			colorToValue.put(COLOR_FOUR.getRGB(), 4);
			colorToValue.put(COLOR_HEIGHT.getRGB(), 8);
			colorToValue.put(COLOR_SIXTEEN.getRGB(), 16);
			colorToValue.put(COLOR_THIRTY_TWO.getRGB(), 32);
			colorToValue.put(COLOR_SIXTY_FOUR.getRGB(), 64);
			colorToValue.put(COLOR_128.getRGB(), 128);
			colorToValue.put(COLOR_256.getRGB(), 256);
			colorToValue.put(COLOR_512.getRGB(), 512);
			colorToValue.put(COLOR_1024.getRGB(), 1024);
			colorToValue.put(COLOR_2048.getRGB(), 2048);

			robot = new Robot();
			while (!end) {
				// getGridPosition()
				// robot.getPixelColor(x, y)
				DecisionState state = getGameBoard();
				if (state != null) {
					state.print();
					MoveChild res = state.getBestMove(heuristic);
					if (res != null) {
						System.out.println("action : " + res.move);
						res.state.print();
						executeAction(res.move);
					}
					// System.out.println("found");

					// System.out.println("enabled : "+state.isRightEnable());
					// System.out.println("----------------");
					// state.hash=30;
					// if(!visitedStates.contains(state)){
					// visitedStates.add(state);
					// System.out.println("states : "+visitedStates.size());
					// System.out.println("----------------");
					// }
					// return;
				}
				Thread.sleep(400);

			}
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void executeAction(MOVE move) {
		switch (move) {
		case DOWN:
			robot.keyPress(KeyEvent.VK_DOWN);
			break;
		case RIGHT:
			robot.keyPress(KeyEvent.VK_RIGHT);
			break;
		case UP:
			robot.keyPress(KeyEvent.VK_UP);
			break;
		case LEFT:
			robot.keyPress(KeyEvent.VK_LEFT);
			break;

		default:
			break;
		}
	}

	public static DecisionState getGameBoard() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		BufferedImage screen = robot.createScreenCapture(new Rectangle(size));
		File outputfile = new File("C:\\Users\\Cedric\\Desktop\\image.jpg");
		try {
			// upperLeftPoint
			int x = 0;
			int y = size.height / 2;

			int color = screen.getRGB(x, y);
			int step = 8;
			boolean found = false;

			while (x < size.width) {
				color = screen.getRGB(x, y);
				if (isBorder(color)) {
					found = true;
					break;
				}
				x += step;
			}

			if (found) {

				// System.out.println(new Color(color));
				int oldX = x;
				int oldY = y;
				// border width
				while (isBorder(color)) {
					x--;
					color = screen.getRGB(x, y);
				}
				int startX = x + 1;
				// System.out.println("second");
				x = oldX;
				color = screen.getRGB(x, y);
				while (isBorder(color)) {
					x++;
					color = screen.getRGB(x, y);
				}
				// System.out.println(new Color(color));
				// System.out.println("end border : "+x);
				int border_width = x - 1 - startX;
//				 System.out.println("startX : " + startX + " border_width : "
//						+ border_width);
				// TODO check border

				// height
				x = startX + border_width;
				color = screen.getRGB(x, y);
				while (isBorder(color)) {
					y += 10;
					color = screen.getRGB(x, y);
				}
				while (!isBorder(color)) {
					y--;
					color = screen.getRGB(x, y);
				}
				int top = y;

				y = oldY;
//				screen.setRGB(startX, y, Color.GREEN.getRGB());
				while (isBorder(color)) {
					y -= 10;
					color = screen.getRGB(x, y);
				}
				while (!isBorder(color)) {
					y++;
					color = screen.getRGB(x, y);
				}
				int height = top - y;
				int bottom = y;
//				 System.out.println(height);

				// topright
				x = startX + height - (border_width / 2);
				y = bottom + (border_width / 2);
				// screen.setRGB(x, y, Color.RED.getRGB());
				// screen.setRGB(x+1, y-1, Color.RED.getRGB());
				// screen.setRGB(x+1, y+1, Color.RED.getRGB());
				// screen.setRGB(x-1, y+1, Color.RED.getRGB());
				// screen.setRGB(x-1, y-1, Color.RED.getRGB());
				//

				if (!isBorder(screen.getRGB(x, y))) {
					return null;
				}
				x = startX + height + (border_width / 2);
				y = bottom + (border_width / 2);
				if (isBorder(screen.getRGB(x, y))) {
//
//					screen.setRGB(x, y, Color.RED.getRGB());
//					try {
//						boolean ok = ImageIO.write(screen, "jpg", outputfile);
//						if (ok) {
//							System.out.println("done");
//						}
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					return null;
				}
				x = startX + height + (border_width / 2);
				y = bottom - (border_width / 2);
				if (isBorder(screen.getRGB(x, y)))
					return null;
				// bottom right
				x = startX + height - (border_width / 2);
				y = bottom + height - (border_width / 2);
				if (!isBorder(screen.getRGB(x, y)))
					return null;
				x = startX + height - (border_width / 2);
				y = bottom + height + (border_width / 2);
				if (isBorder(screen.getRGB(x, y)))
					return null;
				x = startX + height + (border_width / 2);
				y = bottom + height - (border_width / 2);
				if (isBorder(screen.getRGB(x, y)))
					return null;
				// visible.
				int square_size = (int) (height - ((border_width + 2f) * 5f)) / 4;
				// System.out.println(square_size);
				DecisionState state = DecisionState.buildState();
				x = startX;
				y = bottom;
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 4; j++) {
						x = startX - 1 + border_width + 2
								+ (int) (square_size / 5) + j
								* (border_width + 2 + square_size);
						y = bottom - 1 + border_width + 2
								+ (int) (square_size / 5) + i
								* (border_width + 2 + square_size);

						color = screen.getRGB(x, y);

						int value = -2;
						if (colorToValue.containsKey(color)) {
							value = colorToValue.get(color);
						}
						state.set(i, j, value);
						if (value == -2) {
							System.out.print(new Color(color) + " ");
						}
						// System.out.print(colorGrid[i][j] + " ");
					}
					// System.out.println();
				}

				// try {
				// boolean ok = ImageIO.write(screen, "jpg", outputfile);
				// if (ok) {
				// System.out.println("done");
				// }
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				return state;
				// System.out
				// .println("--------------------------------------------------------------");
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("error while reading the grid");
			// e.printStackTrace();
		}

		return null;
	}

	public static float MAX_ERROR = 0;

	public static boolean isBorder(int rgb) {
		int range = 10;
		Color c = new Color(rgb);
		float[] ratio = new float[2];
		ratio[0] = (float) c.getGreen() / (float) c.getRed();
		ratio[1] = (float) c.getBlue() / (float) c.getRed();
		float error = Math.abs(ratio[0] - borderRatio[0])
				+ Math.abs(ratio[1] - borderRatio[1]);
		// MAX_ERROR = Math.max(MAX_ERROR, error);
		// System.out.println("MAX_ERROR : "+MAX_ERROR);
		boolean ret = error < 0.1
				&& Math.abs(c.getRed() - BORDER_COLOR.getRed()) < range
				&& Math.abs(c.getBlue() - BORDER_COLOR.getBlue()) < range
				&& Math.abs(c.getGreen() - BORDER_COLOR.getGreen()) < range;
		// System.out.println(error+" "+c+" "+ret);
		return ret;
	}
}
