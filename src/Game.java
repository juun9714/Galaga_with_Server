import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Game extends Thread{
    // 게임 아이디
    public String username = null;

    //게임의 진행에 관련된 내용을 담을 클래스
    private int delay = 20;
    private int check=0;

    //게임의 딜레이
    private long pretime;
    private int cnt;
    //cnt는 게임의 딜레이마다 증가, cnt가 이벤트 발생 주기를 컨트롤 하는 변수
    private int score;
    private int best;
    //게임 점수를 나타낼 변수
    private Image endScreen = new ImageIcon("src/images/end_screen.png").getImage();
    private Image winnerScreen = new ImageIcon("src/images/new_winner.png").getImage();
    //player 관련 변수들
    private Image player = new ImageIcon("src/images/player.png").getImage();
    private int playerX, playerY;
    private int playerWidth = player.getWidth(null);
    private int playerHeight = player.getHeight(null);
    private int playerSpeed = 10;
    //playerSpeed : 방향키 입력이 들어왔을 때, player가 이동할 거리
    private int playerHP = 30;

    private boolean up,down,left,right,shooting;
    //shooting변수 : 이 변수가 true일 경우, 공격이 발사되게 함
    private boolean isOver;

    //플레이어의 공격을 담을 ArrayList를 만들어준다.
    ArrayList<PlayerAttack> playerAttackList = new ArrayList<PlayerAttack>();
    ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
    ArrayList<EnemyAttack> enemyAttackList = new ArrayList<EnemyAttack>();

    private PlayerAttack playerAttack;
    private EnemyAttack enemyAttack;
    private Enemy enemy;


    private Audio backgroundMusic;
    private Audio hitSound;

    public String serverIP="localhost";
    public Socket soc;

    InputStream in;
    DataInputStream dis;
    @Override
    public void run(){
        backgroundMusic=new Audio("src/audio/gameBGM.wav",true);
        hitSound=new Audio("src/audio/hitSound.wav",false);

        try {
            soc = new Socket(serverIP, 4949);
            in = soc.getInputStream();
            dis = new DataInputStream(in);
            best=Integer.parseInt(dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reset();

        while(true) {
            //게임이 시작할 때 최고점 받아오기


            while (!isOver) {
                //cnt를 앞서 설정한 delay 밀리초가 지날 때마다 증가시켜준다.
                pretime = System.currentTimeMillis();
                //현재 시간
                if (System.currentTimeMillis() - pretime < delay) {
                    //pretime보다 시간이 지난 시간에서 pretime을 뺀 시간이 delay시간 보다 작으면 좀더 기다려 sleep
                    try {
                        Thread.sleep(delay - System.currentTimeMillis() + pretime);
                        keyProcess();
                        playerAttackProcess();
                        enemyAppearProcess();
                        enemyMoveProcess();
                        enemyAttackProcess();
                        cnt++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try{
                if(check==0) {
                    OutputStream os = soc.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    String buffer = username + "," + score;
                    //dos.writeUTF(Integer.toString(score));
                    dos.writeUTF(buffer);
                    check=1;
                }
                Thread.sleep(100);
                //isOver = true일 경우, 계속 정지해있기 위해서 Thread.sleep()을 해준다.
            }catch (InterruptedException | IOException e){
                e.printStackTrace();
            }
        }
    }
    
    
    //다시 하기 기능 : 모든 게임 메소드들을 리셋해주어야 함
    public void reset(){
        isOver = false;
        //스레드 시작시 실행할 내용
        cnt=0;
        playerY=640-playerHeight;
        playerX=(Main.SCREEN_WIDTH-playerWidth)/2;
        playerHP=30;
        //플레이어 시작 위치 초기화
        score=0;
        backgroundMusic.start();
        check=0;

        playerAttackList.clear();
        enemyList.clear();
        enemyAttackList.clear();
    }

    private void keyProcess(){
        //키 입력을 처리하는 메서드
        if(up && playerY - playerSpeed>0)
            playerY-=playerSpeed;
        if(down && playerY+playerHeight+playerSpeed<Main.SCREEN_HEIGHT)
            playerY+=playerSpeed;
        
        if(left && playerX-playerSpeed>0)
            playerX-=playerSpeed;
        if(right && playerX + playerWidth+playerSpeed<Main.SCREEN_WIDTH)
            playerX+=playerSpeed;

        if(shooting && cnt % 3 ==0){
            //cnt는 0.02초마다 1씩 증가한다. -> 0.14초마다 총알이 나간다.
            playerAttack=new PlayerAttack(playerX,playerY);
            //FIX
            playerAttackList.add(playerAttack);
        }
    }

    private void playerAttackProcess(){
        //플레이어의 공격을 처리해주는 메서드
        for(int i=0;i<playerAttackList.size();i++){
            playerAttack=playerAttackList.get(i);
            playerAttack.fire();
            //get 메서드로 총알 하나하나에 접근해서, 발사해준다.

            //충돌 판정

            for(int j=0;j<enemyList.size();j++){
                enemy=enemyList.get(j);
                if(playerAttack.x>= enemy.x-1 && playerAttack.x<=enemy.x+enemy.width+1 && playerAttack.y>=enemy.y-1 && playerAttack.y<=enemy.y+enemy.height+1){
                    enemy.hp-=playerAttack.attack;
                    score+=100;
                    hitSound.start();
                    playerAttackList.remove(playerAttack);
                }
                if(enemy.hp<=0){
                    enemyList.remove(enemy);
                }
            }

            for(int j=0;j<enemyAttackList.size();j++){
                enemyAttack=enemyAttackList.get(j);
                if(playerAttack.x>= enemyAttack.x-1 && playerAttack.x<=enemyAttack.x+enemyAttack.width+1 && playerAttack.y>=enemyAttack.y-1 && playerAttack.y<=enemyAttack.y+enemy.height+1){
                    score+=50;
                    hitSound.start();
                    playerAttackList.remove(playerAttack);
                    enemyAttackList.remove(enemyAttack);
                }
            }
        }
    }

    private void enemyAppearProcess() {
        if(cnt % 50 ==0){
            enemy=new Enemy((int)(Math.random()*315),0);
            enemyList.add(enemy);
        }

        for(int i=0;i<enemyList.size();i++){
            enemy=enemyList.get(i);
            if(enemy.x>=playerX-1 && enemy.x<=playerX+playerWidth+1 && enemy.y+ enemy.height>=playerY-1 && enemy.y <= playerY + playerHeight+1){
                playerHP-=5;
                hitSound.start();
                enemyList.remove(enemy);
                if(playerHP<=0) {
                    if(score>best)
                        best=score;
                    isOver = true;
                }
            }
        }
    }

    private void enemyMoveProcess(){
        for(int i=0;i<enemyList.size();i++){
            enemy=enemyList.get(i);
            enemy.move();
        }
    }

    private void enemyAttackProcess(){
        if(cnt % 20 == 0){
            enemyAttack= new EnemyAttack(enemy.x,enemy.y+10);
            enemyAttackList.add(enemyAttack);
        }

        for(int i=0;i<enemyAttackList.size();i++){
            enemyAttack=enemyAttackList.get(i);
            enemyAttack.fire();

            if(enemyAttack.x>=playerX-1 && enemyAttack.x<=playerX+playerWidth+1 && enemyAttack.y+ enemyAttack.height>=playerY-1 && enemyAttack.y <= playerY + playerHeight+1){
                playerHP-=enemyAttack.attack;
                hitSound.start();
                enemyAttackList.remove(enemyAttack);

                if(playerHP<=0) {
                    if(score>best)
                        best=score;
                    isOver = true;
                }
            }
        }
    }

    
    public void gameDraw(Graphics g){
        //게임 안의 요소들을 그려줄 gameDraw 메서드
        //앞으로 게임 안의 요소들을 그려주는 메서드들은 모두 여기 안에서 호출할 것임
        playerDraw(g);
        //플레이어와 적군은 gameDraw에서 그려주고, 각 개체의 총알은 playerDraw(), enemyDraw() 안에서 그려준다
        //enemy의 경우 enemy 자체가 여러개라 enemyDraw안에서 enemy를 그려준다.
        enemyDraw(g);
        infoDraw(g);
    }

    public void infoDraw(Graphics g){
        g.setColor(Color.white);
        g.setFont(new Font("ARIAL",Font.BOLD,15));
        g.drawString("SCORE : "+score,40,40);

        g.setColor(Color.white);
        g.setFont(new Font("ARIAL",Font.BOLD,15));
        g.drawString("HIGH SCORE : "+best,200,40);
        if(isOver){
            if(best<=score){


                g.drawImage(winnerScreen,0,0,null);

                g.setColor(Color.white);
                g.setFont(new Font("ARIAL",Font.BOLD,20));
                g.drawString("YOUR RECORD : "+score,80,440);
            }else{

                g.drawImage(endScreen,0,0,null);

                g.setColor(Color.white);
                g.setFont(new Font("ARIAL",Font.BOLD,20));
                g.drawString("YOUR SCORE : "+score,100,400);

                g.setColor(Color.white);
                g.setFont(new Font("ARIAL",Font.BOLD,20));
                g.drawString("BEST RECORD : "+best,90,430);
            }
        }
    }
    
    public void playerDraw(Graphics g){
        //player 관련한 요소를 그릴 playerDraw 메소드
        g.drawImage(player,playerX,playerY,null);
        g.setColor(Color.green);
        g.fillRect(playerX-(playerWidth/2),playerY-40,playerHP*3,10);
        for(int i=0;i<playerAttackList.size();i++){
            playerAttack=playerAttackList.get(i);
            g.drawImage(playerAttack.image,playerAttack.x,playerAttack.y,null);
            //get 메서드로 총알 하나하나에 접근해서, 발사해준다.
        }
    }

    public void enemyDraw(Graphics g){
        for(int i=0;i<enemyList.size();i++){
            enemy=enemyList.get(i);
            g.drawImage(enemy.image,enemy.x,enemy.y,null);
            g.setColor(Color.green);
            g.fillRect(enemy.x,enemy.y-40,enemy.hp*5,10);
        }

        for(int i=0;i<enemyAttackList.size();i++){
            enemyAttack=enemyAttackList.get(i);
            g.drawImage(enemyAttack.image,enemyAttack.x,enemyAttack.y,null);
        }
    }

    public boolean isOver() {
        return isOver;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}
