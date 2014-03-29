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

import javax.imageio.ImageIO;

import solver.DecisionState.MOVE;

public class IOManager {

	public final static int MAX_BORDER = 40;

	private Robot robot;

	public IOManager() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			System.err.println("Error while Initialising Robot.");
			e.printStackTrace();
		}
	}

	public void executeAction(MOVE move) {
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

	public GameInfo initGameBoardLocation(BufferedImage screen) {

		GameInfo ret = null;

		int x = 0;
		int y = screen.getHeight() / 2;

		int color = screen.getRGB(x, y);
		int step = 8;
		boolean found = false;

		while (x < screen.getWidth()) {
			color = screen.getRGB(x, y);
			if (SolverAgent.getInstance().isBorder(color)) {
				found = true;
				break;
			}
			x += step;
		}

		if (found) {
			Rectangle rect = new Rectangle();
			int oldX = x;
			int oldY = y;
			// border width
			while (SolverAgent.getInstance().isBorder(color)) {
				x--;
				color = screen.getRGB(x, y);
			}
			rect.x = x;
			// System.out.println("second");
			x = oldX;
			color = screen.getRGB(x, y);
			while (SolverAgent.getInstance().isBorder(color)) {
				x++;
				color = screen.getRGB(x, y);
				if (x - rect.x > MAX_BORDER) {
					// TODO change y instead
					return ret;
				}
			}
			// System.out.println(new Color(color));
			// System.out.println("end border : "+x);
			int borderWidth = x - rect.x;
			// System.out.println("startX : " + startX + " border_width : "
			// + border_width);

			// height
			x = rect.x + borderWidth / 2;
			color = screen.getRGB(x, y);
			while (SolverAgent.getInstance().isBorder(color)) {
				y -= 10;
				color = screen.getRGB(x, y);
			}
			while (!SolverAgent.getInstance().isBorder(color)) {
				y++;
				color = screen.getRGB(x, y);
			}

			rect.y = y - 1;

			y = oldY;
			color = screen.getRGB(x, y);
			while (SolverAgent.getInstance().isBorder(color)) {
				y += 10;
				color = screen.getRGB(x, y);
			}
			while (!SolverAgent.getInstance().isBorder(color)) {
				y--;
				color = screen.getRGB(x, y);
			}

			rect.height = y - rect.y;
			rect.width = rect.height;
			int square_size = (int) (rect.height - ((borderWidth + 2f) * 5f)) / 4;

			ret = new GameInfo(borderWidth, square_size, rect,
					DecisionState.buildState());
		}
		return ret;
	}

	private boolean CHECK_INFO_DEBUG = true;
	
	public boolean checkInfo(GameInfo info, BufferedImage screen) {
		// topright
		int x = info.boardBounds.x + info.boardBounds.width
				- (info.borderWidth / 2);
		int y = info.boardBounds.y + (info.borderWidth / 2);
		if (!SolverAgent.getInstance().isBorder(screen.getRGB(x, y))) {
			if(CHECK_INFO_DEBUG){
				System.out.println(new Color(screen.getRGB(x, y)));
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 1");
			}
			return false;
		}
		x = info.boardBounds.x + info.boardBounds.width
				- (info.borderWidth / 2);
		y = info.boardBounds.y - (info.borderWidth / 2);
		if (SolverAgent.getInstance().isBorder(screen.getRGB(x, y))) {
			if(CHECK_INFO_DEBUG){
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 2");
			}
			return false;
		}
		
		x = info.boardBounds.x + info.boardBounds.width
				+ (info.borderWidth / 2);
		y = info.boardBounds.y + (info.borderWidth / 2);
		if (SolverAgent.getInstance().isBorder(screen.getRGB(x, y))){
			if(CHECK_INFO_DEBUG){
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 3");
			}
			return false;
		}
		// bottom right
		x = info.boardBounds.x + info.boardBounds.width
				- (info.borderWidth / 2);
		y = info.boardBounds.y + info.boardBounds.height
				- (info.borderWidth / 2);
		if (!SolverAgent.getInstance().isBorder(screen.getRGB(x, y))) {
			if(CHECK_INFO_DEBUG){
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 4");
			}
			return false;
		}
		x = info.boardBounds.x + info.boardBounds.width
				+ (info.borderWidth / 2);
		y = info.boardBounds.y + info.boardBounds.height
				- (info.borderWidth / 2);
		if (SolverAgent.getInstance().isBorder(screen.getRGB(x, y))) {
			if(CHECK_INFO_DEBUG){
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 5");
			}
			return false;
		}
		x = info.boardBounds.x + info.boardBounds.width
				- (info.borderWidth / 2);
		y = info.boardBounds.y + info.boardBounds.height
				+ (info.borderWidth / 2);
		if (SolverAgent.getInstance().isBorder(screen.getRGB(x, y))) {
			if(CHECK_INFO_DEBUG){
				debugImg(screen,x,y,Color.RED);
				System.err.println("Check info failled at stage 6");
			}
			return false;
		}
		return true;
	}

	private final static boolean GETGAMEINFODEBUG = false;

	public GameInfo getGameInfo(GameInfo info) {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		BufferedImage screen = robot.createScreenCapture(new Rectangle(size));
		if (info == null) {
			info = initGameBoardLocation(screen);
		}
		if (info == null) {
			return null;
		}
		if (GETGAMEINFODEBUG) {
			info.print();
		}
		if (!checkInfo(info, screen)) {
			if (GETGAMEINFODEBUG) {
				System.err.println("Info check failled.");
			}
			return null;
		}

		int x;
		int y;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				x = info.boardBounds.x + info.borderWidth
						+ (int) (info.squareSize / 4) + j
						* (info.borderWidth + 2 + info.squareSize);
				y = info.boardBounds.y + info.borderWidth
						+ (int) (info.squareSize / 4) + i
						* (info.borderWidth + 2 + info.squareSize);

				int color = screen.getRGB(x, y);

				info.colorState.set(i, j, color);
			}
		}
		return info;
	}

	public static float MAX_ERROR = 0;

	public void debugImg(BufferedImage screen, int x, int y, Color color) {
		File out = new File("C:\\Users\\Morin\\Desktop\\screen.png");
		screen.setRGB(x, y, color.getRGB());
		screen.setRGB(x + 1, y + 1, color.getRGB());
		screen.setRGB(x + 1, y - 1, color.getRGB());
		screen.setRGB(x - 1, y + 1, color.getRGB());
		screen.setRGB(x - 1, y - 1, color.getRGB());
		try {
			ImageIO.write(screen, "png", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
