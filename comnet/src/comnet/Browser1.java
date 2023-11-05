package comnet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Browser1 extends Frame implements ActionListener, WindowListener {
	public JFrame frame;

	public JPanel BrowsePanel;
	public JTextField BrowseTextField;
	public JButton BrowseButton;

	public JPanel ResultPanel;

	// 검색결과들 저장할 리스트 정의
	public LinkedList<ResultImage> searchedResultImages = new LinkedList<>();

	// 브라우저 생성
	public Browser1() throws IOException {

		// 검색 판넬
		BrowsePanel = new JPanel();

		// 검색 창
		BrowseTextField = new JTextField(30);

		// 검색 버튼
		BrowseButton = new JButton("Browse");
		BrowseButton.addActionListener(this);

		// 검색 판넬 구성
		BrowsePanel.add(BrowseTextField);
		BrowsePanel.add(BrowseButton);
		BrowsePanel.setBackground(new Color(180, 211, 211));

		// 검색 결과 스크롤
		ResultPanel = new JPanel();
		ResultPanel.setPreferredSize(new Dimension(650, 650));

		// 윈도우 이벤트
		addWindowListener(this);

		// 프레임
		frame = new JFrame("COMNET_BROWSER");
		frame.setBounds(300, 300, 800, 800);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width / 2) - 400, (dim.height / 2) - 400);
		frame.setVisible(true);

		// 프레임 구성
		frame.add(BrowsePanel, BorderLayout.NORTH);
		frame.add(ResultPanel, BorderLayout.CENTER);
		frame.revalidate();
	}

	public static void main(String[] args) throws Exception {
		new Browser1();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String name = e.getActionCommand();

		if (name.equals("Browse")) {
			try {
				SendHttpGetRequestMsg();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public String ImageColorHEXExtractor(URL imageUrl) throws MalformedURLException {
		try {

			// 이미지 파일 로드
			BufferedImage image = ImageIO.read(imageUrl.openStream());

			int width = image.getWidth();
			int height = image.getHeight();
			long totalRed = 0;
			long totalGreen = 0;
			long totalBlue = 0;

			// 모든 픽셀을 반복하면서 색상 값 누적
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = image.getRGB(x, y);
					int red = (rgb >> 16) & 0xFF;
					int green = (rgb >> 8) & 0xFF;
					int blue = rgb & 0xFF;

					totalRed += red;
					totalGreen += green;
					totalBlue += blue;
				}
			}

			// 이미지의 평균 색상 계산
			long totalPixels = width * height;
			int avrRed = (int) (totalRed / totalPixels);
			int avrGreen = (int) (totalGreen / totalPixels);
			int avrBlue = (int) (totalBlue / totalPixels);
			String avrHex = String.format("%02X%02X%02X", avrRed, avrGreen, avrBlue).toUpperCase();
			return avrHex;

		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void SendHttpGetRequestMsg() {
		// 검색창으로부터 URL 획득
		String urlText = BrowseTextField.getText().trim();

		try {
			URL obj = new URL(urlText);
			String host = obj.getHost();
			String path = (obj.getPath() != "") ? obj.getPath() : "/";

			// SSL 소켓 팩토리 생성
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket clientSocket = (SSLSocket) factory.createSocket(host, 443);

			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// 간단한 HTTP GET Request 메시지 (파싱한 URL을 바탕으로, 봇 감지를 피하기 위해 user-agent 추가)
			String HTTPGETRequestMsg = "GET " + path + " HTTP/1.1\r\n" + "Host: " + host + "\r\n"
					+ "Accept-Language: ko,en;q=0.9,en-US;q=0.8\r\n"
					+ "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\r\n"
					+ "\r\n";

			// 소켓을 통해서 HTTP GET Request 메시지 전송
			outToServer.writeBytes(HTTPGETRequestMsg);

			// 한 페이지에서 최대 5개까지 이미지 출력
			List<String> imageURLs = new ArrayList<>();
			String title = "";
			int count = 0;
			while (true) {
				String imgURL = "";

				String line = inFromServer.readLine();

				// 이미지
				if (line.contains(".jpg") || line.contains(".png")) {
					int start = line.indexOf("src=\"");
					if (start != -1) {
						int end = line.indexOf("\"", start + 5);
						imgURL = "https:" + line.substring(start + 5, end);
						System.out.println(imgURL);
						imageURLs.add(imgURL);
						count++;
					}
				}

				// 타이틀
				if (line.contains("<title>") && title.isBlank()) {
					int start = line.indexOf("<title>");
					String temp = line.substring(start + 7);
					int end = temp.indexOf("</title>");
					title = temp.substring(0, end);
					System.out.println(title);
				}

				// 메시지 종료
				if (line.contains("</html>") || line.contains("</body>") || count > 5) {
					break;
				}
			}

			// 검색 결과 추가
			for (int i = 0; i < imageURLs.size(); i++) {
				AddBrowsingResultPanel(title, imageURLs.get(i));
			}

			// 소켓 종료
			clientSocket.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void AddBrowsingResultPanel(String title, String imgURL) throws Exception {
		// 검색 결과 판넬
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// 검색 결과 이미지
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(150, 150));

		// 검색 결과 텍스트
		JTextArea textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(150, 50));
		textArea.setAutoscrolls(true);

		// 검색 결과 판넬에 이미지와 텍스트 추가
		panel.add(label, BorderLayout.PAGE_START);
		panel.add(textArea, BorderLayout.PAGE_END);

		// 검색 결과 텍스트 업데이트
		try {
			// 링크 이미지에서 평균색상 추출
			String HEX = ImageColorHEXExtractor(new URL(imgURL));
			title += "\n";
			title += "#" + HEX;
			textArea.setText(title);

			// 검색결과 이미지 객체로 리스트에 저장
			ResultImage result = new ResultImage(title, imgURL, HEX);
			
			// 순서 정렬해서 리스트에 객체 저장
			insertSortedResult(result);

			// 검색 결과 이미지를 (150, 150) 크기로 조정하고 업데이트
			ImageIcon originalImage = new ImageIcon(ImageIO.read(new URL(imgURL)));
			Image ximg = originalImage.getImage();
			Image yimg = ximg.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
			ImageIcon scaledImage = new ImageIcon(yimg);
			label.setIcon(scaledImage);

			// 검색 결과를 토대로 타이틀과 이미지로 구성된 판넬을 생성하여 결과 판넬에 추가
			ResultPanel.add(panel);
			updateResultPanel();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public String toOrderString(int n) {
		switch (n) {
		case 0:
			return "1st < ";
		case 1:
			return "2nd < ";
		case 2:
			return "3rd < ";
		case 3:
			return "4th < ";
		case 4:
			return "5th < ";
		default:
			return "";
		}
	}

	public void updateResultPanel() {
		// 검색 결과 패널 초기화
		ResultPanel.removeAll();

		Iterator<ResultImage> iterator = searchedResultImages.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			ResultImage result = iterator.next();
			String title = toOrderString(i) + result.getTitle();
			i++;

			try {
				URL url = new URL(result.getImgUrl());
				title += "\n";
				title += "#" + ImageColorHEXExtractor(url);

				// 검색 결과 패널 생성 및 구성
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());

				JLabel label = new JLabel();
				label.setPreferredSize(new Dimension(150, 150));

				JTextArea textArea = new JTextArea();
				textArea.setPreferredSize(new Dimension(150, 50));
				textArea.setAutoscrolls(true);
				textArea.setText(title);

				ImageIcon originalImage = new ImageIcon(ImageIO.read(url));
				Image ximg = originalImage.getImage();
				Image yimg = ximg.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
				ImageIcon scaledImage = new ImageIcon(yimg);
				label.setIcon(scaledImage);

				panel.add(label, BorderLayout.PAGE_START);
				panel.add(textArea, BorderLayout.PAGE_END);
				ResultPanel.add(panel);

				// 반복자를 사용하여 현재 요소를 제거
				iterator.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 결과 패널 다시 그리기
		ResultPanel.revalidate();
	}

	public void insertSortedResult(ResultImage result) {
		// 검색결과 저장된 리스트 대상으로
		for (int i = 0; i < searchedResultImages.size(); i++) {
			// hex코드값 비교해서 순서대로 삽입
			ResultImage existingResult = searchedResultImages.get(i);
			if (result.getHEXcode().compareTo(existingResult.getHEXcode()) < 0) {
				searchedResultImages.add(i, result);
				return;
			}
		}
		searchedResultImages.add(result);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

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
}
