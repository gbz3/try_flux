package com.github.gbz3.try_flux;

import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class MyController {
	private static Log log = LogFactory.getLog( MyController.class );
	
	@RequestMapping( path = "/command/front", method = RequestMethod.GET )
	public Mono<String> front( @RequestBody RequestResource rr ) {
		log.info( "received. rr=" + rr.getParams() );
		
		return Flux.fromIterable( rr.getParams() )
//		.takeUntil( s -> "remote".equals( s ) )		// 最初の "remote" で Stream 終了
//		.skipLast( 1 )
		.map( s -> "☆" + s + "☆" )
		.flatMap( s -> requestToRear() )
		.collect( Collectors.joining( " ### " ) );
	}
	
	private Mono<String> requestToRear() {
		return WebClient.create( "http://localhost:8080" )
				.get().uri( "/command/rear" )
				.retrieve()
				.bodyToFlux( String.class )
				.next();
	}

	@RequestMapping( path = "/command/rear", method = RequestMethod.GET )
	public String rear() throws Exception {
		log.info( "rear started." );
		Thread.sleep( 1000L );
		return "rear end.";
	}

}
