package sample;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.iggroup.api.positions.sprintmarkets.createSprintMarketPositionV1.CreateSprintMarketPositionV1Request;
import com.iggroup.api.positions.sprintmarkets.createSprintMarketPositionV1.CreateSprintMarketPositionV1Response;
import com.iggroup.api.positions.sprintmarkets.createSprintMarketPositionV1.Direction;
import com.iggroup.api.positions.sprintmarkets.createSprintMarketPositionV1.ExpiryPeriod;
import com.iggroup.api.positions.sprintmarkets.getSprintMarketPositionsV1.GetSprintMarketPositionsV1Response;
import com.iggroup.api.service.AuthenticationResponseAndConversationContext;
import com.iggroup.api.service.Constants;
import com.iggroup.api.service.ConversationContext;
import com.iggroup.api.session.createSessionV2.CreateSessionV2Request;
import com.iggroup.api.session.createSessionV2.CreateSessionV2Response;
import com.iggroup.api.streaming.HandyTableListenerAdapter;
import com.lightstreamer.ls_client.ConnectionInfo;
import com.lightstreamer.ls_client.ConnectionListener;
import com.lightstreamer.ls_client.ExtendedTableInfo;
import com.lightstreamer.ls_client.LSClient;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SubscribedTableKey;
import com.lightstreamer.ls_client.UpdateInfo;
import com.tictactec.ta.lib.Core;

public class ABCD {

	private static final Logger LOGGER = Logger.getLogger(ABCD.class);

	public static String SEC_30 = "30SEC";
	public static String MIN_1 = "1MIN";
	public static String ALL = "ALL";
	public static String STR_ENABLE = SEC_30;
	public static String UNIT = null;
	static RestTemplate restTemplate = new RestTemplate();

	private static final String CHART_CANDLE_PATTERN = "CHART:{epic}:{scale}";

	static String uri = "https://demo-api.ig.com/gateway/deal";

	static LSClient lsClient = null;

	public static String EPIC = "FM.D.EURUSD24.EURUSD24.IP";

	static Core core = new Core();

	static List<String> trades = new ArrayList<String>();

	static AuthenticationResponseAndConversationContext context;

	static DateTime lastTradedTime = null;

	final static Queue<Tick> queue30 = new ConcurrentLinkedQueue<Tick>();
	final static Queue<Tick> queue60 = new ConcurrentLinkedQueue<Tick>();

	private static OrderProcessingThread30 orderProcessingThread30 = null;

	private static OrderProcessingThread60 orderProcessingThread60 = null;

	static boolean isFirst = false;

	public static void main(String[] args) throws Exception {
		ABCD main = new ABCD();
		CreateSessionV2Request request = new CreateSessionV2Request();
		request.setIdentifier("zosman");
		request.setPassword("Waheed01");
		context = main.createSession(request, "84df164ace00c09cd39cafea900d1d8a214632e2");
		connect(context.getConversationContext(), context.getCreateSessionResponse().getCurrentAccountId(),
				context.getCreateSessionResponse().getLightstreamerEndpoint());
		if (!isFirst) {
			process(true);
			isFirst = true;
		} else {
			process(false);
		}

	}

	public static void disableThreads() {
		LOGGER.info("Disabling order processing threads");
		orderProcessingThread30.setEnable(false);
		orderProcessingThread30.setEnable(false);
	}

