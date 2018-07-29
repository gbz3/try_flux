package com.github.gbz3.try_flux;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
	private static Log log = LogFactory.getLog( MyController.class );
	
	@RequestMapping( path = "/command/front", method = RequestMethod.GET )
	public String front( @RequestBody RequestResource rr ) {
		log.info( "received. rr=" + rr.getParams() );
		return "front end.";
	}

	@RequestMapping( path = "/command/rear", method = RequestMethod.GET )
	public String rear() throws Exception {
		Thread.sleep( 1000L );
		return "rear end.";
	}

}
