package com.floristeriaakasia.backend.feature.flowers

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import com.floristeriaakasia.backend.global.exeption.FlowerNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlowersService(
    private val repository: FlowersRepository,
    private val floralArrangementRepository: FloralArrangementRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)


    fun create(request: CreateFlowersDTO): FlowerResponse {
        val product = floralArrangementRepository.findByIdOrNull(request.floralArrangementId)
            ?: throw FloralArrangementNotFoundException(request.floralArrangementId)
        val saved = repository.save(
            Flowers(
                name = request.name.trim(),
                meaning = request.meaning.trim(),
                floralArrangement = product
            )
        )
        log.info("FLOWER_CREATED id={} arrangementId={}", saved.id, product.id)
        return FlowerResponse.from(saved)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): FlowerResponse =
        FlowerResponse.from(repository.findByIdOrNull(id) ?: throw FlowerNotFoundException(id))

    @Transactional(readOnly = true)
    fun findByArrangement(arrangementId: Long, pageable: Pageable): Page<FlowerResponse> =
        repository.findByFloralArrangementId(arrangementId, pageable).map(FlowerResponse::from)

    fun update(id: Long, request: UpdateFlowersDTO): FlowerResponse {
        val flower = repository.findByIdOrNull(id) ?: throw FlowerNotFoundException(id)
        flower.name = request.name.trim()
        flower.meaning = request.meaning.trim()
        val saved = repository.save(flower)
        log.info("FLOWER_UPDATED id={}", saved.id)
        return FlowerResponse.from(saved)
    }

    fun delete(id: Long) {
        if (!repository.existsById(id)) throw FlowerNotFoundException(id)
        repository.deleteById(id)
        log.info("FLOWER_DELETED id={}", id)
    }
}