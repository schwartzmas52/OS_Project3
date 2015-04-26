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
		String url = args[0];	//	stores command line arguments
		String 	savedURL = url,	//	stores url
				host,			//	domain
				directory,		//	directory within domain that contains html file
				webfile = "",	//	String to store html in
				title,			//	holds text within <title> to be printed
				text,			//	temporarily holds text within <p> to be printed
				img,			//	temporarily holds text within <img to be printed
				head;			//	temporarily holds text within <h(1,2)> to be printed
		ArrayList<String> imgArray = new ArrayList<String>();
		int port = 80,			//	default port
			textIndex,			//	first position in webfile that has <p>
			imgIndex,			//	first position in webfile that has <img src="
			headIndex;			//	first position in webfile that has <h(1,2)>
		boolean	textCont,		//	whether or not <p> is in webfile
				imgCont,		//	whether or not <img src=" is in webfile
				headCont,		//	whether or not <h(1,2)> is in webfile
				textRun,		//	controls if text should be parsed
				imgRun,			//	controls if text should be parsed
				headRun;		//	controls if text should be parsed
		if (url.contains("http://"))
		{
			url = url.substring(7);
		}
		if (url.contains(":"))	//	searches for ":" in url
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
		if (webfile != "404 Page Not Found")	//	only enters if not a 404 page
		{			
			if (webfile.contains("<title>"))	//	searches for website title
			{
				webfile = webfile.substring(webfile.indexOf("<title>") + 7);	//	removes first header tag from webfile
				title = webfile.substring(0, webfile.indexOf("</title>"));	//	puts everything from beginning of webfile to </title> in title
				title = title.replaceAll("    ", " ");	//	gets rid of large blanks
				title = title.replaceAll("   ", " ");
				System.out.println(title + "\n");	//	prints website title
			}
					
			while (webfile.contains("<p>") || webfile.contains("<img src=") || webfile.contains("<h"))	//	only enters if <p>, <img src=", or <h are in webfile
			{
				textIndex = imgIndex = headIndex = 0;	//	sets all variables to 0 so previous loops will not affect current loop
				textCont = imgCont = headCont = textRun = imgRun = headRun = false;
				
				if (webfile.contains("<p>"))	//	acknowledges that webfile contains <p> with textCont
				{
					textIndex = webfile.indexOf("<p>");
					textCont = true;
				}
				
				if (webfile.contains("<img src="))	//	acknowledges that webfile contains <img src=" with imgCont
				{
					imgIndex = webfile.indexOf("<img src=");
					imgCont = true;
				}
				
				if (webfile.contains("<h1"))	//	acknowledges that webfile contains <h with headCont
				{
					headIndex = webfile.indexOf("<h1");
					headCont = true;
				}
				//	figures out which text element to parse next
				if (textCont && imgCont && headCont)	//	checks if all three are in webfile
				{
					if ((textIndex < imgIndex) && (textIndex < headIndex))	//	if textIndex comes first, sets textRun to true
						textRun = true;
					else if ((imgIndex < textIndex) && (imgIndex < headIndex))	//	if imgIndex comes first, sets imgRun to true
						imgRun = true;
					else if ((headIndex < textIndex) && (headIndex < imgIndex))	//	if headIndex comes first, sets headRun to true
						headRun = true;
				}
				else if (textCont && imgCont)	//checks if <p> and <img src=" are in webfile
				{
					if (textIndex < imgIndex)	//	if textIndex comes first, sets textRun to true
						textRun = true;
					else						//	otherwise, sets imgRun to true
						imgRun = true;
				}
				else if (textCont && headCont)
				{
					if (textIndex < headIndex)	//	if textIndex comes first, sets textRun to true
						textRun = true;
					else
						headRun = true;
				}
				else if (headCont && imgCont)
				{
					if (headIndex < imgIndex)	//	if headIndex comes first, sets headRun to true
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
					text = webfile.substring(webfile.indexOf("<p>") + 3, webfile.indexOf("</p>"));	//	sets text equal to characters inside <p> and </p> within webfile
					text = text.replaceAll("<b>", "");	//	gets rid of tags useless to the command line
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
		for (int i = 0; i < imgArray.size(); i++)	//	continues while
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
			while ((line = br.readLine()) != null)	//	only loops while next line is not blank
			{
				if (line.contains("HTTP/1.1 404 Not Found"))	//	checks for 404 message
				{
					html = "404 Page Not Found";
					break;
				}
				if (line.contains("</body>"))	//	checks for end of body
				{
					html += line;
					break;
				}
				html += line;
			}
		}
		catch (SocketException e)	//	catches error reading file
		{
			System.out.println(e.getMessage() + "\n");
		}
		return html;
	}
}