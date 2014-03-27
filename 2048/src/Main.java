import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;



public class Main {
	
	public static Robot robot;
	public final static Color BORDER_COLOR = new Color(187,173,160);
	public final static Color COLOR_TWO = new Color(196,183,171);
	public final static Color COLOR_ZERO = new Color(204,192,179);
	public static boolean end = false;
	
	
	public static void main(String[] args) {
		try {
			robot = new Robot();
			while(!end){
//				getGridPosition()
//				robot.getPixelColor(x, y)
				getGameBoardPosition();
				
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static Point getGameBoardPosition(){
		Dimension size  = Toolkit.getDefaultToolkit().getScreenSize();
		
		//upperLeftPoint
		int x=0;
		int y=size.height/2;
		Color color = robot.getPixelColor(x,y);
		int step =5;
		boolean found =false;
		
		while(x<size.width){
			x+=step;
			color = robot.getPixelColor(x,y);
			if(color.equals(BORDER_COLOR)){
				found = true;
				break;
			}
		}
		
		if(found){
			int oldX = x;
			int oldY = y;
			//border width
			while(color.equals(BORDER_COLOR)){
				x--;
				color = robot.getPixelColor(x,y);
			}
			int startX = x+1;
			x = oldX;
			while(color.equals(BORDER_COLOR)){
				x++;
				color = robot.getPixelColor(x,y);
			}
			int border_width = x-1-startX;
//			System.out.println(""+startX+" "+border_width);
			//TODO check border
			
			//height
			//TODO approche dichotomique ?
			x=startX+border_width;
			while(color.equals(BORDER_COLOR)){
				y+=10;
				color = robot.getPixelColor(x,y);;
			}
			while(!color.equals(BORDER_COLOR)){
				y--;
				color = robot.getPixelColor(x,y);;
			}
			int top = y;
			
			y=oldY;
			while(color.equals(BORDER_COLOR)){
				y-=10;
				color = robot.getPixelColor(x,y);;
			}
			while(!color.equals(BORDER_COLOR)){
				y++;
				color = robot.getPixelColor(x,y);
			}
			int height = top-y;
			int bottom = y;
//			System.out.println(height);
			
			//TODO check for other angles to validate that all the grid is visible.
			int square_size = (int)(height - (border_width+2f*5f))/4;
//			System.out.println(square_size);
			int[][] colorGrid = new int[4][4];
			x=startX;
			y=bottom;
			for(int i=0;i<4;i++){
				for(int j =0;j<4;j++){
					x=startX-1+border_width+2+(int)(square_size/5)+j*(border_width+2+square_size);
					y=bottom-1+border_width+2+(int)(square_size/5)+i*(border_width+2+square_size);
					color=robot.getPixelColor(x, y);
					if(color.equals(BORDER_COLOR)){
						colorGrid[i][j]=-1;
					}else if(color.equals(COLOR_ZERO)){
						colorGrid[i][j]=0;
					}else if(color.equals(COLOR_TWO)){
						colorGrid[i][j]=2;
					}
					System.out.print(colorGrid[i][j]+" ");
				}
				System.out.println();
			}
			color=robot.getPixelColor(x+100, y+100);
			int val = -2;
			if(color.equals(BORDER_COLOR)){
				val=-1;
			}if(color.equals(COLOR_ZERO)){
				val=0;
			}else if(color.equals(COLOR_TWO)){
				val=2;
			}
			System.out.println(""+x+" "+y);
			System.out.println(val);
			System.out.println("--------------------------------------------------------------");
		}
		
		return null;
	}

}
