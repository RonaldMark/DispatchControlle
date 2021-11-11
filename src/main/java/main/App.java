package main;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import controller.ServiceController;
import org.json.JSONException;
import org.json.JSONObject;
import service.BatteryService;
import utils.Logging;
import utils.Prop;

/**
 *
 * @author ronald.langat
 */
public class App extends AbstractVerticle {

    public static Prop props;
    public static Logging logger;
    public static String LOGS_PATH;
    public static String DATABASE_DRIVER;
    public static String DATABASE_IP;
    public static String DATABASE_PORT;
    public static String DATABASE_NAME;
    public static String DATABASE_USER;
    public static String DATABASE_PASSWORD;
    public static String DATABASE_SERVER_TIME_ZONE;
    public static String SYSTEM_PORT;
    public static String SYSTEM_HOST;

    static {
        props = new Prop();
        logger = new Logging();
        LOGS_PATH = "";
        DATABASE_DRIVER = "";
        DATABASE_IP = "";
        DATABASE_PORT = "";
        DATABASE_NAME = "";
        DATABASE_USER = "";
        DATABASE_PASSWORD = "";
        DATABASE_SERVER_TIME_ZONE = "";
        SYSTEM_PORT = "";
        SYSTEM_HOST = "";
    }

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        props = new Prop();
        logger = new Logging();

        // Get properties from property file
        LOGS_PATH = props.getLogsPath();
        DATABASE_DRIVER = props.getDATABASE_DRIVER();
        DATABASE_IP = props.getDATABASE_IP();
        DATABASE_PORT = props.getDATABASE_PORT();
        DATABASE_NAME = props.getDATABASE_NAME();
        DATABASE_USER = props.getDATABASE_USER();
        DATABASE_PASSWORD = props.getDATABASE_PASSWORD();
        DATABASE_SERVER_TIME_ZONE = props.getDATABASE_SERVER_TIME_ZONE();
        SYSTEM_PORT = props.getSYSTEM_PORT();
        SYSTEM_HOST = props.getSYSTEM_HOST();

        //verticle deployment options
        DeploymentOptions options = new DeploymentOptions()
                .setInstances(1)
                .setWorkerPoolName("musala-drone-api")
                .setWorker(true)
                .setWorkerPoolSize(20)
                .setHa(true);

        // deploy verticles
        vertx.deployVerticle(App.class.getName(), options);
        vertx.deployVerticle(ServiceController.class.getName(), options);
        vertx.deployVerticle(ServiceController.class.getName(), options);

        // periodic task to check drones battery levels and create history/audit event log 
        vertx.setPeriodic(25000, id -> {
            BatteryService bs = new BatteryService();
            bs.checkDronesBatteryLevels();
        });
    }

    @Override
    public void start(Future<Void> start) {
        System.out.println("App DeploymentID: " + vertx.getOrCreateContext().deploymentID());

        EventBus eventBus = vertx.eventBus();
        HttpServer httpServer;
        httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.post("/musala/drone/request").handler(rtc -> {

            HttpServerResponse response = rtc.response();
            response.headers()
                    .add("Content-Type", "application/json")
                    .add("Access-Control-Allow-Origin", "*")
                    .add("Access-Control-Allow-Headers", "*")
                    .add("Access-Control-Allow-Methods", "*")
                    .add("Access-Control-Allow-Credentials", "true");

            String method = rtc.request().rawMethod();
            rtc.request().bodyHandler(bodyHandler -> {
                String body = bodyHandler.toString();
                JSONObject responseOBJ = new JSONObject();
                if ("POST".equalsIgnoreCase(method)) {
                    JSONObject data = new JSONObject(body);
                    logger.applicationLog(logger.logPreString() + " Request from channel - " + data + "\n\n", "", 1);
                    try {
                        DeliveryOptions deliveryOptions = new DeliveryOptions()
                                .setSendTimeout(20000);

                        String serviceCode = data.getString("service_code");

                        eventBus.send(serviceCode, data.toString(), deliveryOptions, sendToBus -> {
                            if (sendToBus.succeeded()) {
                                JSONObject resobject = new JSONObject(sendToBus.result().body().toString());
                                logger.applicationLog(logger.logPreString() + " Response to channel - " + resobject + "\n\n", "", 2);
                                //send response
                                response.end(resobject.toString());
                            } else {
                                // error
                                responseOBJ.put("response_code", "555")
                                        .put("response", serviceCode + " failed")
                                        .put("error_data", sendToBus.cause().getLocalizedMessage());
                                response.end(responseOBJ.toString());
                            }
                        });
                    } catch (JSONException ex) {
                        logger.applicationLog(logger.logPreString() + " Response to channel - " + ex.getMessage() + "\n\n", "", 3);
                        responseOBJ.put("response_code", "555")
                                .put("response", "error occured || exception");
                        response.end(responseOBJ.toString());
                    }
                } else {
                    // wrong request method
                    responseOBJ.put("response_code", "555")
                            .put("response", "Bad Request");
                    response.end(responseOBJ.toString());
                }
            });
        });

        httpServer.requestHandler(router).listen(Integer.parseInt(SYSTEM_PORT), resp -> {
            if (resp.succeeded()) {
                System.out.println("System listening at " + SYSTEM_HOST + ":" + SYSTEM_PORT);
            } else {
                System.out.println("System failed to start !!" + resp.failed());
            }
        });
    }
}
