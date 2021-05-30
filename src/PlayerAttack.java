import javax.swing.*;
import java.awt.*;

public class PlayerAttack {
    //플레이어의 총알을 구현
    Image image = new ImageIcon("src/images/player_attack.png").getImage();
    int x, y;
    int width = image.getWidth(null);
    //공격의 이미지, 위치, 공격력, 공격의 충돌 판정을 위해서 이미지의 너비와 높이도 신경써주어야 함
    int height = image.getHeight(null);
    int attack=5;

    public PlayerAttack(int x, int y){
        this.x=x;
        this.y=y;
    }

    public void fire(){
        //플레이어가 공격을 발사하는 메서드 : 플레이어의 공격은 위로만 나가므로 y값을 감소시키면 된다.
        this.y-=15;
    }

}
