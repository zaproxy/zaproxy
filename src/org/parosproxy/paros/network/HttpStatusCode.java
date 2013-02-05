/*
 * Created on Jun 14, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.network;

public final class HttpStatusCode {
	
	// informational codes
	public static final int	CONTINUE						= 100;
	public static final int	SWITCHING_Protocols				= 101;

	// success codes
	public static final int	OK								= 200;
	public static final int	CREATED							= 201;
	public static final int	ACCEPTED						= 202;
	public static final int	NON_AUTHORITATIVE_INFORMATION	= 203;
	public static final int	NO_CONTENT						= 204;
	public static final int	RESET_CONTENT					= 205;
	public static final int	PARTIAL_CONTENT					= 206;

	// Redirection
	public static final int	MULTIPLE_CHOICES				= 300;
	public static final int	MOVED_PERMANENTLY				= 301;
	public static final int	FOUND							= 302;
	public static final int	SEE_OTHER						= 303;
	public static final int	NOT_MODIFIED					= 304;
	public static final int	USE_PROXY						= 305;
	public static final int	TEMPORARY_REDIRECT				= 306;

	// Client error
	public static final int	BAD_REQUEST						= 400;
	public static final int	UNAUTHORIZED					= 401;
	public static final int	PAYMENT_REQUIRED				= 402;
	public static final int	FORBIDDEN						= 403;
	public static final int	NOT_FOUND						= 404;
	public static final int	METHOD_NOT_ALLOWED				= 405;
	public static final int	NOT_ACCEPTABLE					= 406;
	public static final int	PROXY_AUTHENTICATION_REQUIRED	= 407;
	public static final int	REQUEST_TIME_OUT				= 408;
	public static final int	CONFLICT						= 409;
	public static final int	GONE							= 410;
	public static final int	LENGTH_REQUIRED					= 411;
	public static final int	PRECONDITION_FAILED				= 412;
	public static final int	REQUEST_ENTITY_TOO_LARGE		= 413;
	public static final int	REQUEST_URI_TOO_LARGE			= 414;
	public static final int	UNSUPPORTED_MEDIA_TYPE			= 415;
	public static final int	REQUESTED_RANGE_NOT_SATISFIABLE	= 416;
	public static final int	EXPECTATION_FAILED				= 417;

	// Server error
	public static final int	INTERNAL_SERVER_ERROR			= 500;
	public static final int	NOT_IMPLEMENTED					= 501;
	public static final int	BAD_GATEWAY						= 502;
	public static final int	SERVICE_UNAVAILABLE				= 503;
	public static final int	GATEWAY_TIEMOUT					= 504;
	public static final int	HTTP_VERSION_NOT_SUPPORTED		= 505;

	// ZAP: Added code array
	public static final int [] CODES = {CONTINUE, SWITCHING_Protocols,
		OK, CREATED, ACCEPTED, NON_AUTHORITATIVE_INFORMATION, NO_CONTENT, RESET_CONTENT, 
		PARTIAL_CONTENT, MULTIPLE_CHOICES, MOVED_PERMANENTLY, FOUND, SEE_OTHER, 
		NOT_MODIFIED, USE_PROXY, TEMPORARY_REDIRECT, BAD_REQUEST, UNAUTHORIZED, 
		PAYMENT_REQUIRED, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED, NOT_ACCEPTABLE, 
		PROXY_AUTHENTICATION_REQUIRED, REQUEST_TIME_OUT, CONFLICT, GONE, LENGTH_REQUIRED, 
		PRECONDITION_FAILED, REQUEST_ENTITY_TOO_LARGE, REQUEST_URI_TOO_LARGE, 
		UNSUPPORTED_MEDIA_TYPE, REQUESTED_RANGE_NOT_SATISFIABLE, EXPECTATION_FAILED, 
		INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE, 
		GATEWAY_TIEMOUT, HTTP_VERSION_NOT_SUPPORTED};
	
	/**
	 * Check if a status code is informational.  (100 <= status < 200)
	 * @param statusCode
	 * @return true if informational
	 */
	public static boolean isInformatinal(int statusCode) {
		if (statusCode >= 100 && statusCode < 200)
			return true;
		else
			return false;
	}


	/**
	 * Check if a HTTP status code is successful.  (200 <= status < 300)
	 * @param statusCode
	 * @return
	 */
	public static boolean isSuccess(int statusCode) {
		if (statusCode >= 200 && statusCode < 300)
			return true;
		else
			return false;
	}

	/**
	 * Check if a HTTP status code is redirection.  (300 <= status < 400)
	 * @param statusCode
	 * @return
	 */
	public static boolean isRedirection(int statusCode) {
		if (statusCode >= 300 && statusCode < 400) {
			return true;
        }
        
		return false;
	}

	/**
	 * Check if a HTTP status code is client error (400 <= status <500)
	 * @param statusCode
	 * @return
	 */
	public static boolean isClientError(int statusCode) {
		if (statusCode >= 400 && statusCode < 500)
			return true;
		else
			return false;
	}

	/**
	 * Check if a HTTP status code is server error (500 <= status)
	 * @param statusCode
	 * @return
	 */
	public static boolean isServerError(int statusCode) {
		if (statusCode >= 500)
			return true;
		else
			return false;
	}

}