	public static void start() {
		String[] str = new String[10];
		try {
			ABCD.main(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void heinekenAshiCalculation() {
	}

	private static void disconnect() {
		lsClient.closeConnection();
	}

	private static void process(boolean isFirst) throws Exception {
		if (isFirst) {
			BlockingQueue<OHLC> ohlcQueue30 = new ArrayBlockingQueue<OHLC>(20);
			BlockingQueue<OHLC> ohlcQueue60 = new ArrayBlockingQueue<OHLC>(20);
			Object lock = new Object();
			orderProcessingThread30 = new OrderProcessingThread30(ohlcQueue30, lock);
			Thread workerThread30 = new WorkerThread30(queue30, ohlcQueue30);
			Thread workerThread60 = new WorkerThread60(queue60, ohlcQueue60);
			orderProcessingThread60 = new OrderProcessingThread60(ohlcQueue60, lock);
			if (STR_ENABLE.equalsIgnoreCase(SEC_30)) {
				orderProcessingThread30.setEnable(true);
			} else if (STR_ENABLE.equalsIgnoreCase(MIN_1)) {
				orderProcessingThread60.setEnable(true);
			} else if (STR_ENABLE.equalsIgnoreCase(ALL)) {
				orderProcessingThread30.setEnable(true);
				orderProcessingThread60.setEnable(true);
			}
			workerThread30.start();
			orderProcessingThread30.start();
			workerThread60.start();
			orderProcessingThread60.start();
			subscribe(queue30, queue60);
		} else {
			orderProcessingThread30.setEnable(true);
			orderProcessingThread60.setEnable(true);
		}
	}

	private static void subscribe(final Queue<Tick> queue30, final Queue<Tick> queue60) throws Exception {
		subscribeForChartCandles(EPIC, "SECOND", new HandyTableListenerAdapter() {
			@Override
			public void onUpdate(int i, String s, UpdateInfo updateInfo) {
				String newValue = updateInfo.getNewValue("UTM");
				DateTime dateTime = new DateTime(Long.valueOf(newValue),
						DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London")));
				double ofrOpen = Double.valueOf(updateInfo.getNewValue("OFR_OPEN"));
				double ofrHigh = Double.valueOf(updateInfo.getNewValue("OFR_HIGH"));
				double ofrLow = Double.valueOf(updateInfo.getNewValue("OFR_LOW"));
				double ofrClose = Double.valueOf(updateInfo.getNewValue("OFR_CLOSE"));
				double bidOpen = Double.valueOf(updateInfo.getNewValue("BID_OPEN"));
				double bidHigh = Double.valueOf(updateInfo.getNewValue("BID_HIGH"));
				double bidLow = Double.valueOf(updateInfo.getNewValue("BID_LOW"));
				double bidClose = Double.valueOf(updateInfo.getNewValue("BID_CLOSE"));
				Tick tick = new Tick(dateTime, (ofrOpen + bidOpen) / 2, (ofrHigh + bidHigh) / 2, (ofrLow + bidLow) / 2,
						(ofrClose + bidClose) / 2, null);
				LOGGER.info(tick);
				queue30.add(tick);
				queue60.add(tick);
			}

		});
	}

	public static String placeOrder(ConversationContext conversationContext, Direction direction) throws Exception {
		CreateSprintMarketPositionV1Request sprintMarketRequest = new CreateSprintMarketPositionV1Request();
		sprintMarketRequest.setEpic(EPIC);
		sprintMarketRequest.setDirection(direction);
		sprintMarketRequest.setExpiryPeriod(ExpiryPeriod.ONE_MINUTE);
		sprintMarketRequest.setSize(new BigDecimal(UNIT));
		CreateSprintMarketPositionV1Response response = createSprintMarketPositionV1(conversationContext,
				sprintMarketRequest);
		return response.getDealReference();
	}

	public static HandyTableListenerAdapter subscribeForChartCandles(String epic, String scale,
			HandyTableListenerAdapter adapter) throws Exception {
		String subscriptionKey = CHART_CANDLE_PATTERN.replace("{epic}", epic);
		subscriptionKey = subscriptionKey.replace("{scale}", scale);

		/*
		 * String[] fields = new String[]{"LTV", "LTV", "UTM", "DAY_OPEN_MID",
		 * "UTM", "DAY_OPEN_MID", "DAY_PERC_CHG_MID", "DAY_HIGH", "DAY_LOW",
		 * "OFR_OPEN", "OFR_HIGH", "OFR_LOW", "OFR_CLOSE", "BID_OPEN",
		 * "BID_HIGH", "BID_LOW", "BID_CLOSE", "LTP_OPEN", "LTP_HIGH",
		 * "LTP_LOW", "LTP_CLOSE", "CANDLE_START", "CANDLE_TICK_COUNT"};
		 */

		String[] fields = new String[] { "UTM", "OFR_OPEN", "OFR_HIGH", "OFR_LOW", "OFR_CLOSE", "BID_OPEN", "BID_HIGH",
				"BID_LOW", "BID_CLOSE" };

		ExtendedTableInfo extendedTableInfo = new ExtendedTableInfo(new String[] { subscriptionKey }, "MERGE", fields,
				true);

		final SubscribedTableKey subscribedTableKey = lsClient.subscribeTable(extendedTableInfo, adapter, false);
		adapter.setSubscribedTableKey(subscribedTableKey);
		return adapter;
	}

	private static HttpEntity<?> buildHttpEntity(ConversationContext conversationContext, Object request,
			String version) {
		HttpEntity<?> requestEntity;

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		if (conversationContext != null) {
			if (conversationContext.getAccountSecurityToken() != null) {
				requestHeaders.set(Constants.ACCOUNT_SSO_TOKEN_NAME, conversationContext.getAccountSecurityToken());
			}
			if (conversationContext.getClientSecurityToken() != null) {
				requestHeaders.set(Constants.CLIENT_SSO_TOKEN_NAME, conversationContext.getClientSecurityToken());
			}
			requestHeaders.set(Constants.APPLICATION_KEY, conversationContext.getApiKey());
		}
		if (StringUtils.isNotBlank(version)) {
			requestHeaders.set(Constants.VERSION, version);
		}

		if (request != null) {
			requestEntity = new HttpEntity<Object>(request, requestHeaders);
		} else {
			requestEntity = new HttpEntity<Object>(requestHeaders);
		}

		return requestEntity;
	}

	public AuthenticationResponseAndConversationContext createSession(CreateSessionV2Request authenticationRequest,
			String apiKey) {
		String serviceURL = uri + "/session";

		ConversationContext conversationContext = new ConversationContext(null, null, apiKey);
		final HttpEntity<?> httpEntity = buildHttpEntity(conversationContext, authenticationRequest, "2");
		ResponseEntity<CreateSessionV2Response> responseEntity = restTemplate.exchange(serviceURL, HttpMethod.POST,
				httpEntity, CreateSessionV2Response.class);
		return new AuthenticationResponseAndConversationContext(
				new ConversationContext(responseEntity.getHeaders().getFirst(Constants.CLIENT_SSO_TOKEN_NAME),
						responseEntity.getHeaders().getFirst(Constants.ACCOUNT_SSO_TOKEN_NAME), apiKey),
				responseEntity.getBody());
	}

	public static CreateSprintMarketPositionV1Response createSprintMarketPositionV1(
			ConversationContext conversationContext, CreateSprintMarketPositionV1Request request) throws Exception {
		String url = "/positions/sprintmarkets";
		HttpEntity<?> requestEntity = buildHttpEntity(conversationContext, request, "1");
		ResponseEntity<CreateSprintMarketPositionV1Response> response = restTemplate.exchange(uri + url,
				HttpMethod.POST, requestEntity, CreateSprintMarketPositionV1Response.class);
		return response.getBody();
	}

	public static GetSprintMarketPositionsV1Response checkSprintMarketPositionV1(
			ConversationContext conversationContext) throws Exception {
		String url = "/positions/sprintmarkets";
		HttpEntity<?> requestEntity = buildHttpEntity(conversationContext, null, "1");
		ResponseEntity<GetSprintMarketPositionsV1Response> response = restTemplate.exchange(uri + url, HttpMethod.GET,
				requestEntity, GetSprintMarketPositionsV1Response.class);
		return response.getBody();
	}

	private static void connect(ConversationContext conversationContext, String username, String lightstreamerEndpoint)
			throws PushConnException, PushServerException, PushUserException {
		lsClient = new LSClient();
		ConnectionInfo connectionInfo = new ConnectionInfo();
		connectionInfo.user = username;
		String password = "";
		if (conversationContext.getClientSecurityToken() != null
				&& !conversationContext.getAccountSecurityToken().isEmpty()) {
			password = "CST-" + conversationContext.getClientSecurityToken();
		}
		if (conversationContext.getClientSecurityToken() != null
				&& !conversationContext.getClientSecurityToken().isEmpty()
				&& conversationContext.getAccountSecurityToken() != null
				&& !conversationContext.getAccountSecurityToken().isEmpty()) {
			password += "|";
		}
		if (!conversationContext.getAccountSecurityToken().isEmpty()) {
			password += "XST-" + conversationContext.getAccountSecurityToken();
		}
		connectionInfo.password = password;
		connectionInfo.pushServerUrl = lightstreamerEndpoint;

		// final ConnectionListenerAdapter adapter = new
		// ConnectionListenerAdapter();

		lsClient.openConnection(connectionInfo, new ConnectionListener() {

			@Override
			public void onSessionStarted(boolean arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNewBytes(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFailure(PushConnException arg0) {

			}

			@Override
			public void onFailure(PushServerException arg0) {
				System.err.println("Network failure...");
				try {
					connect(context.getConversationContext(), context.getCreateSessionResponse().getCurrentAccountId(),
							context.getCreateSessionResponse().getLightstreamerEndpoint());
					subscribe(queue30, queue60);
				} catch (Exception e) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e1) {
					}
					this.onFailure(arg0);
				}

			}

			@Override
			public void onEnd(int arg0) {

			}

			@Override
			public void onDataError(PushServerException arg0) {

			}

			@Override
			public void onConnectionEstablished() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onClose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivityWarning(boolean arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	public static void trade(Direction direction) {

		if (lastTradedTime == null) {
			extracted(direction);
		} else if (DateTime.now().minus(25 * 1000).isBefore(lastTradedTime)) {
			extracted(direction);
		}
	}

	private static void extracted(Direction direction) {
		String tradeRef = "";
		try {
			tradeRef = placeOrder(context.getConversationContext(), direction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!StringUtils.isEmpty(tradeRef)) {
			LOGGER.info("Trade order placed with deal Reference  : " + tradeRef);
			trades.add(tradeRef);
		}
		lastTradedTime = DateTime.now();
	}
}
