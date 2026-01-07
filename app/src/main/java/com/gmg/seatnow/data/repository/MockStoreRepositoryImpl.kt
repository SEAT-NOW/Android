package com.gmg.seatnow.data.repository

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.StoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class MockStoreRepositoryImpl @Inject constructor() : StoreRepository {

    override fun getStoresAround(centerLat: Double, centerLng: Double): Flow<List<Store>> = flow {
        // 실제 네트워크 딜레이 시뮬레이션 (0.5초)
        delay(500)

        val stores = mutableListOf<Store>()
        val random = Random(System.currentTimeMillis())
        // 술집/가게 이름 더미
        val dummyNames = listOf("낭만포차", "홍대광장", "별밤투어", "서울주막", "달빛포차", "청춘상회", "맛있는맥주", "SeatNow Pub", "감성타코", "치킨매니아")


        // 10개의 더미 데이터 생성
        for (i in 1..10) {
            // 현재 중심 좌표 기준으로 약 500m 반경 내 랜덤 좌표 생성
            // 위도 1도 ≈ 111km, 0.005도 ≈ 550m
            val randomLat = centerLat + (random.nextDouble() - 0.5) / 100
            val randomLng = centerLng + (random.nextDouble() - 0.5) / 100

            val randomStatus = StoreStatus.values().random()

            stores.add(
                Store(
                    id = i.toLong(),
                    name = "${dummyNames.random()} $i" + "호점",
                    latitude = randomLat,
                    longitude = randomLng,
                    status = randomStatus
                )
            )
        }

        emit(stores)
    }
}