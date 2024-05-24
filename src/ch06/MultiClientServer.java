package ch06;

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
				// 1. serverSocket.accept(); 호출하면 블로킹 상태가 된다. 멈춰있음
				// 2. 클라이언트가 연결 요청하면 새로운 소켓 객체 생성
				// 3. 새로운 스레드를 만들어서 처리 (클라이언트와 데이터를 주고 받기 위한 스레드)
				// 4. 새로운 클라이언트가 접속하기까지 다시 대기 (반복)
				Socket socket = serverSocket.accept();
				// 새로운 클라이언트가 연결되면 새로운 스레드가 생성된다
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

				// 여기서 중요 ! - 서버가 관리하는 자료구조에 자원 저장
				clientWriters.add(out);

				String message;
				while ((message = in.readLine()) != null) {
					System.out.println("Received : " + message);
					broadcastMessage(message);
				}
			} catch (Exception e) {
//				e.printStackTrace();
			} finally {
				try {
					socket.close();
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
