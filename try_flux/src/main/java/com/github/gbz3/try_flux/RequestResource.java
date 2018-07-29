package com.github.gbz3.try_flux;

import java.io.Serializable;
import java.util.List;

public class RequestResource implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<String> params;
	
	public void setParams( List<String> p ) {
		params = p;
	}
	
	public List<String> getParams() {
		return params;
	}

}
