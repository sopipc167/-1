package comnet;

import java.awt.*;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Browser extends Frame implements ActionListener, WindowListener
{
    public Panel panel_Browsing;
    public TextField textField_Browse;
    public Button button_Browse;
    public TextArea textArea_Result;

    public Browser()
    {
        super("COMNET_BROWSER");

        //검색 판넬
        panel_Browsing = new Panel();

        //검색 창
        textField_Browse = new TextField(30);

        //검색 버튼
        button_Browse = new Button("Browse");
        button_Browse.addActionListener(this);

        //검색 결과
        textArea_Result = new TextArea();

        add(panel_Browsing, BorderLayout.NORTH);
        add(textArea_Result, BorderLayout.CENTER);
        panel_Browsing.add(textField_Browse);
        panel_Browsing.add(button_Browse);

        //윈도우 이벤트
        addWindowListener(this);

        //프레임 설정
        setBounds(300, 300, 500, 500);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width/2)-(getWidth()/2), (dim.height/2)-(getHeight()/2));
        setBackground(new Color(180,211,211));
        setVisible(true);
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
        if (e.getSource() == button_Browse) {
            // Clear previous results
            textArea_Result.setText("");

            // Split the input by commas
            String[] urls = textField_Browse.getText().split(",");
            for (String url : urls) {
                try {
                    // Trim whitespace and send HTTP request for each URL
                    SendHttpGetRequestMsg(url.trim());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    textArea_Result.append("Failed to send HTTP request for: " + url.trim() + "\n");
                }
            }
        }
    }



    public void SendHttpGetRequestMsg(String url) throws Exception
    {
        //검색창으로부터 URL 획득
        URL obj = new URL(url);
        String host = obj.getHost();
        String path = (obj.getPath() != "") ? obj.getPath() : "/";
        String protocol = obj.getProtocol();

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

        // HTTP 요청 메시지 보내기
        outToServer.writeBytes(HTTPGETRequestMsg);


        // HTTP 응답 메시지 받기
        StringBuilder HTTPGETResponseMsgBuilder = new StringBuilder();
        String line;
        while((line = inFromServer.readLine()) != null) {
            HTTPGETResponseMsgBuilder.append(line).append("\n");
            if(line.trim().isEmpty()) { // 응답 헤더와 본문 사이의 빈 줄 확인
                break; // 헤더만 읽고 중단
            }
        }

        //검색 결과 출력
       // textArea_Result.setText(HTTPGETResponseMsg);

        // 추가된 결과가 있으면 TextArea에 추가
        textArea_Result.append("URL: " + url + "\n");
        textArea_Result.append(HTTPGETResponseMsgBuilder.toString());
        textArea_Result.append("\n----------------------------------------\n");


        //소켓 종료
        clientSocket.close();
    }
}