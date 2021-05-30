import javax.swing.*;
import java.awt.*;

public class Enemy {
    //Enemy의 위치 정보, 체력
    Image image = new ImageIcon("src/images/enemy.png").getImage();
    int x,y;
    int width=image.getWidth(null);
    int height = image.getHeight(null);

    int hp=10;


    public Enemy(int x, int y){
        this.x=x;
        this.y=y;
    }

    public void move(){
        //적의 기체를 움직이게 할 메서드
        this.y+=7;
    }
}
