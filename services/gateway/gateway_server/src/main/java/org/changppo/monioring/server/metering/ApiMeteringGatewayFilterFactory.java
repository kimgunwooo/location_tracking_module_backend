package org.changppo.monioring.server.metering;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.changppo.monioring.server.apikey.ApiKey;
import org.changppo.monioring.domain.ApiMeteringEventPayLoad;
import org.changppo.monioring.server.exception.ErrorCode;
import org.changppo.monioring.server.exception.ErrorResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Slf4j
public class ApiMeteringGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiMeteringGatewayFilterFactory.Config> {
    private final ApiMeteringEventPublisher apiMeteringEventPublisher;

    public ApiMeteringGatewayFilterFactory(ApiMeteringEventPublisher apiMeteringEventPublisher) {
        super(Config.class);
        this.apiMeteringEventPublisher = apiMeteringEventPublisher;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Object attribute = exchange.getAttribute(ApiKey.class.getName());
            if (attribute == null) {
                // api key 가 없는 경우
                return responseError(exchange, ErrorCode.API_KEY_NOT_FOUND);
            }
            if (!(attribute instanceof ApiKey)) {
                //should never happen
                log.error("Invalid ApiKey attribute: {}", attribute);
                return responseError(exchange, ErrorCode.ILLEGAL_STATE);
            }
            var apiKeyContext = (ApiKey) attribute;

            // redis stream 을 통해 호출 정보를 발생시킨다
            String routeId = config.getRouteId();
            ApiMeteringEventPayLoad apiMeteringEventPayLoad = createFromApiKey(apiKeyContext, routeId);

            return apiMeteringEventPublisher.publish(apiMeteringEventPayLoad)
                    .onErrorResume(throwable -> {
                        // 카프카 장애로 인해 서비스 전체가 죽지 않도록, 그냥 통과 시킨다.
                        log.error("Failed to publish ApiMeteringEventPayLoad: {}", apiMeteringEventPayLoad, throwable);
                        return Mono.empty();
                    })
                    .then(chain.filter(exchange));
        };
    }
    protected ApiMeteringEventPayLoad createFromApiKey(ApiKey apiKey, String routeId) {
        String eventId = UUID.randomUUID().toString();
        Long apiKeyId = apiKey.id();
        Long memberId = apiKey.memberId();
        return new ApiMeteringEventPayLoad(eventId, memberId, apiKeyId, routeId);
    }



    protected static Mono<Void> responseError(ServerWebExchange exchange, ErrorCode errorCode) {
        // 에러 응답을 반환한다.
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        exchange.getResponse().setStatusCode(errorCode.getHttpStatus());
        Mono<DataBuffer> bufferMono = Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.toJsonString().getBytes()));
        return exchange.getResponse().writeWith(bufferMono);
    }

    @Getter @Setter
    public static class Config implements HasRouteId {
        private String routeId;
    }

}