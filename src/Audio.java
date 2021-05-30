import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class Audio {
    //음악 관리

    private Clip clip;
    private File audioFile;
    private AudioInputStream audioInputStream;
    private boolean isLoop;

    public Audio(String pathName, boolean isLoop){
        try {
            clip = AudioSystem.getClip();
            audioFile = new File(pathName);
            audioInputStream=AudioSystem.getAudioInputStream(audioFile);
            clip.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        clip.setFramePosition(0);
        clip.start();
        if(isLoop)clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(){
        clip.stop();
    }

}
