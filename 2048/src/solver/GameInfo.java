package solver;

import java.awt.Rectangle;

public class GameInfo {

	public int borderWidth;
	public int squareSize;
	public Rectangle boardBounds;
	public State colorState;

	public GameInfo(int borderWidth, int squareSize, Rectangle boardBounds,
			State colorState) {
		this.borderWidth = borderWidth;
		this.squareSize = squareSize;
		this.boardBounds = boardBounds;
		this.colorState = colorState;
	}

	public void print(){
		StringBuilder sb = new StringBuilder();
		sb.append("---GameInfo---\n");
		sb.append("borderwidth : ");
		sb.append(borderWidth);
		sb.append("\nsquareSize : ");
		sb.append(squareSize);
		sb.append("\nboardBounds : ");
		sb.append(boardBounds);
		System.out.println(sb.toString());
	}
}
