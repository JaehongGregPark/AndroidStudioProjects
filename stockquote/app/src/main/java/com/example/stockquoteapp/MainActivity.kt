package com.example.stockquoteapp

// Activity 기본 클래스
import android.os.Bundle

// Compose Activity
import androidx.activity.ComponentActivity

// Compose UI 설정
import androidx.activity.compose.setContent

// ViewModel delegate
import androidx.activity.viewModels

// 메인 UI 화면
import com.example.stockquoteapp.ui.StockQuoteScreen

// ViewModel
import com.example.stockquoteapp.StockQuoteViewModel


// 앱 테마
import com.example.stockquoteapp.ui.theme.StockQuoteAppTheme

/**
 * 앱 진입 Activity
 * - Compose UI 사용
 * - ViewModel 연결
 * - StockQuoteScreen 표시
 */
class MainActivity : ComponentActivity() {

    /**
     * Activity scoped ViewModel
     * viewModels() delegate가 자동으로 생성/생명주기 관리
     *
     * 특징:
     * - Activity 재생성 시 유지됨
     * - Compose에서 상태 관리
     * - MVVM 구조
     */
    private val viewModel: StockQuoteViewModel by viewModels()

    /**
     * Activity 생성 시 호출
     * Compose UI 설정
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Compose UI 시작 지점
         * 기존 setContentView() 대신 사용
         */
        setContent {

            /**
             * 앱 전체 테마 적용
             * colors / typography / shapes 정의
             */
            StockQuoteAppTheme {

                /**
                 * 메인 화면 Composable
                 * ViewModel 전달
                 */
                StockQuoteScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}