package com.github.gbz3.try_flux;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RunWith( SpringRunner.class )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT  )
public class MyControllerTest {
	
	@LocalServerPort
	private int port;
	
	@Test
	public void testSsh() throws Exception {
		// setup
		final WebClient cli = WebClient.builder()
				.baseUrl( "http://localhost:" + port + "/" )
				.defaultHeader( HttpHeaders.CONTENT_TYPE, "application/json" )
				.build();
		
		// do
		final RequestResource rr = new RequestResource();
		rr.setParams( Arrays.asList( "sleep 1", "hostname" ) );
		final String res = cli.post()
				.uri( "/command/ssh" )
				.body( Mono.just( rr ), RequestResource.class )
				.retrieve()
				.bodyToMono( String.class )
				.block();
		
		// test
		assertThat( res ).isEqualTo( "[tk2-237-28036.vs.sakura.ne.jp\n]\n[][tk2-237-28036.vs.sakura.ne.jp\n]\n[]" );
		
	}

}
