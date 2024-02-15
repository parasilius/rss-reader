import java.net.*;
import java.io.*;

public class RSSReader
{
    private String[] urls;

    public static void main(String[] args) throws Exception
    {
        BufferedReader in = getIn("https://www.rkneusel.com/");
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
    }

    public static BufferedReader getIn(String urlString) throws Exception
    {
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            return new BufferedReader(new InputStreamReader(url.openStream()));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}