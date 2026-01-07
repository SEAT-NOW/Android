package com.gmg.seatnow.data.repository

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class MockMapRepositoryImpl @Inject constructor() : MapRepository {

    // minPerson 파라미터 추가
    override fun getStores(minPerson: Int, centerLat: Double, centerLng: Double): Flow<List<Store>> = flow {
        delay(500)

        // TODO: 나중에 실제 API 연동 시 minPerson 값을 서버로 보냅니다.
        // if (minPerson == 0) -> 홈(전체)
        // if (minPerson > 0) -> 필터링

        val stores = mutableListOf<Store>()
        val random = Random(System.currentTimeMillis())
        val dummyNames = listOf("낭만포차", "홍대광장", "별밤투어", "서울주막", "달빛포차", "청춘상회", "맛있는맥주", "SeatNow Pub", "감성타코", "치킨매니아")

        for (i in 1..10) {
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