package com.example.todoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.todoapi.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling // コンテナ起動時に @Scheduled が付いたメソッドを自動発見して、バックグラウンドで定期実行
@ConfigurationPropertiesScan // プロパティスキャンを有効化
public class TodoApiApplication {

	public static void main(String[] args) {
		// ApplicationContext（IoC コンテナ）を生成
		// IoCコンテナ：立ち上がった Spring の世界そのもの（Bean を管理し、依存を注入する）
		SpringApplication.run(TodoApiApplication.class, args);
	}

}
