import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    // 랭킹 정보 텍스트 영역
    private static JTextArea textArea;
    // Score, UserName
    private static Map<Integer, String> scoreMap = new TreeMap<Integer, String>(Collections.reverseOrder());

    // 서버 윈도우
    public Server() throws IOException {
        // 윈도우 제목
        setTitle("서버");

        // 서버 종료 버튼
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 현재 랭킹 정보
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 15);

        textArea = new JTextArea(15, 30);
        //.textArea.setFont(font);
        textArea.setFont(font);
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(Color.BLACK);
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 0));
        textArea.setEditable(false);

        renderScore(true);

        add(textArea, BorderLayout.CENTER);

        pack();

        // 윈도우 창 보여주기
        setVisible(true);
    }

    public static void renderScore(Boolean isInit) throws IOException {
        textArea.setText("");
        // score.txt 읽음
        InputStream fis = new FileInputStream("score.txt");
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        String[] buffer = null;
        int rank=1;
        textArea.append("------- SCORE BOARD --------\n");
        while( (line = br.readLine()) != null ) {
            if(rank>10)
                break;
            if (isInit == true) {

                buffer = line.split(",");
                scoreMap.put(Integer.parseInt(buffer[1]), buffer[0]);
            }
            buffer = line.split(",");
            textArea.append("RANK  "+rank+"  :  "+buffer[0].toUpperCase()+"   "+buffer[1].toUpperCase() + "\n");
            rank++;
        }
    }

    public static void main(String[] args) throws IOException {
        // 통신 부분
        InputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        BufferedWriter fw;

        ServerSocket ss;
        Scanner scn = new Scanner(System.in);
        System.out.println("Server start");

        File f = new File("score.txt");

        // 파일 존재 여부 판단
        if (!f.isFile()) {
            System.out.println("그런 파일 없습니다.");

            try {
                new FileWriter("score.txt", true);
                System.out.println("파일 만듦.");
                FileOutputStream fos = new FileOutputStream("score.txt");
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write("nobody,0");
                bw.close();
                osw.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //파일 있으면 읽고, 없으면 만들기

        // 서버 윈도우 실행
        Server server = new Server();

        ss = new ServerSocket(4949);
        Socket soc = ss.accept();
        //연결 완
        
        while(true) {
            try {
                fis = new FileInputStream("score.txt");
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);

                java.io.OutputStream out = soc.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                String best=br.readLine().split(",")[1]; // best score
                //String best=br.readLine();
                dos.writeUTF(best);
                dos.flush();

                // read 데이터 아이디,스코어
                InputStream in = soc.getInputStream();
                DataInputStream dis = new DataInputStream(in);
                String rec = dis.readUTF();
                System.out.println("received from client " + rec);

                String[] buffer = rec.split(",");

                // score, username
                scoreMap.put(Integer.parseInt(buffer[1]), buffer[0]);

                // 저장
                FileOutputStream fos = new FileOutputStream("score.txt");
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bw = new BufferedWriter(osw);

                // 결과 출력
                int i = 0;
                for(Map.Entry<Integer, String> entry : scoreMap.entrySet()) {
                    if (i > 10) break;
                    bw.write(entry.getValue() + "," + entry.getKey() + "\r\n");
                    i++;
                }
                bw.close();
                osw.close();
                fos.close();

                renderScore(false);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.exit(0);
            } catch (IOException e) {
                //e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
