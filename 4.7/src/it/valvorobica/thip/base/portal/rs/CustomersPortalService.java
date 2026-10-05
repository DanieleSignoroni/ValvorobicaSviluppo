package it.valvorobica.thip.base.portal.rs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thera.thermfw.base.Trace;
import com.thera.thermfw.persist.ConnectionManager;
import com.thera.thermfw.persist.Factory;
import com.thera.thermfw.rs.errors.ErrorUtils;

import it.valvorobica.thip.susa.CapparioSusa;
import it.valvorobica.thip.susa.CapparioSusaTM;

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

public class CustomersPortalService {
	private static final int DEFAULT_RESULT_LIMIT = 20;
	private static final int MAX_RESULT_LIMIT = 50;
	private static final int MIN_QUERY_LENGTH = 2;

	private static CustomersPortalService instance = null;

	public static CustomersPortalService getInstance() {
		if(instance == null) {
			instance = (CustomersPortalService) Factory.createObject(CustomersPortalService.class);
		}
		return instance;
	}

	public JSONObject toJSON(CapparioSusa cappario) {
		JSONObject data = new JSONObject();

		data.put("idProvincia", cappario.getIdProvincia());
		data.put("localita", cappario.getLocalita());
		data.put("CAP", cappario.getCap());

		return data;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject capparioSusa(String query, Integer requestedLimit) {
		JSONObject response = new JSONObject();
		Status status = Status.OK;
		List errors = new ArrayList();
		JSONObject data = new JSONObject();
		JSONArray provinceAsJSON = new JSONArray();

		try {
			String normalizedQuery = query == null ? "" : query.trim();
			int resultLimit = requestedLimit == null ? DEFAULT_RESULT_LIMIT
					: Math.max(1, Math.min(requestedLimit.intValue(), MAX_RESULT_LIMIT));

			// Non restituiamo l'intero cappario (circa 15.000 righe): l'autocomplete
			// interroga il server soltanto quando l'utente ha digitato almeno 2 caratteri.
			if (normalizedQuery.length() >= MIN_QUERY_LENGTH) {
				String safeQuery = normalizedQuery.replace("'", "''");
				safeQuery = safeQuery.toUpperCase();
				String where = " (" +
						ConnectionManager.getCurrentDatabase().getCallToUppercaseFn(CapparioSusaTM.ID_PROVINCIA) + " LIKE '%" + safeQuery + "%' OR " +
						ConnectionManager.getCurrentDatabase().getCallToUppercaseFn(CapparioSusaTM.LOCALITA) + " LIKE '%" + safeQuery + "%' OR " +
						ConnectionManager.getCurrentDatabase().getCallToUppercaseFn(CapparioSusaTM.CAP) + " LIKE '%" + safeQuery + "%') ";
				Vector province = CapparioSusa.retrieveList(where,
						CapparioSusaTM.LOCALITA + " ASC, " + CapparioSusaTM.CAP + " ASC", false);
				for (Iterator iterator = province.iterator(); iterator.hasNext()
						&& provinceAsJSON.length() < resultLimit;) {
					CapparioSusa cappario = (CapparioSusa) iterator.next();

					provinceAsJSON.put(toJSON(cappario));
				}
			}

			data.put("cappario", provinceAsJSON);

		} catch (Exception e) {
			e.printStackTrace(Trace.excStream);
		} 

		data.put("errors", ErrorUtils.getInstance().toJSON(errors));

		response.put("response", data);

		response.put("status", status);

		return response;
	}

}
