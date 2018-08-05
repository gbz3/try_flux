package com.github.gbz3.try_flux;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.config.keys.ClientIdentityLoader;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.io.AbstractIoServiceFactoryFactory;
import org.apache.sshd.common.io.IoServiceFactory;
import org.apache.sshd.common.io.nio2.Nio2ServiceFactory;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class MyController {
	private static Log log = LogFactory.getLog( MyController.class );
	
	@RequestMapping( path = "/command/ssh", method = RequestMethod.POST )
	public Mono<String> ssh( @RequestBody RequestResource rr ) throws Exception {
		log.info( "received. rr=" + rr.getParams() );
		log.info( "env.host=" + System.getenv( "env.host" ) );
		
//		return ssh( rr.getParams() );
		return sshs( rr.getParams() );
	}
	
	private Mono<String> sshs( List<String> cmds ) {
    	final ExecutorService es = ThreadUtils.newCachedThreadPool( "fuga" );
		final String host = System.getenv( "env.host" );
		return Flux.just( host, host )
				.log()
				
    			.flatMapSequential( s -> sshClient( s, cmds, es ), 2 )
    			//.subscribeOn( Schedulers.newElastic( 30, new ThreadUtils.SshdThreadFactory( "fugo" ) ) )
    			//.subscribeOn( Schedulers.elastic() )
    			.subscribeOn( Schedulers.fromExecutorService( es ), true )
    			.collect( Collectors.joining() )
    			;
	}
	
	private Mono<String> sshClient( String host, List<String> cmds, ExecutorService es ) {
        try ( SshClient client = SshClient.setUpDefaultClient() ) {
        	client.setIoServiceFactoryFactory( new AbstractIoServiceFactoryFactory( es, false ) {

				@Override
				public IoServiceFactory create( FactoryManager manager ) {
					return new Nio2ServiceFactory( manager, getExecutorService(), isShutdownOnExit() );
				}
        		
        	});
    		final KeyPair kp = ClientIdentityLoader.DEFAULT.loadClientIdentity( System.getenv( "env.key" ), FilePasswordProvider.EMPTY );
        	client.addPublicKeyIdentity( kp );
        	client.start();
			log.info( "started." );

        	try {
    	    	ConnectFuture cf = client.connect( System.getenv( "env.user" ), host, Integer.parseInt( System.getenv( "env.port" ) ) );
    	    	cf.await( 2000L );
    	
    	    	try ( ClientSession session = cf.getSession() ) {
    	    		session.auth().verify( 2000L );
    	
    	    		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    	    		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    	    		try ( ClientChannel shell = session.createExecChannel( String.join( "; ", cmds ) ) ) {
    	       			shell.setOut( stdout );
    	       			shell.setErr( stderr );
    	       			shell.open().await();
    	    			shell.waitFor( Arrays.asList( ClientChannelEvent.CLOSED, ClientChannelEvent.EOF ), 0 );
    	
    	    			return Mono.just( "[" + stdout.toString() + "]\n[" + stderr.toString() + "]" );
    	    		} finally {
    	    			session.close();
    	    		}
    	    	}
        	} finally {
        		client.close();
        	}
        } catch ( Exception e) {
        	throw new RuntimeException( e );
        }
	}
	
	private Mono<String> ssh( List<String> cmds ) throws Exception {
        try ( SshClient client = SshClient.setUpDefaultClient() ) {
//        	final ExecutorService es = ThreadUtils.newFixedThreadPool( "hoge", 3 );
        	final ExecutorService es = ThreadUtils.newCachedThreadPool( "fuga" );
        	client.setIoServiceFactoryFactory( new AbstractIoServiceFactoryFactory( es, false ) {

				@Override
				public IoServiceFactory create( FactoryManager manager ) {
					return new Nio2ServiceFactory( manager, getExecutorService(), isShutdownOnExit() );
				}
        		
        	});
    		final KeyPair kp = ClientIdentityLoader.DEFAULT.loadClientIdentity( System.getenv( "env.key" ), FilePasswordProvider.EMPTY );
        	client.addPublicKeyIdentity( kp );
//        	client.start();
//			log.info( "started." );
        	
        	try {
        		final String host = System.getenv( "env.host" );
        		return Flux.just( host, host )
        				.log()
	        			.flatMap( s -> Mono.just( connectAndExec( client, s, cmds ) ) )
	        			.subscribeOn( Schedulers.fromExecutorService( es ), false )
	        			.collect( Collectors.joining() )
	        			;

//        		return Mono.just( connectAndExec( client, System.getenv( "env.host" ), cmds ) );
        	} finally {
        		client.close();
        	}
        }
	}
	
	private String connectAndExec( final SshClient client, String host, List<String> cmds ) {
		try {
			client.start();
			log.info( "host=" + host );
	    	ConnectFuture cf = client.connect( System.getenv( "env.user" ), host, Integer.parseInt( System.getenv( "env.port" ) ) );
	    	cf.await( 2000L );
	
	    	try ( ClientSession session = cf.getSession() ) {
	    		session.auth().verify( 2000L );
	
	    		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
	    		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
	    		try ( ClientChannel shell = session.createExecChannel( String.join( "; ", cmds ) ) ) {
	       			shell.setOut( stdout );
	       			shell.setErr( stderr );
	       			shell.open().await();
	    			shell.waitFor( Arrays.asList( ClientChannelEvent.CLOSED, ClientChannelEvent.EOF ), 0 );
	
	    			return "[" + stdout.toString() + "]\n[" + stderr.toString() + "]";
	    		} finally {
	    			session.close();
	    		}
	    	}
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	@RequestMapping( path = "/command/front", method = RequestMethod.POST )
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
				.bodyToMono( String.class );
				//.next();
	}

	@RequestMapping( path = "/command/rear", method = RequestMethod.GET )
	public String rear() throws Exception {
		log.info( "rear started." );
		Thread.sleep( 1000L );
		return "rear end.";
	}

}
