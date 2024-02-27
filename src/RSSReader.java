import org.jsoup.*;
import org.jsoup.nodes.Document;
import java.net.*;
import java.io.*;

public class RSSReader
{
    private String[] urls;

    public static void main(String[] args) throws Exception
    {
        String html = fetchPageSource("https://magazine.sebastianraschka.com/");
        System.out.println(extractRssUrl(html));
    }

    public static String extractRssUrl(String html)
    {
        Document doc = Jsoup.parse(html);
        return doc.select("[type='application/rss+xml']").attr("href");
    }

    public static String fetchPageSource(String urlString) throws Exception
    {
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
            return toString(urlConnection.getInputStream());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static String toString(InputStream inputStream) throws IOException
    {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")))
        {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null)
                stringBuilder.append(inputLine);

            return stringBuilder.toString();
        }
    }
}