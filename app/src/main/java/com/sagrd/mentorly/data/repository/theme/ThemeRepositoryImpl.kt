package com.sagrd.mentorly.data.repository.theme

import com.sagrd.mentorly.data.remote.Resource
import com.sagrd.mentorly.data.remote.dto.theme.CreateThemeDto
import com.sagrd.mentorly.data.remote.dto.theme.ReorderItemsDto
import com.sagrd.mentorly.data.remote.dto.theme.UpdateThemeDto
import com.sagrd.mentorly.data.remote.remotedatasource.ThemeRemoteDataSource
import com.sagrd.mentorly.domain.model.theme.Theme
import com.sagrd.mentorly.domain.repository.theme.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val remoteDataSource: ThemeRemoteDataSource
) : ThemeRepository {

    override fun getThemesByUnit(unitId: String): Flow<Resource<List<Theme>>> = flow {
        emit(Resource.Loading())
        remoteDataSource.getThemesByUnit(unitId)
            .onSuccess { themes ->
                emit(Resource.Success(themes.map { it.toDomain() }))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudieron cargar los temas."))
            }
    }

    override fun createTheme(
        adminId: String,
        unitId: String,
        dto: CreateThemeDto
    ): Flow<Resource<Theme>> = flow {
        emit(Resource.Loading())
        remoteDataSource.createTheme(adminId, unitId, dto)
            .onSuccess { theme ->
                emit(Resource.Success(theme.toDomain()))
            }
            .onFailure {
                emit(Resource.Error(it.message ?: "No se pudo crear el tema."))
            }
    }

    override fun updateTheme(
        adminId: String,
        themeId: String,
        dto: UpdateThemeDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        remoteDataSource.updateTheme(adminId, themeId, dto)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure { emit(Resource.Error(it.message ?: "No se pudo actualizar el tema.")) }
    }

    override fun deleteTheme(adminId: String, themeId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        remoteDataSource.deleteTheme(adminId, themeId)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure { emit(Resource.Error(it.message ?: "No se pudo eliminar el tema.")) }
    }

    override fun reorderThemes(
        adminId: String,
        unitId: String,
        dto: ReorderItemsDto
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        remoteDataSource.reorderThemes(adminId, unitId, dto)
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure { emit(Resource.Error(it.message ?: "No se pudo reordenar los temas.")) }
    }
}