package com.github.gbz3.try_flux;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@RunWith( SpringRunner.class )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT  )
@AutoConfigureWebTestClient( timeout = "10000" )
public class MyControllerTest {
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private WebTestClient test;
	
	@Test
	public void testSsh() throws Exception {
		
		// do
		final RequestResource rr = new RequestResource();
		rr.setParams( Arrays.asList( "sleep 1", "hostname" ) );
		final WebTestClient.ResponseSpec spec = test.post().uri( "/command/ssh" )
			.contentType( MediaType.APPLICATION_JSON_UTF8 )
			.accept( MediaType.TEXT_PLAIN )
			.body( Mono.just( rr ), RequestResource.class )
			.exchange();

		// test
		spec.expectStatus().isOk();
		spec.expectHeader().contentTypeCompatibleWith( MediaType.TEXT_PLAIN_VALUE );
		assertThat( new String( spec.expectBody().returnResult().getResponseBodyContent() ) ).isEqualTo( "[tk2-237-28036.vs.sakura.ne.jp\n]\n[][tk2-237-28036.vs.sakura.ne.jp\n]\n[]" );
		
	}

}
