package ch04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer {

	public static void main(String[] args) {

		System.out.println("=== 서버 실행 ===");
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(5000);
			socket = serverSocket.accept();
			System.out.println("포트 번호 - 5000 할당 완료");

			// 클라이언트로 데이터를 받을 입력 스트림 필요
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// 클라이언트에게 데이터를 보낼 출력 스트림 필요
			PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);

			// 서버측 - 키보드 입력받을 입력 스트림 필요
			BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));

			// 멀티스레딩 개념의 확장
			// 클라이언트로 받는 데이터를 읽는 스레드
			Thread readThread = new Thread(() -> {
				try {
					String clientMesaage;
					while ((clientMesaage = socketReader.readLine()) != null) {
						System.out.println("서버측 콘솔 : " + clientMesaage);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			// 클라이언트에게 데이터를 보내는 스레드
			Thread writeThread = new Thread(() -> {
				try {
					String serverMessage;
					while ((serverMessage = keyboardReader.readLine()) != null) {
						// 1. 먼저 키보드를 통해서 데이터를 읽고
						// 2. 출력 스트림을 활용해서 데이터를 보내야 한다.
						socketWriter.println(serverMessage);
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			});

			// 스레드 동작 -> start() 호출
			readThread.start();
			writeThread.start();

			// join() 메서드는 하나의 스레드가 종료될 때 까지 기다리도록 하는 기능
			readThread.join();
			writeThread.join();

			System.out.println("--- 서버 프로그램 종료 ---");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
