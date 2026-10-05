package it.valvorobica.thip.base.portal.rs;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.json.JSONObject;

import com.thera.thermfw.rs.BaseResource;

/**
 *
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 02/10/2026
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72676    02/10/2026  DSSOF3   Prima stesura
 */

@Path("/customersPortal")
public class CustomersPortalResource extends BaseResource {
	
	public static CustomersPortalService service = CustomersPortalService.getInstance();

	@PermitAll
	@GET
	@Path("/capparioSusa")
	public Response listaCappario(@QueryParam("query") String query,
			@QueryParam("limit") Integer limit) {
		JSONObject result = service.capparioSusa(query, limit);
		return buildResponse((StatusType) result.get("status"),result.get("response"));
	}
	
}
