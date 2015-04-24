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
		int port = 80;
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
//		String output = getHTML(br);
//		String t;
//		while((t = br.readLine()) != null) {System.out.println(t);}
//		br.close();
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
