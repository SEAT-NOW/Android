package com.gmg.seatnow.presentation.owner.signup

import com.gmg.seatnow.domain.model.AccountInfo
import com.gmg.seatnow.domain.model.BusinessInfo
import com.gmg.seatnow.domain.model.LayoutInfo
import com.gmg.seatnow.domain.model.OperatingHoursInfo
import com.gmg.seatnow.domain.model.OperationInfo
import com.gmg.seatnow.domain.model.OwnerSignUpInfo
import com.gmg.seatnow.domain.model.RegularHolidayInfo
import com.gmg.seatnow.domain.model.TableDetail
import com.gmg.seatnow.domain.model.TemporaryHolidayInfo
import javax.inject.Inject

/**
 * OwnerSignUpUiState → OwnerSignUpInfo(도메인 모델) 변환 책임을 전담하는 Mapper 클래스.
 * 기존 ViewModel의 mapStateToDomain() + mapIndexToDayOfWeek() + extractNeighborhood() 로직을 이관.
 * presentation 레이어에 위치하여 도메인 역방향 의존성 문제를 방지.
 */
class SignUpStateToDomainMapper @Inject constructor() {

    fun map(state: OwnerSignUpUiState): OwnerSignUpInfo {
        val account = AccountInfo(
            email = state.basic.email,
            password = state.basic.password,
            phoneNumber = state.basic.phone
        )

        val business = BusinessInfo(
            representativeName = state.business.repName,
            businessNumber = state.business.businessNumber,
            storeName = state.business.storeName,
            address = state.business.mainAddress,
            neighborhood = extractNeighborhood(state.business.mainAddress),
            latitude = state.business.selectedLatitude,
            longitude = state.business.selectedLongitude,
            universityNames = state.business.nearbyUnivList,
            storePhone = state.business.storeContact
        )

        val layout = state.store.spaceList.map { space ->
            LayoutInfo(
                name = space.name.ifBlank { "기본 홀" },
                tables = space.tableList.map { table ->
                    TableDetail(
                        tableType = table.personCount.toIntOrNull() ?: 0,
                        tableCount = table.tableCount.toIntOrNull() ?: 0
                    )
                }
            )
        }

        val regularHolidays = when (state.operation.regularHolidayType) {
            1 -> {
                state.operation.weeklyHolidayDays.map { dayIdx ->
                    RegularHolidayInfo(mapIndexToDayOfWeek(dayIdx), 0)
                }
            }
            2 -> {
                state.operation.monthlyHolidayWeeks.flatMap { week ->
                    state.operation.monthlyHolidayDays.map { day ->
                        RegularHolidayInfo(mapIndexToDayOfWeek(day), week)
                    }
                }
            }
            else -> emptyList()
        }

        val tempHolidays = if (state.operation.isTempHolidayEnabled &&
            state.operation.tempHolidayStart.isNotBlank()
        ) {
            listOf(
                TemporaryHolidayInfo(
                    startDate = state.operation.tempHolidayStart.replace("/", "-"),
                    endDate = state.operation.tempHolidayEnd.replace("/", "-")
                )
            )
        } else emptyList()

        val hours = state.operation.operatingSchedules.flatMap { schedule ->
            schedule.selectedDays.map { dayIdx ->
                OperatingHoursInfo(
                    dayOfWeek = mapIndexToDayOfWeek(dayIdx),
                    startTime = "${schedule.startHour.toString().padStart(2, '0')}:${schedule.startMin.toString().padStart(2, '0')}",
                    endTime = "${schedule.endHour.toString().padStart(2, '0')}:${schedule.endMin.toString().padStart(2, '0')}"
                )
            }
        }

        return OwnerSignUpInfo(
            account = account,
            business = business,
            layout = layout,
            operation = OperationInfo(regularHolidays, tempHolidays, hours)
        )
    }

    private fun extractNeighborhood(address: String): String {
        val split = address.split(" ")
        return split.find {
            it.endsWith("동") || it.endsWith("읍") || it.endsWith("면")
        } ?: "정보 없음"
    }

    private fun mapIndexToDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "SUNDAY"
            1 -> "MONDAY"
            2 -> "TUESDAY"
            3 -> "WEDNESDAY"
            4 -> "THURSDAY"
            5 -> "FRIDAY"
            6 -> "SATURDAY"
            else -> "MONDAY"
        }
    }
}
