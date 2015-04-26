import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WebBrowser 
{
	public static void main(String[] args) throws IOException
	{
		//String url = "http://www.december.com/html/demo/hello.html";
		//String url = "http://www.utdallas.edu/~ozbirn/image.html";
		//String url = "http://assets.climatecentral.org/images/uploads/news/Earth.jpg";
		String url = "http://htmldog.com/examples/images1.html";
		//String url = "http://portquiz.net:8080/";
		//String url = "http://www.utdallas.edu/os.html";
		ArrayList<String> imgArray = new ArrayList<String>();
		String host;
		String directory;
		String webfile = "";
		String title;
		String text;
		String img;
		int port = 80,
			textIndex,
			imgIndex;			
		boolean	textCont,
				imgCont,
				textRun,
				imgRun;
		if (url.contains("http://"))
		{
			url = url.substring(7);
		}
		if (url.contains(":"))
		{
			host = url.substring(0, url.indexOf(":"));
			port = Integer.parseInt(url.substring(url.indexOf(":") + 1, url.indexOf("/")));
			directory = url.substring(url.indexOf("/"));
		}
		else
		{
			host = url.substring(0, url.indexOf("/"));
			directory = url.substring(url.indexOf("/"));
		}
		Socket socket = new Socket(host, port);
		PrintWriter pw = new PrintWriter(socket.getOutputStream());
		pw.println("GET " + directory + " HTTP/1.1");
		pw.println("Host: " + host);
		pw.println("Accept: */*");
		pw.println("");
		pw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		webfile = getHTML(br);
		if (webfile != "404 Page Not Found")
		{			
			if (webfile.contains("<title>"))
			{
				webfile = webfile.substring(webfile.indexOf("<title>") + 7);
				title = webfile.substring(0, webfile.indexOf("</title>"));
				title = title.replaceAll("    ", " ");
				title = title.replaceAll("   ", " ");
				System.out.println(title + "\n");
			}
					
			while (webfile.contains("<p>") || webfile.contains("<img src="))
			{
				textIndex = imgIndex = 0;
				textCont = imgCont = textRun = imgRun = false;
				
				if (webfile.contains("<p>"))
				{
					textIndex = webfile.indexOf("<p>");
					textCont = true;
				}
				
				if (webfile.contains("<img src="))
				{
					imgIndex = webfile.indexOf("<img src=");
					imgCont = true;
				}
				
				if (textCont && imgCont)
				{
					if (textIndex < imgIndex)
						textRun = true;
					else
						imgRun = true;
				}
				else if (textCont)
					textRun = true;
				else if (imgCont)
					imgRun = true;
				
				if (textRun)
				{
					text = webfile.substring(webfile.indexOf("<p>") + 3, webfile.indexOf("</p>"));
					text = text.replaceAll("<b>", "");
					text = text.replaceAll("</b>", "");
					text = text.replaceAll("<i>", "");
					text = text.replaceAll("</i>", "");
					text = text.replaceAll("<br/>", "");
					text = text.replaceAll("<strong>", "");
					text = text.replaceAll("</strong>", "");
					text = text.replaceAll("    ", " ");
					text = text.replaceAll("   ", " ");
					text = text.replaceAll("<a href.*?>", "");
					text = text.replaceAll("</a>", "");
					text = text.replaceAll("alt=\".*?>", "");
					
					if (text.contains("<img src"))
					{
						System.out.print("Image: ");
						text = text.replaceAll("<img src=", "");
						text = text.replaceAll("\"", "");
					}
					
					webfile = webfile.substring(webfile.indexOf("</p>") + 4);
					System.out.println(text + "\n");
				}
				else if (imgRun)
				{
					webfile = webfile.substring(webfile.indexOf("<img src=") + 9);
					img = webfile.substring(0, webfile.indexOf(">"));
					img = img.replaceAll(" ", "");
					
					if (img.contains("alt=\""))
					{
						img = img.replaceAll("alt=\".*?\"", "");
					}
	
					img = img.replaceAll("\"", "");
					webfile = webfile.substring(webfile.indexOf(">") + 1);
					System.out.println("Image: " + img + "\n");
					imgArray.add(img);
				}
			}
			printImages(imgArray);
		}
		else
		{
			System.out.println(webfile);
		}
	}

	public static void printImages(ArrayList<String> imgArray) throws IOException
	{
		String host;
		String directory;
		int port = 80;
		for (int i = 0; i < imgArray.size(); i++)
		{
			String url = imgArray.get(i);
			int byteLine = 0;
			if (url.contains("http://"))
			{
				url = url.substring(7);
			}
			if (url.contains(":"))
			{
				host = url.substring(0, url.indexOf(":"));
				port = Integer.parseInt(url.substring(url.indexOf(":") + 1, url.indexOf("/")));
				directory = url.substring(url.indexOf("/"));
			}
			else
			{
				host = url.substring(0, url.indexOf("/"));
				directory = url.substring(url.indexOf("/"));
			}
			Socket socket = new Socket(host, port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println("GET " + directory + " HTTP/1.1");
			pw.println("Host: " + host);
			pw.println("Accept: */*");
			pw.println("");
			pw.flush();
			DataInputStream input = new DataInputStream(socket.getInputStream());
			while ((byteLine = input.read()) != -1)
			{
				
			}
		}
	}
	
	public static String getHTML(BufferedReader br) throws IOException
	{
		String html = "";
		String line = "";
		try
		{
			while ((line = br.readLine()) != null)
			{
				if (line.contains("HTTP/1.1 404 Not Found"))
				{
					html = "404 Page Not Found";
					break;
				}
				if (line.contains("</body>"))
				{
					html += line;
					break;
				}
				html += line;
			}
		}
		catch (SocketException e)
		{
			System.out.println(e.getMessage() + "\n");
		}
		return html;
	}
}