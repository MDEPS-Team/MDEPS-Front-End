package com.example.mdeps_application.pm.data.repository

import com.example.mdeps_application.pm.data.remote.PmApiService
import com.example.mdeps_application.pm.data.remote.resolveAppLanguageTag
import com.example.mdeps_application.pm.data.remote.toOperationalPrefs
import com.example.mdeps_application.pm.data.remote.toUserSettingsPutBody
import com.example.mdeps_application.pm.domain.ProfileOperationalPrefs
import com.example.mdeps_application.pm.domain.UserSettingsPullResult
import com.example.mdeps_application.pm.domain.UserSettingsRepository
import com.example.mdeps_application.pm.data.remote.dto.UserSettingsDto
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class UserSettingsRepositoryImpl(
    private val api: PmApiService,
) : UserSettingsRepository {

  @Volatile
  private var cachedServerSnapshot: UserSettingsDto? = null

  override suspend fun pullIntoLocal(existing: ProfileOperationalPrefs): Result<UserSettingsPullResult> {
    return try {
      val response = api.getUserSettings()
      when {
        response.isSuccessful -> {
          val body = response.body()
            ?: return Result.success(UserSettingsPullResult(existing, loadedFromServer = false))
          cachedServerSnapshot = body
          Result.success(
            UserSettingsPullResult(
              body.toOperationalPrefs(existing),
              loadedFromServer = true,
            ),
          )
        }
        response.code() == 404 -> Result.success(
          UserSettingsPullResult(existing, loadedFromServer = false),
        )
        else -> Result.failure(
          IllegalStateException("User settings load failed (HTTP ${response.code()})."),
        )
      }
    } catch (http: HttpException) {
      if (http.code() == 404) {
        return Result.success(UserSettingsPullResult(existing, loadedFromServer = false))
      }
      Result.failure(IllegalStateException("User settings load failed (HTTP ${http.code()}).", http))
    } catch (io: IOException) {
      Result.failure(IOException("Network error while loading user settings.", io))
    }
  }

  override suspend fun pushFromLocal(prefs: ProfileOperationalPrefs): Result<Unit> {
    return try {
      refreshServerSnapshot()
      val language = resolveAppLanguageTag()
      val body = prefs.toUserSettingsPutBody(language, cachedServerSnapshot)
      val put = api.putUserSettings(body)
      if (put.isSuccessful) {
        put.body()?.let { cachedServerSnapshot = it }
        return Result.success(Unit)
      }
      val detail = put.errorBody()?.string()?.take(200)?.trim().orEmpty()
      when (put.code()) {
        404 -> Result.failure(
          IllegalStateException(
            "No hay fila en user_settings. En Railway: + Row, o pide al backend crear fila al sign-up (no hay POST).",
          ),
        )
        400 -> Result.failure(
          IllegalStateException(
                buildString {
              append("User settings save failed (HTTP 400). ")
              append("Abre Perfil tras crear la fila en Railway y prueba de nuevo. ")
              if (detail.isNotBlank()) append("Servidor: $detail")
            },
          ),
        )
        else -> Result.failure(
          IllegalStateException(
            buildString {
              append("User settings save failed (HTTP ${put.code()}).")
              if (detail.isNotBlank()) append(" $detail")
            },
          ),
        )
      }
    } catch (http: HttpException) {
      val detail = http.response()?.errorBody()?.string()?.take(200).orEmpty()
      when (http.code()) {
        404 -> Result.failure(
          IllegalStateException(
            "No hay fila en user_settings. Añádela en MySQL (+ Row) — el API solo tiene PUT.",
          ),
        )
        400 -> Result.failure(
          IllegalStateException(
            "User settings save failed (HTTP 400). ${detail.ifBlank { "Cuerpo distinto al Swagger." }}",
          ),
        )
        else -> Result.failure(
          IllegalStateException("User settings update failed (HTTP ${http.code()}). $detail", http),
        )
      }
    } catch (io: IOException) {
      Result.failure(IOException("Network error while saving user settings.", io))
    }
  }

  private suspend fun refreshServerSnapshot() {
    runCatching {
      val response = api.getUserSettings()
      if (response.isSuccessful) {
        cachedServerSnapshot = response.body()
      }
    }
  }
}
