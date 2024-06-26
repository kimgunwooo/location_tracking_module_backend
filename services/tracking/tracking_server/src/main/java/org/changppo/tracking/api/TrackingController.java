
package org.changppo.tracking.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.changppo.commons.ResponseBody;
import org.changppo.commons.SuccessResponseBody;
import org.changppo.tracking.aop.TrackingContextParam;
import org.changppo.tracking.api.request.GenerateTokenRequest;
import org.changppo.tracking.api.request.StartTrackingRequest;
import org.changppo.tracking.api.request.TrackingRequest;
import org.changppo.tracking.api.response.GenerateTokenResponse;
import org.changppo.tracking.api.response.TrackingResponse;
import org.changppo.tracking.domain.TrackingContext;
import org.changppo.tracking.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tracking/v1")
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * 토큰 생성 API
     * 해당 정보를 저장한 token 을 반환 200
     * @param request ConnectRequest
     * @return response ConnectResponse
     * TODO : 토큰 헤더의 키 값은 상의 필요
     */
    @PostMapping("/generate-token")
    public ResponseEntity<ResponseBody<GenerateTokenResponse>> generateToken(@RequestHeader("APIKEY") String apiKeyToken,
                                                                             @RequestBody @Valid GenerateTokenRequest request) {
        GenerateTokenResponse response = trackingService.generateToken(apiKeyToken, request);

        return ResponseEntity.ok().body(new SuccessResponseBody<>(response));
    }

    /**
     * 배달 시작시 정적인 정보 받아오기
     * 성공 시 200
     * 이미 사용자가 존재하면 409
     * @param request StartTrackingRequest
     * @param context TrackingContext
     * @return void
     */
    @TrackingContextParam
    @PostMapping("/start")
    public ResponseEntity<ResponseBody<Void>> startTracking(@RequestBody @Valid StartTrackingRequest request,
                                                  TrackingContext context) {
        trackingService.startTracking(request, context);

        return ResponseEntity.ok().body(new SuccessResponseBody<>());
    }

    /**
     * Tracking API
     * 200
     * 존재 하지 않는 tracking 404
     * 종료 처리된 tracking 400
     * @param request TrackingRequest
     * @param context TrackingContext
     * @return TODO 반환처리
     */
    @TrackingContextParam
    @PostMapping("/tracking")
    public ResponseEntity<ResponseBody<Void>> tracking(@RequestBody @Valid TrackingRequest request,
                                             TrackingContext context) {
        trackingService.tracking(request, context);

        return ResponseEntity.ok().body(new SuccessResponseBody<>());
    }

    /**
     * tracking 종료 API
     * 존재 하지 않는 tracking 404
     * 종료 처리된 tracking 400
     * @param context TrackingContext
     * @return TODO 반환처리
     */
    @TrackingContextParam
    @GetMapping("/end")
    public ResponseEntity<ResponseBody<Void>> end(TrackingContext context) {
        trackingService.endTracking(context);

        return ResponseEntity.ok().body(new SuccessResponseBody<>());
    }

    /**
     * tracking 요청 API - 가장 최근 좌표 1개를 반환
     * 존재 하지 않는 tracking 404
     * 종료 처리된 tracking 400
     * @param context TrackingContext
     * @return TrackingResponse
     */
    @TrackingContextParam
    @GetMapping("/tracking")
    public ResponseEntity<ResponseBody<TrackingResponse>> getTracking(TrackingContext context) {
        TrackingResponse response = trackingService.getTracking(context);

        return ResponseEntity.ok().body(new SuccessResponseBody<>(response));
    }


    /**
     * TODO : 해당 배달원의 모든 정보 반환
     * 필수 X
     */

    /**
     * TODO : 추가 실시간성이 필요한 API 에 대해서 연결 요청할 API (추후 생각)
     * 토큰에 대해 권한을 다르게 주고, 권한이 다른 토큰을 부여해야 할듯
     * 고민 : API 를 따로 구성해야할지 or 권한 정보를 헤더나 바디로 받아서 처리해야할지
     * API 를 구성하거나, 권한 정보를 헤더로 넣으면 API GATEWAY 에서 거르기 쉬울듯?
     */
}