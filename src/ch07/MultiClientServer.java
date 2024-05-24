package ch07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MultiClientServer {

	private static final int PORT = 5000;
	// 하나의 변수에 자원을 통으로 관리하기 기법 -> 자료 구조
	// 자료 구조 --> 코드 단일, 멀티 스레드
	// 객체 배열 <-- Vector<> : 멀티스레드에 안정적이다
	private static Vector<PrintWriter> clientWriters = new Vector<>();

	public static void main(String[] args) {
		System.out.println("Server started...");

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				Socket socket = serverSocket.accept();
				new ClientHandler(socket).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end of main

	// 정적 내부 클래스 설계
	private static class ClientHandler extends Thread {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		// 스레드 start() 호출시 동작되는 메서드
		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				// 코드 추가
				// 클라이언트로부터 이름 받기(약속되어 있음 !)
				String nameMessage = in.readLine();
				if (nameMessage != null && nameMessage.startsWith("NAME:")) {
					String clientName = nameMessage.substring(5);
					broadcastMessage("해당 서버에 : " + clientName + " 님 입장");
				} else {
					// 약속과 다르게 접근했다면 종료 처리
					socket.close();
					return;
				}

				// 여기서 중요 ! - 서버가 관리하는 자료구조에 자원 저장
				clientWriters.add(out);

				String message;
				while ((message = in.readLine()) != null) {
					System.out.println("Received : " + message);

					// 약속
					// : 기준	으로 처리
					// MSG:안녕\n
					String[] parts = message.split(":", 2);
					System.out.println("parts 인덱스 갯수 : " + parts.length);
					// 명령 부분을 분리
					String command = parts[0];
					// 데이터 부분을 분리
					String data = parts.length > 1 ? parts[1] : "";

					if (command.equals("MSG")) {
						System.out.println("연결된 전체 사용자에게 MSG 방송");
						broadcastMessage(message);
					} else if (command.equals("BYE")) {
						System.out.println("Client disconnected ...");
						break;
					}
				}
			} catch (Exception e) {
//				e.printStackTrace();
			} finally {
				try {
					socket.close();
					// 도전 과제 !!
					// 서버측에서 관리하고 있는 PrintWriter 제거 해야한다.
					// 인덱스 번호가 필요하다.
					// clientWriters.add() 할 때 지정된 나의 인덱스 번호가 필요
					// clientWriters.remove();
					System.out.println("..... 클라이언트 연결 해제 .....");
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
	} // end of ClientHandler

	// 모든 클라이언트에게 메세지 보내기 - 브로드캐스트
	private static void broadcastMessage(String message) {
		for (PrintWriter writer : clientWriters) {
			writer.println(message);
		}
	}
}
