package com.floristeriaakasia.backend.feature.floralArrangment.application

interface CreateFloralArrangementUseCase {
    fun execute(command: CreateFloralArrangementCommand): Long
}