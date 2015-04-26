import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
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
		String 	savedURL = url,
				host,
				directory,
				webfile = "",
				title,
				text,
				img,
				head;
		ArrayList<String> imgArray = new ArrayList<String>();
		int port = 80,
			textIndex,
			imgIndex,
			headIndex;
		boolean	textCont,
				imgCont,
				headCont,
				textRun,
				imgRun,
				headRun;
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
				textIndex = imgIndex = headIndex = 0;
				textCont = imgCont = headCont = textRun = imgRun = headRun = false;
				
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
				
				if (webfile.contains("<h1"))
				{
					headIndex = webfile.indexOf("<h1");
					headCont = true;
				}
				
				if (textCont && imgCont && headCont)
				{
					if ((textIndex < imgIndex) && (textIndex < headIndex))
						textRun = true;
					else if ((imgIndex < textIndex) && (imgIndex < headIndex))
						imgRun = true;
					else if ((headIndex < textIndex) && (headIndex < imgIndex))
						headRun = true;
				}
				else if (textCont && imgCont)
				{
					if (textIndex < imgIndex)
						textRun = true;
					else
						imgRun = true;
				}
				else if (textCont && headCont)
				{
					if (textIndex < headIndex)
						textRun = true;
					else
						headRun = true;
				}
				else if (headCont && imgCont)
				{
					if (headIndex < imgIndex)
						headRun = true;
					else
						imgRun = true;
				}
				else if (textCont)
					textRun = true;
				else if (imgCont)
					imgRun = true;
				else if (headCont)
					headRun = true;
				
				if (textRun)
				{
					text = webfile.substring(webfile.indexOf("<p>") + 3, webfile.indexOf("</p>"));
					text = text.replaceAll("<b>", "");
					text = text.replaceAll("</b>", "");
					text = text.replaceAll("<br/>", "\n");
					text = text.replaceAll("<i>", "");
					text = text.replaceAll("</i>", "");
					text = text.replaceAll("<strong>", "");
					text = text.replaceAll("</strong>", "");
					text = text.replaceAll("    ", " ");
					text = text.replaceAll("   ", " ");
					text = text.replaceAll("<a href.*?>", "");
					text = text.replaceAll("</a>", "");
					text = text.replaceAll("alt=\".*?>", "");
					
					if (text.contains("<img src"))
					{
						text = text.replaceAll("<img src=", "");
						text = text.replaceAll("\"", "");
						text = text.replaceAll("/>", "");
					}
					
					webfile = webfile.substring(webfile.indexOf("</p>") + 4);
					System.out.println(text + "\n");
				}
				else if (imgRun)
				{
					webfile = webfile.substring(webfile.indexOf("<img src=") + 9);
					img = webfile.substring(0, webfile.indexOf(">"));
					img = img.replaceAll(" ", "");
					img = img.replaceAll("alt=\".*?\"", "");
					img = img.replaceAll("\"", "");
					webfile = webfile.substring(webfile.indexOf(">") + 1);
					System.out.println("Image: " + img + "\n");
					imgArray.add(img);
				}
				else if (headRun)
				{
					head = webfile.substring(webfile.indexOf("<h1") + 4, webfile.indexOf("</h1"));
					webfile = webfile.substring(webfile.indexOf("</h1") + 5);
					System.out.println(head + "\n");
				}
			}
			printImages(imgArray, savedURL, port);
		}
		else
		{
			System.out.println(webfile);
		}
	}

	public static void printImages(ArrayList<String> imgArray, String originalURL, int originPort) throws IOException
	{
		int[] intArray = new int[65536];
		int count = 0;
		int byteLine;
		boolean header = true;
		String host;
		String directory;
		int port = originPort;
		for (int i = 0; i < imgArray.size(); i++)
		{
			String url = imgArray.get(i);
			if (url.contains("http://"))
			{
				url = url.substring(7);
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
			}
			else
			{
				host = originalURL.substring(7);
				host = host.substring(0, host.indexOf("/"));
				directory = originalURL.substring(7);
				directory = directory.substring(directory.indexOf("/"), directory.lastIndexOf("/") + 1);
				directory += url;
			}
			Socket socket = new Socket(host, port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println("GET " + directory + " HTTP/1.1");
			pw.println("Connection: close");
			pw.println("Host: " + host);
			pw.println("Accept: */*");
			pw.println("");
			pw.flush();
			DataInputStream input = new DataInputStream(socket.getInputStream());
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			while ((byteLine = input.read()) != -1)
			{
				if (header) 
				{
					intArray[count] = byteLine;
					if (count >= 4) 
					{
						if (intArray[count-3] == 13 && intArray[count-2] == 10 && intArray[count-1] == 13 && intArray[count] == 10) 
						{
							header = false;
						}
					}
					count++;
				} 
				else 
				{
					byteStream.write(byteLine);
				}
			}
			byte[] byteArray = byteStream.toByteArray();
			FileOutputStream writer = new FileOutputStream(new File(directory.substring(directory.lastIndexOf("/") + 1)));
	        writer.write(byteArray);
	        writer.flush();
	        writer.close();
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