package org.usfirst.frc.team1294.vision;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MJpegStreamerVerticle extends AbstractVerticle {

  private static final String BOUNDARY = "--mjpegframe";
  private static final String CRLF = "\r\n";

  @Override
  public void start() throws Exception {
    final Router router = Router.router(vertx);
    router.get().handler(this::handle);

    final HttpServer httpServer = vertx.createHttpServer();
    httpServer.requestHandler(router::accept);
    httpServer.listen(8080);
  }

  private void handle(RoutingContext routingContext) {
    final HttpServerResponse response = routingContext.response();
    response.setChunked(true);
    response.setStatusCode(200);
    response.headers().add("Cache-Control", "no-cache");
    response.headers().add("Cache-Control", "private");
    response.headers().add("Pragma", "no-cache");
    response.headers().add("Content-Type", "multipart/x-mixed-replace; boundary=" + BOUNDARY);

    vertx.eventBus().consumer("images", handler -> {
      final Buffer imageBuffer = (Buffer)handler.body();
      response.write(BOUNDARY);
      response.write(CRLF);
      response.write("Content-type: image/jpeg");
      response.write(CRLF);
      response.write("Content-Length: " + imageBuffer.length());
      response.write(CRLF);
      response.write(CRLF);
      response.write(imageBuffer);
    });
  }
}
