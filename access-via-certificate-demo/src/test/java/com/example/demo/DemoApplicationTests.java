package com.example.demo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DemoApplicationTests
{
	@Autowired OkHttpClient client;

	/**
	 * Beispiel zur Nutzung des OkHttpClients, um das Schema abzuholen
	 *
	 * Die Anmeldung geschieht automatisch und wird automatisch wiederholt, sollte die Session abgelaufen sein.
	 *
	 * Wenn mit mehreren Mandanten gearbeitet wird, muss der Mandant bei jedem Aufruf gesetzt werden, damit der Cookie-Manager
	 * die passende Session auswählen kann.
	 */
	@Test
	void requestSchemaTest() throws Exception
	{
		Response response = this.client.newCall(new Request.Builder().get()
						.url("http://localhost:30080/api/admin/schema")
						.addHeader("X-ID-TENANT-NAME", "yuuvistest")
						.build()).execute();

		String schema = response.body().string();
		System.out.println(schema);
	}
}
