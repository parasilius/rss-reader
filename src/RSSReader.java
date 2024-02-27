import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class RSSReader
{
    private ArrayList<String> websiteNames;
    private ArrayList<String> websiteUrls;
    private ArrayList<String> rssUrls;
    private int rssCount = 0;

    public static void main(String[] args) throws Exception
    {
        String url = "https://www.omgubuntu.co.uk/";
        String html = fetchPageSource(url);
        System.out.println(extractPageTitle(html));
        retrieveRssContent(extractRssUrl(html));
    }

    public void saveData(String dataFilePath)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dataFilePath)));
            for (int i = 0; i < rssCount; ++i)
                writer.write(websiteNames.get(i) + "," + websiteUrls.get(i) + "," + rssUrls.get(i));
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("Error: failed to open data file:" + e.getMessage());
        }
    }

    public void loadData(String dataFilePath)
    {
        try
        {
            File dataFile = new File(dataFilePath);
            FileReader fileReader = new FileReader(dataFile);

            BufferedReader reader = new BufferedReader(fileReader);

            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] dataStringList = line.split(",");
                websiteNames.add(dataStringList[0]);
                websiteUrls.add(dataStringList[1]);
                rssUrls.add(dataStringList[2]);
                rssCount += 1;
            }
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("Error: failed to open data file: " + e.getMessage());
        }
    }

    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }

    public static void retrieveRssContent(String rssUrl) throws Exception
    {
        String rssXml = fetchPageSource(rssUrl);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(rssXml);
        ByteArrayInputStream input = new ByteArrayInputStream(
                xmlStringBuilder.toString().getBytes("UTF-8"));
        org.w3c.dom.Document doc = documentBuilder.parse(input);
        NodeList itemNodes = doc.getElementsByTagName("item");

        for (int i = 0; i < itemNodes.getLength(); ++i)
        {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) itemNode;
                System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
            }
        }
    }

    public static String extractRssUrl(String html)
    {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
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