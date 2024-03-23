import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class RSSReader
{
    private ArrayList<String> websiteNames;
    private ArrayList<String> websiteUrls;
    private ArrayList<String> rssUrls;
    private int rssCount;
    private static final int MAX_ITEMS = 5;
    private static final String DATA_FILE_PATH = "data.txt";

    public static void main(String[] args) throws Exception
    {
        System.out.println("Welcome to RSS Reader!");
        RSSReader rssReader = new RSSReader();
        while (true)
        {
            System.out.println("Type a valid number for your desired action:");
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] Exit");

            Scanner in = new Scanner(System.in);
            int number = in.nextInt();
            in.nextLine();

            if (number == 1)
            {
                System.out.println("Show updates for:");
                System.out.println("[0] All websites");
                for (int i = 0; i < rssReader.rssCount; ++i)
                    System.out.println("[" + (i + 1) + "] " + rssReader.websiteNames.get(i));
                System.out.println("Enter -1 to return.");
                int value = Integer.parseInt(in.nextLine());
                if (value == -1)
                    continue;
                rssReader.showUpdates(value - 1);
            }
            if (number == 2)
            {
                System.out.println("Please enter website URL to add:");
                String url = in.nextLine();
                try
                {
                    if (rssReader.addUrl(url))
                        System.out.printf("Added %s successfully.\n", url);
                    else System.out.printf("%s already exists.\n", url);
                }
                catch (Exception e)
                {
                    System.out.println("Error in adding url: " + e.getMessage());
                }
            }
            if (number == 3)
            {
                System.out.println("Please enter website URL to remove:");
                String url = in.nextLine();
                if (rssReader.removeUrl(url))
                    System.out.printf("Removed %s successfully.\n", url);
                else System.out.printf("Couldn't find %s.\n", url);
            }
            if (number == 4)
            {
                rssReader.saveData();
                break;
            }
        }
    }

    public RSSReader()
    {
        websiteNames = new ArrayList<String>();
        websiteUrls = new ArrayList<String>();
        rssUrls = new ArrayList<String>();
        rssCount = 0;
        loadData(DATA_FILE_PATH);
    }

    public boolean removeUrl(String url)
    {
        for (int i = 0; i < rssCount; ++i)
        {
            if (websiteUrls.get(i).equals(url))
            {
                websiteNames.remove(i);
                websiteUrls.remove(i);
                rssUrls.remove(i);
                rssCount -= 1;
                return true;
            }
        }
        return false;
    }

    public boolean addUrl(String url) throws Exception
    {
        String html = fetchPageSource(url);
        for (int i = 0; i < rssCount; ++i)
            if (websiteUrls.get(i).equals(url))
                return false;
        websiteNames.add(extractPageTitle(html));
        websiteUrls.add(url);
        String rssUrl = extractRssUrl(html);
        if (rssUrl.startsWith("/"))
            if (url.endsWith("/"))
                rssUrl = url.substring(0, url.length() - 1) + rssUrl;
            else
                rssUrl = url + rssUrl;
        rssUrls.add(rssUrl);
        rssCount += 1;
        return true;
    }

    public void showUpdates(int i) throws Exception
    {
        if (i == -1)
            for (int j = 0; j < rssCount; ++j)
            {
                System.out.println(websiteNames.get(j));
                retrieveRssContent(rssUrls.get(j));
            }
        else if (0 <= i && i < rssCount)
        {
            System.out.println(websiteNames.get(i));
            retrieveRssContent(rssUrls.get(i));
        }
        else
            throw new IndexOutOfBoundsException("Index " + (i + 1) + " is not valid!");
    }

    public void saveData()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(DATA_FILE_PATH)));
            for (int i = 0; i < rssCount; ++i)
                writer.write(websiteNames.get(i) + ";" + websiteUrls.get(i) + ";" + rssUrls.get(i) + "\n");
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
                String[] dataStringList = line.split(";");
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

    public static void retrieveRssContent(String rssUrl)
    {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String extractRssUrl(String html)
    {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        return doc.select("[type='application/rss+xml']").attr("href");
    }

    public static String fetchPageSource(String urlString) throws Exception
    {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    private static String toString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);

        return stringBuilder.toString();
    }
}