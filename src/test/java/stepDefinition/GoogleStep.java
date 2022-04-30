package stepDefinition;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

public class GoogleStep {

    WebDriver driver;

    public void GoogleStep(WebDriver driver){
        this.driver = driver;
    }

    @After
    public void AfterSetup(){
        driver.close();
    }

    @Given("open the browser")
    public void open_the_browser() {
        // Write code here that turns the phrase above into concrete actions
        String user=  System.getProperty("user.dir");
        System.setProperty("webdriver.chrome.driver",user+"/src/test/resources/driver/chromedriver");
        driver=new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.manage().window().maximize();

    }

    @Given("user is on google search")
    public void user_is_on_google_search() {
        driver.get("http://media.bww.com/Account/Login?ReturnUrl=%2f");
    }
    @When("user enter a text in the google search")
    public void user_enter_a_text_in_the_google_search() {
        driver.findElement(By.id("Ibo")).sendKeys("6599721");
        driver.findElement(By.id("Password")).sendKeys("*******");
    }
    @When("hit enter")
    public void hit_enter() throws Exception {
        driver.findElement(By.xpath("//*[@value='Log In']")).click();

        int pageNumber = 1;
        while(pageNumber <= 166){
            Thread.sleep(2500);
            for(int i=1 ; i <= 12; i++){
                String element= "//form[1]/div[2]/div["+i+"]/div[2]";

                String category= "//html/body/div[3]/div/article/form/div[2]/div["+ i +"]/div[2]/div[@class='category']";
                String genre = "";
                if(existsElement(category)) {
                     genre = driver.findElement(By.xpath(category)).getText();
                } else {
                    genre = "BWW Motivation";
                }

                if(existsElement(element)) {
                    driver.findElement(By.xpath(element)).click();
                    Thread.sleep(1000);
                    String bwwID = driver.findElement(By.xpath("//html[1]/body[1]/div[4]/div[2]/div[1]/div[2]")).getText();
                    /*
                    To Skip other language talks
                    if(bwwID.startsWith("SLS") || bwwID.startsWith("RLS")){
                        driver.findElement(By.xpath("//body[1]/div[4]/div[1]/button[1]")).click();
                        continue;
                    }*/
                    Thread.sleep(1000);
                    String talkName = driver.findElement(By.xpath("//html[1]/body[1]/div[4]/div[2]/div[1]/div[3]")).getText();
                    String speakerName = talkName.split(":")[0];
                    System.out.println(bwwID+ " - " + talkName);
                    String finalTalkName = bwwID+ " - " + talkName;
                    String URL = driver.findElement(By.xpath("//audio//source[2]")).getAttribute("src");
                    System.out.println(URL);
                    String fileName = "/Users/akinepalli/Desktop/BWWTalks/BWWTalks/" + finalTalkName.replace("/", "") + ".m4a";
                    File file = new File(fileName);
                    boolean exists = file.exists();
                    if(!exists) {
                        downloadFile(new URL(URL), fileName);
                        setTags(fileName, speakerName, genre);
                    } else {
                        setTags(fileName, speakerName, genre);
                    }
                    driver.findElement(By.xpath("//body[1]/div[4]/div[1]/button[1]")).click();
                }else
                    continue;
                }
            pageNumber++;
            driver.findElement(By.xpath("//a[contains(text(),'"+pageNumber+"')]")).click();
            System.out.println("Page Number:"+pageNumber);
        }



    }
    @Then("results will be populated based on the search")
    public void results_will_be_populated_based_on_the_search() {
        String URL = driver.findElement(By.xpath("//audio//source[2]")).getAttribute("src");
        driver.close();
    }

    private boolean existsElement(String id) {
        try {
            driver.findElement(By.xpath(id));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public static void downloadFile(URL url, String outputFileName) throws IOException
    {
        try (InputStream in = url.openStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream fos = new FileOutputStream(outputFileName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    public static void setTags(String path, String artist, String genre) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException, CannotWriteException {
        File testFile = new File(path);
        AudioFile f = AudioFileIO.read(testFile);
        Tag tag = f.getTag();
        tag.setField(FieldKey.ALBUM, "BWW");
        tag.setField(FieldKey.ARTIST, artist);
        tag.setField(FieldKey.GENRE, genre);
        f.setTag(tag);
        f.commit();
    }

}
