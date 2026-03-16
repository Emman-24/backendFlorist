package com.floristeriaakasia.backend.feature.flowers

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class FlowersService(
    private val repository: FlowersRepository,
    private val floralArrangementRepository: FloralArrangementRepository,
){
    fun create(
        request : CreateFlowersDTO
    ){
        val product = floralArrangementRepository.findByIdOrNull(request.floralArrangementId)
        val flowers = Flowers(
            name = request.name,
            meaning = request.meaning,
            floralArrangement = product
        )
        repository.save(flowers)
    }
}