package comnet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Browser extends Frame implements ActionListener, WindowListener
{
	public JFrame frame;
	
	public JPanel BrowsePanel;
	public JTextField BrowseTextField;
	public JButton BrowseButton;
	
	public JPanel ResultPanel;
	 
	public Browser() throws IOException
	{	
		//검색 판넬
		BrowsePanel = new JPanel();
		
		//검색 창
		BrowseTextField = new JTextField(30);
		
		//검색 버튼
		BrowseButton = new JButton("Browse");
		BrowseButton.addActionListener(this);
	
		//검색 판넬 구성
		BrowsePanel.add(BrowseTextField);
		BrowsePanel.add(BrowseButton);
		BrowsePanel.setBackground(new Color(180,211,211));
		
		//검색 결과 스크롤
		ResultPanel = new JPanel();
		ResultPanel.setPreferredSize(new Dimension(650, 150));
	
		//윈도우 이벤트
		addWindowListener(this);
		
		//프레임
		frame = new JFrame("COMNET_BROWSER");
		frame.setBounds(300, 300, 800, 800);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width/2) - 400, (dim.height/2) - 400);
		frame.setVisible(true);
		
		//프레임 구성
		frame.add(BrowsePanel, BorderLayout.NORTH);
		frame.add(ResultPanel, BorderLayout.CENTER);
	}
	
	public static void main(String[] args) throws Exception
	{
		//브라우저 프레임 생성
		new Browser();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		System.exit(0);
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String name = e.getActionCommand();
		
		if(name.equals("Browse"))
		{
			try 
			{
				SendHttpGetRequestMsg();
			} 
			catch (Exception e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	public void SendHttpGetRequestMsg() throws Exception
	{
		//검색창으로부터 URL 획득
    	URL obj = new URL(BrowseTextField.getText());
    	String host = obj.getHost();
    	String path = (obj.getPath()!="") ? obj.getPath() : "/";
    	
        /*와어어샤크 때 사용하던 DNS와 포트 80번 (Deprecated : HTTP용)
        Socket clientSocket = new Socket(host, 80); */
    	
    	// SSL 소켓 팩토리 생성
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket clientSocket = (SSLSocket) factory.createSocket(host, 443);
    	
        //Chapter02 마지막에 있는 Java 예제
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 

        //간단하게 HTTP GET Request 메시지 (파싱한 URL을 바탕으로, 봇 감지를 피하기 위해 user-agent 추가)
        String HTTPGETRequestMsg = "GET "+path+" HTTP/1.1\r\n"
            + "Host: "+host+"\r\n"
            + "Accept-Language: ko,en,q=0.9,en-US,q=0.8\r\n"
            + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\r\n"
            + "\r\n";

        //소켓을 통해서 HTTP GET Request 메시지 전송
        outToServer.writeBytes(HTTPGETRequestMsg);
        
        String imgURL = "";
        String title = "";
        while(true)
        {
            String line = inFromServer.readLine();

            //이미지
            if(line.contains("jpg") && imgURL.isBlank())
            {
            	int start = line.lastIndexOf("content=");          	
            	String temp = line.substring(start + 9);
            	int end = temp.indexOf("jpg\"");
            	imgURL = temp.substring(0, end + 3);
            	System.out.println(imgURL);
            }
            
            //이미지
            if(line.contains("png") && imgURL.isBlank())
            {
            	int start = line.lastIndexOf("content=");          	
            	String temp = line.substring(start + 9);
            	int end = temp.indexOf("png\"");
            	imgURL = temp.substring(0, end + 3);
            	System.out.println(imgURL);
            }
            
            //타이틀
            if(line.contains("<title>") && title.isBlank())
            {
            	int start = line.indexOf("<title>");          	
            	String temp = line.substring(start + 7);
            	int end = temp.indexOf("</title>");
            	title = temp.substring(0, end);
            	System.out.println(title);
            }
            
            //메시지 종료
            if(line.contains("</html>") || line.contains("</body>"))
            	break;
        }
        
        //검색 결과 추가
        AddBrowsingResultPanel(title, imgURL);

        //소켓 종료
        clientSocket.close();
	}
	
	public void AddBrowsingResultPanel(String title, String imgURL) throws Exception
	{
		//검색 결과 판넬
		JPanel panel = new JPanel();
		
		//검색 결과 이미지
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(100, 100));
		
		//검색 결과 텍스트
		JTextArea textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(500, 100));
        
		//검색 결과 판넬에 이미지와 텍스트 추가
  		panel.add(label);
  		panel.add(textArea);
  		
  		//검색 결과 텍스트 업데이트
  		textArea.setText(title);
        
		//검색 결과 이미지를 (100, 100)사이즈로 조정하고 업데이트
        URL url = new URL(imgURL);
		ImageIcon originalImage = new ImageIcon(ImageIO.read(url));
		Image ximg = originalImage.getImage();
		Image yimg = ximg.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledImage = new ImageIcon(yimg); 
		label.setIcon(scaledImage);
		
		//검색 결과를 토대로 타이틀과 이미지로 구성된 판넬을 생성하여 결과 판넬에 추가한다
		ResultPanel.add(panel);
		ResultPanel.revalidate();
	}
}
