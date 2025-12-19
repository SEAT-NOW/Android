package com.gmg.seatnow.presentation.component

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// Bridge는 기존과 동일
class SeatNowWebViewBridge(
    private val onAddressSelected: (String, String) -> Unit
) {
    @JavascriptInterface
    fun processDATA(zoneCode: String, roadAddress: String) {
        onAddressSelected(zoneCode, roadAddress)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PostcodeScreen(
    onBackClick: () -> Unit,
    onAddressSelected: (zoneCode: String, roadAddress: String) -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "주소 검색",
                onBackClick = onBackClick,
                topMargin = 15.dp
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // [수정 1] 하드웨어 가속 사용 (Software layer 제거)
                    // setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null) <- 삭제됨

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        javaScriptCanOpenWindowsAutomatically = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    addJavascriptInterface(
                        SeatNowWebViewBridge(onAddressSelected),
                        "AndroidBridge"
                    )

                    // [수정 2] 웹뷰 콘솔 로그를 안드로이드 로그켓(Logcat)에서 확인하기 위한 설정
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        // 1. 페이지 로딩 시작 시 로그
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewDebug", "Page Started Loading: $url")
                        }

                        // 2. 페이지 로딩 완료 시 로그
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("WebViewDebug", "Page Finished Loading: $url")
                        }

                        // 3. 네트워크 에러 발생 시 로그 (구형 API)
                        @Deprecated("Deprecated in Java")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Log.e("WebViewDebug", "Error: $errorCode, Desc: $description")
                        }

                        // 4. HTTP 에러 발생 시 로그 (404, 500 등)
                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            errorResponse: android.webkit.WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            Log.e("WebViewDebug", "Http Error: ${errorResponse?.statusCode}")
                        }

                        // 5. SSL 인증서 에러 (HTTPS 문제)
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: android.webkit.SslErrorHandler?,
                            error: android.net.http.SslError?
                        ) {
                            super.onReceivedSslError(view, handler, error)
                            Log.e("WebViewDebug", "SSL Error: ${error?.primaryError}")
                            // 개발 중 테스트를 위해 SSL 에러 무시하려면 아래 주석 해제 (배포 시엔 위험)
                            // handler?.proceed()
                        }
                    }

                    loadDataWithBaseURL(
                        "https://postcode.map.daum.net/",
                        getDaumPostcodeHtml(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}

private fun getDaumPostcodeHtml(): String {
    return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    overflow: hidden; /* 스크롤바 방지 */
                }
                #layer {
                    width: 100%;
                    height: 100%;
                    border: 0px solid; /* 테두리 제거 */
                    margin: 0;
                    position: absolute; /* 위치 고정 */
                    top: 0;
                    left: 0;
                }
            </style>
        </head>
        <body>
            <div id="layer"></div>
            
            <script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
            <script>
                // 디버깅을 위해 콘솔 로그 추가
                console.log("JS: Script Loaded");

                window.onload = function() {
                    console.log("JS: Window Loaded");
                    var element_layer = document.getElementById('layer');
                    
                    if (!element_layer) {
                        console.error("JS: Layer element not found");
                        return;
                    }

                    new daum.Postcode({
                        oncomplete: function(data) {
                            var fullRoadAddr = data.roadAddress;
                            var extraRoadAddr = '';

                            if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                                extraRoadAddr += data.bname;
                            }
                            if(data.buildingName !== '' && data.apartment === 'Y'){
                               extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                            }
                            if(extraRoadAddr !== ''){
                                extraRoadAddr = ' (' + extraRoadAddr + ')';
                            }
                            if(fullRoadAddr !== ''){
                                fullRoadAddr += extraRoadAddr;
                            }

                            window.AndroidBridge.processDATA(data.zonecode, fullRoadAddr);
                        },
                        width : '100%',
                        height : '100%'
                    }).embed(element_layer);
                    
                    console.log("JS: Postcode Embedded");
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}