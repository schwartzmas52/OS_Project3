import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WebBrowser 
{
	public static void main(String[] args) throws IOException
	{
		String url = "http://www.december.com/html/demo/hello.html";
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
		//String output = getHTML(br);
		String t;
		
		while((t = br.readLine()) != null)
		{
			//System.out.println(t);
			webfile += t;
		}
		
		br.close();
		
		if (webfile.contains("<title>"))
		{
			webfile = webfile.substring(webfile.indexOf("<title>") + 7);
			title = webfile.substring(0, webfile.indexOf("</title>"));
			title = title.replaceAll("    ", " ");
			title = title.replaceAll("   ", " ");
			System.out.println(title + "\n");
		}
		
		//System.out.print(webfile);
		
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
				text = text.replaceAll("    ", " ");
				text = text.replaceAll("   ", " ");
				
				if (text.contains("<a href"))
				{
					text = text.replaceAll("<a href.*?>", "");
					text = text.replaceAll("</a>", "");
				}
				
				webfile = webfile.substring(webfile.indexOf("</p>") + 4);
				System.out.println(text + "\n");
			}
			else if (imgRun)
			{
				webfile = webfile.substring(webfile.indexOf("<img src=") + 9);
				img = webfile.substring(webfile.indexOf("<img src=") + 3, webfile.indexOf(">"));
				img = img.replaceAll("    ", " ");
				img = img.replaceAll("   ", " ");
				webfile = webfile.substring(webfile.indexOf(">") + 1);
				System.out.println(img + "\n");
			}
		}
	}
	
	public static String getHTML(BufferedReader br) throws IOException
	{
		String html = "";
		String line = "";
		while ((line = br.readLine()) != null)
		{
			if (line.contains("<title>"))
			{
				
			}
			html += "\n";
		}
		return html;
	}
}
