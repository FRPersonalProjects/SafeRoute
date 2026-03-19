package com.saferoute.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Startup {

	public static void main(String[] args) {
		SpringApplication.run(Startup.class, args);
	}

}

// ponto unico de entrada de todas as requisicoes, tudo chega nele e ele
// encaminha para os outros microsservicos, ele é o unico que tem acesso
// externo, os outros microsservicos so tem acesso interno, isso é uma boa
// pratica de seguranca, pois os microsservicos ficam protegidos de ataques
// externos, e o api gateway pode fazer a validacao das requisicoes antes de
// encaminhar para os microsservicos, ele pode fazer a autenticacao,
// autorizacao, rate limiting, etc.
